package com.example.monitor

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //启动service
        bindService(intent, conn, Context.BIND_AUTO_CREATE)
    }


    private var service: MqttListenService? = null
    private var isBind = false


    private var conn = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            isBind = true
            val myBinder = p1 as MqttListenService.MyBinder
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