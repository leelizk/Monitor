package com.example.monitor.mqtt

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class MyServiceConnection : ServiceConnection {
    private var mqttService: MQTTService? = null
    private var IGetMessageCallBack: IGetMessageCallBack? = null
    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
        mqttService = (iBinder as MQTTService.CustomBinder).service
        mqttService!!.setCallback(IGetMessageCallBack)
    }

    override fun onServiceDisconnected(componentName: ComponentName) {}
    fun getMqttService(): MQTTService? {
        return mqttService
    }

    fun setIGetMessageCallBack(IGetMessageCallBack: IGetMessageCallBack?) {
        this.IGetMessageCallBack = IGetMessageCallBack
    }
}