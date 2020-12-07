package com.example.monitor.utils

import android.app.Application
import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import kotlin.concurrent.timerTask


object WifiManagerUtils {

    private  var wifimanager:WifiManager? = null;

    private fun initWifiManager(application: Application){
        wifimanager = application.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun startWifi(application: Application, ssid: String = "leelizk", passwd: String = "joeleejoke"){
            initWifiManager(application)
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            if(wifimanager!!.isWifiEnabled()){
                wifimanager!!.isWifiEnabled = true;
            }
            if(!isWifiApEnabled()!!){
                stratWifiAp(ssid, passwd);
            }
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

}