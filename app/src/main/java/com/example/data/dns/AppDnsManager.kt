package com.example.data.dns

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.InetAddress

class AppDnsManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _dnsConfig = MutableStateFlow(loadConfig())
    val dnsConfig: StateFlow<DnsConfig> = _dnsConfig.asStateFlow()

    private var onDnsUpdatedCallback: (() -> Unit)? = null

    fun setOnDnsUpdatedCallback(callback: () -> Unit) {
        this.onDnsUpdatedCallback = callback
    }

    private fun loadConfig(): DnsConfig {
        val enabled = prefs.getBoolean(KEY_ENABLED, false)
        val providerName = prefs.getString(KEY_PROVIDER, DnsProvider.CLOUDFLARE.name) ?: DnsProvider.CLOUDFLARE.name
        val provider = try {
            DnsProvider.valueOf(providerName)
        } catch (_: Exception) {
            DnsProvider.CLOUDFLARE
        }
        val customIp = prefs.getString(KEY_CUSTOM_IP, "1.1.1.1") ?: "1.1.1.1"

        return DnsConfig(
            isEnabled = enabled,
            provider = provider,
            customDnsIp = customIp
        )
    }

    fun setDnsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        _dnsConfig.value = _dnsConfig.value.copy(isEnabled = enabled)
        onDnsUpdatedCallback?.invoke()
    }

    fun setDnsProvider(provider: DnsProvider) {
        prefs.edit().putString(KEY_PROVIDER, provider.name).apply()
        _dnsConfig.value = _dnsConfig.value.copy(provider = provider)
        onDnsUpdatedCallback?.invoke()
    }

    fun setCustomDnsIp(ip: String) {
        val cleanIp = ip.trim()
        prefs.edit().putString(KEY_CUSTOM_IP, cleanIp).apply()
        _dnsConfig.value = _dnsConfig.value.copy(customDnsIp = cleanIp)
        onDnsUpdatedCallback?.invoke()
    }

    suspend fun testDnsConnection(resolver: AppDnsResolver): DnsTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val addresses = resolver.lookupDirect("cloudflare.com")
            val latency = System.currentTimeMillis() - startTime
            if (addresses.isNotEmpty()) {
                val ip = addresses.first().hostAddress ?: "Unknown IP"
                DnsTestResult(
                    isSuccess = true,
                    message = "Connected successfully",
                    latencyMs = latency,
                    resolvedIp = ip
                )
            } else {
                DnsTestResult(
                    isSuccess = false,
                    message = "DNS server returned no IP addresses",
                    latencyMs = latency
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            DnsTestResult(
                isSuccess = false,
                message = e.localizedMessage ?: "Failed to reach DNS server",
                latencyMs = latency
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "electro_dns_preferences"
        private const val KEY_ENABLED = "key_dns_enabled"
        private const val KEY_PROVIDER = "key_dns_provider"
        private const val KEY_CUSTOM_IP = "key_dns_custom_ip"

        @Volatile
        private var INSTANCE: AppDnsManager? = null

        fun getInstance(context: Context): AppDnsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppDnsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
