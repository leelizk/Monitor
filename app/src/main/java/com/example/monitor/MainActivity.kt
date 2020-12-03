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
import com.example.monitor.utils.WifiManagerUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TAG:String?=MainActivity::class.java.simpleName;

    companion object{
        const val START_WIFI:Int = 0
        const val RESTART_WIFI:Int = 1
        const val STOP_WIFI:Int = 2
    }

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
            service!!.setCallback(object : IGetMessageCallBack {
                override fun setMessage(message: String?) {
                    try {
                        var cmd: MqttCmd = Gson().fromJson(message, MqttCmd::class.java)
                        var action:Int = cmd.action as Int;
                        if(action == STOP_WIFI){
                            callStopWifi()
                        }else if(action == START_WIFI){
                            callStartWifi()
                        }else if(action == RESTART_WIFI){
                            callRestartWifi();
                        }
                    }catch (e:Exception){
                        e.message?.let { Log.i(TAG, it) }
                    }

                }
            })
            Log.i(TAG, "onServiceConnected")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBind = false
            Log.i(TAG, "onServiceDisconnected")
        }
    }

    fun callStartWifi(){
        GlobalScope.launch(Dispatchers.IO) {
            WifiManagerUtils.startWifi(application)
        }
    }

    fun callStopWifi(){
        GlobalScope.launch(Dispatchers.IO) {
            WifiManagerUtils.stopWifi(application)
        }
    }

    fun callRestartWifi(){
        GlobalScope.launch(Dispatchers.IO) {
            WifiManagerUtils.restartWifi(application)
        }
    }


}