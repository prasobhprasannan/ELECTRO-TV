package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Electro IPTV", appName)
  }

  @Test
  fun `verify dns providers and config`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dnsManager = com.example.data.dns.AppDnsManager.getInstance(context)

    // Initially or default toggle
    dnsManager.setDnsEnabled(true)
    org.junit.Assert.assertTrue(dnsManager.dnsConfig.value.isEnabled)

    // Set Cloudflare
    dnsManager.setDnsProvider(com.example.data.dns.DnsProvider.CLOUDFLARE)
    assertEquals(com.example.data.dns.DnsProvider.CLOUDFLARE, dnsManager.dnsConfig.value.provider)
    assertEquals("1.1.1.1", dnsManager.dnsConfig.value.activeIp)

    // Set Google DNS
    dnsManager.setDnsProvider(com.example.data.dns.DnsProvider.GOOGLE)
    assertEquals(com.example.data.dns.DnsProvider.GOOGLE, dnsManager.dnsConfig.value.provider)
    assertEquals("8.8.8.8", dnsManager.dnsConfig.value.activeIp)

    // Set Custom DNS
    dnsManager.setDnsProvider(com.example.data.dns.DnsProvider.CUSTOM)
    dnsManager.setCustomDnsIp("208.67.222.222")
    assertEquals(com.example.data.dns.DnsProvider.CUSTOM, dnsManager.dnsConfig.value.provider)
    assertEquals("208.67.222.222", dnsManager.dnsConfig.value.activeIp)

    // Toggle off
    dnsManager.setDnsEnabled(false)
    org.junit.Assert.assertFalse(dnsManager.dnsConfig.value.isEnabled)
  }

  @Test
  fun `verify dns resolver ip lookup`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dnsManager = com.example.data.dns.AppDnsManager.getInstance(context)
    val resolver = com.example.data.dns.AppDnsResolver(dnsManager)

    dnsManager.setDnsEnabled(true)

    // Direct IP address lookup does not require external network
    val results = resolver.lookup("8.8.8.8")
    org.junit.Assert.assertNotNull(results)
    org.junit.Assert.assertEquals("8.8.8.8", results.first().hostAddress)
  }
}
