package com.example.data.dns

enum class DnsProvider(
    val title: String,
    val description: String,
    val primaryIp: String,
    val secondaryIp: String,
    val dohUrl: String?
) {
    CLOUDFLARE(
        title = "Cloudflare (1.1.1.1)",
        description = "Ultra-fast, privacy-first DNS. Bypasses ISP IPTV blocks.",
        primaryIp = "1.1.1.1",
        secondaryIp = "1.0.0.1",
        dohUrl = "https://cloudflare-dns.com/dns-query"
    ),
    GOOGLE(
        title = "Google Public DNS (8.8.8.8)",
        description = "Global high-reliability DNS by Google.",
        primaryIp = "8.8.8.8",
        secondaryIp = "8.8.4.4",
        dohUrl = "https://dns.google/resolve"
    ),
    ADGUARD(
        title = "AdGuard DNS",
        description = "Blocks video ads, trackers, and malicious domains.",
        primaryIp = "94.140.14.14",
        secondaryIp = "94.140.15.15",
        dohUrl = "https://dns.adguard-dns.com/resolve"
    ),
    QUAD9(
        title = "Quad9 Secure (9.9.9.9)",
        description = "Protects against cyber threats & malicious streams.",
        primaryIp = "9.9.9.9",
        secondaryIp = "149.112.112.112",
        dohUrl = "https://dns.quad9.net:5053/dns-query"
    ),
    CUSTOM(
        title = "Custom DNS Server",
        description = "Use your own custom IPv4 DNS or SmartDNS server.",
        primaryIp = "",
        secondaryIp = "",
        dohUrl = null
    )
}

data class DnsConfig(
    val isEnabled: Boolean = false,
    val provider: DnsProvider = DnsProvider.CLOUDFLARE,
    val customDnsIp: String = "1.1.1.1"
) {
    val activeIp: String
        get() = when (provider) {
            DnsProvider.CUSTOM -> customDnsIp.ifBlank { "1.1.1.1" }
            else -> provider.primaryIp
        }
}

data class DnsTestResult(
    val isSuccess: Boolean,
    val message: String,
    val latencyMs: Long = 0,
    val resolvedIp: String? = null
)
