package com.example.ui.components

import com.example.data.model.ChannelEntity
import java.util.Locale

fun ChannelEntity.getThumbnailUrl(): String {
    val cleanId = id.lowercase()
    return when {
        cleanId.contains("nasa") -> "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=80"
        cleanId.contains("bloomberg") -> "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=800&q=80"
        cleanId.contains("dw_news") -> "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=800&q=80"
        cleanId.contains("france24") -> "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800&q=80"
        cleanId.contains("redbull") -> "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=800&q=80"
        cleanId.contains("aljazeera") -> "https://images.unsplash.com/photo-1495020689067-958852a7765e?w=800&q=80"
        cleanId.contains("big_buck_bunny") -> "https://peach.blender.org/wp-content/uploads/title_bbb_render.jpg"
        cleanId.contains("sintel") -> "https://durian.blender.org/wp-content/themes/durian/images/sintel_header.jpg"
        cleanId.contains("tears_of_steel") -> "https://mango.blender.org/wp-content/themes/mango/images/logo_tos.png"
        cleanId.contains("nature") -> "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&q=80"
        cleanId.contains("elephants") -> "https://orange.blender.org/wp-content/themes/orange/images/header.jpg"
        cleanId.contains("lofi") -> "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80"
        !logoUrl.isNullOrEmpty() && (logoUrl.endsWith(".jpg") || logoUrl.endsWith(".png") || logoUrl.endsWith(".webp") || logoUrl.contains("unsplash")) -> logoUrl
        else -> when (groupTitle.lowercase()) {
            "news", "portal: news" -> "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=800&q=80"
            "sports", "portal: sports" -> "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=800&q=80"
            "movies & cinema", "movies", "portal: movies" -> "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&q=80"
            "science & tech" -> "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=80"
            "documentary" -> "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&q=80"
            "music" -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&q=80"
            else -> "https://images.unsplash.com/photo-1522869635100-9f4c5e86aa37?w=800&q=80"
        }
    }
}

fun ChannelEntity.getViewersCountFormatted(): String {
    val seed = (name.hashCode() % 8500).let { if (it < 0) -it else it } + 1200
    return if (seed >= 1000) {
        String.format(Locale.US, "%.1fK", seed / 1000.0)
    } else {
        "$seed"
    }
}

fun ChannelEntity.getQualityBadge(): String {
    return when {
        name.contains("4k", ignoreCase = true) -> "4K"
        name.contains("hd", ignoreCase = true) -> "HD"
        name.contains("fhd", ignoreCase = true) -> "1080p"
        else -> "HD"
    }
}
