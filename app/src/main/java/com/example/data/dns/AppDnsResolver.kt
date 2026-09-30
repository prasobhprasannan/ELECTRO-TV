package com.example.data.dns

import okhttp3.Dns
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

class AppDnsResolver(private val dnsManager: AppDnsManager) : Dns {

    private val cache = ConcurrentHashMap<String, Pair<List<InetAddress>, Long>>()
    private val cacheTtlMs = 5 * 60 * 1000L // 5 minutes cache

    private val ipv4Pattern = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")

    init {
        dnsManager.setOnDnsUpdatedCallback {
            clearCache()
        }
    }

    fun clearCache() {
        cache.clear()
    }

    override fun lookup(hostname: String): List<InetAddress> {
        val config = dnsManager.dnsConfig.value

        // If DNS is disabled by user, route through system default resolver
        if (!config.isEnabled) {
            return Dns.SYSTEM.lookup(hostname)
        }

        // If hostname is already an IPv4 or IPv6 address, parse directly
        if (isIpAddress(hostname)) {
            return try {
                listOf(InetAddress.getByName(hostname))
            } catch (_: Exception) {
                Dns.SYSTEM.lookup(hostname)
            }
        }

        // Localhost special case
        if (hostname.equals("localhost", ignoreCase = true) || hostname.endsWith(".local")) {
            return Dns.SYSTEM.lookup(hostname)
        }

        // Check memory cache for instant resolution
        val now = System.currentTimeMillis()
        val cached = cache[hostname]
        if (cached != null && (now - cached.second) < cacheTtlMs) {
            return cached.first
        }

        // Perform custom DNS resolution
        return try {
            val resolved = resolveHost(hostname, config)
            if (resolved.isNotEmpty()) {
                cache[hostname] = Pair(resolved, now)
                resolved
            } else {
                Dns.SYSTEM.lookup(hostname)
            }
        } catch (_: Exception) {
            // Safe fallback to system DNS if custom resolution encounters any network timeout
            Dns.SYSTEM.lookup(hostname)
        }
    }

    fun lookupDirect(hostname: String): List<InetAddress> {
        val config = dnsManager.dnsConfig.value
        return resolveHost(hostname, config)
    }

    private fun resolveHost(hostname: String, config: DnsConfig): List<InetAddress> {
        // Priority 1: If DoH is available for the provider, use DNS-over-HTTPS (encrypted, bypasses ISP port 53 blocks)
        val dohUrl = config.provider.dohUrl
        if (dohUrl != null && config.provider != DnsProvider.CUSTOM) {
            try {
                val dohResults = resolveViaDoH(hostname, dohUrl)
                if (dohResults.isNotEmpty()) {
                    return dohResults
                }
            } catch (_: Exception) {
                // Fall back to UDP query
            }
        }

        // Priority 2: Direct RFC 1035 UDP query to DNS server port 53
        val targetIp = config.activeIp
        if (targetIp.isNotBlank()) {
            try {
                val udpResults = resolveViaUdp(hostname, targetIp)
                if (udpResults.isNotEmpty()) {
                    return udpResults
                }
            } catch (_: Exception) {
                // Secondary IP fallback if available
                if (config.provider.secondaryIp.isNotBlank()) {
                    try {
                        val secondaryResults = resolveViaUdp(hostname, config.provider.secondaryIp)
                        if (secondaryResults.isNotEmpty()) {
                            return secondaryResults
                        }
                    } catch (_: Exception) {
                        // Ignore
                    }
                }
            }
        }

        throw UnknownHostException("Unable to resolve host '$hostname' via custom DNS (${config.provider.title})")
    }

    private fun resolveViaDoH(hostname: String, endpoint: String): List<InetAddress> {
        val queryUrl = if (endpoint.contains("?")) {
            "$endpoint&name=$hostname&type=A"
        } else {
            "$endpoint?name=$hostname&type=A"
        }

        val url = URL(queryUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/dns-json")
        conn.setRequestProperty("User-Agent", "ElectroIPTV-DNS/1.0")

        val responseCode = conn.responseCode
        if (responseCode != 200) {
            conn.disconnect()
            return emptyList()
        }

        val reader = BufferedReader(InputStreamReader(conn.inputStream))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        reader.close()
        conn.disconnect()

        val json = JSONObject(sb.toString())
        val answers = json.optJSONArray("Answer") ?: return emptyList()

        val results = mutableListOf<InetAddress>()
        for (i in 0 until answers.length()) {
            val item = answers.getJSONObject(i)
            val type = item.optInt("type", -1)
            // Type 1 is DNS A record (IPv4)
            if (type == 1) {
                val data = item.optString("data", "").trim()
                if (isIpAddress(data)) {
                    val bytes = parseIpv4Bytes(data)
                    if (bytes != null) {
                        results.add(InetAddress.getByAddress(hostname, bytes))
                    }
                }
            }
        }
        return results
    }

    private fun resolveViaUdp(hostname: String, dnsServerIp: String): List<InetAddress> {
        val queryBytes = buildDnsQuery(hostname)
        val dnsAddress = InetAddress.getByName(dnsServerIp)

        DatagramSocket().use { socket ->
            socket.soTimeout = 3000
            val sendPacket = DatagramPacket(queryBytes, queryBytes.size, dnsAddress, 53)
            socket.send(sendPacket)

            val receiveBuf = ByteArray(512)
            val receivePacket = DatagramPacket(receiveBuf, receiveBuf.size)
            socket.receive(receivePacket)

            return parseDnsResponse(receiveBuf, receivePacket.length, hostname)
        }
    }

    private fun buildDnsQuery(hostname: String): ByteArray {
        val bytes = mutableListOf<Byte>()
        // 12-byte header
        // Transaction ID (2 bytes)
        bytes.add(0x12.toByte())
        bytes.add(0x34.toByte())
        // Flags: standard query, recursion desired (0x0100)
        bytes.add(0x01.toByte())
        bytes.add(0x00.toByte())
        // Questions count: 1
        bytes.add(0x00.toByte())
        bytes.add(0x01.toByte())
        // Answer RRs: 0
        bytes.add(0x00.toByte())
        bytes.add(0x00.toByte())
        // Authority RRs: 0
        bytes.add(0x00.toByte())
        bytes.add(0x00.toByte())
        // Additional RRs: 0
        bytes.add(0x00.toByte())
        bytes.add(0x00.toByte())

        // Question: QNAME
        val parts = hostname.split(".")
        for (part in parts) {
            bytes.add(part.length.toByte())
            for (char in part.toCharArray()) {
                bytes.add(char.code.toByte())
            }
        }
        bytes.add(0x00.toByte()) // End of QNAME

        // QTYPE: A (IPv4) = 1
        bytes.add(0x00.toByte())
        bytes.add(0x01.toByte())
        // QCLASS: IN = 1
        bytes.add(0x00.toByte())
        bytes.add(0x01.toByte())

        return bytes.toByteArray()
    }

    private fun parseDnsResponse(data: ByteArray, length: Int, hostname: String): List<InetAddress> {
        if (length < 12) return emptyList()

        val ancount = ((data[6].toInt() and 0xFF) shl 8) or (data[7].toInt() and 0xFF)
        if (ancount <= 0) return emptyList()

        // Skip Question section
        var pos = 12
        while (pos < length && data[pos].toInt() != 0) {
            pos += (data[pos].toInt() and 0xFF) + 1
        }
        pos += 1 // Skip terminating 0 byte
        pos += 4 // Skip QTYPE (2) and QCLASS (2)

        val results = mutableListOf<InetAddress>()

        // Parse Answer section
        for (i in 0 until ancount) {
            if (pos >= length) break

            // Handle name compression or plain name
            if ((data[pos].toInt() and 0xC0) == 0xC0) {
                pos += 2 // Compressed pointer
            } else {
                while (pos < length && data[pos].toInt() != 0) {
                    pos += (data[pos].toInt() and 0xFF) + 1
                }
                pos += 1
            }

            if (pos + 10 > length) break

            val type = ((data[pos].toInt() and 0xFF) shl 8) or (data[pos + 1].toInt() and 0xFF)
            // val clazz = ((data[pos + 2].toInt() and 0xFF) shl 8) or (data[pos + 3].toInt() and 0xFF)
            // val ttl = 4 bytes
            val dataLen = ((data[pos + 8].toInt() and 0xFF) shl 8) or (data[pos + 9].toInt() and 0xFF)
            pos += 10

            if (type == 1 && dataLen == 4 && pos + 4 <= length) {
                val ipBytes = byteArrayOf(data[pos], data[pos + 1], data[pos + 2], data[pos + 3])
                results.add(InetAddress.getByAddress(hostname, ipBytes))
            }
            pos += dataLen
        }

        return results
    }

    private fun isIpAddress(host: String): Boolean {
        return ipv4Pattern.matcher(host).matches() || host.contains(":")
    }

    private fun parseIpv4Bytes(ipStr: String): ByteArray? {
        val parts = ipStr.split(".")
        if (parts.size != 4) return null
        val bytes = ByteArray(4)
        for (i in 0..3) {
            val num = parts[i].toIntOrNull() ?: return null
            if (num !in 0..255) return null
            bytes[i] = num.toByte()
        }
        return bytes
    }
}
