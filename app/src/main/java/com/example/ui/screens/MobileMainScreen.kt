package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.model.ChannelCategory
import com.example.core.model.StreamItem
import com.example.core.model.StreamType
import com.example.core.service.XtreamCatalogService
import com.example.core.storage.AppStorage
import com.example.ui.components.LowPolyBackground
import com.example.ui.components.ParentalPinDialog
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileMainScreen(
    appStorage: AppStorage,
    catalogService: XtreamCatalogService,
    onOpenFullscreenPlayer: (StreamItem, Long) -> Unit,
    onSwitchToTv: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live, 1: Filmes, 2: Séries, 3: Favoritos

    val initialCatalog = remember { catalogService.getInitialCatalog() }
    val (liveChannels, vodMovies, seriesList) = initialCatalog

    // Modals
    var showParentalPinModal by remember { mutableStateOf(false) }
    var pendingAdultAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showResumeModal by remember { mutableStateOf(false) }
    var resumeCandidateItem by remember { mutableStateOf<StreamItem?>(null) }
    var resumeSavedPos by remember { mutableStateOf(0L) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SurfaceDark,
                modifier = Modifier.width(280.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = NeonCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LiveTv, contentDescription = null, tint = NeonCyan)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("UNEX VISION", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Mobile Touch Edition", color = NeonCyan, fontSize = 11.sp)
                        }
                    }
                }

                NavigationDrawerItem(
                    label = { Text("TV Ao Vivo", color = TextPrimary) },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.LiveTv, contentDescription = null, tint = NeonCyan) }
                )

                NavigationDrawerItem(
                    label = { Text("Filmes VOD", color = TextPrimary) },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Movie, contentDescription = null, tint = NeonPurple) }
                )

                NavigationDrawerItem(
                    label = { Text("Séries VIP", color = TextPrimary) },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = NeonGold) }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Switch to TV Mode
                NavigationDrawerItem(
                    label = { Text("Mudar Para Modo TV Box", color = TextSecondary) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSwitchToTv()
                    },
                    icon = { Icon(Icons.Default.Tv, contentDescription = null, tint = TextMuted) },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = BackgroundDark,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("UNEX", color = NeonCyan, fontWeight = FontWeight.ExtraBold)
                            Text(" VISION", color = TextPrimary, fontWeight = FontWeight.Light)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = onSwitchToTv) {
                            Icon(Icons.Default.Tv, contentDescription = "Modo TV", tint = TextSecondary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark.copy(alpha = 0.95f))
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LowPolyBackground()

                Column(modifier = Modifier.fillMaxSize()) {
                    // Floating Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar canais, filmes ou séries...", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpar", tint = TextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )

                    // Tab Selector
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = NeonCyan,
                        edgePadding = 12.dp
                    ) {
                        listOf("Canais Ao Vivo", "Filmes VOD", "Séries", "Favoritos").forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        color = if (selectedTab == index) NeonCyan else TextSecondary,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            )
                        }
                    }

                    // Tab Content
                    when (selectedTab) {
                        0 -> { // Live TV
                            val filtered = liveChannels.filter {
                                searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filtered) { ch ->
                                    Card(
                                        onClick = {
                                            if (ch.isAdult) {
                                                pendingAdultAction = {
                                                    appStorage.addRecent(ch)
                                                    onOpenFullscreenPlayer(ch, 0L)
                                                }
                                                showParentalPinModal = true
                                            } else {
                                                appStorage.addRecent(ch)
                                                onOpenFullscreenPlayer(ch, 0L)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(10.dp)
                                        ) {
                                            AsyncImage(
                                                model = ch.streamIcon,
                                                contentDescription = ch.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp))
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(ch.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("${ch.categoryName} • ${ch.resolution}", color = NeonCyan, fontSize = 11.sp)
                                            }
                                            IconButton(onClick = { appStorage.toggleFavorite(ch.streamId) }) {
                                                Icon(
                                                    if (appStorage.isFavorite(ch.streamId)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Favoritar",
                                                    tint = if (appStorage.isFavorite(ch.streamId)) LiveRed else TextMuted
                                                )
                                            }
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = NeonCyan)
                                        }
                                    }
                                }
                            }
                        }

                        1 -> { // Movies VOD Grid
                            val filtered = vodMovies.filter {
                                searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filtered) { movie ->
                                    Card(
                                        onClick = {
                                            val progress = appStorage.getVodProgress(movie.streamId)
                                            if (progress != null && progress.positionMs > 10_000L) {
                                                resumeCandidateItem = movie
                                                resumeSavedPos = progress.positionMs
                                                showResumeModal = true
                                            } else {
                                                appStorage.addRecent(movie)
                                                onOpenFullscreenPlayer(movie, 0L)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                                    ) {
                                        Column {
                                            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                                                AsyncImage(
                                                    model = movie.streamIcon,
                                                    contentDescription = movie.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.75f),
                                                    shape = RoundedCornerShape(bottomStart = 8.dp),
                                                    modifier = Modifier.align(Alignment.TopEnd)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Star, contentDescription = null, tint = NeonGold, modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text("${movie.rating}", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(movie.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                                Text("${movie.releaseDate} • ${movie.resolution}", color = TextSecondary, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> { // Series
                            LazyColumn(
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(seriesList) { ser ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row {
                                                AsyncImage(
                                                    model = ser.cover,
                                                    contentDescription = ser.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp))
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(ser.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                    Text("${ser.genre} • ${ser.seasonsCount} Temporada", color = NeonCyan, fontSize = 11.sp)
                                                    Text(ser.plot, color = TextSecondary, fontSize = 11.sp, maxLines = 2)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text("Episódios:", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            ser.episodes.forEach { ep ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            val item = StreamItem(
                                                                streamId = ep.episodeId,
                                                                name = "${ser.name} - ${ep.title}",
                                                                type = StreamType.SERIES,
                                                                streamIcon = ser.cover,
                                                                streamUrl = ep.streamUrl,
                                                                categoryId = "series",
                                                                categoryName = "Séries"
                                                            )
                                                            appStorage.addRecent(item)
                                                            onOpenFullscreenPlayer(item, 0L)
                                                        }
                                                        .padding(vertical = 6.dp)
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(ep.title, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                                    Text("${ep.durationMinutes}m", color = TextMuted, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        3 -> { // Favorites
                            val favChannels = liveChannels.filter { appStorage.isFavorite(it.streamId) }
                            if (favChannels.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Nenhum item marcado como favorito.", color = TextMuted)
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(favChannels) { ch ->
                                        Card(
                                            onClick = {
                                                appStorage.addRecent(ch)
                                                onOpenFullscreenPlayer(ch, 0L)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(12.dp)
                                            ) {
                                                Icon(Icons.Default.Favorite, contentDescription = null, tint = LiveRed)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(ch.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
