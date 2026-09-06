package com.example.persianquran.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.persianquran.audio.QuranAudioPlayer
import com.example.persianquran.data.local.BookmarkEntity
import com.example.persianquran.data.local.QuranDatabase
import com.example.persianquran.data.local.ReadingProgressEntity
import com.example.persianquran.data.model.AudioQuality
import com.example.persianquran.data.model.AudioTrackState
import com.example.persianquran.data.model.QuranTheme
import com.example.persianquran.data.model.ReaderSettings
import com.example.persianquran.data.model.SearchResultItem
import com.example.persianquran.data.model.Surah
import com.example.persianquran.data.model.Verse
import com.example.persianquran.data.repository.QuranRepository
import com.example.persianquran.data.surah.QuranMetadata
import com.example.persianquran.data.surah.toPersianDigits
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data object SurahList : Screen()
    data class Reader(val surahId: Int, val targetVerseNumber: Int = 1) : Screen()
    data object Search : Screen()
    data object Bookmarks : Screen()
    data object Planning : Screen()
    data object Schedule : Screen()
    data object Checklist : Screen()
    data object Settings : Screen()
}

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

sealed class FirstLaunchState {
    data object Checking : FirstLaunchState()
    data class Downloading(
        val currentSurah: Int,
        val totalSurahs: Int,
        val currentSurahName: String,
        val progressPercent: Float,
        val statusMessage: String
    ) : FirstLaunchState()
    data class Completed(val message: String) : FirstLaunchState()
    data class Error(val message: String) : FirstLaunchState()
    data object Ready : FirstLaunchState()
}

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        QuranDatabase::class.java,
        "persian_quran.db"
    ).fallbackToDestructiveMigration().build()

    private val repository = QuranRepository(
        context = application,
        quranDao = db.quranDao()
    )

    val audioPlayer = QuranAudioPlayer(
        context = application,
        repository = repository,
        scope = viewModelScope
    )

    private val prefs: SharedPreferences = application.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)

    // Navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenBackstack = mutableListOf<Screen>(Screen.Home)

    // Reader state
    private val _currentSurah = MutableStateFlow<Surah>(QuranMetadata.surahs[0])
    val currentSurah: StateFlow<Surah> = _currentSurah.asStateFlow()

    private val _versesState = MutableStateFlow<UiState<List<Verse>>>(UiState.Idle)
    val versesState: StateFlow<UiState<List<Verse>>> = _versesState.asStateFlow()

    private val _targetVerseToScroll = MutableStateFlow<Int?>(null)
    val targetVerseToScroll: StateFlow<Int?> = _targetVerseToScroll.asStateFlow()

    // Surah List search
    private val _surahSearchQuery = MutableStateFlow("")
    val surahSearchQuery: StateFlow<String> = _surahSearchQuery.asStateFlow()

    private val _surahList = MutableStateFlow<List<Surah>>(QuranMetadata.surahs)
    val surahList: StateFlow<List<Surah>> = _surahList.asStateFlow()

    // Quran Search
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val searchResults: StateFlow<List<SearchResultItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    // Bookmarks and Reading Progress
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarksFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val readingProgress: StateFlow<ReadingProgressEntity?> = repository.readingProgressFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Audio player state
    val playerState: StateFlow<AudioTrackState> = audioPlayer.playerState

    // Settings
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ReaderSettings> = _settings.asStateFlow()

    // Contextual verse menu state
    private val _selectedVerseForMenu = MutableStateFlow<Verse?>(null)
    val selectedVerseForMenu: StateFlow<Verse?> = _selectedVerseForMenu.asStateFlow()

    // Download state tracking
    private val _downloadingVerseKeys = MutableStateFlow<Set<String>>(emptySet())
    val downloadingVerseKeys: StateFlow<Set<String>> = _downloadingVerseKeys.asStateFlow()

    // Planning & Checklist State
    val allPlans: StateFlow<List<com.example.persianquran.data.model.ReadingPlan>> = repository.getAllPlans()
        .map { entities -> entities.map { it.toModel() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activePlan: StateFlow<com.example.persianquran.data.model.ReadingPlan?> = repository.getActivePlan()
        .map { it?.toModel() }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activePlanSchedule: StateFlow<List<com.example.persianquran.data.model.PlanDaySchedule>> = activePlan
        .flatMapLatest { plan ->
            if (plan != null) {
                repository.getScheduleForPlan(plan.id).map { list -> list.map { it.toModel() } }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val todayScheduleItem: StateFlow<com.example.persianquran.data.model.PlanDaySchedule?> = activePlanSchedule
        .map { schedule ->
            if (schedule.isEmpty()) null
            else schedule.firstOrNull { !it.isCompleted } ?: schedule.lastOrNull()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val checklistItems: StateFlow<List<com.example.persianquran.data.model.ChecklistItem>> = repository.getAllChecklistItems()
        .map { entities -> entities.map { it.toModel() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // First Launch Data Cache State
    private val _firstLaunchState = MutableStateFlow<FirstLaunchState>(FirstLaunchState.Checking)
    val firstLaunchState: StateFlow<FirstLaunchState> = _firstLaunchState.asStateFlow()

    private var downloadJob: Job? = null

    init {
        applySettingsToAudioPlayer(_settings.value)
        checkAndStartFirstLaunchDownload()
    }

    fun checkAndStartFirstLaunchDownload() {
        val hasCompletedFirstLaunch = prefs.getBoolean("has_completed_first_launch", false)
        viewModelScope.launch {
            if (hasCompletedFirstLaunch && repository.isQuranDataFullyCached()) {
                _firstLaunchState.value = FirstLaunchState.Ready
            } else {
                startFirstLaunchDownload()
            }
        }
    }

    fun startFirstLaunchDownload() {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _firstLaunchState.value = FirstLaunchState.Downloading(
                currentSurah = 1,
                totalSurahs = 114,
                currentSurahName = "حمد",
                progressPercent = 0.01f,
                statusMessage = "در حال آماده‌سازی دانلود اطلاعات قرآن کریم..."
            )
            val success = repository.downloadAllQuranData { current, total, surahName, percent, statusMsg ->
                _firstLaunchState.value = FirstLaunchState.Downloading(
                    currentSurah = current,
                    totalSurahs = total,
                    currentSurahName = surahName,
                    progressPercent = percent,
                    statusMessage = statusMsg
                )
            }
            if (success) {
                prefs.edit().putBoolean("has_completed_first_launch", true).apply()
                _firstLaunchState.value = FirstLaunchState.Completed("داده‌های قرآن با موفقیت آماده شد.")
                delay(1200)
                _firstLaunchState.value = FirstLaunchState.Ready
            } else {
                _firstLaunchState.value = FirstLaunchState.Error("خطا در دریافت کامل اطلاعات. لطفاً اتصال اینترنت خود را بررسی نمایید.")
            }
        }
    }

    fun dismissFirstLaunchScreen() {
        _firstLaunchState.value = FirstLaunchState.Ready
    }

    private fun loadSettings(): ReaderSettings {
        val arabicSize = prefs.getFloat("arabic_font_size", 24f)
        val trSize = prefs.getFloat("tr_font_size", 16f)
        val lineSpacing = prefs.getFloat("line_spacing", 1.6f)
        val showTr = prefs.getBoolean("show_translation", true)
        val themeOrdinal = prefs.getInt("theme", QuranTheme.SYSTEM.ordinal)
        val theme = QuranTheme.entries.getOrElse(themeOrdinal) { QuranTheme.SYSTEM }
        val reciterId = prefs.getInt("reciter_id", 7)
        val speed = prefs.getFloat("playback_speed", 1.0f)
        val autoAdv = prefs.getBoolean("auto_advance", true)
        val repeat = prefs.getBoolean("repeat_ayah", false)

        return ReaderSettings(
            arabicFontSize = arabicSize,
            translationFontSize = trSize,
            lineSpacing = lineSpacing,
            showTranslation = showTr,
            theme = theme,
            selectedReciterId = reciterId,
            playbackSpeed = speed,
            autoAdvance = autoAdv,
            repeatAyah = repeat
        )
    }

    private fun saveSettings(s: ReaderSettings) {
        prefs.edit()
            .putFloat("arabic_font_size", s.arabicFontSize)
            .putFloat("tr_font_size", s.translationFontSize)
            .putFloat("line_spacing", s.lineSpacing)
            .putBoolean("show_translation", s.showTranslation)
            .putInt("theme", s.theme.ordinal)
            .putInt("reciter_id", s.selectedReciterId)
            .putFloat("playback_speed", s.playbackSpeed)
            .putBoolean("auto_advance", s.autoAdvance)
            .putBoolean("repeat_ayah", s.repeatAyah)
            .apply()
    }

    private fun applySettingsToAudioPlayer(s: ReaderSettings) {
        val reciter = QuranMetadata.reciters.find { it.id == s.selectedReciterId }
            ?: QuranMetadata.reciters.first()
        audioPlayer.configureSettings(
            reciterId = s.selectedReciterId,
            reciterName = reciter.namePersian,
            autoAdvance = s.autoAdvance,
            repeatAyah = s.repeatAyah,
            speed = s.playbackSpeed
        )
    }

    // Navigation
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value == screen) return
        screenBackstack.add(screen)
        _currentScreen.value = screen
    }

    fun handleBack(): Boolean {
        if (screenBackstack.size > 1) {
            screenBackstack.removeAt(screenBackstack.lastIndex)
            _currentScreen.value = screenBackstack.last()
            return true
        }
        return false
    }

    // Reader actions
    fun openSurah(surahId: Int, targetVerseNumber: Int = 1) {
        val surah = QuranMetadata.getSurahById(surahId) ?: return
        _currentSurah.value = surah
        _targetVerseToScroll.value = targetVerseNumber
        navigateTo(Screen.Reader(surahId, targetVerseNumber))
        loadVersesForSurah(surahId, targetVerseNumber)
    }

    fun loadVersesForSurah(surahId: Int, targetVerse: Int = 1, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _versesState.value = UiState.Loading
            try {
                val verses = repository.getVersesForSurah(
                    surahId = surahId,
                    reciterId = _settings.value.selectedReciterId,
                    forceRefresh = forceRefresh
                )
                if (verses.isNotEmpty()) {
                    _versesState.value = UiState.Success(verses)
                    // Save reading progress
                    val surah = QuranMetadata.getSurahById(surahId)
                    repository.saveReadingProgress(
                        surahNumber = surahId,
                        verseNumber = targetVerse,
                        surahNamePersian = surah?.namePersian ?: "سوره $surahId",
                        scrollIndex = (targetVerse - 1).coerceAtLeast(0)
                    )
                } else {
                    _versesState.value = UiState.Error("خطا در بارگذاری آیات. لطفاً اتصال اینترنت خود را بررسی نمایید.")
                }
            } catch (e: Exception) {
                _versesState.value = UiState.Error("خطا در بارگذاری اطلاعات: ${e.localizedMessage ?: "نامشخص"}")
            }
        }
    }

    fun nextSurah() {
        val nextId = _currentSurah.value.id + 1
        if (nextId <= 114) {
            openSurah(nextId, 1)
        }
    }

    fun previousSurah() {
        val prevId = _currentSurah.value.id - 1
        if (prevId >= 1) {
            openSurah(prevId, 1)
        }
    }

    fun clearScrollTarget() {
        _targetVerseToScroll.value = null
    }

    // Surah Search
    fun onSurahSearchQueryChanged(q: String) {
        _surahSearchQuery.value = q
        _surahList.value = QuranMetadata.searchSurahs(q)
    }

    // Global Search
    fun onGlobalSearchQueryChanged(q: String) {
        _globalSearchQuery.value = q
        searchJob?.cancel()
        if (q.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // Debounce
            _isSearching.value = true
            val results = repository.search(q)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    // Audio Playback
    fun playVerse(verse: Verse) {
        val state = _versesState.value
        if (state is UiState.Success) {
            val reciter = QuranMetadata.reciters.find { it.id == _settings.value.selectedReciterId }
            val reciterName = reciter?.namePersian ?: "مشاری راشد العفاسی"
            val index = state.data.indexOfFirst { it.verseNumber == verse.verseNumber }
            if (index != -1) {
                audioPlayer.playVerseList(state.data, index, reciterName)
            } else {
                audioPlayer.playVerse(verse)
            }
        } else {
            audioPlayer.playVerse(verse)
        }
    }

    fun playCurrentSurahFromStart() {
        val state = _versesState.value
        if (state is UiState.Success && state.data.isNotEmpty()) {
            val reciter = QuranMetadata.reciters.find { it.id == _settings.value.selectedReciterId }
            val reciterName = reciter?.namePersian ?: "مشاری راشد العفاسی"
            audioPlayer.playVerseList(state.data, 0, reciterName)
        }
    }

    fun togglePlayPause() {
        audioPlayer.togglePlayPause()
    }

    fun playNextTrack() {
        audioPlayer.playNext()
    }

    fun playPreviousTrack() {
        audioPlayer.playPrevious()
    }

    fun seekTo(positionMs: Int) {
        audioPlayer.seekTo(positionMs)
    }

    fun stopAudio() {
        audioPlayer.stop()
    }

    // Bookmarks
    fun toggleBookmark(verse: Verse) {
        viewModelScope.launch {
            val surah = QuranMetadata.getSurahById(verse.chapterId)
            val name = surah?.namePersian ?: "سوره ${verse.chapterId}"
            repository.toggleBookmark(verse, name)
            val isNowBookmarked = !repository.isBookmarked(verse.chapterId, verse.verseNumber)
            val msg = if (isNowBookmarked) "آیه به نشان‌شده‌ها اضافه شد." else "آیه از نشان‌شده‌ها حذف شد."
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark)
            Toast.makeText(getApplication(), "نشان حذف شد.", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearAllBookmarks() {
        viewModelScope.launch {
            repository.clearAllBookmarks()
            Toast.makeText(getApplication(), "تمام نشان‌شده‌ها پاک شدند.", Toast.LENGTH_SHORT).show()
        }
    }

    // Contextual menu
    fun openVerseMenu(verse: Verse) {
        _selectedVerseForMenu.value = verse
    }

    fun closeVerseMenu() {
        _selectedVerseForMenu.value = null
    }

    // Copying & Sharing
    fun copyVerseText(verse: Verse, includeTranslation: Boolean = true) {
        val surah = QuranMetadata.getSurahById(verse.chapterId)
        val surahName = surah?.namePersian ?: "سوره ${verse.chapterId}"

        val textToCopy = buildString {
            append(verse.textUthmani)
            append(" [سوره $surahName، آیه ${verse.verseNumber.toPersianDigits()}]")
            if (includeTranslation && verse.translation.isNotBlank()) {
                append("\n\nترجمه: ")
                append(verse.translation)
            }
        }

        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("آیه قرآن", textToCopy)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(getApplication(), "متن آیه کپی شد.", Toast.LENGTH_SHORT).show()
        closeVerseMenu()
    }

    fun shareVerse(verse: Verse) {
        val surah = QuranMetadata.getSurahById(verse.chapterId)
        val surahName = surah?.namePersian ?: "سوره ${verse.chapterId}"

        val shareText = buildString {
            append(verse.textUthmani)
            append(" [سوره $surahName، آیه ${verse.verseNumber.toPersianDigits()}]")
            if (verse.translation.isNotBlank()) {
                append("\n\nترجمه: ")
                append(verse.translation)
            }
            append("\n\n— قرآن کریم (Persian Quran)")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "آیه قرآن کریم - سوره $surahName")
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "اشتراک‌گذاری آیه").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(chooser)
        closeVerseMenu()
    }

    // Audio Download
    fun downloadVerse(verse: Verse) {
        val reciterId = _settings.value.selectedReciterId
        val currentKeys = _downloadingVerseKeys.value.toMutableSet()
        currentKeys.add(verse.verseKey)
        _downloadingVerseKeys.value = currentKeys

        viewModelScope.launch {
            val success = repository.downloadVerseAudio(verse, reciterId)
            val updatedKeys = _downloadingVerseKeys.value.toMutableSet()
            updatedKeys.remove(verse.verseKey)
            _downloadingVerseKeys.value = updatedKeys

            val msg = if (success) "صوت آیه دانلود و ذخیره شد." else "خطا در دانلود صوت آیه."
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Settings Updates
    fun updateArabicFontSize(size: Float) {
        val newSettings = _settings.value.copy(arabicFontSize = size)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    fun updateTranslationFontSize(size: Float) {
        val newSettings = _settings.value.copy(translationFontSize = size)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    fun updateLineSpacing(spacing: Float) {
        val newSettings = _settings.value.copy(lineSpacing = spacing)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    fun toggleShowTranslation(show: Boolean) {
        val newSettings = _settings.value.copy(showTranslation = show)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    fun updateTheme(theme: QuranTheme) {
        val newSettings = _settings.value.copy(theme = theme)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    fun updateReciter(reciterId: Int) {
        val newSettings = _settings.value.copy(selectedReciterId = reciterId)
        _settings.value = newSettings
        saveSettings(newSettings)
        applySettingsToAudioPlayer(newSettings)
    }

    fun updatePlaybackSpeed(speed: Float) {
        val newSettings = _settings.value.copy(playbackSpeed = speed)
        _settings.value = newSettings
        saveSettings(newSettings)
        applySettingsToAudioPlayer(newSettings)
    }

    fun toggleAutoAdvance(auto: Boolean) {
        val newSettings = _settings.value.copy(autoAdvance = auto)
        _settings.value = newSettings
        saveSettings(newSettings)
        applySettingsToAudioPlayer(newSettings)
    }

    fun toggleRepeatAyah(repeat: Boolean) {
        val newSettings = _settings.value.copy(repeatAyah = repeat)
        _settings.value = newSettings
        saveSettings(newSettings)
        applySettingsToAudioPlayer(newSettings)
    }

    // =================== PLANNING ACTIONS ===================

    fun createPlan(
        title: String,
        method: com.example.persianquran.data.model.PlanMethod,
        totalDays: Int = 30,
        startSurahId: Int = 1,
        startVerse: Int = 1,
        endSurahId: Int = 114,
        endVerse: Int = 6,
        startPage: Int = 1,
        endPage: Int = 604,
        dailyTarget: Int = 1,
        startDateMillis: Long = System.currentTimeMillis()
    ) {
        val safeTitle = title.trim().ifEmpty { "برنامه مطالعه قرآن" }
        val safeDays = totalDays.coerceIn(1, 1000)

        viewModelScope.launch {
            try {
                // Generate schedule first to know exact duration and boundaries
                val tempSchedule = QuranMetadata.calculateScheduleForPlan(
                    planId = 0L,
                    method = method,
                    totalDays = safeDays,
                    startDateMillis = startDateMillis,
                    startSurahId = startSurahId,
                    startVerse = startVerse,
                    endSurahId = endSurahId,
                    endVerse = endVerse,
                    startPageInput = startPage,
                    endPageInput = endPage,
                    dailyTarget = dailyTarget
                )

                val actualTotalDays = maxOf(1, tempSchedule.size)
                val oneDayMillis = 24L * 60L * 60L * 1000L
                val endMillis = startDateMillis + (actualTotalDays * oneDayMillis)

                val planEntity = com.example.persianquran.data.local.ReadingPlanEntity(
                    title = safeTitle,
                    startDateMillis = startDateMillis,
                    endDateMillis = endMillis,
                    totalDays = actualTotalDays,
                    method = method.name,
                    startSurahId = startSurahId,
                    startVerseNumber = startVerse,
                    endSurahId = endSurahId,
                    endVerseNumber = endVerse,
                    dailyTarget = dailyTarget,
                    isActive = true,
                    isPaused = false,
                    createdAt = System.currentTimeMillis()
                )

                repository.createPlan(planEntity) { planId ->
                    val scheduleModels = QuranMetadata.calculateScheduleForPlan(
                        planId = planId,
                        method = method,
                        totalDays = actualTotalDays,
                        startDateMillis = startDateMillis,
                        startSurahId = startSurahId,
                        startVerse = startVerse,
                        endSurahId = endSurahId,
                        endVerse = endVerse,
                        startPageInput = startPage,
                        endPageInput = endPage,
                        dailyTarget = dailyTarget
                    )
                    scheduleModels.map { it.toEntity() }
                }

                Toast.makeText(getApplication(), "برنامه «$safeTitle» با موفقیت تنظیم شد.", Toast.LENGTH_SHORT).show()
                navigateTo(Screen.Planning)
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "خطا در ایجاد برنامه: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun activatePlan(planId: Long) {
        viewModelScope.launch {
            repository.activatePlan(planId)
            Toast.makeText(getApplication(), "برنامه به عنوان برنامه فعال انتخاب شد.", Toast.LENGTH_SHORT).show()
        }
    }

    fun togglePlanPause(planId: Long, currentPaused: Boolean) {
        viewModelScope.launch {
            val newPaused = !currentPaused
            repository.setPlanPaused(planId, newPaused)
            val msg = if (newPaused) "برنامه متوقف شد." else "ادامه برنامه فعال گردید."
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun deletePlan(planId: Long) {
        viewModelScope.launch {
            repository.deletePlan(planId)
            Toast.makeText(getApplication(), "برنامه با موفقیت حذف شد.", Toast.LENGTH_SHORT).show()
        }
    }

    fun togglePlanDayCompletion(planId: Long, dayNumber: Int, currentCompleted: Boolean) {
        viewModelScope.launch {
            val newCompleted = !currentCompleted
            repository.updateDayCompletion(planId, dayNumber, newCompleted)

            // If day completed, also mark any linked checklist item
            if (newCompleted) {
                val currentChecklist = checklistItems.value
                val linkedItem = currentChecklist.find { it.linkedPlanId == planId && it.linkedDayNumber == dayNumber }
                if (linkedItem != null && !linkedItem.isCompleted) {
                    repository.toggleChecklistItem(linkedItem.id, true)
                }
                Toast.makeText(getApplication(), "مطالعه روز ${dayNumber.toPersianDigits()} با موفقیت ثبت شد.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // =================== CHECKLIST ACTIONS ===================

    fun addChecklistItem(title: String, category: String = "روزانه") {
        val safeTitle = title.trim()
        if (safeTitle.isEmpty()) return
        viewModelScope.launch {
            val entity = com.example.persianquran.data.local.ChecklistItemEntity(
                title = safeTitle,
                category = category,
                isCompleted = false,
                isDaily = true
            )
            repository.addChecklistItem(entity)
            Toast.makeText(getApplication(), "مورد جدید به چک‌لیست افزوده شد.", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateChecklistItem(id: Long, newTitle: String, newCategory: String) {
        val safeTitle = newTitle.trim()
        if (safeTitle.isEmpty()) return
        viewModelScope.launch {
            val currentItem = checklistItems.value.find { it.id == id } ?: return@launch
            repository.updateChecklistItem(
                com.example.persianquran.data.local.ChecklistItemEntity(
                    id = id,
                    title = safeTitle,
                    category = newCategory,
                    isCompleted = currentItem.isCompleted,
                    isDaily = currentItem.isDaily,
                    linkedPlanId = currentItem.linkedPlanId,
                    linkedDayNumber = currentItem.linkedDayNumber
                )
            )
            Toast.makeText(getApplication(), "مورد با موفقیت ویرایش شد.", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleChecklistItem(id: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            val newCompleted = !currentCompleted
            repository.toggleChecklistItem(id, newCompleted)

            // If this was linked to a plan day, update that day too
            val item = checklistItems.value.find { it.id == id }
            if (item?.linkedPlanId != null && item.linkedDayNumber != null) {
                repository.updateDayCompletion(item.linkedPlanId, item.linkedDayNumber, newCompleted)
            }
        }
    }

    fun deleteChecklistItem(id: Long) {
        viewModelScope.launch {
            repository.deleteChecklistItem(id)
            Toast.makeText(getApplication(), "مورد از چک‌لیست حذف شد.", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetDailyChecklist() {
        viewModelScope.launch {
            repository.resetDailyChecklist()
            Toast.makeText(getApplication(), "چک‌لیست روزانه بازنشانی شد.", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearCompletedChecklist() {
        viewModelScope.launch {
            repository.clearCompletedChecklist()
            Toast.makeText(getApplication(), "موارد انجام شده پاک شدند.", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearAllChecklist() {
        viewModelScope.launch {
            repository.clearAllChecklist()
            Toast.makeText(getApplication(), "کلیه موارد چک‌لیست پاک شدند.", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearAllPlans() {
        viewModelScope.launch {
            repository.clearAllPlans()
            Toast.makeText(getApplication(), "کلیه برنامه‌های مطالعه پاک شدند.", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetAllSettings() {
        val defaultSettings = ReaderSettings()
        _settings.value = defaultSettings
        saveSettings(defaultSettings)
        applySettingsToAudioPlayer(defaultSettings)
        Toast.makeText(getApplication(), "تنظیمات به حالت پیش‌فرض بازگردانده شد.", Toast.LENGTH_SHORT).show()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}

private fun com.example.persianquran.data.local.ReadingPlanEntity.toModel(): com.example.persianquran.data.model.ReadingPlan {
    val planMethod = try {
        com.example.persianquran.data.model.PlanMethod.valueOf(method)
    } catch (e: Exception) {
        com.example.persianquran.data.model.PlanMethod.PAGES
    }
    return com.example.persianquran.data.model.ReadingPlan(
        id = id,
        title = title,
        startDateMillis = startDateMillis,
        endDateMillis = endDateMillis,
        totalDays = totalDays,
        method = planMethod,
        startSurahId = startSurahId,
        startVerseNumber = startVerseNumber,
        endSurahId = endSurahId,
        endVerseNumber = endVerseNumber,
        dailyTarget = dailyTarget,
        isActive = isActive,
        isPaused = isPaused,
        createdAt = createdAt
    )
}

private fun com.example.persianquran.data.local.PlanDayScheduleEntity.toModel(): com.example.persianquran.data.model.PlanDaySchedule {
    return com.example.persianquran.data.model.PlanDaySchedule(
        id = id,
        planId = planId,
        dayNumber = dayNumber,
        dateMillis = dateMillis,
        dateFormatted = dateFormatted,
        startSurahId = startSurahId,
        startSurahName = startSurahName,
        startVerse = startVerse,
        endSurahId = endSurahId,
        endSurahName = endSurahName,
        endVerse = endVerse,
        startPage = startPage,
        endPage = endPage,
        rangeDescription = rangeDescription,
        isCompleted = isCompleted,
        completedAt = completedAt
    )
}

private fun com.example.persianquran.data.model.PlanDaySchedule.toEntity(): com.example.persianquran.data.local.PlanDayScheduleEntity {
    return com.example.persianquran.data.local.PlanDayScheduleEntity(
        id = id,
        planId = planId,
        dayNumber = dayNumber,
        dateMillis = dateMillis,
        dateFormatted = dateFormatted,
        startSurahId = startSurahId,
        startSurahName = startSurahName,
        startVerse = startVerse,
        endSurahId = endSurahId,
        endSurahName = endSurahName,
        endVerse = endVerse,
        startPage = startPage,
        endPage = endPage,
        rangeDescription = rangeDescription,
        isCompleted = isCompleted,
        completedAt = completedAt
    )
}

private fun com.example.persianquran.data.local.ChecklistItemEntity.toModel(): com.example.persianquran.data.model.ChecklistItem {
    return com.example.persianquran.data.model.ChecklistItem(
        id = id,
        title = title,
        category = category,
        isCompleted = isCompleted,
        isDaily = isDaily,
        linkedPlanId = linkedPlanId,
        linkedDayNumber = linkedDayNumber,
        createdAt = createdAt
    )
}
