package com.example.monitor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.example.monitor.mqtt.MQTTService

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