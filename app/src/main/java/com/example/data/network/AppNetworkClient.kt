package com.example.data.network

import android.content.Context
import com.example.data.dns.AppDnsManager
import com.example.data.dns.AppDnsResolver
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object AppNetworkClient {

    @Volatile
    private var okHttpClient: OkHttpClient? = null

    @Volatile
    private var dnsResolver: AppDnsResolver? = null

    fun getResolver(context: Context): AppDnsResolver {
        return dnsResolver ?: synchronized(this) {
            dnsResolver ?: AppDnsResolver(AppDnsManager.getInstance(context.applicationContext)).also {
                dnsResolver = it
            }
        }
    }

    fun getOkHttpClient(context: Context): OkHttpClient {
        return okHttpClient ?: synchronized(this) {
            okHttpClient ?: buildOkHttpClient(context.applicationContext).also {
                okHttpClient = it
            }
        }
    }

    private fun buildOkHttpClient(context: Context): OkHttpClient {
        val resolver = getResolver(context)

        return OkHttpClient.Builder()
            .dns(resolver)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
    }
}
