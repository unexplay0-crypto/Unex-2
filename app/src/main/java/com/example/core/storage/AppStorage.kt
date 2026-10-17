package com.example.core.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.core.model.StreamItem
import com.example.core.model.StreamType
import com.example.core.model.UserSession
import com.example.core.model.VodProgressEntry
import org.json.JSONArray
import org.json.JSONObject

class AppStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("unex_vision_storage", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_PARENTAL_PIN = "parental_pin"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_RECENTS = "recents"
        private const val KEY_VOD_PROGRESS = "vod_progress"
        private const val KEY_DEVICE_MODE = "device_mode" // "tv" or "mobile"
        private const val DEFAULT_PIN = "0000"
    }

    fun getSession(): UserSession {
        val server = prefs.getString(KEY_SERVER_URL, "http://live.unex-stream.net:8080") ?: ""
        val user = prefs.getString(KEY_USERNAME, "vip_demo") ?: ""
        val pass = prefs.getString(KEY_PASSWORD, "unex2026") ?: ""
        return UserSession(
            serverUrl = server,
            username = user,
            password = pass,
            isConnected = true,
            serverName = "UNEX Global Ultra Server",
            expirationDate = "2029-12-31"
        )
    }

    fun saveSession(serverUrl: String, user: String, pass: String) {
        val cleanUrl = sanitizeUrl(serverUrl)
        prefs.edit()
            .putString(KEY_SERVER_URL, cleanUrl)
            .putString(KEY_USERNAME, user.trim())
            .putString(KEY_PASSWORD, pass.trim())
            .apply()
    }

    fun sanitizeUrl(url: String): String {
        var clean = url.trim()
        if (clean.startsWith("http://http://")) clean = clean.replace("http://http://", "http://")
        if (clean.startsWith("https://https://")) clean = clean.replace("https://https://", "https://")
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "http://$clean"
        }
        return clean.removeSuffix("/")
    }

    fun getParentalPin(): String {
        return prefs.getString(KEY_PARENTAL_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setParentalPin(newPin: String) {
        prefs.edit().putString(KEY_PARENTAL_PIN, newPin).apply()
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun toggleFavorite(streamId: String): Boolean {
        val current = getFavorites().toMutableSet()
        val isFav = if (current.contains(streamId)) {
            current.remove(streamId)
            false
        } else {
            current.add(streamId)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return isFav
    }

    fun isFavorite(streamId: String): Boolean {
        return getFavorites().contains(streamId)
    }

    fun getRecents(): List<StreamItem> {
        val jsonStr = prefs.getString(KEY_RECENTS, null) ?: return emptyList()
        val list = mutableListOf<StreamItem>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    StreamItem(
                        streamId = obj.getString("id"),
                        name = obj.getString("name"),
                        type = StreamType.valueOf(obj.optString("type", StreamType.LIVE.name)),
                        streamIcon = obj.optString("icon", ""),
                        streamUrl = obj.optString("url", ""),
                        categoryId = obj.optString("catId", ""),
                        categoryName = obj.optString("catName", ""),
                        rating = obj.optDouble("rating", 4.5).toFloat(),
                        plot = obj.optString("plot", ""),
                        releaseDate = obj.optString("release", "2025")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun addRecent(item: StreamItem) {
        val current = getRecents().filter { it.streamId != item.streamId }.toMutableList()
        current.add(0, item)
        val limited = current.take(25)
        try {
            val arr = JSONArray()
            for (it in limited) {
                val obj = JSONObject().apply {
                    put("id", it.streamId)
                    put("name", it.name)
                    put("type", it.type.name)
                    put("icon", it.streamIcon)
                    put("url", it.streamUrl)
                    put("catId", it.categoryId)
                    put("catName", it.categoryName)
                    put("rating", it.rating.toDouble())
                    put("plot", it.plot)
                    put("release", it.releaseDate)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_RECENTS, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveVodProgress(progress: VodProgressEntry) {
        val map = getAllVodProgress().toMutableMap()
        map[progress.streamId] = progress
        try {
            val arr = JSONArray()
            map.values.sortedByDescending { it.lastWatchedTimestamp }.take(50).forEach { entry ->
                val obj = JSONObject().apply {
                    put("streamId", entry.streamId)
                    put("title", entry.title)
                    put("pos", entry.positionMs)
                    put("dur", entry.durationMs)
                    put("ts", entry.lastWatchedTimestamp)
                    entry.episodeId?.let { put("epId", it) }
                    entry.seasonNum?.let { put("season", it) }
                    entry.episodeNum?.let { put("episode", it) }
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_VOD_PROGRESS, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getVodProgress(streamId: String): VodProgressEntry? {
        return getAllVodProgress()[streamId]
    }

    fun getAllVodProgress(): Map<String, VodProgressEntry> {
        val jsonStr = prefs.getString(KEY_VOD_PROGRESS, null) ?: return emptyMap()
        val result = mutableMapOf<String, VodProgressEntry>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val streamId = obj.getString("streamId")
                result[streamId] = VodProgressEntry(
                    streamId = streamId,
                    title = obj.getString("title"),
                    positionMs = obj.getLong("pos"),
                    durationMs = obj.getLong("dur"),
                    lastWatchedTimestamp = obj.optLong("ts", System.currentTimeMillis()),
                    episodeId = if (obj.has("epId")) obj.getString("epId") else null,
                    seasonNum = if (obj.has("season")) obj.getInt("season") else null,
                    episodeNum = if (obj.has("episode")) obj.getInt("episode") else null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getDeviceMode(): String {
        return prefs.getString(KEY_DEVICE_MODE, "tv") ?: "tv"
    }

    fun setDeviceMode(mode: String) {
        prefs.edit().putString(KEY_DEVICE_MODE, mode).apply()
    }
}
