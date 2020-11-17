package com.example.monitor

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class MqttListenService : Service() {
    private val TAG:String = MqttListenService::class.java.simpleName;

    private val binder = MyBinder()


    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"onDestroy")
    }

    inner class MyBinder : Binder() {
        val service: MqttListenService
            get() = this@MqttListenService
    }

    /*** START_FLAG_RETRY
    *  该flag代表当onStartCommand()调用后一直没有返回值时，会尝试重新去调用onStartCommand()。
    *
    *  @param startId 指明当前服务的唯一ID，与stopSelfResult(int startId)配合使用，stopSelfResult()可以更安全地根据ID停止服务。
    *
    *  @return
    *  START_STICKY:
    *  当Service因内存不足而被系统kill后，一段时间后内存再次空闲时，
    *  系统将会尝试重新创建此Service，一旦创建成功后将回调onStartCommand方法，
    *  但其中的Intent将是null，除非有挂起的Intent，如pendingintent，
    *  这个状态下比较适用于不执行命令、但无限期运行并等待作业的媒体播放器或类似服务
    *
    *
    *  START_NOT_STICKY:
    *  当Service因内存不足而被系统kill后，即使系统内存再次空闲时，
    *  系统也不会尝试重新创建此Service。除非程序中再次调用startService启动此Service，
    *  这是最安全的选项，可以避免在不必要时以及应用能够轻松重启所有未完成的作业时运行服务。
    *
    *  START_REDELIVER_INTENT:
    *  当Service因内存不足而被系统kill后，则会重建服务，
    *  并通过传递给服务的最后一个 Intent 调用 onStartCommand()，任何挂起 Intent均依次传递。
    *  与START_STICKY不同的是，其中的传递的Intent将是非空，是最后一次调用startService中的intent。
    *  这个值适用于主动执行应该立即恢复的作业（例如下载文件）的服务。
    ****/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY;
    }
}