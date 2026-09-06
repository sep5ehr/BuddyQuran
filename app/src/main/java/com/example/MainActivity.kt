package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.persianquran.data.model.QuranTheme
import com.example.persianquran.data.surah.QuranMetadata
import com.example.persianquran.ui.components.AudioMiniPlayer
import com.example.persianquran.ui.components.FullAudioPlayerDialog
import com.example.persianquran.ui.components.PersianBottomBar
import com.example.persianquran.ui.screens.BookmarksScreen
import com.example.persianquran.ui.screens.ChecklistScreen
import com.example.persianquran.ui.screens.FirstLaunchScreen
import com.example.persianquran.ui.screens.HomeScreen
import com.example.persianquran.ui.screens.PlanningScreen
import com.example.persianquran.ui.screens.ReaderScreen
import com.example.persianquran.ui.screens.ScheduleScreen
import com.example.persianquran.ui.screens.SearchScreen
import com.example.persianquran.ui.screens.SettingsScreen
import com.example.persianquran.ui.screens.SurahListScreen
import com.example.persianquran.ui.viewmodel.FirstLaunchState
import com.example.persianquran.ui.viewmodel.QuranViewModel
import com.example.persianquran.ui.viewmodel.Screen
import com.example.ui.theme.QuranAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: QuranViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()

            val isDark = when (settings.theme) {
                QuranTheme.LIGHT -> false
                QuranTheme.DARK -> true
                QuranTheme.SYSTEM -> isSystemInDarkTheme()
            }

            QuranAppTheme(darkTheme = isDark) {
                // Ensure RTL layout across the entire application for authentic Persian experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    PersianQuranApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PersianQuranApp(viewModel: QuranViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentSurah by viewModel.currentSurah.collectAsState()
    val versesState by viewModel.versesState.collectAsState()
    val targetVerseToScroll by viewModel.targetVerseToScroll.collectAsState()
    val surahList by viewModel.surahList.collectAsState()
    val surahSearchQuery by viewModel.surahSearchQuery.collectAsState()
    val globalSearchQuery by viewModel.globalSearchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val readingProgress by viewModel.readingProgress.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val selectedVerseForMenu by viewModel.selectedVerseForMenu.collectAsState()

    val allPlans by viewModel.allPlans.collectAsState()
    val activePlan by viewModel.activePlan.collectAsState()
    val activeSchedule by viewModel.activePlanSchedule.collectAsState()
    val todayScheduleItem by viewModel.todayScheduleItem.collectAsState()
    val checklistItems by viewModel.checklistItems.collectAsState()
    val firstLaunchState by viewModel.firstLaunchState.collectAsState()

    var showFullPlayerDialog by remember { mutableStateOf(false) }

    // If first launch data download is in progress or failed, show FirstLaunchScreen (user can also skip/continue)
    if (firstLaunchState !is FirstLaunchState.Ready) {
        FirstLaunchScreen(
            state = firstLaunchState,
            onRetry = { viewModel.startFirstLaunchDownload() },
            onContinueToApp = { viewModel.dismissFirstLaunchScreen() }
        )
        return
    }

    // System Back Press handling
    BackHandler(enabled = currentScreen !is Screen.Home || selectedVerseForMenu != null) {
        if (selectedVerseForMenu != null) {
            viewModel.closeVerseMenu()
        } else {
            viewModel.handleBack()
        }
    }

    val activeSurahForPlayer = playerState.currentSurahId?.let { QuranMetadata.getSurahById(it) }
    val activeSurahPersianName = activeSurahForPlayer?.namePersian ?: currentSurah.namePersian
    val activeSurahArabicName = activeSurahForPlayer?.nameArabic ?: currentSurah.nameArabic

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mini Player pinned above navigation bar when audio is playing
                if (playerState.currentVerseKey != null) {
                    AudioMiniPlayer(
                        playerState = playerState,
                        surahNamePersian = activeSurahPersianName,
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onNext = { viewModel.playNextTrack() },
                        onPrevious = { viewModel.playPreviousTrack() },
                        onStop = { viewModel.stopAudio() },
                        onExpand = { showFullPlayerDialog = true }
                    )
                }

                // Persian Navigation Bar (Visible across top-level screens)
                if (currentScreen !is Screen.Reader) {
                    PersianBottomBar(
                        currentScreen = currentScreen,
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        readingProgress = readingProgress,
                        bookmarks = bookmarks,
                        activePlan = activePlan,
                        todayPlanItem = todayScheduleItem,
                        checklistItems = checklistItems,
                        onOpenSurah = { sId, vNum -> viewModel.openSurah(sId, vNum) },
                        onNavigateToSurahs = { viewModel.navigateTo(Screen.SurahList) },
                        onNavigateToSearch = { viewModel.navigateTo(Screen.Search) },
                        onNavigateToBookmarks = { viewModel.navigateTo(Screen.Bookmarks) },
                        onNavigateToPlanning = { viewModel.navigateTo(Screen.Planning) },
                        onNavigateToSchedule = { viewModel.navigateTo(Screen.Schedule) },
                        onNavigateToChecklist = { viewModel.navigateTo(Screen.Checklist) },
                        onNavigateToSettings = { viewModel.navigateTo(Screen.Settings) },
                        onToggleDayCompletion = { pId, dayNum, comp ->
                            viewModel.togglePlanDayCompletion(pId, dayNum, comp)
                        },
                        onToggleChecklistItem = { id, comp ->
                            viewModel.toggleChecklistItem(id, comp)
                        }
                    )
                }

                is Screen.SurahList -> {
                    SurahListScreen(
                        surahs = surahList,
                        searchQuery = surahSearchQuery,
                        onSearchQueryChanged = { viewModel.onSurahSearchQueryChanged(it) },
                        onOpenSurah = { sId -> viewModel.openSurah(sId, 1) },
                        onPlaySurah = { surah ->
                            viewModel.openSurah(surah.id, 1)
                            viewModel.playCurrentSurahFromStart()
                        }
                    )
                }

                is Screen.Reader -> {
                    ReaderScreen(
                        surah = currentSurah,
                        versesState = versesState,
                        targetVerseToScroll = targetVerseToScroll,
                        settings = settings,
                        playerState = playerState,
                        bookmarks = bookmarks,
                        selectedVerseForMenu = selectedVerseForMenu,
                        onBack = { viewModel.handleBack() },
                        onNextSurah = { viewModel.nextSurah() },
                        onPreviousSurah = { viewModel.previousSurah() },
                        onPlaySurah = { viewModel.playCurrentSurahFromStart() },
                        onPlayVerse = { verse -> viewModel.playVerse(verse) },
                        onToggleBookmark = { verse -> viewModel.toggleBookmark(verse) },
                        onOpenVerseMenu = { verse -> viewModel.openVerseMenu(verse) },
                        onCloseVerseMenu = { viewModel.closeVerseMenu() },
                        onCopyAll = { verse -> viewModel.copyVerseText(verse, true) },
                        onCopyArabicOnly = { verse -> viewModel.copyVerseText(verse, false) },
                        onCopyTranslationOnly = { verse ->
                            val clipboard = viewModel.getApplication<android.app.Application>()
                                .getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("ترجمه آیه", verse.translation))
                            android.widget.Toast.makeText(viewModel.getApplication(), "متن ترجمه کپی شد.", android.widget.Toast.LENGTH_SHORT).show()
                            viewModel.closeVerseMenu()
                        },
                        onShareVerse = { verse -> viewModel.shareVerse(verse) },
                        onDownloadVerse = { verse -> viewModel.downloadVerse(verse) },
                        onUpdateArabicSize = { viewModel.updateArabicFontSize(it) },
                        onUpdateTranslationSize = { viewModel.updateTranslationFontSize(it) },
                        onToggleShowTranslation = { viewModel.toggleShowTranslation(it) },
                        onRetry = { viewModel.loadVersesForSurah(currentSurah.id, forceRefresh = true) },
                        onClearScrollTarget = { viewModel.clearScrollTarget() }
                    )
                }

                is Screen.Search -> {
                    SearchScreen(
                        query = globalSearchQuery,
                        results = searchResults,
                        isSearching = isSearching,
                        onQueryChanged = { viewModel.onGlobalSearchQueryChanged(it) },
                        onResultClick = { sId, vNum -> viewModel.openSurah(sId, vNum) }
                    )
                }

                is Screen.Bookmarks -> {
                    BookmarksScreen(
                        bookmarks = bookmarks,
                        onBookmarkClick = { sId, vNum -> viewModel.openSurah(sId, vNum) },
                        onRemoveBookmark = { viewModel.removeBookmark(it) },
                        onClearAll = { viewModel.clearAllBookmarks() }
                    )
                }

                is Screen.Planning -> {
                    PlanningScreen(
                        activePlan = activePlan,
                        allPlans = allPlans,
                        activeSchedule = activeSchedule,
                        todayItem = todayScheduleItem,
                        onCreatePlan = { title, method, days, startS, startV, endS, endV, startP, endP, dailyT ->
                            viewModel.createPlan(title, method, days, startS, startV, endS, endV, startP, endP, dailyT)
                        },
                        onActivatePlan = { viewModel.activatePlan(it) },
                        onTogglePause = { id, paused -> viewModel.togglePlanPause(id, paused) },
                        onDeletePlan = { viewModel.deletePlan(it) },
                        onToggleDayCompletion = { pId, dayNum, comp ->
                            viewModel.togglePlanDayCompletion(pId, dayNum, comp)
                        },
                        onOpenReader = { sId, vNum -> viewModel.openSurah(sId, vNum) },
                        onNavigateToSchedule = { viewModel.navigateTo(Screen.Schedule) }
                    )
                }

                is Screen.Schedule -> {
                    ScheduleScreen(
                        activePlan = activePlan,
                        schedule = activeSchedule,
                        onToggleDayCompletion = { pId, dayNum, comp ->
                            viewModel.togglePlanDayCompletion(pId, dayNum, comp)
                        },
                        onOpenReader = { sId, vNum -> viewModel.openSurah(sId, vNum) },
                        onBack = { viewModel.handleBack() },
                        onNavigateToPlanning = { viewModel.navigateTo(Screen.Planning) }
                    )
                }

                is Screen.Checklist -> {
                    ChecklistScreen(
                        activePlan = activePlan,
                        schedule = activeSchedule,
                        items = checklistItems,
                        onToggleDayCompletion = { pId, dayNum, comp ->
                            viewModel.togglePlanDayCompletion(pId, dayNum, comp)
                        },
                        onOpenReader = { sId, vNum -> viewModel.openSurah(sId, vNum) },
                        onAddItem = { title, category -> viewModel.addChecklistItem(title, category) },
                        onUpdateItem = { id, title, category -> viewModel.updateChecklistItem(id, title, category) },
                        onToggleItem = { id, comp -> viewModel.toggleChecklistItem(id, comp) },
                        onDeleteItem = { viewModel.deleteChecklistItem(it) },
                        onResetDaily = { viewModel.resetDailyChecklist() },
                        onClearCompleted = { viewModel.clearCompletedChecklist() },
                        onClearAll = { viewModel.clearAllChecklist() },
                        onNavigateToPlanning = { viewModel.navigateTo(Screen.Planning) }
                    )
                }

                is Screen.Settings -> {
                    SettingsScreen(
                        settings = settings,
                        onUpdateTheme = { viewModel.updateTheme(it) },
                        onUpdateArabicSize = { viewModel.updateArabicFontSize(it) },
                        onUpdateTranslationSize = { viewModel.updateTranslationFontSize(it) },
                        onUpdateLineSpacing = { viewModel.updateLineSpacing(it) },
                        onToggleShowTranslation = { viewModel.toggleShowTranslation(it) },
                        onUpdateReciter = { viewModel.updateReciter(it) },
                        onUpdateSpeed = { viewModel.updatePlaybackSpeed(it) },
                        onToggleAutoAdvance = { viewModel.toggleAutoAdvance(it) },
                        onToggleRepeatAyah = { viewModel.toggleRepeatAyah(it) },
                        onClearAllBookmarks = { viewModel.clearAllBookmarks() },
                        onClearAllPlans = { viewModel.clearAllPlans() },
                        onClearAllChecklist = { viewModel.clearAllChecklist() },
                        onResetAllSettings = { viewModel.resetAllSettings() }
                    )
                }
            }
        }
    }

    // Full audio player dialog
    if (showFullPlayerDialog && playerState.currentVerseKey != null) {
        FullAudioPlayerDialog(
            playerState = playerState,
            surahNamePersian = activeSurahPersianName,
            surahNameArabic = activeSurahArabicName,
            playbackSpeed = settings.playbackSpeed,
            repeatAyah = settings.repeatAyah,
            onTogglePlay = { viewModel.togglePlayPause() },
            onNext = { viewModel.playNextTrack() },
            onPrevious = { viewModel.playPreviousTrack() },
            onSeek = { positionMs -> viewModel.seekTo(positionMs) },
            onSpeedChange = { speed -> viewModel.updatePlaybackSpeed(speed) },
            onToggleRepeat = { repeat -> viewModel.toggleRepeatAyah(repeat) },
            onDismiss = { showFullPlayerDialog = false }
        )
    }
}
