package com.example.monitor.mqtt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import org.apache.commons.lang3.StringUtils
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.android.service.MqttService
import org.eclipse.paho.client.mqttv3.*
import java.util.*

class MQTTService : Service() {
    private val TAG = MqttService::class.java.simpleName
    private var conOpt: MqttConnectOptions? = null
    private var host = ""
    private var userName = ""
    private var clientId = "" //客户端标识
    private var IGetMessageCallBack: IGetMessageCallBack? = null

    /**
     *
    mqtt.serverip=tcp://47.107.62.127
    mqtt.port=61613

    mqtt.username=admin
    mqtt.password=~!yw_654321
     */
    override fun onCreate() {
            super.onCreate()
            Log.e(TAG, "onCreate")

                Log.e(TAG, "开启 mqtt")
                host = ""
                userName = "admin"
                //默认关注的主题,用于统计或者版本发布，广告推送
                myTopic!![0] = "wifi_listener"

                clientId = genClientId()
                init()
                createNotificationChannel();
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        }
    }

    //发布消息--> 客户端不需要发布消息,如果需要发布，走http通道
    /*public static void publish(String topic,String msg) {
        Log.d(TAG,topic + "==>" + msg);
        Integer qos = 0;
        Boolean retained = false;
        try {
            if (client != null) {
                client.publish(topic, msg.getBytes(), qos.intValue(), retained.booleanValue());
            }else{
                Log.e(TAG,"未实例化终端");
            }
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG,"异常:"+e.getMessage());
        }
    }*/
    private fun init() {
        // 服务器地址（协议+地址+端口号）
        val uri = host
        client = MqttAndroidClient(this, uri, clientId)
        // 设置MQTT监听并且接受消息
        client!!.setCallback(mqttCallback)
        conOpt = MqttConnectOptions()
        // 清除缓存
        conOpt!!.isCleanSession = true
        // 设置超时时间，单位：秒
        conOpt!!.connectionTimeout = 10
        // 心跳包发送间隔，单位：秒
        conOpt!!.keepAliveInterval = 20
        // 用户名
        conOpt!!.userName = userName
        // 密码
        conOpt!!.password = passWord.toCharArray() //将字符串转换为字符串数组

        // last will message
        var doConnect = true
        val message = "{\"terminal_uid\":\"$clientId\"}"
        Log.e(TAG, "message是:$message")
        val qos = 0
        val retained = false
        if (StringUtils.isNotBlank(message) || myTopic != null && myTopic.size == 3) {
            // 最后的遗嘱
            // MQTT本身就是为信号不稳定的网络设计的，所以难免一些客户端会无故的和Broker断开连接。
            //当客户端连接到Broker时，可以指定LWT，Broker会定期检测客户端是否有异常。
            //当客户端异常掉线时，Broker就往连接时指定的topic里推送当时指定的LWT消息。
            try {
                conOpt!!.setWill("LAST_WORD_TOPIC", message.toByteArray(), qos, retained)
            } catch (e: Exception) {
                Log.i(TAG, "Exception Occured", e)
                doConnect = false
                iMqttActionListener.onFailure(null, e)
            }
        }
        if (doConnect) {
            doClientConnection()
        }
    }

    override fun onDestroy() {
        stopSelf()
        try {
            if (client != null) {
                client!!.disconnect()
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    /**
     * 连接MQTT服务器
     */
    private fun doClientConnection() {
        if (!client!!.isConnected && isConnectIsNormal) {
            try {
                client!!.connect(conOpt, null, iMqttActionListener) as IMqttToken
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    // MQTT是否连接成功
    private val iMqttActionListener: IMqttActionListener = object : IMqttActionListener {
        override fun onSuccess(arg0: IMqttToken) {
            Log.i(TAG, "连接成功 ")
            try {
                // 默认订阅myTopic系统主题
                client!!.subscribe(myTopic!![0], 1)
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }

        override fun onFailure(arg0: IMqttToken, arg1: Throwable) {
            try {
                client!!.connect(conOpt, null, this)
            } catch (e: MqttException) {
                e.printStackTrace()
            }
            arg1.printStackTrace()
            // 连接失败，重连
        }
    }

    // MQTT监听并且接受消息
    private val mqttCallback: MqttCallback = object : MqttCallback {
        override fun messageArrived(topic: String, message: MqttMessage) {

            //回调
            val str1 = String(message.payload)
            if (IGetMessageCallBack != null) {
                IGetMessageCallBack!!.setMessage(str1)
            }
            val str2 = topic + ";qos:" + message.qos + ";retained:" + message.isRetained
            Log.i(TAG, "messageArrived:$str1")
            Log.i(TAG, str2)
        }

        //发送完成
        override fun deliveryComplete(arg0: IMqttDeliveryToken) {
            Log.d(TAG, "发送消息完成 ... ")
        }

        override fun connectionLost(arg0: Throwable) {
            // 失去连接，重连
            Log.d(TAG, "丢失连接 ... ")
            clientId = genClientId()
            init()
        }
    }

    /**
     * 判断网络是否连接
     */
    private val isConnectIsNormal: Boolean
        private get() {
            val connectivityManager = this.applicationContext
                    .getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivityManager.activeNetworkInfo
            return if (info != null && info.isAvailable) {
                val name = info.typeName
                Log.i(TAG, "MQTT当前网络名称：$name")
                true
            } else {
                Log.i(TAG, "MQTT 没有可用网络")
                false
            }
        }

    override fun onBind(intent: Intent): IBinder? {
        Log.e(TAG, "onBind")
        return CustomBinder()
    }

    fun setIGetMessageCallBack(IGetMessageCallBack: IGetMessageCallBack?) {
        this.IGetMessageCallBack = IGetMessageCallBack
    }

    inner class CustomBinder : Binder() {
        val service: MQTTService
            get() = this@MQTTService
    }

    fun toCreateNotification(message: String?) {

        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(this, MQTTService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            Log.e(TAG, "toCreateNotification ")
            val channel = NotificationChannel("xxx", "xxx", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
        /*val notification: Notification = Builder(this, "xxx")
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentText(message)
                .setOngoing(true)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .build()
        this.startForeground(101, notification)
        manager.notify(101, notification)*/
    }

    companion object {
        val TAG = MQTTService::class.java.simpleName
        private var client: MqttAndroidClient? = null

        //# 密码

        private const val passWord = ""

        //0 系统主题
        //1 支持主题
        //2 广告主题
        private val myTopic: Array<String?>? = arrayOfNulls(3) //要订阅的主题

        //生成一个uuid,长度不超过20
        private fun genClientId(): String {
            val clientId = UUID.randomUUID().toString().replace("-", "").substring(0, 20)
            Log.d(TAG, "mqtt client id : $clientId")
            return clientId
        }

        //订阅主题
        fun addTopic(newTopic: String, qos: Int): Boolean {
            Log.d(TAG, " addTopic ==> $newTopic")
            try {
                if (client != null) {
                    if (qos > 0 && qos < 2) {
                        client!!.subscribe(newTopic, qos)
                        return true
                    } else {
                        Log.d(TAG, "非法 qos ==> $qos")
                    }
                } else {
                    Log.d(TAG, "client 未初始化")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "异常 : " + e.message)
            }
            return false
        }
    }
}