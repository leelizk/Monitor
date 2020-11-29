package com.example.monitor

import android.app.Application

class App :Application(){

    override fun onCreate() {
        super.onCreate()
        listenMqtt();
    }

    fun listenMqtt(){

    }

}