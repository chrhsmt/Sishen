package com.chrhsmt.sisheng.network

import android.app.Activity
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import android.widget.Toast
import com.chrhsmt.sisheng.Settings
import com.chrhsmt.sisheng.R

/**
 * Created by chihiro on 2017/11/05.
 */
class MDnsResolver {

    companion object {
        val TAG = "MDnsResolver"
        val SERVICE_TYPE = "_workstation._tcp."
    }

    val activity: Activity
    val manager: NsdManager
    var mServiceName: String? = null
    var mAddress: String? = null
    val piDefaultHostName: String
    var started: Boolean = false

    val discoveryListener: NsdManager.DiscoveryListener
    val resolveListener: NsdManager.ResolveListener

    constructor(activity: Activity) {
        this.activity = activity
        manager = activity.getSystemService(android.content.Context.NSD_SERVICE) as NsdManager
        this.piDefaultHostName = this.activity.getString(R.string.default_pi_hostname)

        resolveListener = object : NsdManager.ResolveListener {

            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                serviceInfo?.let {
                    val host = serviceInfo.host
                    val hostAddress = host.hostAddress

                    if (this@MDnsResolver.mAddress.isNullOrBlank() || !this@MDnsResolver.mAddress!!.equals(hostAddress)) {
                        this@MDnsResolver.mAddress = hostAddress
                        Settings.raspberrypiHost = hostAddress + ":" + this@MDnsResolver.activity.getString(R.string.default_pi_port)
                        this@MDnsResolver.activity.runOnUiThread {
                            Toast.makeText(this@MDnsResolver.activity, String.format("pi address is changed: %s", this@MDnsResolver.mAddress), Toast.LENGTH_LONG).show()
                        }
                        Log.d(TAG, "onServiceResolved host:" + host + ", address: " + hostAddress)
                    }
                }
            }
        }

        discoveryListener = object : NsdManager.DiscoveryListener {

            override fun onServiceFound(serviceInfo: NsdServiceInfo?) {

                Log.d(TAG, "Service discovery success:" + serviceInfo);

                serviceInfo?.let {
                    if (!serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                        Log.d(TAG, "Unknown Service Type: " + serviceInfo.getServiceType());
                    } else if (serviceInfo.getServiceName().equals(mServiceName)) {
                        Log.d(TAG, "Same machine: " + mServiceName);
                    } else if (serviceInfo.serviceType.equals(SERVICE_TYPE) && serviceInfo.serviceName.contains(this@MDnsResolver.piDefaultHostName)){
                        this@MDnsResolver.mServiceName = serviceInfo.serviceName
                        this@MDnsResolver.manager.resolveService(serviceInfo, resolveListener);
                    } else {

                    }
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                this@MDnsResolver.manager.stopServiceDiscovery(this);
            }

            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                this@MDnsResolver.manager.stopServiceDiscovery(this);
            }

            override fun onDiscoveryStarted(regType: String?) {
                Log.d(TAG, "Service discovery started");
                started = true
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
                started = false
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                Log.e(TAG, "service lost: " + serviceInfo);
            }
        }
    }

    fun discoverServices() {

        if (!started) {
            manager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        }
    }

    fun tearDown() {
        if (started) {
//            manager.unregisterService(this)
            manager.stopServiceDiscovery(discoveryListener)
        }
    }
}