package com.example.core.player

import android.content.Context
import android.media.AudioManager
import com.example.core.model.StreamItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class VideoScaleMode {
    FIT,
    FILL,
    RATIO_16_9
}

sealed interface PlayerState {
    data object Idle : PlayerState
    data class Buffering(val streamName: String, val opId: Long) : PlayerState
    data class Playing(val streamItem: StreamItem, val positionMs: Long, val durationMs: Long) : PlayerState
    data class Paused(val streamItem: StreamItem, val positionMs: Long, val durationMs: Long) : PlayerState
    data class Error(val streamName: String, val message: String) : PlayerState
}

class SharedStreamEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var currentOpId: Long = 0L
    private var watchdogJob: Job? = null

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentStream = MutableStateFlow<StreamItem?>(null)
    val currentStream: StateFlow<StreamItem?> = _currentStream.asStateFlow()

    private val _scaleMode = MutableStateFlow(VideoScaleMode.RATIO_16_9)
    val scaleMode: StateFlow<VideoScaleMode> = _scaleMode.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _brightness = MutableStateFlow(0.80f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    fun playStream(item: StreamItem, startPositionMs: Long = 0L) {
        currentOpId++
        val thisOp = currentOpId
        _currentStream.value = item
        _playerState.value = PlayerState.Buffering(item.name, thisOp)

        // Reset and launch watchdog routine to auto-recover if buffer stalls
        startWatchdog(thisOp, item)

        scope.launch {
            // Debounce / race-condition protection: ensure no faster switch superseded this
            delay(200)
            if (currentOpId != thisOp) return@launch

            // Transition to Playing state
            val dur = if (item.durationMinutes > 0) item.durationMinutes * 60_000L else 7200_000L
            _playerState.value = PlayerState.Playing(item, startPositionMs, dur)
        }
    }

    private fun startWatchdog(opId: Long, item: StreamItem) {
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            delay(12_000) // 12 seconds buffer timeout watchdog
            if (currentOpId == opId && _playerState.value is PlayerState.Buffering) {
                // Auto-recover watchdog: restart stream or notify error
                _playerState.value = PlayerState.Error(
                    item.name,
                    "Buffer timeout no servidor. Tentando reconexão automática..."
                )
                delay(1500)
                if (currentOpId == opId) {
                    playStream(item)
                }
            }
        }
    }

    fun togglePlayPause() {
        val curr = _playerState.value
        val stream = _currentStream.value ?: return
        when (curr) {
            is PlayerState.Playing -> {
                _playerState.value = PlayerState.Paused(stream, curr.positionMs, curr.durationMs)
            }
            is PlayerState.Paused -> {
                _playerState.value = PlayerState.Playing(stream, curr.positionMs, curr.durationMs)
            }
            else -> {}
        }
    }

    fun seekRelative(deltaMs: Long) {
        val curr = _playerState.value
        if (curr is PlayerState.Playing) {
            val newPos = (curr.positionMs + deltaMs).coerceIn(0L, curr.durationMs)
            _playerState.value = curr.copy(positionMs = newPos)
        } else if (curr is PlayerState.Paused) {
            val newPos = (curr.positionMs + deltaMs).coerceIn(0L, curr.durationMs)
            _playerState.value = curr.copy(positionMs = newPos)
        }
    }

    fun seekTo(positionMs: Long) {
        val curr = _playerState.value
        if (curr is PlayerState.Playing) {
            _playerState.value = curr.copy(positionMs = positionMs.coerceIn(0L, curr.durationMs))
        } else if (curr is PlayerState.Paused) {
            _playerState.value = curr.copy(positionMs = positionMs.coerceIn(0L, curr.durationMs))
        }
    }

    fun toggleScaleMode() {
        _scaleMode.value = when (_scaleMode.value) {
            VideoScaleMode.RATIO_16_9 -> VideoScaleMode.FIT
            VideoScaleMode.FIT -> VideoScaleMode.FILL
            VideoScaleMode.FILL -> VideoScaleMode.RATIO_16_9
        }
    }

    fun adjustVolume(delta: Float) {
        val newVol = (_volume.value + delta).coerceIn(0f, 1f)
        _volume.value = newVol
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (newVol * max).toInt(), 0)
        } catch (e: Exception) {
            // Ignore on systems without audio service permission
        }
    }

    fun adjustBrightness(delta: Float) {
        _brightness.value = (_brightness.value + delta).coerceIn(0.1f, 1f)
    }

    fun stop() {
        currentOpId++
        watchdogJob?.cancel()
        _playerState.value = PlayerState.Idle
    }
}
