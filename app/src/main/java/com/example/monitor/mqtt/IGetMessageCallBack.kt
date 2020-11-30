package com.example.monitor.mqtt

//获取消息的接口回调
interface IGetMessageCallBack {
    fun setMessage(message: String?)
}