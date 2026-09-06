package com.example.persianquran.data.model

data class Surah(
    val id: Int,
    val namePersian: String,
    val nameArabic: String,
    val versesCount: Int,
    val revelationType: String, // "مکی" or "مدنی"
    val revelationOrder: Int,
    val startPage: Int,
    val bismillahPre: Boolean = true
)

data class Verse(
    val id: Int,
    val verseNumber: Int,
    val verseKey: String,
    val textUthmani: String,
    val chapterId: Int,
    val pageNumber: Int,
    val juzNumber: Int,
    val translation: String,
    val translatorName: String = "اسلام‌هاوس",
    val audioUrl: String? = null,
    val isBookmarked: Boolean = false
)

data class Reciter(
    val id: Int,
    val namePersian: String,
    val nameArabic: String,
    val style: String? = null,
    val quranFoundationId: Int
)

data class SearchResultItem(
    val verseKey: String,
    val surahNumber: Int,
    val verseNumber: Int,
    val surahNamePersian: String,
    val textUthmani: String,
    val translation: String
)

enum class QuranTheme {
    LIGHT, DARK, SYSTEM
}

enum class AudioQuality(val titlePersian: String) {
    AUTO("خودکار"),
    HIGH("کیفیت بالا"),
    MEDIUM("کیفیت متوسط"),
    LOW("کیفیت پایین")
}

data class ReaderSettings(
    val arabicFontSize: Float = 24f,
    val translationFontSize: Float = 16f,
    val lineSpacing: Float = 1.6f,
    val showTranslation: Boolean = true,
    val theme: QuranTheme = QuranTheme.SYSTEM,
    val selectedReciterId: Int = 7, // Default: Mishari Rashid al-Afasy
    val audioQuality: AudioQuality = AudioQuality.AUTO,
    val playbackSpeed: Float = 1.0f,
    val autoAdvance: Boolean = true,
    val repeatAyah: Boolean = false
)

data class AudioTrackState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentSurahId: Int? = null,
    val currentVerseNumber: Int? = null,
    val currentVerseKey: String? = null,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val reciterName: String = "",
    val errorMessage: String? = null
)

// =================== PLANNING MODELS ===================

enum class PlanMethod(val titlePersian: String, val descriptionPersian: String) {
    PAGES("بر اساس صفحات", "تقسیم ۶۰۴ صفحه مصحف عثمان‌طه بر روزهای برنامه"),
    VERSES("بر اساس آیات", "تقسیم آیات بر حسب تعداد مشخص در هر روز"),
    SURAHS("بر اساس سوره", "تقسیم سوره‌های قرآن به صورت فصل‌بندی روزانه"),
    RANGE("بر اساس محدوده", "انتخاب سوره و آیه آغازین و پایانی دلخواه")
}

data class ReadingPlan(
    val id: Long = 0,
    val title: String,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val totalDays: Int,
    val method: PlanMethod,
    val startSurahId: Int = 1,
    val startVerseNumber: Int = 1,
    val endSurahId: Int = 114,
    val endVerseNumber: Int = 6,
    val dailyTarget: Int = 1,
    val isActive: Boolean = true,
    val isPaused: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class PlanDaySchedule(
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

// =================== CHECKLIST MODELS ===================

data class ChecklistItem(
    val id: Long = 0,
    val title: String,
    val category: String = "روزانه", // "روزانه", "برنامه‌ریزی", "شخصی"
    val isCompleted: Boolean = false,
    val isDaily: Boolean = true,
    val linkedPlanId: Long? = null,
    val linkedDayNumber: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
