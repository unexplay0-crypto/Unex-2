package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.core.model.StreamItem
import com.example.core.player.SharedStreamEngine
import com.example.core.service.XtreamCatalogService
import com.example.core.storage.AppStorage
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MobileMainScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.TvMainScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    LOGIN,
    TV_MAIN,
    MOBILE_MAIN,
    FULLSCREEN_PLAYER
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    UnexVisionApp()
                }
            }
        }
    }
}

@Composable
fun UnexVisionApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appStorage = remember { AppStorage(context) }
    val catalogService = remember { XtreamCatalogService() }
    val streamEngine = remember { SharedStreamEngine(context) }

    val initialMode = remember { appStorage.getDeviceMode() }
    var currentScreen by remember {
        mutableStateOf(if (initialMode == "mobile") AppScreen.MOBILE_MAIN else AppScreen.TV_MAIN)
    }

    var activeStreamItem by remember { mutableStateOf<StreamItem?>(null) }
    var activeStartPos by remember { mutableLongStateOf(0L) }
    var previousScreen by remember { mutableStateOf(AppScreen.TV_MAIN) }

    val allChannels = remember { catalogService.getInitialCatalog().first }

    when (currentScreen) {
        AppScreen.LOGIN -> {
            LoginScreen(
                appStorage = appStorage,
                onLoginSuccess = {
                    currentScreen = if (appStorage.getDeviceMode() == "mobile") AppScreen.MOBILE_MAIN else AppScreen.TV_MAIN
                }
            )
        }

        AppScreen.TV_MAIN -> {
            TvMainScreen(
                appStorage = appStorage,
                catalogService = catalogService,
                streamEngine = streamEngine,
                onOpenFullscreenPlayer = { item, startPos ->
                    activeStreamItem = item
                    activeStartPos = startPos
                    previousScreen = AppScreen.TV_MAIN
                    currentScreen = AppScreen.FULLSCREEN_PLAYER
                },
                onSwitchToMobile = {
                    appStorage.setDeviceMode("mobile")
                    currentScreen = AppScreen.MOBILE_MAIN
                }
            )
        }

        AppScreen.MOBILE_MAIN -> {
            MobileMainScreen(
                appStorage = appStorage,
                catalogService = catalogService,
                onOpenFullscreenPlayer = { item, startPos ->
                    activeStreamItem = item
                    activeStartPos = startPos
                    previousScreen = AppScreen.MOBILE_MAIN
                    currentScreen = AppScreen.FULLSCREEN_PLAYER
                },
                onSwitchToTv = {
                    appStorage.setDeviceMode("tv")
                    currentScreen = AppScreen.TV_MAIN
                }
            )
        }

        AppScreen.FULLSCREEN_PLAYER -> {
            activeStreamItem?.let { item ->
                PlayerScreen(
                    streamItem = item,
                    startPositionMs = activeStartPos,
                    streamEngine = streamEngine,
                    appStorage = appStorage,
                    allChannels = allChannels,
                    onClose = {
                        streamEngine.stop()
                        currentScreen = previousScreen
                    }
                )
            } ?: run {
                currentScreen = previousScreen
            }
        }
    }
}
