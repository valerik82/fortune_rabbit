package com.rabbitsluckandfortuneppamobs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rabbitsluckandfortuneppamobs.analytics.AnalyticsGate
import com.rabbitsluckandfortuneppamobs.audio.AudioManager
import com.rabbitsluckandfortuneppamobs.data.Catalog
import com.rabbitsluckandfortuneppamobs.data.GameRepository
import com.rabbitsluckandfortuneppamobs.game.LevelGenerator
import com.rabbitsluckandfortuneppamobs.models.Difficulty
import com.rabbitsluckandfortuneppamobs.models.GameMode
import com.rabbitsluckandfortuneppamobs.ui.AppViewModel
import com.rabbitsluckandfortuneppamobs.ui.GameViewModel
import com.rabbitsluckandfortuneppamobs.ui.navigation.Routes
import com.rabbitsluckandfortuneppamobs.ui.screens.CollectionScreen
import com.rabbitsluckandfortuneppamobs.ui.screens.GameScreen
import com.rabbitsluckandfortuneppamobs.ui.screens.GateErrorScreen
import com.rabbitsluckandfortuneppamobs.ui.screens.LevelSelectScreen
import com.rabbitsluckandfortuneppamobs.ui.screens.MainMenuScreen
import com.rabbitsluckandfortuneppamobs.ui.screens.PrivacyPolicyScreen
import com.rabbitsluckandfortuneppamobs.ui.screens.SettingsScreen
import com.rabbitsluckandfortuneppamobs.ui.screens.ShopScreen
import com.rabbitsluckandfortuneppamobs.ui.screens.SplashScreen
import com.rabbitsluckandfortuneppamobs.ui.screens.StubScreen
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneRabbitTheme

class MainActivity : ComponentActivity() {

    private var pendingExternalUrl: String? = null
    private var hasPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as FortuneRabbitApp

        setContent {
            FortuneRabbitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    var showSplash by remember { mutableStateOf(true) }
                    var gateDecision by remember { mutableStateOf<AnalyticsGate.GateDecision?>(null) }
                    var retryToken by remember { mutableIntStateOf(0) }

                    LaunchedEffect(showSplash, retryToken) {
                        if (!showSplash) {
                            gateDecision = null
                            app.analytics.awaitAttribution()
                            val url = AnalyticsGate.buildStubUrl(
                                context = context,
                                isOrganic = app.analytics.isOrganic,
                                appsFlyerId = app.analytics.appsFlyerId,
                            )
                            gateDecision = AnalyticsGate.fetchGateDecision(context, url)
                        }
                    }

                    LaunchedEffect(gateDecision) {
                        val decision = gateDecision
                        if (decision is AnalyticsGate.GateDecision.OpenExternalBrowser) {
                            pendingExternalUrl = decision.url
                            openExternalUrl(decision.url)
                        }
                    }

                    when {
                        showSplash -> SplashScreen(onFinished = { showSplash = false })
                        gateDecision == null -> SplashScreen(onFinished = {})
                        gateDecision is AnalyticsGate.GateDecision.ShowApp ->
                            FortuneRabbitGame(app.repository, app.audio)
                        gateDecision is AnalyticsGate.GateDecision.ShowWebView ->
                            StubScreen(url = (gateDecision as AnalyticsGate.GateDecision.ShowWebView).url)
                        gateDecision is AnalyticsGate.GateDecision.OpenExternalBrowser ->
                            SplashScreen(onFinished = {})
                        gateDecision is AnalyticsGate.GateDecision.Error ->
                            GateErrorScreen(onRetry = { retryToken++ })
                    }
                }
            }
        }
    }

    private fun openExternalUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onPause() {
        super.onPause()
        hasPaused = true
    }

    override fun onResume() {
        super.onResume()
        if (hasPaused) {
            pendingExternalUrl?.let { openExternalUrl(it) }
        }
    }
}

@Composable
private fun FortuneRabbitGame(repository: GameRepository, audio: AudioManager) {
    val navController = rememberNavController()

    val appViewModel: AppViewModel = viewModel(
        factory = viewModelFactory {
            initializer { AppViewModel(repository, audio) }
        }
    )
    val progress by appViewModel.progress.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Routes.MENU) {

        composable(Routes.MENU) {
            val rabbit = Catalog.byId(progress.selectedRabbitSkin)?.glyph ?: "🐰"
            val dailyAvailable = progress.dailyChallengeLastDate != LevelGenerator.dateKey()
            MainMenuScreen(
                coins = progress.totalCoins,
                rabbitGlyph = rabbit,
                selectedBackground = progress.selectedBackground,
                dailyAvailable = dailyAvailable,
                onPlay = { appViewModel.tap(); navController.navigate(Routes.LEVELS) },
                onDaily = {
                    appViewModel.tap()
                    navController.navigate(
                        Routes.game(GameMode.DAILY_CHALLENGE, 0, LevelGenerator.dailyDifficulty())
                    )
                },
                onCollection = { appViewModel.tap(); navController.navigate(Routes.COLLECTION) },
                onShop = { appViewModel.tap(); navController.navigate(Routes.SHOP) },
                onSettings = { appViewModel.tap(); navController.navigate(Routes.SETTINGS) },
                onPrivacy = { appViewModel.tap(); navController.navigate(Routes.PRIVACY) }
            )
        }

        composable(Routes.LEVELS) {
            val levels = LevelGenerator.buildLevels(progress.completedLevels)
            LevelSelectScreen(
                levels = levels,
                starsFor = { progress.starsFor(it) },
                selectedBackground = progress.selectedBackground,
                onSelect = { level, mode ->
                    appViewModel.tap()
                    navController.navigate(Routes.game(mode, level.levelId, level.difficulty))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("levelId") { type = NavType.IntType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { entry ->
            val mode = GameMode.valueOf(entry.arguments?.getString("mode") ?: GameMode.CLASSIC.name)
            val levelId = entry.arguments?.getInt("levelId") ?: 1
            val difficulty =
                Difficulty.valueOf(entry.arguments?.getString("difficulty") ?: Difficulty.EASY.name)

            val gameViewModel: GameViewModel = viewModel(
                key = "game_${mode}_$levelId",
                factory = viewModelFactory {
                    initializer { GameViewModel(repository, audio, mode, levelId, difficulty) }
                }
            )
            androidx.compose.runtime.LaunchedEffect(progress.totalCoins) {
                gameViewModel.bindCoins(progress.totalCoins)
            }

            GameScreen(
                state = gameViewModel.uiState,
                selectedCardBack = progress.selectedCardBack,
                selectedBackground = progress.selectedBackground,
                hasNextLevel = mode == GameMode.CLASSIC && levelId < LevelGenerator.TOTAL_LEVELS,
                onCardTapped = gameViewModel::onCardTapped,
                onPause = gameViewModel::pause,
                onResume = gameViewModel::resume,
                onRestart = gameViewModel::restart,
                onNextLevel = {
                    val next = levelId + 1
                    val nextDiff = LevelGenerator.difficultyForLevel(next)
                    navController.navigate(Routes.game(GameMode.CLASSIC, next, nextDiff)) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                },
                onMenu = {
                    navController.navigate(Routes.MENU) {
                        popUpTo(Routes.MENU) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.COLLECTION) {
            CollectionScreen(
                progress = progress,
                selectedBackground = progress.selectedBackground,
                onEquip = { appViewModel.selectItem(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SHOP) {
            ShopScreen(
                progress = progress,
                selectedBackground = progress.selectedBackground,
                onBuy = { appViewModel.purchase(it) },
                onEquip = { appViewModel.selectItem(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                progress = progress,
                appVersion = BuildConfig.VERSION_NAME,
                onSound = { appViewModel.setSound(it) },
                onMusic = { appViewModel.setMusic(it) },
                onVibration = { appViewModel.setVibration(it) },
                onReset = { appViewModel.resetProgress() },
                onPrivacy = { navController.navigate(Routes.PRIVACY) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PRIVACY) {
            PrivacyPolicyScreen(
                selectedBackground = progress.selectedBackground,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
