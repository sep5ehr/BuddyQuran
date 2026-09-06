package com.example.persianquran.data.repository

import android.content.Context
import android.util.Log
import com.example.persianquran.data.api.QuranApiClient
import com.example.persianquran.data.local.AudioDownloadEntity
import com.example.persianquran.data.local.BookmarkEntity
import com.example.persianquran.data.local.CachedVerseEntity
import com.example.persianquran.data.local.QuranDao
import com.example.persianquran.data.local.ReadingProgressEntity
import com.example.persianquran.data.model.SearchResultItem
import com.example.persianquran.data.model.Surah
import com.example.persianquran.data.model.Verse
import com.example.persianquran.data.surah.QuranMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class QuranRepository(
    private val context: Context,
    private val quranDao: QuranDao,
    private val apiClient: QuranApiClient = QuranApiClient()
) {
    companion object {
        private const val TAG = "QuranRepository"
    }

    val surahs: List<Surah> get() = QuranMetadata.surahs
    val reciters = QuranMetadata.reciters

    fun getSurah(id: Int): Surah? = QuranMetadata.getSurahById(id)

    fun searchSurahsLocally(query: String): List<Surah> = QuranMetadata.searchSurahs(query)

    suspend fun getVersesForSurah(
        surahId: Int,
        reciterId: Int = 7,
        forceRefresh: Boolean = false
    ): List<Verse> = withContext(Dispatchers.IO) {
        val surah = getSurah(surahId) ?: return@withContext emptyList()

        // 1. If not forcing refresh, check local Room database cache
        if (!forceRefresh) {
            val cached = quranDao.getVersesForSurah(surahId)
            if (cached.isNotEmpty() && cached.size >= surah.versesCount) {
                Log.d(TAG, "Loaded ${cached.size} verses from local Room cache for Surah $surahId")
                return@withContext cached.map { entity ->
                    Verse(
                        id = entity.verseNumber,
                        verseNumber = entity.verseNumber,
                        verseKey = entity.verseKey,
                        textUthmani = entity.textUthmani,
                        chapterId = entity.surahNumber,
                        pageNumber = entity.pageNumber,
                        juzNumber = entity.juzNumber,
                        translation = entity.translation,
                        audioUrl = entity.audioUrl
                    )
                }
            }
        }

        // 2. Fetch from Quran Foundation API
        try {
            Log.d(TAG, "Fetching verses for Surah $surahId from Quran Foundation API...")
            val versesFromApi = apiClient.getVersesByChapter(surahId, reciterId)
            if (versesFromApi.isNotEmpty()) {
                // Save to Room cache
                val entities = versesFromApi.map { v ->
                    CachedVerseEntity(
                        verseKey = v.verseKey,
                        surahNumber = v.chapterId,
                        verseNumber = v.verseNumber,
                        textUthmani = v.textUthmani,
                        translation = v.translation,
                        pageNumber = v.pageNumber,
                        juzNumber = v.juzNumber,
                        audioUrl = v.audioUrl
                    )
                }
                quranDao.insertVerses(entities)
                return@withContext versesFromApi
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load from API: ${e.message}. Checking fallback cache...")
        }

        // 3. Fallback: check whatever cached verses exist in Room
        val cachedFallback = quranDao.getVersesForSurah(surahId)
        if (cachedFallback.isNotEmpty()) {
            return@withContext cachedFallback.map { entity ->
                Verse(
                    id = entity.verseNumber,
                    verseNumber = entity.verseNumber,
                    verseKey = entity.verseKey,
                    textUthmani = entity.textUthmani,
                    chapterId = entity.surahNumber,
                    pageNumber = entity.pageNumber,
                    juzNumber = entity.juzNumber,
                    translation = entity.translation,
                    audioUrl = entity.audioUrl
                )
            }
        }

        // 4. Built-in emergency offline fallback for Surah Al-Fatiha (1) and Al-Ikhlas (112)
        if (surahId == 1) {
            return@withContext getBuiltInFatiha()
        } else if (surahId == 112) {
            return@withContext getBuiltInIkhlas()
        }

        emptyList()
    }

    suspend fun isQuranDataFullyCached(): Boolean = withContext(Dispatchers.IO) {
        val cachedSurahs = quranDao.getCachedSurahsCount()
        val cachedVerses = quranDao.getCachedVersesCount()
        Log.d(TAG, "Checking local cache: $cachedSurahs/114 surahs, $cachedVerses/6236 verses")
        cachedSurahs >= 114 && cachedVerses >= 6200
    }

    suspend fun getCachedVersesCount(): Int = withContext(Dispatchers.IO) {
        quranDao.getCachedVersesCount()
    }

    suspend fun downloadAllQuranData(
        onProgress: (current: Int, total: Int, surahName: String, percent: Float, statusMsg: String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val totalSurahs = 114
        var successfullyProcessed = 0

        for (surahId in 1..totalSurahs) {
            val surah = getSurah(surahId) ?: continue
            val surahName = surah.namePersian
            val percent = (surahId.toFloat() / totalSurahs.toFloat()).coerceIn(0f, 1f)

            // Check if already in local cache
            val existing = quranDao.getVersesForSurah(surahId)
            if (existing.isNotEmpty() && existing.size >= surah.versesCount) {
                successfullyProcessed++
                onProgress(
                    surahId,
                    totalSurahs,
                    surahName,
                    percent,
                    "اطلاعات سوره مبارکه $surahName در حافظه دستگاه موجود است."
                )
                continue
            }

            try {
                onProgress(
                    surahId,
                    totalSurahs,
                    surahName,
                    percent,
                    "در حال دریافت آیات سوره مبارکه $surahName (سوره $surahId از ۱۱۴)..."
                )

                val verses = apiClient.getVersesByChapter(surahId, reciterId = 7)
                if (verses.isNotEmpty()) {
                    val entities = verses.map { v ->
                        CachedVerseEntity(
                            verseKey = v.verseKey,
                            surahNumber = v.chapterId,
                            verseNumber = v.verseNumber,
                            textUthmani = v.textUthmani,
                            translation = v.translation,
                            pageNumber = v.pageNumber,
                            juzNumber = v.juzNumber,
                            audioUrl = v.audioUrl // Note: only saving remote URL; NO audio downloaded!
                        )
                    }
                    onProgress(
                        surahId,
                        totalSurahs,
                        surahName,
                        percent,
                        "در حال ذخیره‌سازی سوره $surahName برای استفاده آفلاین..."
                    )
                    quranDao.insertVerses(entities)
                    successfullyProcessed++
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error downloading surah $surahId: ${e.message}")
                // If it fails on a surah, continue with next to maximize cached coverage
            }
        }

        val totalCached = quranDao.getCachedVersesCount()
        Log.d(TAG, "Finished Quran download loop. Cached verses count: $totalCached")
        totalCached >= 6200 || successfullyProcessed >= 110
    }

    suspend fun search(query: String): List<SearchResultItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        // 1. Search locally in Room database cache first (fast, reliable, offline-capable)
        val localMatches = quranDao.searchCachedVerses(query.trim())
        if (localMatches.isNotEmpty()) {
            Log.d(TAG, "Found ${localMatches.size} search results in local Room cache for: $query")
            return@withContext localMatches.map { c ->
                val surah = QuranMetadata.getSurahById(c.surahNumber)
                SearchResultItem(
                    verseKey = c.verseKey,
                    surahNumber = c.surahNumber,
                    verseNumber = c.verseNumber,
                    surahNamePersian = surah?.namePersian ?: "سوره ${c.surahNumber}",
                    textUthmani = c.textUthmani,
                    translation = c.translation
                )
            }
        }

        // 2. Fallback to API search if local cache is empty or hasn't completed yet
        try {
            apiClient.search(query)
        } catch (e: Exception) {
            Log.e(TAG, "Search query failed: ${e.message}", e)
            emptyList()
        }
    }

    // Bookmarks
    val bookmarksFlow: Flow<List<BookmarkEntity>> = quranDao.getAllBookmarks()

    suspend fun isBookmarked(surahNumber: Int, verseNumber: Int): Boolean =
        quranDao.isBookmarked(surahNumber, verseNumber)

    fun observeIsBookmarked(surahNumber: Int, verseNumber: Int): Flow<Boolean> =
        quranDao.observeIsBookmarked(surahNumber, verseNumber)

    suspend fun toggleBookmark(verse: Verse, surahName: String) = withContext(Dispatchers.IO) {
        val exists = quranDao.isBookmarked(verse.chapterId, verse.verseNumber)
        if (exists) {
            quranDao.deleteBookmarkByVerse(verse.chapterId, verse.verseNumber)
        } else {
            quranDao.insertBookmark(
                BookmarkEntity(
                    surahNumber = verse.chapterId,
                    verseNumber = verse.verseNumber,
                    verseKey = verse.verseKey,
                    surahNamePersian = surahName,
                    arabicText = verse.textUthmani,
                    translationText = verse.translation
                )
            )
        }
    }

    suspend fun deleteBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        quranDao.deleteBookmark(bookmark)
    }

    suspend fun clearAllBookmarks() = withContext(Dispatchers.IO) {
        quranDao.clearAllBookmarks()
    }

    // Reading Progress
    val readingProgressFlow: Flow<ReadingProgressEntity?> = quranDao.getReadingProgress()

    suspend fun saveReadingProgress(
        surahNumber: Int,
        verseNumber: Int,
        surahNamePersian: String,
        scrollIndex: Int
    ) = withContext(Dispatchers.IO) {
        quranDao.saveReadingProgress(
            ReadingProgressEntity(
                id = 1,
                surahNumber = surahNumber,
                verseNumber = verseNumber,
                verseKey = "$surahNumber:$verseNumber",
                surahNamePersian = surahNamePersian,
                scrollIndex = scrollIndex,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // Audio Downloads
    suspend fun getDownloadedAudioPath(verseKey: String, reciterId: Int): String? = withContext(Dispatchers.IO) {
        val download = quranDao.getDownloadedVerse(verseKey, reciterId)
        if (download != null) {
            val file = File(download.filePath)
            if (file.exists() && file.length() > 0) {
                return@withContext file.absolutePath
            } else {
                quranDao.deleteDownload(verseKey, reciterId)
            }
        }
        null
    }

    suspend fun downloadVerseAudio(
        verse: Verse,
        reciterId: Int,
        onProgress: (Int) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        val audioUrl = verse.audioUrl ?: return@withContext false
        try {
            val audioDir = File(context.filesDir, "quran_audio/reciter_$reciterId")
            if (!audioDir.exists()) audioDir.mkdirs()

            val fileName = "v_${verse.chapterId}_${verse.verseNumber}.mp3"
            val targetFile = File(audioDir, fileName)

            val url = URL(audioUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 20000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Audio download error: ${connection.responseCode}")
                return@withContext false
            }

            val fileLength = connection.contentLength
            val input = connection.inputStream
            val output = FileOutputStream(targetFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    val progress = (total * 100 / fileLength).toInt()
                    onProgress(progress)
                }
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()

            quranDao.insertDownload(
                AudioDownloadEntity(
                    verseKey = verse.verseKey,
                    surahNumber = verse.chapterId,
                    verseNumber = verse.verseNumber,
                    reciterId = reciterId,
                    filePath = targetFile.absolutePath,
                    fileSize = targetFile.length(),
                    downloadedAt = System.currentTimeMillis()
                )
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download audio for ${verse.verseKey}: ${e.message}", e)
            false
        }
    }

    suspend fun deleteAudioDownload(verseKey: String, reciterId: Int) = withContext(Dispatchers.IO) {
        val entity = quranDao.getDownloadedVerse(verseKey, reciterId)
        if (entity != null) {
            val file = File(entity.filePath)
            if (file.exists()) file.delete()
            quranDao.deleteDownload(verseKey, reciterId)
        }
    }

    // =================== PLANNING OPERATIONS ===================

    fun getAllPlans(): Flow<List<com.example.persianquran.data.local.ReadingPlanEntity>> =
        quranDao.getAllPlans()

    fun getActivePlan(): Flow<com.example.persianquran.data.local.ReadingPlanEntity?> =
        quranDao.getActivePlan()

    suspend fun getActivePlanOnce(): com.example.persianquran.data.local.ReadingPlanEntity? = withContext(Dispatchers.IO) {
        quranDao.getActivePlanOnce()
    }

    suspend fun createPlan(
        plan: com.example.persianquran.data.local.ReadingPlanEntity,
        scheduleGenerator: (Long) -> List<com.example.persianquran.data.local.PlanDayScheduleEntity>
    ): Long = withContext(Dispatchers.IO) {
        // Deactivate others if this one is active
        if (plan.isActive) {
            quranDao.deactivateAllPlans()
        }
        val planId = quranDao.insertPlan(plan)
        val schedule = scheduleGenerator(planId)
        quranDao.insertScheduleItems(schedule)

        // Automatically create a linked checklist item for today's reading
        quranDao.insertChecklistItem(
            com.example.persianquran.data.local.ChecklistItemEntity(
                title = "مطالعه امروز (${plan.title})",
                category = "برنامه‌ریزی",
                isCompleted = false,
                isDaily = true,
                linkedPlanId = planId,
                linkedDayNumber = 1
            )
        )
        planId
    }

    suspend fun updatePlan(plan: com.example.persianquran.data.local.ReadingPlanEntity) = withContext(Dispatchers.IO) {
        quranDao.updatePlan(plan)
    }

    suspend fun activatePlan(planId: Long) = withContext(Dispatchers.IO) {
        quranDao.deactivateAllPlans()
        quranDao.activatePlan(planId)
    }

    suspend fun setPlanPaused(planId: Long, isPaused: Boolean) = withContext(Dispatchers.IO) {
        quranDao.setPlanPaused(planId, isPaused)
    }

    suspend fun deletePlan(planId: Long) = withContext(Dispatchers.IO) {
        quranDao.deleteScheduleForPlan(planId)
        quranDao.deletePlan(planId)
    }

    fun getScheduleForPlan(planId: Long): Flow<List<com.example.persianquran.data.local.PlanDayScheduleEntity>> =
        quranDao.getScheduleForPlan(planId)

    suspend fun getScheduleForPlanOnce(planId: Long): List<com.example.persianquran.data.local.PlanDayScheduleEntity> =
        withContext(Dispatchers.IO) {
            quranDao.getScheduleForPlanOnce(planId)
        }

    suspend fun updateDayCompletion(
        planId: Long,
        dayNumber: Int,
        isCompleted: Boolean
    ) = withContext(Dispatchers.IO) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        quranDao.updateDayCompletion(planId, dayNumber, isCompleted, completedAt)
    }

    suspend fun clearAllPlans() = withContext(Dispatchers.IO) {
        quranDao.clearAllSchedules()
        quranDao.clearAllPlans()
    }

    // =================== CHECKLIST OPERATIONS ===================

    fun getAllChecklistItems(): Flow<List<com.example.persianquran.data.local.ChecklistItemEntity>> =
        quranDao.getAllChecklistItems()

    suspend fun addChecklistItem(item: com.example.persianquran.data.local.ChecklistItemEntity): Long =
        withContext(Dispatchers.IO) {
            quranDao.insertChecklistItem(item)
        }

    suspend fun updateChecklistItem(item: com.example.persianquran.data.local.ChecklistItemEntity) =
        withContext(Dispatchers.IO) {
            quranDao.updateChecklistItem(item)
        }

    suspend fun toggleChecklistItem(id: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        quranDao.updateChecklistItemCompletion(id, isCompleted)
    }

    suspend fun deleteChecklistItem(id: Long) = withContext(Dispatchers.IO) {
        quranDao.deleteChecklistItem(id)
    }

    suspend fun resetDailyChecklist() = withContext(Dispatchers.IO) {
        quranDao.resetDailyChecklistItems()
    }

    suspend fun clearCompletedChecklist() = withContext(Dispatchers.IO) {
        quranDao.clearCompletedChecklistItems()
    }

    suspend fun clearAllChecklist() = withContext(Dispatchers.IO) {
        quranDao.clearAllChecklistItems()
    }

    private fun getBuiltInFatiha(): List<Verse> = listOf(
        Verse(1, 1, "1:1", "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", 1, 1, 1, "به نام الله بخشنده مهربان", audioUrl = "https://audio.qurancdn.com/Alafasy/mp3/001001.mp3"),
        Verse(2, 2, "1:2", "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ", 1, 1, 1, "ستایش مخصوص الله است که پروردگار جهانیان است", audioUrl = "https://audio.qurancdn.com/Alafasy/mp3/001002.mp3"),
        Verse(3, 3, "1:3", "ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", 1, 1, 1, "بخشنده و مهربان است", audioUrl = "https://audio.qurancdn.com/Alafasy/mp3/001003.mp3"),
        Verse(4, 4, "1:4", "مَـٰلِكِ يَوْمِ ٱلدِّينِ", 1, 1, 1, "مالک و فرمانروای روز جزا است", audioUrl = "https://audio.qurancdn.com/Alafasy/mp3/001004.mp3"),
        Verse(5, 5, "1:5", "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", 1, 1, 1, "تنها تو را می‌پرستیم و تنها از تو یاری می‌جوییم", audioUrl = "https://audio.qurancdn.com/Alafasy/mp3/001005.mp3"),
        Verse(6, 6, "1:6", "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", 1, 1, 1, "ما را به راه راست هدایت فرما", audioUrl = "https://audio.qurancdn.com/Alafasy/mp3/001006.mp3"),
        Verse(7, 7, "1:7", "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", 1, 1, 1, "راه کسانی که بر آنان نعمت دادی، نه آنان که مورد خشم قرار گرفتند و نه گمراهان", audioUrl = "https://audio.qurancdn.com/Alafasy/mp3/001007.mp3")
    )

    private fun getBuiltInIkhlas(): List<Verse> = listOf(
        Verse(1, 1, "112:1", "قُلْ هُوَ ٱللَّهُ أَحَدٌ", 112, 604, 30, "بگو: او الله، یگانه و بی‌همتاست", audioUrl = "https://audio.qurancdn.com/Alafasy/mp3/112001.mp3"),
        Verse(2, 2, "112:2", "ٱللَّهُ ٱلصَّمَدُ", 112, 604, 30, "الله بی‌نیاز است و همه نیازمند اویند", audioUrl = "https://audio.qurancdn.com/Alafasy/mp3/112002.mp3"),
        Verse(3, 3, "112:3", "لَمْ يَلِدْ وَلَمْ يُولَدْ", 112, 604, 30, "نه زاده و نه زاده شده است", audioUrl = "https://audio.qurancdn.com/Alafasy/mp3/112003.mp3"),
        Verse(4, 4, "112:4", "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌۢ", 112, 604, 30, "و هیچ‌کس همتا و مانند او نبوده و نیست", audioUrl = "https://audio.qurancdn.com/Alafasy/mp3/112004.mp3")
    )
}
