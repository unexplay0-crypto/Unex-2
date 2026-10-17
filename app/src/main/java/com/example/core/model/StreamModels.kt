package com.example.core.model

enum class StreamType {
    LIVE,
    MOVIE,
    SERIES
}

data class StreamItem(
    val streamId: String,
    val name: String,
    val type: StreamType,
    val streamIcon: String,
    val streamUrl: String,
    val categoryId: String,
    val categoryName: String,
    val rating: Float = 4.5f,
    val plot: String = "",
    val releaseDate: String = "2025",
    val durationMinutes: Int = 120,
    val isAdult: Boolean = false,
    val isFavorite: Boolean = false,
    val resolution: String = "FHD"
)

data class EpisodeItem(
    val episodeId: String,
    val title: String,
    val seasonNum: Int,
    val episodeNum: Int,
    val streamUrl: String,
    val streamIcon: String = "",
    val plot: String = "",
    val durationMinutes: Int = 45
)

data class SeriesGroup(
    val seriesId: String,
    val name: String,
    val cover: String,
    val backdrop: String = "",
    val plot: String,
    val rating: Float = 4.8f,
    val genre: String = "Ficção Científica",
    val isAdult: Boolean = false,
    val isFavorite: Boolean = false,
    val seasonsCount: Int = 1,
    val episodes: List<EpisodeItem> = emptyList()
)

data class VodProgressEntry(
    val streamId: String,
    val title: String,
    val positionMs: Long,
    val durationMs: Long,
    val lastWatchedTimestamp: Long = System.currentTimeMillis(),
    val episodeId: String? = null,
    val seasonNum: Int? = null,
    val episodeNum: Int? = null
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

data class EpgProgram(
    val title: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val progressPercent: Float = 0.5f
)

data class ChannelCategory(
    val categoryId: String,
    val categoryName: String,
    val type: StreamType,
    val isAdult: Boolean = false,
    val channelCount: Int = 0
)

data class UserSession(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isConnected: Boolean = true,
    val serverName: String = "UNEX Cloud VIP",
    val expirationDate: String = "2029-12-31",
    val activeConnections: Int = 1,
    val maxConnections: Int = 3
)
