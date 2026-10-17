package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.StreamItem
import com.example.core.model.StreamType
import com.example.core.model.VodProgressEntry
import com.example.core.player.PlayerState
import com.example.core.player.SharedStreamEngine
import com.example.core.player.VideoScaleMode
import com.example.core.storage.AppStorage
import com.example.ui.components.TVFocusableCard
import com.example.ui.components.VideoSurfacePlayer
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    streamItem: StreamItem,
    startPositionMs: Long = 0L,
    streamEngine: SharedStreamEngine,
    appStorage: AppStorage,
    allChannels: List<StreamItem>,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var currentItem by remember { mutableStateOf(streamItem) }
    var isPlaying by remember { mutableStateOf(true) }
    var showOsd by remember { mutableStateOf(true) }
    var showChannelDrawer by remember { mutableStateOf(false) }

    var currentPosition by remember { mutableLongStateOf(startPositionMs) }
    var duration by remember { mutableLongStateOf(streamItem.durationMinutes * 60_000L) }

    val scaleMode by streamEngine.scaleMode.collectAsState()
    val volume by streamEngine.volume.collectAsState()
    val brightness by streamEngine.brightness.collectAsState()

    // Auto-hide OSD after 4 seconds of inactivity
    LaunchedEffect(showOsd, isPlaying) {
        if (showOsd && isPlaying) {
            delay(4000)
            showOsd = false
        }
    }

    // Auto-save VOD progress periodically
    LaunchedEffect(currentPosition) {
        if (currentItem.type != StreamType.LIVE && currentPosition > 5000L) {
            appStorage.saveVodProgress(
                VodProgressEntry(
                    streamId = currentItem.streamId,
                    title = currentItem.name,
                    positionMs = currentPosition,
                    durationMs = duration
                )
            )
        }
    }

    // Touch gesture detector for mobile (left vertical = brightness, right vertical = volume, horizontal = seek)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showOsd = !showOsd },
                    onDoubleTap = {
                        isPlaying = !isPlaying
                        showOsd = true
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val screenWidth = size.width
                    val touchX = change.position.x

                    if (Math.abs(dragAmount.y) > Math.abs(dragAmount.x)) {
                        // Vertical drag: Brightness on left, Volume on right
                        if (touchX < screenWidth / 2) {
                            streamEngine.adjustBrightness(-dragAmount.y * 0.005f)
                        } else {
                            streamEngine.adjustVolume(-dragAmount.y * 0.005f)
                        }
                    } else {
                        // Horizontal drag: Timeline seek
                        if (currentItem.type != StreamType.LIVE) {
                            currentPosition = (currentPosition + (dragAmount.x * 200).toLong()).coerceIn(0L, duration)
                        }
                    }
                    showOsd = true
                }
            }
    ) {
        // Video Surface
        VideoSurfacePlayer(
            streamUrl = currentItem.streamUrl,
            isPlaying = isPlaying,
            startPositionMs = startPositionMs,
            scaleMode = scaleMode,
            onPositionUpdate = { pos, dur ->
                currentPosition = pos
                if (dur > 0) duration = dur
            },
            modifier = Modifier.fillMaxSize()
        )

        // OSD Overlay
        AnimatedVisibility(
            visible = showOsd,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Bar: Back button, Title, Resolution, Aspect Ratio, Channels Drawer button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                currentItem.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (currentItem.type == StreamType.LIVE) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = LiveRed,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("AO VIVO", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                        }
                        Text(
                            "${currentItem.categoryName} • ${currentItem.resolution}",
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }

                    // Aspect ratio toggle
                    IconButton(onClick = { streamEngine.toggleScaleMode() }) {
                        Icon(Icons.Default.AspectRatio, contentDescription = "Proporção", tint = TextPrimary)
                    }

                    // Channel Zapping Drawer toggle
                    IconButton(onClick = { showChannelDrawer = !showChannelDrawer }) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = "Lista de Canais", tint = NeonCyan)
                    }
                }

                // Volume & Brightness HUD (Center indicators)
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.6f),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${(volume * 100).toInt()}%", color = TextPrimary, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${(brightness * 100).toInt()}%", color = TextPrimary, fontSize = 12.sp)
                    }
                }

                // Bottom Controls: Timeline slider, Play/Pause, -10s, +10s, Next/Prev channel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    if (currentItem.type != StreamType.LIVE) {
                        val progressFraction = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
                        Slider(
                            value = progressFraction,
                            onValueChange = { fraction ->
                                currentPosition = (fraction * duration).toLong()
                                streamEngine.seekTo(currentPosition)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val posMin = currentPosition / 60_000
                            val posSec = (currentPosition % 60_000) / 1000
                            val durMin = duration / 60_000
                            val durSec = (duration % 60_000) / 1000
                            Text(String.format("%02d:%02d", posMin, posSec), color = TextSecondary, fontSize = 11.sp)
                            Text(String.format("%02d:%02d", durMin, durSec), color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Channel / Zapping
                        IconButton(
                            onClick = {
                                val idx = allChannels.indexOfFirst { it.streamId == currentItem.streamId }
                                if (idx > 0) {
                                    currentItem = allChannels[idx - 1]
                                    currentPosition = 0L
                                }
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Canal Anterior", tint = TextPrimary, modifier = Modifier.size(28.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Seek -10s
                        IconButton(
                            onClick = {
                                currentPosition = (currentPosition - 10_000L).coerceAtLeast(0L)
                                streamEngine.seekRelative(-10_000L)
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "-10s", tint = TextPrimary, modifier = Modifier.size(28.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Play / Pause Giant Button
                        Surface(
                            shape = CircleShape,
                            color = NeonCyan,
                            modifier = Modifier.size(56.dp)
                        ) {
                            IconButton(onClick = { isPlaying = !isPlaying }) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pausar" else "Play",
                                    tint = BackgroundDark,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Seek +10s
                        IconButton(
                            onClick = {
                                currentPosition = (currentPosition + 10_000L).coerceAtMost(duration)
                                streamEngine.seekRelative(10_000L)
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "+10s", tint = TextPrimary, modifier = Modifier.size(28.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Next Channel / Zapping
                        IconButton(
                            onClick = {
                                val idx = allChannels.indexOfFirst { it.streamId == currentItem.streamId }
                                if (idx != -1 && idx < allChannels.size - 1) {
                                    currentItem = allChannels[idx + 1]
                                    currentPosition = 0L
                                }
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Próximo Canal", tint = TextPrimary, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }

        // Side Channel Zapping Drawer
        AnimatedVisibility(
            visible = showChannelDrawer,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp),
                color = SurfaceDark.copy(alpha = 0.95f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ZAPPING DE CANAIS", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        IconButton(onClick = { showChannelDrawer = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allChannels) { ch ->
                            val isSelected = ch.streamId == currentItem.streamId
                            TVFocusableCard(
                                onClick = {
                                    currentItem = ch
                                    currentPosition = 0L
                                    showChannelDrawer = false
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else SurfaceCard)
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = if (isSelected) NeonCyan else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        ch.name,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
