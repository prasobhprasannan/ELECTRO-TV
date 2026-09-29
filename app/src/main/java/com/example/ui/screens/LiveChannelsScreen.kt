package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChannelEntity
import com.example.data.model.EpgProgramEntity
import com.example.player.PlayerUiState
import com.example.ui.components.YouTubeCompactVideoRow
import com.example.ui.components.YouTubeVideoFeedCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.YouTubeBlue
import com.example.ui.theme.YouTubeDarkBg
import com.example.ui.theme.YouTubeDarkCard
import com.example.ui.theme.YouTubeDarkSurface
import com.example.ui.theme.YouTubePillActiveBg
import com.example.ui.theme.YouTubePillActiveText
import com.example.ui.theme.YouTubePillBg
import com.example.ui.theme.YouTubeRed

@Composable
fun LiveChannelsScreen(
    channels: List<ChannelEntity>,
    allChannels: List<ChannelEntity>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isGridView: Boolean,
    onToggleViewMode: () -> Unit,
    playerState: PlayerUiState,
    epgMap: Map<String, Pair<EpgProgramEntity?, EpgProgramEntity?>>,
    onChannelClick: (ChannelEntity) -> Unit,
    onToggleFavorite: (ChannelEntity) -> Unit,
    onWatchlistClick: (ChannelEntity) -> Unit,
    onOpenAddPlaylist: () -> Unit,
    onOpenAbout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(searchQuery.isNotEmpty()) }
    var isCategoryDrawerOpen by remember { mutableStateOf(false) }
    var drawerFilterQuery by remember { mutableStateOf("") }

    // Close drawer on system back press
    BackHandler(enabled = isCategoryDrawerOpen) {
        isCategoryDrawerOpen = false
    }

    val categories = remember(allChannels) {
        val cats = allChannels.map { it.groupTitle }.distinct().filter { it.isNotBlank() }
        listOf("All", "Favorites") + cats
    }

    val categoryCounts = remember(allChannels) {
        val map = mutableMapOf<String, Int>()
        map["All"] = allChannels.size
        map["Favorites"] = allChannels.count { it.isFavorite }
        allChannels.forEach { ch ->
            map[ch.groupTitle] = (map[ch.groupTitle] ?: 0) + 1
        }
        map
    }

    val filteredCategories = remember(categories, drawerFilterQuery) {
        if (drawerFilterQuery.isBlank()) categories
        else categories.filter { it.contains(drawerFilterQuery, ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(YouTubeDarkBg)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Slide-out Category Drawer Button + Brand
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isCategoryDrawerOpen = true },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("btn_open_categories_drawer")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Categories",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCategorySelected("All") }
                    ) {
                        // Electro Cyan & Indigo Bolt Icon Box
                        Box(
                            modifier = Modifier
                                .size(width = 30.dp, height = 26.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(
                                            Color(0xFF00E5FF),
                                            Color(0xFF4F46E5)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Electro IPTV",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Electro",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Surface(
                            color = YouTubeBlue,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "IPTV",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Top Actions: Cast, Add Playlist (+), View Mode, About Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onOpenAbout != null) {
                        IconButton(
                            onClick = onOpenAbout,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_top_about")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "About",
                                tint = Color.White,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { /* Cast */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Cast, contentDescription = "Cast", tint = Color.White, modifier = Modifier.size(21.dp))
                    }

                    IconButton(
                        onClick = onOpenAddPlaylist,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Playlist", tint = Color.White, modifier = Modifier.size(24.dp))
                    }

                    IconButton(
                        onClick = onToggleViewMode,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle View",
                            tint = Color.White,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }

            // Big, Prominent Search Bar (Larger, perfectly aligned, no clipped text)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search live channels & programs...",
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            color = TextMutedDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (searchQuery.isNotEmpty()) YouTubeBlue else TextSecondaryDark,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("youtube_search_input"),
                    shape = RoundedCornerShape(28.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Normal
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedBorderColor = YouTubeBlue,
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )
            }

            // Horizontal Category Pills (aligned and never cut off)
            if (categories.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Slide Drawer Trigger Pill
                    Surface(
                        color = Color(0xFF272727),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .clickable { isCategoryDrawerOpen = true }
                            .testTag("btn_drawer_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Categories",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Groups",
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }

                    categories.forEach { cat ->
                        val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                        Surface(
                            color = if (isSelected) YouTubePillActiveBg else YouTubePillBg,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .clickable { onCategorySelected(cat) }
                                .testTag("channel_cat_pill_$cat")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                if (cat == "Favorites") {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.Black else AmberGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                }
                                Text(
                                    text = cat,
                                    color = if (isSelected) YouTubePillActiveText else Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Active Category Bar (when a specific category is filtered or drawer opened)
            if (selectedCategory != "All" && allChannels.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = Color(0xFF272727),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { isCategoryDrawerOpen = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(selectedCategory),
                                contentDescription = null,
                                tint = YouTubeBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedCategory,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { onCategorySelected("All") },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear filter", tint = TextSecondaryDark, modifier = Modifier.size(12.dp))
                            }
                        }
                    }

                    Text(
                        text = "${channels.size} channels",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                }
            }

            // Empty State (when playlist is empty by default)
            if (allChannels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF272727), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF272727)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Your Playlist is Empty",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Stream live TV channels, movies, and series. Connect your IPTV subscription via Xtream Codes, M3U playlist, or Stalker Portal.",
                                color = TextSecondaryDark,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Add Playlist Action
                            Button(
                                onClick = onOpenAddPlaylist,
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = YouTubeBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_empty_add_playlist")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add IPTV Playlist", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            } else if (channels.isEmpty()) {
                // Category or search filter yielded 0 results
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No channels match this filter", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Try selecting another category from the left menu or clearing search.", color = TextSecondaryDark, fontSize = 12.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                onCategorySelected("All")
                                onSearchQueryChange("")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF272727)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Reset Filter to All", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            } else if (isGridView) {
                // YouTube Feed Mode: Full 16:9 video cards
                LazyColumn(
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(channels, key = { it.id }) { ch ->
                        val isPlaying = playerState.currentChannel?.id == ch.id
                        val epgPair = epgMap[ch.tvgId ?: ch.id]
                        YouTubeVideoFeedCard(
                            channel = ch,
                            currentProgram = epgPair?.first,
                            nextProgram = epgPair?.second,
                            isPlaying = isPlaying,
                            onChannelClick = { onChannelClick(ch) },
                            onToggleFavorite = { onToggleFavorite(ch) },
                            onWatchlistClick = { onWatchlistClick(ch) }
                        )
                    }
                }
            } else {
                // YouTube Compact Row Mode
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(channels, key = { it.id }) { ch ->
                        val isPlaying = playerState.currentChannel?.id == ch.id
                        val epgPair = epgMap[ch.tvgId ?: ch.id]
                        YouTubeCompactVideoRow(
                            channel = ch,
                            currentProgram = epgPair?.first,
                            isPlaying = isPlaying,
                            onChannelClick = { onChannelClick(ch) },
                            onToggleFavorite = { onToggleFavorite(ch) },
                            onWatchlistClick = { onWatchlistClick(ch) }
                        )
                    }
                }
            }
        }

        // Left Slide-out Category Drawer
        if (isCategoryDrawerOpen) {
            // Scrim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isCategoryDrawerOpen = false }
            )

            // Animated Slide-Out Drawer Panel
            androidx.compose.animation.AnimatedVisibility(
                visible = isCategoryDrawerOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Surface(
                    color = Color(0xFF181818),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(290.dp)
                        .border(width = 0.5.dp, color = Color(0xFF2E2E2E))
                        .testTag("left_category_slide_drawer")
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Drawer Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(YouTubeRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Categories",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${categories.size} Groups • ${allChannels.size} Channels",
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isCategoryDrawerOpen = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Search Categories Input (if more than 4 categories)
                        if (categories.size > 4) {
                            OutlinedTextField(
                                value = drawerFilterQuery,
                                onValueChange = { drawerFilterQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Filter groups...",
                                        fontSize = 13.5.sp,
                                        color = TextMutedDark,
                                        lineHeight = 18.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    if (drawerFilterQuery.isNotEmpty()) {
                                        IconButton(onClick = { drawerFilterQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondaryDark, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 13.5.sp,
                                    lineHeight = 18.sp,
                                    color = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF222222),
                                    unfocusedContainerColor = Color(0xFF222222),
                                    focusedBorderColor = YouTubeBlue,
                                    unfocusedBorderColor = Color(0xFF333333),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Category Items List
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredCategories, key = { it }) { cat ->
                                val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                                val count = categoryCounts[cat] ?: 0

                                Surface(
                                    color = if (isSelected) Color(0xFF272727) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onCategorySelected(cat)
                                            isCategoryDrawerOpen = false
                                        }
                                        .testTag("drawer_cat_item_$cat")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Red indicator bar for active selection
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 3.dp, height = 18.dp)
                                                    .background(if (isSelected) YouTubeRed else Color.Transparent)
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Icon(
                                                imageVector = getCategoryIcon(cat),
                                                contentDescription = null,
                                                tint = if (isSelected) YouTubeBlue else if (cat == "Favorites") Color(0xFFF59E0B) else Color(0xFFAAAAAA),
                                                modifier = Modifier.size(18.dp)
                                            )

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Text(
                                                text = cat,
                                                color = if (isSelected) Color.White else Color(0xFFCCCCCC),
                                                fontSize = 13.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Channel Count Badge
                                        if (count > 0) {
                                            Surface(
                                                color = if (isSelected) Color(0xFF333333) else Color(0xFF222222),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(
                                                    text = "$count",
                                                    color = if (isSelected) Color.White else TextSecondaryDark,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Drawer Footer
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF141414))
                                .padding(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isCategoryDrawerOpen = false
                                    onOpenAddPlaylist()
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = YouTubeBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add New Playlist", color = YouTubeBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    val clean = category.lowercase()
    return when {
        clean == "all" -> Icons.Default.Explore
        clean == "favorites" -> Icons.Default.Star
        clean.contains("news") -> Icons.Default.Feed
        clean.contains("sport") -> Icons.Default.SportsSoccer
        clean.contains("movie") || clean.contains("cinema") -> Icons.Default.Movie
        clean.contains("music") -> Icons.Default.MusicNote
        clean.contains("science") || clean.contains("tech") -> Icons.Default.Science
        clean.contains("doc") || clean.contains("nature") -> Icons.Default.Public
        else -> Icons.Default.Tv
    }
}
