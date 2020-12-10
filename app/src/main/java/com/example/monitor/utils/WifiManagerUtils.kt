package com.example.monitor.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation
import android.os.Build
import android.os.Handler
import android.support.v4.os.ResultReceiver
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.vkpapps.apmanager.APManager
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import kotlin.concurrent.timerTask


object WifiManagerUtils {

    private  val TAG: String = WifiManagerUtils::class.java.simpleName


    private const val config_ap = "config_ap"

    /**
     * ssid
     */
    private const val DEFAULT_SSID = "lzk"

    private val DEFAULT_PWD: String = "12345678";

    private  var wifimanager:WifiManager? = null;

    private var apManager: APManager? = null;

    private fun initWifiManager(application: Application){
        wifimanager = application.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun startWifi(application: Application, ssid: String = "leelizk", passwd: String = "joeleejoke"){
            initWifiManager(application)
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
           /* if(wifimanager!!.isWifiEnabled()){
                wifimanager!!.isWifiEnabled = true;
            }
            if(!isWifiApEnabled()!!){
                stratWifiAp(ssid, passwd);
            }*/
            if(!isApOn(application.applicationContext)){
                //开启热点时，必须关闭wifi
                turnOffWifi(application);
                //setWifiApEnabled(application.applicationContext,"leelizk","",true)
                turnOnByApManager(application,ssid,passwd);
            }
        }

    fun turnOffWifi(application: Application){
        initWifiManager(application)
        wifimanager?.isWifiEnabled = false
    }

        fun restartWifi(application: Application){
            initWifiManager(application)

            val timer = Timer()
            timer.schedule(timerTask { closeWifiAp() }, 500);
            timer.schedule(timerTask { startWifi(application) }, 1000);
        }

        fun stopWifi(application: Application){
            initWifiManager(application)
            closeWifiAp()
        }




    /**
     * 热点开关是否打开
     *
     * @return
     */
     fun isWifiApEnabled(): Boolean? {
        try {
            val method: Method = wifimanager!!::class.java.getMethod("isWifiApEnabled")
            method.setAccessible(true)
            var result =  method.invoke(wifimanager) as Boolean;
            return result;
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    /**
     * 设置热点名称及密码，并创建热点
     *
     * @param mSSID
     * @param mPasswd
     */
     fun stratWifiAp(mSSID: String, mPasswd: String) {
        var method1: Method? = null
        try {
            //通过反射机制打开热点
            method1 = wifimanager!!::class.java.getMethod(
                    "setWifiApEnabled",
                    WifiConfiguration::class.java,
                    Boolean::class.javaPrimitiveType
            )
            val netConfig = WifiConfiguration()
            netConfig.SSID = mSSID
            netConfig.preSharedKey = mPasswd
            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            //            netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            netConfig.allowedKeyManagement.set(4)
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            method1.invoke(wifimanager, netConfig, true)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
    }


    /**
     * 关闭热点
     */
    fun closeWifiAp() {
        if (isWifiApEnabled()!!) {
            try {
                val method: Method =
                    wifimanager!!::class.java.getMethod("getWifiApConfiguration")
                method.isAccessible = true
                val config = method.invoke(wifimanager) as WifiConfiguration
                val method2: Method = wifimanager!!::class.java.getMethod(
                        "setWifiApEnabled",
                        WifiConfiguration::class.java,
                        Boolean::class.javaPrimitiveType
                )
                method2.invoke(wifimanager, config, false)
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }


    /**
     * 开启/关闭热点
     *
     * @param context  上下文
     * @param ssid     ssid
     * @param password 密码
     * @param enabled  true打开，false关闭
     * @return
     */
    private fun setWifiApEnabled(context: Context, ssid: String, password: String, enabled: Boolean): Boolean {
        //8.0这种方式就只能是打开系统默认那个ssid和密码的热点了，不支持设置ssid和密码
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return setWifiApEnabledForAndroidO(context, enabled)
        }
        //处理低版本，只适用于安卓7.0或7.0以下版本且版本>=4.0
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            // 因为6.0及其以下版本，在开启热点之前要先手动关闭wifi。以后版本就不需要了会自动关闭，热点关闭后也会自动打开
            wifiManager.isWifiEnabled = false
            closeAp(context)
            var apConfig: WifiConfiguration? = null
            if (enabled) {
                if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
                    return false
                }
                // 热点的配置类
                apConfig = getApConfig(ssid, password, 2)
            }
            val method = wifiManager.javaClass.getMethod("setWifiApEnabled", WifiConfiguration::class.java, java.lang.Boolean.TYPE)
            method.invoke(wifiManager, apConfig, enabled) as Boolean
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 设置热点
     *
     * @param ssid     热点名称
     * @param password 热点密码
     * @param type     加密类型
     * @return
     */
    fun getApConfig(ssid: String?, password: String?, type: Int): WifiConfiguration? {
        val config = WifiConfiguration()
        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear()
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()
        config.SSID = ssid
        if (type == 0) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        } else if (type == 1) {
            config.wepKeys[0] = password
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            config.wepTxKeyIndex = 0
        } else if (type == 2) { //   WPA/WPA2 PSK的加密方式都可以通过此方法连上热点  也就是说我们连接热点只用分为有密码和无密码情况
            config.preSharedKey = password
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            config.status = WifiConfiguration.Status.ENABLED
        }
        return config
    }


    /**
     * 8.0 开启热点方法
     * 注意：这个方法开启的热点名称和密码是手机系统里面默认的那个
     * 权限： android.permission.OVERRIDE_WIFI_CONFIG
     *
     * @param context
     */
    private fun setWifiApEnabledForAndroidO(context: Context, isEnable: Boolean): Boolean {
        val connManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var iConnMgrField: Field? = null
        return try {
            iConnMgrField = connManager.javaClass.getDeclaredField("mService")
            iConnMgrField.setAccessible(true)
            val iConnMgr: Any = iConnMgrField.get(connManager)
            val iConnMgrClass = Class.forName(iConnMgr.javaClass.name)
            if (isEnable) {
                val startTethering = iConnMgrClass.getMethod("startTethering", Int::class.javaPrimitiveType, ResultReceiver::class.java, Boolean::class.javaPrimitiveType)
                startTethering.invoke(iConnMgr, 0, null, true)
            } else {
                val startTethering = iConnMgrClass.getMethod("stopTethering", Int::class.javaPrimitiveType)
                startTethering.invoke(iConnMgr, 0)
            }
            true
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "8.0开启热点异常", e)
            false
        }
    }

    /**
     * 判断热点是否开启
     *
     * @param context
     * @return
     */
    fun isApOn(context: Context): Boolean {
        val wifimanager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        try {
            val method = wifimanager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            return method.invoke(wifimanager) as Boolean
        } catch (e: Throwable) {
            Log.e(TAG, "判断热点是否开启", e)
        }
        return false
    }


    /**
     * 关闭热点
     *
     * @param context
     */
    fun closeAp(context: Context) {
        val wifimanager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        try {
            val method = wifimanager.javaClass.getMethod("setWifiApEnabled", WifiConfiguration::class.java, Boolean::class.javaPrimitiveType)
            method.invoke(wifimanager, null, false)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 获取开启热点后的IP地址
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    fun getHotspotLocalIpAddress(context: Context): String? {
        val wifimanager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wifimanager.startLocalOnlyHotspot(object : LocalOnlyHotspotCallback() {}, null)
        }
        val dhcpInfo = wifimanager.dhcpInfo
        if (dhcpInfo != null) {
            val address = dhcpInfo.serverAddress
            return ((address and 0xFF)
                    .toString() + "." + (address shr 8 and 0xFF)
                    + "." + (address shr 16 and 0xFF)
                    + "." + (address shr 24 and 0xFF))
        }
        return null
    }


    /**
     * 保存ssid信息
     *
     * @param context
     * @param ssid
     * @param pwd
     */
    fun saveApInfo(context: Context, ssid: String?, pwd: String?) {
        val preferences = context.getSharedPreferences(config_ap, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("ssid", ssid)
        editor.putString("pwd", pwd)
        editor.commit()
    }

    /**
     * 获取ssid
     *
     * @param context
     */
    fun getSsid(context: Context): String? {
        val preferences = context.getSharedPreferences(config_ap, Context.MODE_PRIVATE)
        return preferences.getString("ssid", DEFAULT_SSID)
    }

    /**
     * 获取pwd
     *
     * @param context
     */
    fun getPwd(context: Context): String? {
        val preferences = context.getSharedPreferences(config_ap, Context.MODE_PRIVATE)
        return preferences.getString("pwd", DEFAULT_PWD)
    }

    fun turnOnByApManager(application: Application,ssid:String,password:String){
        apManager = APManager.getApManager(application)
        apManager?.turnOnHotspot(application.applicationContext,APManager.OnSuccessListener(){ ssid, password ->
            Log.i(TAG,"开启热点成功===>?")
        },APManager.OnFailureListener(){ code: Int, exception: java.lang.Exception? ->
            Log.i(TAG,"开启热点失败===>?")
        })
    }

    fun turnOffByApManager(application: Application){
        apManager = APManager.getApManager(application);
        apManager?.disableWifiAp()
    }


    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun turnOnHotspot(application: Application) {
        initWifiManager(application)
        wifimanager?.startLocalOnlyHotspot(object : LocalOnlyHotspotCallback() {
            override fun onStarted(reservation: LocalOnlyHotspotReservation) {
                super.onStarted(reservation)
                var hotspotReservation:WifiManager.LocalOnlyHotspotReservation = reservation
               // var currentConfig:WifiEnterpriseConfig = hotspotReservation.getWifiConfiguration()

            }

            override fun onStopped() {
                super.onStopped()
                Log.v("DANG", "Local Hotspot Stopped")
            }

            override fun onFailed(reason: Int) {
                super.onFailed(reason)
                Log.v("DANG", "Local Hotspot failed to start")
            }
        }, Handler())
    }

}

