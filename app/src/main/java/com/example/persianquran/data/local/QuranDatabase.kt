package com.example.persianquran.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahNumber: Int,
    val verseNumber: Int,
    val verseKey: String,
    val surahNamePersian: String,
    val arabicText: String,
    val translationText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey
    val id: Int = 1,
    val surahNumber: Int,
    val verseNumber: Int,
    val verseKey: String,
    val surahNamePersian: String,
    val scrollIndex: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_verses")
data class CachedVerseEntity(
    @PrimaryKey
    val verseKey: String,
    val surahNumber: Int,
    val verseNumber: Int,
    val textUthmani: String,
    val translation: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val audioUrl: String?
)

@Entity(tableName = "audio_downloads")
data class AudioDownloadEntity(
    @PrimaryKey
    val verseKey: String,
    val surahNumber: Int,
    val verseNumber: Int,
    val reciterId: Int,
    val filePath: String,
    val fileSize: Long,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_plans")
data class ReadingPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val totalDays: Int,
    val method: String, // "PAGES", "VERSES", "SURAHS", "RANGE"
    val startSurahId: Int = 1,
    val startVerseNumber: Int = 1,
    val endSurahId: Int = 114,
    val endVerseNumber: Int = 6,
    val dailyTarget: Int = 1,
    val isActive: Boolean = true,
    val isPaused: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "plan_day_schedules",
    indices = [
        Index(value = ["planId"]),
        Index(value = ["planId", "dayNumber"], unique = true)
    ]
)
data class PlanDayScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long,
    val dayNumber: Int,
    val dateMillis: Long,
    val dateFormatted: String,
    val startSurahId: Int,
    val startSurahName: String,
    val startVerse: Int,
    val endSurahId: Int,
    val endSurahName: String,
    val endVerse: Int,
    val startPage: Int = 1,
    val endPage: Int = 1,
    val rangeDescription: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "روزانه",
    val isCompleted: Boolean = false,
    val isDaily: Boolean = true,
    val linkedPlanId: Long? = null,
    val linkedDayNumber: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface QuranDao {

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber)")
    suspend fun isBookmarked(surahNumber: Int, verseNumber: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber)")
    fun observeIsBookmarked(surahNumber: Int, verseNumber: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surahNumber AND verseNumber = :verseNumber")
    suspend fun deleteBookmarkByVerse(surahNumber: Int, verseNumber: Int)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks")
    suspend fun clearAllBookmarks()

    // Reading Progress
    @Query("SELECT * FROM reading_progress WHERE id = 1")
    fun getReadingProgress(): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress WHERE id = 1")
    suspend fun getReadingProgressOnce(): ReadingProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadingProgress(progress: ReadingProgressEntity)

    // Cached Verses
    @Query("SELECT * FROM cached_verses WHERE surahNumber = :surahNumber ORDER BY verseNumber ASC")
    suspend fun getVersesForSurah(surahNumber: Int): List<CachedVerseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<CachedVerseEntity>)

    @Query("SELECT COUNT(*) FROM cached_verses")
    suspend fun getCachedVersesCount(): Int

    @Query("SELECT COUNT(DISTINCT surahNumber) FROM cached_verses")
    suspend fun getCachedSurahsCount(): Int

    @Query("SELECT * FROM cached_verses WHERE verseKey = :verseKey LIMIT 1")
    suspend fun getCachedVerseByKey(verseKey: String): CachedVerseEntity?

    @Query("SELECT * FROM cached_verses WHERE textUthmani LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%' LIMIT 100")
    suspend fun searchCachedVerses(query: String): List<CachedVerseEntity>

    // Downloads
    @Query("SELECT * FROM audio_downloads WHERE surahNumber = :surahNumber AND reciterId = :reciterId")
    suspend fun getDownloadsForSurah(surahNumber: Int, reciterId: Int): List<AudioDownloadEntity>

    @Query("SELECT * FROM audio_downloads WHERE verseKey = :verseKey AND reciterId = :reciterId LIMIT 1")
    suspend fun getDownloadedVerse(verseKey: String, reciterId: Int): AudioDownloadEntity?

    @Query("SELECT COUNT(*) FROM audio_downloads")
    fun getDownloadsCount(): Flow<Int>

    @Query("SELECT SUM(fileSize) FROM audio_downloads")
    fun getTotalDownloadsSize(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: AudioDownloadEntity)

    @Query("DELETE FROM audio_downloads WHERE verseKey = :verseKey AND reciterId = :reciterId")
    suspend fun deleteDownload(verseKey: String, reciterId: Int)

    @Query("DELETE FROM audio_downloads")
    suspend fun clearAllDownloads()

    // =================== READING PLANS ===================
    @Query("SELECT * FROM reading_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<ReadingPlanEntity>>

    @Query("SELECT * FROM reading_plans WHERE isActive = 1 LIMIT 1")
    fun getActivePlan(): Flow<ReadingPlanEntity?>

    @Query("SELECT * FROM reading_plans WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlanOnce(): ReadingPlanEntity?

    @Query("SELECT * FROM reading_plans WHERE id = :planId LIMIT 1")
    suspend fun getPlanById(planId: Long): ReadingPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: ReadingPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: ReadingPlanEntity)

    @Query("UPDATE reading_plans SET isActive = 0")
    suspend fun deactivateAllPlans()

    @Query("UPDATE reading_plans SET isActive = 1 WHERE id = :planId")
    suspend fun activatePlan(planId: Long)

    @Query("UPDATE reading_plans SET isPaused = :isPaused WHERE id = :planId")
    suspend fun setPlanPaused(planId: Long, isPaused: Boolean)

    @Query("DELETE FROM reading_plans WHERE id = :planId")
    suspend fun deletePlan(planId: Long)

    @Query("DELETE FROM reading_plans")
    suspend fun clearAllPlans()

    // =================== PLAN DAY SCHEDULES ===================
    @Query("SELECT * FROM plan_day_schedules WHERE planId = :planId ORDER BY dayNumber ASC")
    fun getScheduleForPlan(planId: Long): Flow<List<PlanDayScheduleEntity>>

    @Query("SELECT * FROM plan_day_schedules WHERE planId = :planId ORDER BY dayNumber ASC")
    suspend fun getScheduleForPlanOnce(planId: Long): List<PlanDayScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItems(items: List<PlanDayScheduleEntity>)

    @Query("UPDATE plan_day_schedules SET isCompleted = :isCompleted, completedAt = :completedAt WHERE planId = :planId AND dayNumber = :dayNumber")
    suspend fun updateDayCompletion(planId: Long, dayNumber: Int, isCompleted: Boolean, completedAt: Long?)

    @Query("DELETE FROM plan_day_schedules WHERE planId = :planId")
    suspend fun deleteScheduleForPlan(planId: Long)

    @Query("DELETE FROM plan_day_schedules")
    suspend fun clearAllSchedules()

    // =================== CHECKLIST ITEMS ===================
    @Query("SELECT * FROM checklist_items ORDER BY id ASC")
    fun getAllChecklistItems(): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: ChecklistItemEntity): Long

    @Update
    suspend fun updateChecklistItem(item: ChecklistItemEntity)

    @Query("UPDATE checklist_items SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateChecklistItemCompletion(id: Long, isCompleted: Boolean)

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun deleteChecklistItem(id: Long)

    @Query("UPDATE checklist_items SET isCompleted = 0 WHERE isDaily = 1")
    suspend fun resetDailyChecklistItems()

    @Query("DELETE FROM checklist_items WHERE isCompleted = 1")
    suspend fun clearCompletedChecklistItems()

    @Query("DELETE FROM checklist_items")
    suspend fun clearAllChecklistItems()
}

@Database(
    entities = [
        BookmarkEntity::class,
        ReadingProgressEntity::class,
        CachedVerseEntity::class,
        AudioDownloadEntity::class,
        ReadingPlanEntity::class,
        PlanDayScheduleEntity::class,
        ChecklistItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
}
