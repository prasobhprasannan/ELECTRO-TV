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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.StalkerPortalApi
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LiveBroadcastRed
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightDarkBg
import com.example.ui.theme.MidnightDarkCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.AddProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistBottomSheet(
    onDismiss: () -> Unit,
    addProfileState: AddProfileUiState,
    onAddXtream: (name: String, server: String, user: String, pass: String) -> Unit,
    onAddM3u: (name: String, urlOrContent: String, isRaw: Boolean, epgUrl: String) -> Unit,
    onAddStalker: (name: String, portalUrl: String, macAddress: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Xtream state
    var xtreamName by remember { mutableStateOf("") }
    var xtreamServer by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPass by remember { mutableStateOf("") }

    // M3U state
    var m3uName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }
    var m3uEpgUrl by remember { mutableStateOf("") }
    var m3uIsRawText by remember { mutableStateOf(false) }
    var m3uRawContent by remember { mutableStateOf("") }

    // Stalker state
    var stalkerName by remember { mutableStateOf("") }
    var stalkerPortalUrl by remember { mutableStateOf("") }
    var stalkerMac by remember { mutableStateOf(StalkerPortalApi.generateRandomMagMac()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightDarkBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add IPTV Playlist",
                        color = TextPrimaryDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Connect Xtream Codes, M3U URL, or Stalker Portal",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Protocol Selection Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MidnightDarkCard,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonCyan,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MidnightBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Xtream Codes",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "M3U Playlist",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "MAC Portal",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status message
            if (addProfileState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LiveBroadcastRed.copy(alpha = 0.15f))
                        .border(1.dp, LiveBroadcastRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = addProfileState.errorMessage,
                        color = LiveBroadcastRed,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (addProfileState.successMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = addProfileState.successMessage,
                        color = NeonCyan,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Xtream Codes
                    OutlinedTextField(
                        value = xtreamName,
                        onValueChange = { xtreamName = it },
                        label = { Text("Profile Name (optional)") },
                        placeholder = { Text("e.g. My Xtream Server") },
                        leadingIcon = { Icon(Icons.Default.Tv, contentDescription = null, tint = NeonCyan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_xtream_name"),
                        colors = textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = xtreamServer,
                        onValueChange = { xtreamServer = it },
                        label = { Text("Server URL:Port") },
                        placeholder = { Text("http://example.com:8080") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = NeonCyan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_xtream_server"),
                        colors = textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = xtreamUser,
                        onValueChange = { xtreamUser = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_xtream_user"),
                        colors = textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = xtreamPass,
                        onValueChange = { xtreamPass = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_xtream_pass"),
                        colors = textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (xtreamServer.isNotBlank() && xtreamUser.isNotBlank() && xtreamPass.isNotBlank()) {
                                onAddXtream(xtreamName, xtreamServer, xtreamUser, xtreamPass)
                            }
                        },
                        enabled = !addProfileState.isLoading && xtreamServer.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_xtream"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF031A24)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (addProfileState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF031A24), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connecting...")
                        } else {
                            Text("Connect Xtream Codes", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                1 -> {
                    // M3U Playlist
                    OutlinedTextField(
                        value = m3uName,
                        onValueChange = { m3uName = it },
                        label = { Text("Playlist Name") },
                        placeholder = { Text("e.g. World IPTV") },
                        leadingIcon = { Icon(Icons.Default.Tv, contentDescription = null, tint = NeonCyan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_m3u_name"),
                        colors = textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { m3uIsRawText = false },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (!m3uIsRawText) ElectricIndigo.copy(alpha = 0.25f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (!m3uIsRawText) NeonCyan else MidnightBorder),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "M3U URL",
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                color = if (!m3uIsRawText) NeonCyan else TextSecondaryDark
                            )
                        }

                        OutlinedButton(
                            onClick = { m3uIsRawText = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (m3uIsRawText) ElectricIndigo.copy(alpha = 0.25f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (m3uIsRawText) NeonCyan else MidnightBorder),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Paste M3U Text",
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                color = if (m3uIsRawText) NeonCyan else TextSecondaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!m3uIsRawText) {
                        OutlinedTextField(
                            value = m3uUrl,
                            onValueChange = { m3uUrl = it },
                            label = { Text("M3U / M3U8 Playlist URL") },
                            placeholder = { Text("https://example.com/playlist.m3u8") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = NeonCyan) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_m3u_url"),
                            colors = textFieldColors()
                        )
                    } else {
                        OutlinedTextField(
                            value = m3uRawContent,
                            onValueChange = { m3uRawContent = it },
                            label = { Text("Paste M3U Content") },
                            placeholder = { Text("#EXTM3U\n#EXTINF:-1,Sample Channel\nhttps://...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("input_m3u_raw"),
                            colors = textFieldColors(),
                            maxLines = 6
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = m3uEpgUrl,
                        onValueChange = { m3uEpgUrl = it },
                        label = { Text("EPG XMLTV URL (optional)") },
                        placeholder = { Text("https://example.com/epg.xml.gz") },
                        leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, tint = NeonCyan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_m3u_epg"),
                        colors = textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset filler for easy testing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Fill Public IPTV-Org M3U URL",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    m3uName = "IPTV-Org Public Streams"
                                    m3uUrl = "https://iptv-org.github.io/iptv/index.m3u"
                                    m3uEpgUrl = "https://iptv-org.github.io/epg/guides/us/tvguide.com.epg.xml"
                                    m3uIsRawText = false
                                }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val content = if (m3uIsRawText) m3uRawContent else m3uUrl
                            if (content.isNotBlank()) {
                                onAddM3u(m3uName, content, m3uIsRawText, m3uEpgUrl)
                            }
                        },
                        enabled = !addProfileState.isLoading && ((!m3uIsRawText && m3uUrl.isNotBlank()) || (m3uIsRawText && m3uRawContent.isNotBlank())),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_m3u"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF031A24)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (addProfileState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF031A24), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading Playlist...")
                        } else {
                            Text("Import M3U Playlist", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                2 -> {
                    // Stalker / MAG Portal
                    OutlinedTextField(
                        value = stalkerName,
                        onValueChange = { stalkerName = it },
                        label = { Text("Profile Name") },
                        placeholder = { Text("e.g. MAG 254 Portal") },
                        leadingIcon = { Icon(Icons.Default.Tv, contentDescription = null, tint = NeonCyan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_stalker_name"),
                        colors = textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = stalkerPortalUrl,
                        onValueChange = { stalkerPortalUrl = it },
                        label = { Text("Stalker Portal URL") },
                        placeholder = { Text("http://portal.domain.com/c/") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = NeonCyan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_stalker_url"),
                        colors = textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = stalkerMac,
                        onValueChange = { stalkerMac = it },
                        label = { Text("Device MAC Address") },
                        placeholder = { Text("00:1A:79:XX:XX:XX") },
                        leadingIcon = { Icon(Icons.Default.Router, contentDescription = null, tint = NeonCyan) },
                        trailingIcon = {
                            IconButton(onClick = { stalkerMac = StalkerPortalApi.generateRandomMagMac() }) {
                                Icon(Icons.Default.Autorenew, contentDescription = "Generate MAC", tint = NeonCyan)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_stalker_mac"),
                        colors = textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset filler for easy testing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Fill Sample Portal URL",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    stalkerName = "Infomir Stalker Portal"
                                    stalkerPortalUrl = "http://mag.stalker-portal.tv/c/"
                                }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (stalkerPortalUrl.isNotBlank() && stalkerMac.isNotBlank()) {
                                onAddStalker(stalkerName, stalkerPortalUrl, stalkerMac)
                            }
                        },
                        enabled = !addProfileState.isLoading && stalkerPortalUrl.isNotBlank() && stalkerMac.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_stalker"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF031A24)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (addProfileState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF031A24), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connecting Portal...")
                        } else {
                            Text("Connect Stalker Portal", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MidnightDarkCard,
    unfocusedContainerColor = MidnightDarkCard,
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = MidnightBorder,
    focusedTextColor = TextPrimaryDark,
    unfocusedTextColor = TextPrimaryDark,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = TextSecondaryDark
)
