package com.example

import android.app.Application
import com.example.data.network.AppNetworkClient

class IptvApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Pre-warm the unified OkHttpClient and DNS resolver
        AppNetworkClient.getOkHttpClient(this)
    }

    companion object {
        lateinit var instance: IptvApplication
            private set
    }
}
