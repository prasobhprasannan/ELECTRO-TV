package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dns.DnsConfig
import com.example.data.dns.DnsProvider
import com.example.data.dns.DnsTestResult
import kotlinx.coroutines.launch

private val BgDark = Color(0xFF121212)
private val CardDark = Color(0xFF1E1E1E)
private val BorderDark = Color(0xFF2E2E2E)
private val AccentCyan = Color(0xFF00E5FF)
private val AccentGreen = Color(0xFF00E676)
private val TextMuted = Color(0xFF888888)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsSettingsDialog(
    dnsConfig: DnsConfig,
    onToggleDns: (Boolean) -> Unit,
    onSelectProvider: (DnsProvider) -> Unit,
    onSetCustomIp: (String) -> Unit,
    onClearCache: () -> Unit,
    onTestDns: suspend () -> DnsTestResult,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var customIpInput by remember(dnsConfig.customDnsIp) { mutableStateOf(dnsConfig.customDnsIp) }
    var testResult by remember { mutableStateOf<DnsTestResult?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var cacheClearedMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BgDark,
        contentColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (dnsConfig.isEnabled) AccentCyan.copy(alpha = 0.2f) else CardDark,
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (dnsConfig.isEnabled) Icons.Default.Security else Icons.Default.Dns,
                                contentDescription = null,
                                tint = if (dnsConfig.isEnabled) AccentCyan else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "In-Built DNS Protection",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (dnsConfig.isEnabled) "All IPTV streams & data routed via custom DNS" else "System default DNS active (Disabled)",
                            fontSize = 12.sp,
                            color = if (dnsConfig.isEnabled) AccentCyan else TextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp).testTag("btn_close_dns_dialog")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Master Toggle Card
            Surface(
                color = CardDark,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (dnsConfig.isEnabled) AccentCyan.copy(alpha = 0.5f) else BorderDark,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Enable In-Built DNS",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (dnsConfig.isEnabled) AccentGreen.copy(alpha = 0.2f) else Color(0xFF333333),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (dnsConfig.isEnabled) "ACTIVE" else "OFF",
                                    color = if (dnsConfig.isEnabled) AccentGreen else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Routes all live streams, playlists, EPG, and video segments through encrypted DNS to prevent ISP blocking and throttling.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = dnsConfig.isEnabled,
                        onCheckedChange = { enabled ->
                            onToggleDns(enabled)
                            testResult = null
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = AccentCyan,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color(0xFF2A2A2A)
                        ),
                        modifier = Modifier.testTag("switch_dns_enable")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DNS Providers Section
            Text(
                text = "Select DNS Provider",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DnsProvider.entries.forEach { provider ->
                    val isSelected = dnsConfig.provider == provider
                    val isCustom = provider == DnsProvider.CUSTOM

                    Surface(
                        color = if (isSelected && dnsConfig.isEnabled) AccentCyan.copy(alpha = 0.08f) else CardDark,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isSelected && dnsConfig.isEnabled) AccentCyan else BorderDark,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelectProvider(provider)
                                testResult = null
                            }
                            .testTag("dns_provider_item_${provider.name}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onSelectProvider(provider)
                                        testResult = null
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AccentCyan,
                                        unselectedColor = TextMuted
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = provider.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected && dnsConfig.isEnabled) AccentCyan else Color.White
                                        )
                                        if (provider.dohUrl != null) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFF272727),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "DoH",
                                                    color = Color(0xFF00E5FF),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = provider.description,
                                        fontSize = 11.5.sp,
                                        color = TextMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (provider.primaryIp.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "IP: ${provider.primaryIp}  •  Alt: ${provider.secondaryIp}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFAAAAAA)
                                        )
                                    }
                                }
                            }

                            // If CUSTOM is selected, show IP Input field
                            if (isCustom && isSelected) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = customIpInput,
                                    onValueChange = {
                                        customIpInput = it
                                        onSetCustomIp(it)
                                        testResult = null
                                    },
                                    label = { Text("Custom DNS Server IPv4") },
                                    placeholder = { Text("e.g. 1.1.1.1 or 8.8.8.8") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .testTag("input_custom_dns_ip"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF141414),
                                        unfocusedContainerColor = Color(0xFF141414),
                                        focusedBorderColor = AccentCyan,
                                        unfocusedBorderColor = BorderDark,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = AccentCyan
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Test DNS Connection & Diagnostics
            Surface(
                color = CardDark,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DNS Diagnostics & Latency",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                cacheClearedMessage = "DNS Cache Flushed"
                                onClearCache()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Flush Cache", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    if (cacheClearedMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = cacheClearedMessage!!,
                            color = AccentGreen,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            isTesting = true
                            testResult = null
                            scope.launch {
                                testResult = onTestDns()
                                isTesting = false
                            }
                        },
                        enabled = !isTesting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (dnsConfig.isEnabled) AccentCyan else Color(0xFF272727)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_test_dns")
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing DNS Resolution...", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (dnsConfig.isEnabled) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (dnsConfig.isEnabled) "Test Active DNS (${dnsConfig.provider.title.split(" ").first()})" else "Test DNS Server",
                                    color = if (dnsConfig.isEnabled) Color.Black else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Test Result Display
                    testResult?.let { res ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (res.isSuccess) AccentGreen.copy(alpha = 0.15f) else Color(0xFFFF5252).copy(alpha = 0.15f))
                                .border(1.dp, if (res.isSuccess) AccentGreen.copy(alpha = 0.5f) else Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (res.isSuccess) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (res.isSuccess) AccentGreen else Color(0xFFFF5252),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (res.isSuccess) "DNS Working: ${res.latencyMs} ms latency" else "DNS Test Failed",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = if (res.isSuccess) AccentGreen else Color(0xFFFF5252)
                                    )
                                }
                                if (res.resolvedIp != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Resolved domain to: ${res.resolvedIp}",
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
