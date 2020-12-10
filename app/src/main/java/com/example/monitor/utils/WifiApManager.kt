package com.example.monitor.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.util.Preconditions
import java.lang.reflect.Method
import kotlin.jvm.Throws


class WifiApManager {
    private val WIFI_AP_STATE_FAILED = 4
    private var mWifiManager: WifiManager? = null
    private val TAG = "Wifi Access Manager"
    private var wifiControlMethod: Method? = null
    private var wifiApConfigurationMethod: Method? = null
    private var wifiApState: Method? = null


    @Deprecated("NoSuchMethodException of setWifiApEnabled")
    @SuppressLint("RestrictedApi")
    @Throws(SecurityException::class, NoSuchMethodException::class)
    fun WifiApManager(context: Context) {
        var context: Context = context
        context = Preconditions.checkNotNull(context)
        mWifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiControlMethod = mWifiManager?.javaClass?.getMethod(
            "setWifiApEnabled",
            WifiConfiguration::class.java,
            Boolean::class.javaPrimitiveType
        )
        wifiApConfigurationMethod = mWifiManager?.javaClass?.getMethod("getWifiApConfiguration", null)
        wifiApState = mWifiManager?.javaClass?.getMethod("getWifiApState")
    }

    @SuppressLint("RestrictedApi")
    fun setWifiApState(config: WifiConfiguration?, enabled: Boolean): Boolean {
        var config = config
        config = Preconditions.checkNotNull(config)
        return try {
            if (enabled) {
                mWifiManager!!.isWifiEnabled = !enabled
            }
            wifiControlMethod?.invoke(mWifiManager, config, enabled) as Boolean
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            false
        }
    }

    fun getWifiApConfiguration(): WifiConfiguration? {
        return try {
            wifiApConfigurationMethod?.invoke(mWifiManager, null) as WifiConfiguration?
        } catch (e: Exception) {
            null
        }
    }

    fun getWifiApState(): Int {
        return try {
            wifiApState?.invoke(mWifiManager) as Int
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            WIFI_AP_STATE_FAILED
        }
    }
}