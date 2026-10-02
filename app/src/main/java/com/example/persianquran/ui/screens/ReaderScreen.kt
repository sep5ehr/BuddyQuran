package com.example.persianquran.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.persianquran.data.local.BookmarkEntity
import com.example.persianquran.data.model.AudioTrackState
import com.example.persianquran.data.model.PlanDaySchedule
import com.example.persianquran.data.model.QuranFont
import com.example.persianquran.data.model.ReaderSettings
import com.example.persianquran.data.model.Surah
import com.example.persianquran.data.model.Verse
import com.example.persianquran.data.model.toFontFamily
import com.example.persianquran.data.surah.QuranHizbMetadata
import com.example.persianquran.data.surah.QuranMetadata
import com.example.persianquran.data.surah.QuranPageMetadata
import com.example.persianquran.data.surah.toPersianDigits
import com.example.persianquran.ui.components.AutoSelectOutlinedTextField
import com.example.persianquran.ui.components.AyahCard
import com.example.persianquran.ui.components.ContextualAyahSheet
import com.example.persianquran.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    surah: Surah,
    versesState: UiState<List<Verse>>,
    targetVerseToScroll: Int?,
    settings: ReaderSettings,
    playerState: AudioTrackState,
    bookmarks: List<BookmarkEntity>,
    selectedVerseForMenu: Verse?,
    currentPage: Int? = null,
    activeStudyDay: PlanDaySchedule? = null,
    nextPlanDay: PlanDaySchedule? = null,
    onBack: () -> Unit,
    onNextSurah: () -> Unit,
    onPreviousSurah: () -> Unit,
    onNextPage: (() -> Unit)? = null,
    onPreviousPage: (() -> Unit)? = null,
    onNextPlan: (() -> Unit)? = null,
    onFinishPlanStudy: (() -> Unit)? = null,
    onStudyCompleted: (() -> Unit)? = null,
    onPlaySurah: () -> Unit,
    onPlayVerse: (Verse) -> Unit,
    onToggleBookmark: (Verse) -> Unit,
    onOpenVerseMenu: (Verse) -> Unit,
    onCloseVerseMenu: () -> Unit,
    onCopyAll: (Verse) -> Unit,
    onCopyArabicOnly: (Verse) -> Unit,
    onCopyTranslationOnly: (Verse) -> Unit,
    onShareVerse: (Verse) -> Unit,
    onDownloadVerse: (Verse) -> Unit,
    onUpdateArabicSize: (Float) -> Unit,
    onUpdateTranslationSize: (Float) -> Unit,
    onToggleShowTranslation: (Boolean) -> Unit,
    onUpdateQuranFont: (QuranFont) -> Unit = {},
    onSelectPage: (Int) -> Unit = {},
    onRetry: () -> Unit,
    onClearScrollTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var showQuickSettings by remember { mutableStateOf(false) }
    var showPagePickerDialog by remember { mutableStateOf(false) }

    val safePage = currentPage?.coerceIn(1, QuranPageMetadata.TOTAL_PAGES)
    val isDailyStudy = activeStudyDay != null
    val targetEndPage = activeStudyDay?.let { maxOf(it.startPage, it.endPage) } ?: QuranPageMetadata.TOTAL_PAGES
    val targetStartPage = activeStudyDay?.let { it.startPage.coerceIn(1, QuranPageMetadata.TOTAL_PAGES) } ?: 1
    val isAtTargetEndPage = isDailyStudy && (safePage != null && safePage >= targetEndPage)
    val isAtDailyStart = isDailyStudy && (safePage != null && safePage <= targetStartPage)

    var hasReadLastVerse by remember(activeStudyDay?.id, activeStudyDay?.dayNumber) {
        mutableStateOf(activeStudyDay?.isCompleted == true)
    }

    // Audio completion trigger: automatically marked when reciter finishes the target end verse
    LaunchedEffect(playerState.isPlaying, playerState.currentSurahId, playerState.currentVerseNumber) {
        if (activeStudyDay != null && !hasReadLastVerse && isAtTargetEndPage) {
            val sId = playerState.currentSurahId
            val vNum = playerState.currentVerseNumber
            if (sId != null && vNum != null && sId == activeStudyDay.endSurahId && vNum >= activeStudyDay.endVerse) {
                hasReadLastVerse = true
                onStudyCompleted?.invoke()
            }
        }
    }

    val isAtDailyLimit = isAtTargetEndPage && hasReadLastVerse

    // Scroll to target verse when verses are loaded
    LaunchedEffect(versesState, targetVerseToScroll) {
        if (versesState is UiState.Success && targetVerseToScroll != null) {
            val list = versesState.data
            val idx = list.indexOfFirst { it.verseNumber == targetVerseToScroll }.let { if (it >= 0) it else 0 }
            listState.animateScrollToItem(idx)
            onClearScrollTarget()
        }
    }

    // Scroll to active playing verse
    LaunchedEffect(playerState.currentVerseNumber) {
        if (versesState is UiState.Success && playerState.currentVerseNumber != null) {
            val vNum = playerState.currentVerseNumber!!
            val sId = playerState.currentSurahId
            val idx = versesState.data.indexOfFirst { it.verseNumber == vNum && (sId == null || it.chapterId == sId) }
            if (idx >= 0) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("reader_screen")
    ) {
        // Reader Top App Bar
        TopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (currentPage != null) {
                        val safePage = currentPage.coerceIn(1, QuranPageMetadata.TOTAL_PAGES)
                        val boundary = QuranPageMetadata.getPageBoundary(safePage)
                        val startS = QuranMetadata.getSurahById(boundary.startSurahId)
                        val endS = QuranMetadata.getSurahById(boundary.endSurahId)
                        val surahSubtitle = if (boundary.startSurahId == boundary.endSurahId) {
                            "سوره ${startS?.namePersian ?: ""} (${startS?.nameArabic ?: ""})"
                        } else {
                            "سوره ${startS?.namePersian ?: ""} تا ${endS?.namePersian ?: ""}"
                        }
                        val juzNum = QuranPageMetadata.getJuzForPage(safePage)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showPagePickerDialog = true }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "صفحه ${safePage.toPersianDigits()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "انتخاب صفحه",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        val hizbNum = remember(safePage) { QuranPageMetadata.getHizbForPage(safePage) }
                        Text(
                            text = "$surahSubtitle • جزء ${juzNum.toPersianDigits()} • حزب ${hizbNum.toPersianDigits()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    } else {
                        val surahHizb = remember(surah.id) { QuranHizbMetadata.getHizbForSurahAndVerse(surah.id, 1) }
                        Text(
                            text = "سوره ${surah.namePersian} (${surah.nameArabic})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${surah.revelationType} • ${surah.versesCount.toPersianDigits()} آیه • جزء ${getJuzForSurah(surah.id).toPersianDigits()} • حزب ${surahHizb.toPersianDigits()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "بازگشت")
                }
            },
            actions = {
                // Quick Settings: Button for font size and appearance (اندازه)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showQuickSettings = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "اندازه و قلم",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "اندازه",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Play from beginning
                IconButton(onClick = {
                    if (versesState is UiState.Success && versesState.data.isNotEmpty()) {
                        onPlayVerse(versesState.data.first())
                    } else {
                        onPlaySurah()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = if (currentPage != null) "پخش صفحه" else "پخش سوره",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Navigation Bar: Page navigation if reading by page, else Surah navigation
        if (currentPage != null && safePage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isAtDailyStart) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPreviousPage?.invoke() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "صفحه قبل",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "صفحه قبل",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showPagePickerDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isDailyStudy)
                                "صفحه ${safePage.toPersianDigits()} (سهمیه این برنامه: ${targetStartPage.toPersianDigits()} تا ${targetEndPage.toPersianDigits()})"
                            else
                                "صفحه ${safePage.toPersianDigits()} از ۶۰۴",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "انتخاب صفحه",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Next page OR Next plan OR Finish study
                if (isAtDailyLimit) {
                    if (nextPlanDay != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNextPlan?.invoke() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "برنامه بعد",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "برنامه بعد",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onFinishPlanStudy?.invoke() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "ثبت مطالعه",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ثبت و چک‌لیست",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                } else if (isAtTargetEndPage) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "مطالعه تا آیه ${activeStudyDay?.endVerse?.toPersianDigits() ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNextPage?.invoke() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "صفحه بعد",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "صفحه بعد",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Daily study quota completed banner (shown only when last ayah is reached)
            if (isAtDailyLimit) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "سهمیه مطالعه این برنامه با موفقیت خوانده شد",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (nextPlanDay != null) "برنامه دیگری برای امروز وجود دارد." else "وضعیت مطالعه ثبت و تیک خورده شد.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Button(
                            onClick = {
                                if (nextPlanDay != null) onNextPlan?.invoke()
                                else onFinishPlanStudy?.invoke()
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = if (nextPlanDay != null) "برنامه بعد" else "ورود به چک‌لیست",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (surah.id > 1) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPreviousSurah() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "سوره قبل",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "سوره قبل",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showPagePickerDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "صفحه ${surah.startPage.toPersianDigits()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "انتخاب صفحه",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (surah.id < 114) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNextSurah() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سوره بعد",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "سوره بعد",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }
        }

        // Verses Content / Loading / Error
        when (versesState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "در حال بارگذاری آیات سوره ${surah.namePersian}...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = versesState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "تلاش مجدد")
                        }
                    }
                }
            }

            is UiState.Success -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // For whole-surah mode: Bismillah Banner (All surahs except Surah 9 At-Tawbah and Surah 1 where Bismillah is Ayah 1)
                    if (currentPage == null && surah.bismillahPre && surah.id != 9 && surah.id != 1) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = settings.quranFont.toFontFamily(),
                                            fontSize = 24.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Verses belonging strictly to this page or surah
                    itemsIndexed(versesState.data, key = { _, verse -> verse.verseKey }) { index, verse ->
                        val prevVerse = if (index > 0) versesState.data[index - 1] else null
                        val isNewSurahTransition = prevVerse != null && prevVerse.chapterId != verse.chapterId
                        val isPageFirstVerseAndSurahStart = currentPage != null && index == 0 && verse.verseNumber == 1
                        val verseSurah = QuranMetadata.getSurahById(verse.chapterId)

                        // Surah Header and Bismillah when a Surah starts within the page or at verse 1
                        if (isNewSurahTransition || isPageFirstVerseAndSurahStart) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "سوره ${verseSurah?.namePersian ?: ""} (${verseSurah?.nameArabic ?: ""})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                if (verseSurah?.bismillahPre == true && verse.chapterId != 9 && verse.chapterId != 1) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontFamily = settings.quranFont.toFontFamily(),
                                                    fontSize = 22.sp
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val isPlaying = playerState.isPlaying &&
                                playerState.currentSurahId == verse.chapterId &&
                                playerState.currentVerseNumber == verse.verseNumber

                        val isBookmarked = bookmarks.any {
                            it.surahNumber == verse.chapterId && it.verseNumber == verse.verseNumber
                        }

                        AyahCard(
                            verse = verse,
                            surahNamePersian = verseSurah?.namePersian ?: surah.namePersian,
                            settings = settings,
                            isCurrentlyPlaying = isPlaying,
                            isBookmarked = isBookmarked,
                            onPlayClick = { onPlayVerse(verse) },
                            onBookmarkClick = { onToggleBookmark(verse) },
                            onMoreClick = { onOpenVerseMenu(verse) }
                        )
                    }

                    // Confirmation action card shown right at the end of the targeted section before marked read
                    if (isAtTargetEndPage && !hasReadLastVerse && activeStudyDay != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "پایان سهمیه مطالعه این برنامه",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "تا سوره ${activeStudyDay.endSurahName} آیه ${activeStudyDay.endVerse.toPersianDigits()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            hasReadLastVerse = true
                                            onStudyCompleted?.invoke()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "خواندم", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Study Completion Card right after last verse in LazyColumn
                    if (isAtDailyLimit) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "سهمیه مطالعه این برنامه با موفقیت خوانده شد",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (nextPlanDay != null) "برنامه دیگری برای امروز وجود دارد." else "وضعیت مطالعه ثبت و تیک خورده شد.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            if (nextPlanDay != null) onNextPlan?.invoke()
                                            else onFinishPlanStudy?.invoke()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(
                                            text = if (nextPlanDay != null) "برنامه بعد" else "ورود به چک‌لیست",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is UiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }

    // Contextual Bottom Sheet for selected Ayah
    if (selectedVerseForMenu != null) {
        val isBookmarked = bookmarks.any {
            it.surahNumber == selectedVerseForMenu.chapterId && it.verseNumber == selectedVerseForMenu.verseNumber
        }
        val sheetSurah = QuranMetadata.getSurahById(selectedVerseForMenu.chapterId)

        ContextualAyahSheet(
            verse = selectedVerseForMenu,
            surahNamePersian = sheetSurah?.namePersian ?: surah.namePersian,
            isBookmarked = isBookmarked,
            onDismiss = onCloseVerseMenu,
            onPlay = { onPlayVerse(selectedVerseForMenu) },
            onToggleBookmark = { onToggleBookmark(selectedVerseForMenu) },
            onCopyAll = { onCopyAll(selectedVerseForMenu) },
            onCopyArabicOnly = { onCopyArabicOnly(selectedVerseForMenu) },
            onCopyTranslationOnly = { onCopyTranslationOnly(selectedVerseForMenu) },
            onShare = { onShareVerse(selectedVerseForMenu) },
            onDownload = { onDownloadVerse(selectedVerseForMenu) }
        )
    }

    // Quick Reader Settings Sheet
    if (showQuickSettings) {
        QuickReaderSettingsSheet(
            settings = settings,
            onDismiss = { showQuickSettings = false },
            onUpdateQuranFont = onUpdateQuranFont,
            onUpdateArabicSize = onUpdateArabicSize,
            onUpdateTranslationSize = onUpdateTranslationSize,
            onToggleShowTranslation = onToggleShowTranslation
        )
    }

    // Page Chooser Dialog (Requirement: click on current page text to choose page)
    if (showPagePickerDialog) {
        ChoosePageDialog(
            initialPage = currentPage ?: surah.startPage,
            onDismiss = { showPagePickerDialog = false },
            onConfirm = { selectedPage ->
                showPagePickerDialog = false
                onSelectPage(selectedPage)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickReaderSettingsSheet(
    settings: ReaderSettings,
    onDismiss: () -> Unit,
    onUpdateQuranFont: (QuranFont) -> Unit = {},
    onUpdateArabicSize: (Float) -> Unit,
    onUpdateTranslationSize: (Float) -> Unit,
    onToggleShowTranslation: (Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تنظیمات قلم و نمایش",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quran Font Selector ("قدیمی" vs "جدید")
            Text(
                text = "نوع قلم قرآن کریم",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(
                    Pair(QuranFont.OLD, "قدیمی"),
                    Pair(QuranFont.NEW, "جدید")
                ).forEach { (f, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onUpdateQuranFont(f) }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        RadioButton(
                            selected = settings.quranFont == f,
                            onClick = { onUpdateQuranFont(f) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Arabic Font Size
            Text(
                text = "اندازه متن قرآن: ${settings.arabicFontSize.toInt().toPersianDigits()} واحد",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = settings.arabicFontSize,
                onValueChange = onUpdateArabicSize,
                valueRange = 20f..40f,
                steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Translation Font Size
            Text(
                text = "اندازه متن ترجمه: ${settings.translationFontSize.toInt().toPersianDigits()} واحد",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = settings.translationFontSize,
                onValueChange = onUpdateTranslationSize,
                valueRange = 13f..24f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Show Translation Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نمایش ترجمه فارسی",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = settings.showTranslation,
                    onCheckedChange = onToggleShowTranslation,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ChoosePageDialog(
    initialPage: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val safeInitial = initialPage.coerceIn(1, QuranPageMetadata.TOTAL_PAGES)
    var pageInput by remember { mutableStateOf(safeInitial.toString()) }
    var selectedPage by remember { mutableIntStateOf(safeInitial) }

    val boundary = remember(selectedPage) {
        QuranPageMetadata.getPageBoundary(selectedPage)
    }
    val juzNum = remember(selectedPage) {
        QuranPageMetadata.getJuzForPage(selectedPage)
    }
    val surahObj = remember(boundary.startSurahId) {
        QuranMetadata.getSurahById(boundary.startSurahId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "انتخاب صفحه قرآن کریم",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Info preview badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "صفحه ${selectedPage.toPersianDigits()} از ۶۰۴",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val hizbNum = remember(selectedPage) { QuranPageMetadata.getHizbForPage(selectedPage) }
                        Text(
                            text = "سوره ${surahObj?.namePersian ?: "سوره ${boundary.startSurahId}"} • جزء ${juzNum.toPersianDigits()} • حزب ${hizbNum.toPersianDigits()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Auto-select text input for page number
                AutoSelectOutlinedTextField(
                    value = pageInput,
                    onValueChange = { newVal ->
                        val digitsOnly = newVal.filter { it.isDigit() }
                        pageInput = digitsOnly
                        val p = digitsOnly.toIntOrNull()
                        if (p != null) {
                            selectedPage = p.coerceIn(1, QuranPageMetadata.TOTAL_PAGES)
                        }
                    },
                    label = { Text("شماره صفحه (۱ تا ۶۰۴)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick jump buttons (-10, -1, +1, +10)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(-10, -1, 1, 10).forEach { delta ->
                        OutlinedButton(
                            onClick = {
                                val newP = (selectedPage + delta).coerceIn(1, QuranPageMetadata.TOTAL_PAGES)
                                selectedPage = newP
                                pageInput = newP.toString()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            val sign = if (delta > 0) "+${delta.toPersianDigits()}" else delta.toPersianDigits()
                            Text(text = sign, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Slider for interactive browsing
                Slider(
                    value = selectedPage.toFloat(),
                    onValueChange = {
                        val p = it.toInt().coerceIn(1, QuranPageMetadata.TOTAL_PAGES)
                        selectedPage = p
                        pageInput = p.toString()
                    },
                    valueRange = 1f..604f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedPage) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("رفتن به صفحه")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

private fun getJuzForSurah(surahId: Int): Int {
    return when {
        surahId == 1 -> 1
        surahId == 2 -> 1 // Juz 1, 2, 3
        surahId == 3 -> 3 // Juz 3, 4
        surahId == 4 -> 4
        surahId == 5 -> 6
        surahId == 6 -> 7
        surahId == 7 -> 8
        surahId == 8 -> 9
        surahId == 9 -> 10
        surahId in 10..11 -> 11
        surahId in 12..14 -> 12
        surahId in 15..16 -> 14
        surahId in 17..18 -> 15
        surahId in 19..20 -> 16
        surahId in 21..22 -> 17
        surahId in 23..25 -> 18
        surahId in 26..28 -> 19
        surahId in 29..32 -> 20
        surahId in 33..36 -> 21
        surahId in 37..39 -> 23
        surahId in 40..45 -> 24
        surahId in 46..51 -> 26
        surahId in 52..57 -> 27
        surahId in 58..66 -> 28
        surahId in 67..77 -> 29
        else -> 30
    }
}
