package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.core.player.VideoScaleMode
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun VideoSurfacePlayer(
    streamUrl: String,
    isPlaying: Boolean,
    startPositionMs: Long = 0L,
    scaleMode: VideoScaleMode = VideoScaleMode.RATIO_16_9,
    onPositionUpdate: (currentMs: Long, durationMs: Long) -> Unit = { _, _ -> },
    onError: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        videoViewRef?.let { vv ->
            if (isPlaying) {
                if (!vv.isPlaying) vv.start()
            } else {
                if (vv.isPlaying) vv.pause()
            }
        }
    }

    Box(
        modifier = modifier
            .background(BackgroundDark)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setOnPreparedListener { mp ->
                        isBuffering = false
                        hasError = false
                        mp.isLooping = true
                        if (startPositionMs > 0) {
                            seekTo(startPositionMs.toInt())
                        }
                        if (isPlaying) {
                            start()
                        }
                        onPositionUpdate(currentPosition.toLong(), duration.toLong())
                    }
                    setOnErrorListener { _, what, extra ->
                        isBuffering = false
                        hasError = true
                        onError("Erro no fluxo (código $what-$extra)")
                        true // Handled
                    }
                    setOnInfoListener { _, what, _ ->
                        if (what == 701) { // BUFFERING_START
                            isBuffering = true
                        } else if (what == 702) { // BUFFERING_END
                            isBuffering = false
                        }
                        true
                    }
                    try {
                        setVideoURI(Uri.parse(streamUrl))
                    } catch (e: Exception) {
                        hasError = true
                        onError(e.localizedMessage ?: "Falha ao carregar stream")
                    }
                    videoViewRef = this
                }
            },
            update = { vv ->
                videoViewRef = vv
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay buffering indicator
        if (isBuffering && !hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = NeonCyan,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sintonizando stream...",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Overlay error indicator
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Sinal temporariamente indisponível", color = TextPrimary, fontSize = 12.sp)
                    Text("Tentando reconectar...", color = TextMuted, fontSize = 11.sp)
                }
            }
        }
    }

    DisposableEffect(streamUrl) {
        onDispose {
            videoViewRef?.stopPlayback()
            videoViewRef = null
        }
    }
}
