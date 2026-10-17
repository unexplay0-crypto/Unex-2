package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.model.ChannelCategory
import com.example.core.model.SeriesGroup
import com.example.core.model.StreamItem
import com.example.core.model.StreamType
import com.example.core.player.PlayerState
import com.example.core.player.SharedStreamEngine
import com.example.core.service.XtreamCatalogService
import com.example.core.storage.AppStorage
import com.example.ui.components.LowPolyBackground
import com.example.ui.components.ParentalPinDialog
import com.example.ui.components.TVFocusableCard
import com.example.ui.components.VideoSurfacePlayer
import com.example.ui.components.VodResumeDialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

enum class TvNavSection {
    LIVE_TV,
    MOVIES,
    SERIES,
    FAVORITES,
    RECENTS,
    SETTINGS
}

@Composable
fun TvMainScreen(
    appStorage: AppStorage,
    catalogService: XtreamCatalogService,
    streamEngine: SharedStreamEngine,
    onOpenFullscreenPlayer: (StreamItem, Long) -> Unit,
    onSwitchToMobile: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(TvNavSection.LIVE_TV) }
    var isRailExpanded by remember { mutableStateOf(false) }

    // Catalog state
    val initialCatalog = remember { catalogService.getInitialCatalog() }
    val (liveChannels, vodMovies, seriesList) = initialCatalog

    // Selection states
    var selectedLiveCategory by remember { mutableStateOf(catalogService.defaultLiveCategories.first()) }
    var focusedChannel by remember { mutableStateOf(liveChannels.first()) }
    var selectedMovie by remember { mutableStateOf(vodMovies.first()) }
    var selectedSeries by remember { mutableStateOf(seriesList.first()) }

    // Modals state
    var showParentalPinModal by remember { mutableStateOf(false) }
    var pendingAdultAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showResumeModal by remember { mutableStateOf(false) }
    var resumeCandidateItem by remember { mutableStateOf<StreamItem?>(null) }
    var resumeSavedPos by remember { mutableStateOf(0L) }

    // Debounce for channel preview in TV mode (650ms as specified in doc)
    var debouncedPreviewChannel by remember { mutableStateOf(liveChannels.first()) }
    LaunchedEffect(focusedChannel) {
        delay(650)
        debouncedPreviewChannel = focusedChannel
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        LowPolyBackground()

        Row(modifier = Modifier.fillMaxSize()) {
            // 1. Navigation Rail (Collapsible: 64dp to 200dp)
            NavigationRail(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (isRailExpanded) 200.dp else 68.dp),
                containerColor = SurfaceDark.copy(alpha = 0.95f),
                header = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .clickable { isRailExpanded = !isRailExpanded }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NeonCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Tv, contentDescription = "UNEX", tint = NeonCyan, modifier = Modifier.size(24.dp))
                            }
                        }
                        if (isRailExpanded) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("UNEX VISION", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            ) {
                Spacer(modifier = Modifier.weight(1f))

                val railItems = listOf(
                    TvNavSection.LIVE_TV to ("TV Ao Vivo" to Icons.Default.LiveTv),
                    TvNavSection.MOVIES to ("Filmes VOD" to Icons.Default.Movie),
                    TvNavSection.SERIES to ("Séries" to Icons.Default.VideoLibrary),
                    TvNavSection.FAVORITES to ("Favoritos" to Icons.Default.Favorite),
                    TvNavSection.RECENTS to ("Recentes" to Icons.Default.History),
                    TvNavSection.SETTINGS to ("Ajustes / TV" to Icons.Default.Settings)
                )

                railItems.forEach { (section, pair) ->
                    val (label, icon) = pair
                    NavigationRailItem(
                        selected = selectedSection == section,
                        onClick = { selectedSection = section },
                        icon = {
                            if (section == TvNavSection.LIVE_TV) {
                                BadgedBox(badge = { Badge(containerColor = LiveRed) }) {
                                    Icon(icon, contentDescription = label)
                                }
                            } else {
                                Icon(icon, contentDescription = label)
                            }
                        },
                        label = if (isRailExpanded) { { Text(label, fontSize = 11.sp) } } else null,
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = NeonCyan.copy(alpha = 0.2f)
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Mobile Mode Switcher quick button
                IconButton(onClick = onSwitchToMobile) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = "Modo Celular", tint = TextMuted)
                }
            }

            // 2. Main Content Area
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (selectedSection) {
                    TvNavSection.LIVE_TV -> {
                        // 3-Column Live TV Interface (Categories, Channels, Preview Dock + EPG)
                        LiveTv3Columns(
                            categories = catalogService.defaultLiveCategories,
                            channels = liveChannels,
                            selectedCategory = selectedLiveCategory,
                            focusedChannel = focusedChannel,
                            previewChannel = debouncedPreviewChannel,
                            epgList = catalogService.getEpgForChannel(debouncedPreviewChannel.name),
                            onSelectCategory = { cat ->
                                if (cat.isAdult) {
                                    pendingAdultAction = { selectedLiveCategory = cat }
                                    showParentalPinModal = true
                                } else {
                                    selectedLiveCategory = cat
                                }
                            },
                            onFocusChannel = { focusedChannel = it },
                            onPlayChannel = { ch ->
                                appStorage.addRecent(ch)
                                onOpenFullscreenPlayer(ch, 0L)
                            },
                            onToggleFav = { appStorage.toggleFavorite(it.streamId) },
                            isFavorite = { appStorage.isFavorite(it.streamId) }
                        )
                    }

                    TvNavSection.MOVIES -> {
                        // Netflix-Style Immersive VOD with Dynamic Hero Banner
                        VodMoviesSection(
                            movies = vodMovies,
                            selectedMovie = selectedMovie,
                            onSelectMovie = { selectedMovie = it },
                            onWatchMovie = { movie ->
                                val progress = appStorage.getVodProgress(movie.streamId)
                                if (progress != null && progress.positionMs > 10_000L) {
                                    resumeCandidateItem = movie
                                    resumeSavedPos = progress.positionMs
                                    showResumeModal = true
                                } else {
                                    appStorage.addRecent(movie)
                                    onOpenFullscreenPlayer(movie, 0L)
                                }
                            }
                        )
                    }

                    TvNavSection.SERIES -> {
                        // Series Section
                        SeriesSection(
                            seriesList = seriesList,
                            selectedSeries = selectedSeries,
                            onSelectSeries = { selectedSeries = it },
                            onPlayEpisode = { ep ->
                                val item = StreamItem(
                                    streamId = ep.episodeId,
                                    name = "${selectedSeries.name} - ${ep.title}",
                                    type = StreamType.SERIES,
                                    streamIcon = selectedSeries.cover,
                                    streamUrl = ep.streamUrl,
                                    categoryId = "series",
                                    categoryName = "Séries VIP"
                                )
                                appStorage.addRecent(item)
                                onOpenFullscreenPlayer(item, 0L)
                            }
                        )
                    }

                    TvNavSection.FAVORITES -> {
                        FavoritesSection(
                            channels = liveChannels.filter { appStorage.isFavorite(it.streamId) },
                            onPlay = {
                                appStorage.addRecent(it)
                                onOpenFullscreenPlayer(it, 0L)
                            }
                        )
                    }

                    TvNavSection.RECENTS -> {
                        RecentsSection(
                            recents = appStorage.getRecents(),
                            onPlay = { onOpenFullscreenPlayer(it, 0L) }
                        )
                    }

                    TvNavSection.SETTINGS -> {
                        SettingsSection(
                            appStorage = appStorage,
                            onSwitchToMobile = onSwitchToMobile
                        )
                    }

                    else -> {}
                }
            }
        }

        // Modals
        if (showParentalPinModal) {
            ParentalPinDialog(
                correctPin = appStorage.getParentalPin(),
                onUnlocked = {
                    showParentalPinModal = false
                    pendingAdultAction?.invoke()
                    pendingAdultAction = null
                },
                onDismiss = {
                    showParentalPinModal = false
                    pendingAdultAction = null
                }
            )
        }

        if (showResumeModal && resumeCandidateItem != null) {
            val item = resumeCandidateItem!!
            VodResumeDialog(
                title = item.name,
                positionMs = resumeSavedPos,
                durationMs = item.durationMinutes * 60_000L,
                onResume = {
                    showResumeModal = false
                    appStorage.addRecent(item)
                    onOpenFullscreenPlayer(item, resumeSavedPos)
                },
                onRestart = {
                    showResumeModal = false
                    appStorage.addRecent(item)
                    onOpenFullscreenPlayer(item, 0L)
                },
                onDismiss = { showResumeModal = false }
            )
        }
    }
}

/**
 * 3-Column Live TV Interface: Categories, Channels, Preview Dock + EPG
 */
@Composable
fun LiveTv3Columns(
    categories: List<ChannelCategory>,
    channels: List<StreamItem>,
    selectedCategory: ChannelCategory,
    focusedChannel: StreamItem,
    previewChannel: StreamItem,
    epgList: List<com.example.core.model.EpgProgram>,
    onSelectCategory: (ChannelCategory) -> Unit,
    onFocusChannel: (StreamItem) -> Unit,
    onPlayChannel: (StreamItem) -> Unit,
    onToggleFav: (StreamItem) -> Unit,
    isFavorite: (StreamItem) -> Boolean
) {
    val filteredChannels = remember(selectedCategory) {
        if (selectedCategory.categoryId == "all") channels
        else channels.filter { it.categoryId == selectedCategory.categoryId }
    }

    Row(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Coluna 1: Categorias (Largura fixa ~190dp)
        Column(
            modifier = Modifier
                .width(190.dp)
                .fillMaxHeight()
                .padding(end = 10.dp)
        ) {
            Text(
                "CATEGORIAS",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    val isSelected = cat.categoryId == selectedCategory.categoryId
                    TVFocusableCard(
                        onClick = { onSelectCategory(cat) },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                cat.categoryName,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Coluna 2: Grelha de Canais (Largura flexível)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${selectedCategory.categoryName.uppercase()} (${filteredChannels.size})",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text("D-Pad: OK para assistir", color = TextMuted, fontSize = 10.sp)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filteredChannels) { ch ->
                    val isFocused = ch.streamId == focusedChannel.streamId
                    TVFocusableCard(
                        onClick = {
                            onFocusChannel(ch)
                            onPlayChannel(ch)
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isFocused) SurfaceCardElevated else SurfaceCard)
                                .padding(horizontal = 10.dp)
                        ) {
                            // Logo
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                AsyncImage(
                                    model = ch.streamIcon,
                                    contentDescription = ch.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))

                            // Name & Resolution tag
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    ch.name,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${ch.resolution} • Ao Vivo",
                                    color = if (isFocused) NeonCyan else TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            // Favorite Icon
                            IconButton(
                                onClick = { onToggleFav(ch) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    if (isFavorite(ch)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favoritar",
                                    tint = if (isFavorite(ch)) LiveRed else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Coluna 3: Dock de Pré-Visualização (Mini-player 16:9 + EPG)
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .padding(start = 10.dp)
        ) {
            Text(
                "PRÉVIA AO VIVO & EPG",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Mini-player Surface (Aspect Ratio 16:9)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    VideoSurfacePlayer(
                        streamUrl = previewChannel.streamUrl,
                        isPlaying = true,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Fullscreen Button Overlay
                    Button(
                        onClick = { onPlayChannel(previewChannel) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan.copy(alpha = 0.85f),
                            contentColor = BackgroundDark
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .height(28.dp)
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Tela Cheia", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Channel Title & Resolution
            Text(
                previewChannel.name,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )
            Text(
                previewChannel.plot,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // EPG (Electronic Program Guide) Details
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceCard,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "GUIA DE PROGRAMAÇÃO (EPG)",
                        color = NeonPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    epgList.forEach { prog ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    prog.title,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "${prog.startTime} - ${prog.endTime}",
                                    color = NeonCyan,
                                    fontSize = 10.sp
                                )
                            }
                            if (prog.progressPercent > 0f) {
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { prog.progressPercent },
                                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
                                    color = NeonCyan,
                                    trackColor = SurfaceDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Netflix-Style Immersive VOD with Dynamic Hero Banner
 */
@Composable
fun VodMoviesSection(
    movies: List<StreamItem>,
    selectedMovie: StreamItem,
    onSelectMovie: (StreamItem) -> Unit,
    onWatchMovie: (StreamItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 1. Dynamic Hero Banner (Synopsis, Star Rating, Backdrop)
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = selectedMovie.streamIcon,
                    contentDescription = selectedMovie.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Vignette Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    BackgroundDark.copy(alpha = 0.95f),
                                    BackgroundDark.copy(alpha = 0.75f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.65f)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("LANÇAMENTO EXCLUSIVO UNEX", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(selectedMovie.name, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = NeonGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${selectedMovie.rating} • ${selectedMovie.releaseDate} • ${selectedMovie.durationMinutes} min • ${selectedMovie.resolution}", color = TextSecondary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        selectedMovie.plot,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onWatchMovie(selectedMovie) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Assistir Agora", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Horizontal Movie Posters Carousel
        Text("CATÁLOGO DE FILMES & CINEMA 4K", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(movies) { movie ->
                val isSelected = movie.streamId == selectedMovie.streamId
                TVFocusableCard(
                    onClick = {
                        onSelectMovie(movie)
                        onWatchMovie(movie)
                    },
                    modifier = Modifier
                        .width(135.dp)
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = movie.streamIcon,
                            contentDescription = movie.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Vignette shadow at bottom
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                                        startY = 100f
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                movie.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${movie.releaseDate} • ${movie.resolution}",
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Series section with episode list
 */
@Composable
fun SeriesSection(
    seriesList: List<SeriesGroup>,
    selectedSeries: SeriesGroup,
    onSelectSeries: (SeriesGroup) -> Unit,
    onPlayEpisode: (com.example.core.model.EpisodeItem) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Series Poster List
        Column(modifier = Modifier.width(220.dp)) {
            Text("SÉRIES DISPONÍVEIS", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(seriesList) { ser ->
                    val isSelected = ser.seriesId == selectedSeries.seriesId
                    TVFocusableCard(
                        onClick = { onSelectSeries(ser) },
                        modifier = Modifier.fillMaxWidth().height(64.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isSelected) SurfaceCardElevated else SurfaceCard)
                                .padding(6.dp)
                        ) {
                            AsyncImage(
                                model = ser.cover,
                                contentDescription = ser.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(ser.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${ser.genre} • ★ ${ser.rating}", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Selected Series Episodes
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${selectedSeries.name.uppercase()} - EPISÓDIOS",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(selectedSeries.plot, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectedSeries.episodes) { ep ->
                    TVFocusableCard(
                        onClick = { onPlayEpisode(ep) },
                        modifier = Modifier.fillMaxWidth().height(58.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ep.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(ep.plot, color = TextMuted, fontSize = 11.sp, maxLines = 1)
                            }
                            Text("${ep.durationMinutes} min", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesSection(
    channels: List<StreamItem>,
    onPlay: (StreamItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("MEUS FAVORITOS", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (channels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum canal ou filme favoritado ainda.", color = TextMuted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(channels) { ch ->
                    TVFocusableCard(
                        onClick = { onPlay(ch) },
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = LiveRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(ch.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentsSection(
    recents: List<StreamItem>,
    onPlay: (StreamItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("HISTÓRICO RECENTE", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (recents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum histórico recente disponível.", color = TextMuted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recents) { item ->
                    TVFocusableCard(
                        onClick = { onPlay(item) },
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(item.categoryName, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    appStorage: AppStorage,
    onSwitchToMobile: () -> Unit
) {
    val session = appStorage.getSession()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("CONFIGURAÇÕES DO SISTEMA UNEX VISION", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SurfaceCard,
            modifier = Modifier.fillMaxWidth(0.7f).padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("SERVIDOR XTREAM CONECTADO", color = NeonPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("URL: ${session.serverUrl}", color = TextPrimary, fontSize = 13.sp)
                Text("Usuário: ${session.username}", color = TextSecondary, fontSize = 12.sp)
                Text("Status: Ativo até ${session.expirationDate}", color = NeonCyan, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSwitchToMobile,
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = TextPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.PhoneAndroid, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Mudar Para Interface Mobile (Touch)")
        }
    }
}
