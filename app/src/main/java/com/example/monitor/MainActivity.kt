package com.example.monitor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.example.monitor.bean.MqttCmd
import com.example.monitor.mqtt.IGetMessageCallBack
import com.example.monitor.mqtt.MQTTService
import com.example.monitor.utils.CmdEnum.START_WIFI
import com.example.monitor.utils.CmdEnum.STOP_WIFI
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private val TAG:String?=MainActivity::class.java.simpleName;

    private var myIntent: Intent?=null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myIntent = Intent(this,MQTTService::class.java);
        //启动service
        var result:Boolean  = bindService(myIntent, conn, Context.BIND_AUTO_CREATE)
        Log.i(TAG,"绑定service 结果:" + result);
    }


    private var service: MQTTService? = null
    private var isBind = false


    private var conn = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            isBind = true
            val myBinder = p1 as MQTTService.CustomBinder
            service = myBinder.service
            service!!.setIGetMessageCallBack(object : IGetMessageCallBack {
                override fun setMessage(message: String?) {
                    try {
                        var cmd: MqttCmd = Gson().fromJson(message, MqttCmd::class.java)
                        when(cmd.action){
                           //is STOP_WIFI ->{}
                           // is START_WIFI ->{}
                        }
                    }catch (e:Exception){
                        e.message?.let { Log.i(TAG, it) }
                    }

                }
            })
            Log.i("xiao", "ActivityA - onServiceConnected")
            //val num = service!!.getRandomNumber()
            //Log.i("xiao", "ActivityA - getRandomNumber = $num");
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBind = false
            Log.i("xiao", "ActivityA - onServiceDisconnected")
        }
    }


}