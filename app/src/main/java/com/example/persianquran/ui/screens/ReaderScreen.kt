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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.persianquran.data.model.ReaderSettings
import com.example.persianquran.data.model.Surah
import com.example.persianquran.data.model.Verse
import com.example.persianquran.data.surah.toPersianDigits
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
    onBack: () -> Unit,
    onNextSurah: () -> Unit,
    onPreviousSurah: () -> Unit,
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
    onRetry: () -> Unit,
    onClearScrollTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var showQuickSettings by remember { mutableStateOf(false) }

    // Scroll to target verse when verses are loaded
    LaunchedEffect(versesState, targetVerseToScroll) {
        if (versesState is UiState.Success && targetVerseToScroll != null) {
            val idx = (targetVerseToScroll - 1).coerceAtLeast(0)
            if (idx in versesState.data.indices) {
                // If Surah has bismillah banner, offset by 1
                val scrollIndex = if (surah.bismillahPre && surah.id != 9) idx + 1 else idx
                listState.animateScrollToItem(scrollIndex)
                onClearScrollTarget()
            }
        }
    }

    // Scroll to active playing verse
    LaunchedEffect(playerState.currentVerseNumber) {
        if (versesState is UiState.Success && playerState.currentSurahId == surah.id && playerState.currentVerseNumber != null) {
            val vNum = playerState.currentVerseNumber!!
            val idx = (vNum - 1).coerceAtLeast(0)
            if (idx in versesState.data.indices) {
                val scrollIndex = if (surah.bismillahPre && surah.id != 9) idx + 1 else idx
                listState.animateScrollToItem(scrollIndex)
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
                    Text(
                        text = "سوره ${surah.namePersian} (${surah.nameArabic})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${surah.revelationType} • ${surah.versesCount.toPersianDigits()} آیه • جزء ${getJuzForSurah(surah.id).toPersianDigits()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "بازگشت")
                }
            },
            actions = {
                // Quick Settings (Font size & translation toggle)
                IconButton(onClick = { showQuickSettings = true }) {
                    Icon(imageVector = Icons.Default.FormatSize, contentDescription = "تنظیم قلم")
                }

                // Play entire surah from beginning
                IconButton(onClick = onPlaySurah) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "پخش سوره",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Surah Navigation Bar (Previous Surah / Next Surah)
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

            Text(
                text = "صفحه ${surah.startPage.toPersianDigits()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

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
                    // Bismillah Banner (All surahs except Surah 9 At-Tawbah and Surah 1 where Bismillah is Ayah 1)
                    if (surah.bismillahPre && surah.id != 9 && surah.id != 1) {
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
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 24.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Verses
                    itemsIndexed(versesState.data, key = { _, verse -> verse.verseKey }) { _, verse ->
                        val isPlaying = playerState.isPlaying &&
                                playerState.currentSurahId == verse.chapterId &&
                                playerState.currentVerseNumber == verse.verseNumber

                        val isBookmarked = bookmarks.any {
                            it.surahNumber == verse.chapterId && it.verseNumber == verse.verseNumber
                        }

                        AyahCard(
                            verse = verse,
                            surahNamePersian = surah.namePersian,
                            settings = settings,
                            isCurrentlyPlaying = isPlaying,
                            isBookmarked = isBookmarked,
                            onPlayClick = { onPlayVerse(verse) },
                            onBookmarkClick = { onToggleBookmark(verse) },
                            onMoreClick = { onOpenVerseMenu(verse) }
                        )
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

        ContextualAyahSheet(
            verse = selectedVerseForMenu,
            surahNamePersian = surah.namePersian,
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
            onUpdateArabicSize = onUpdateArabicSize,
            onUpdateTranslationSize = onUpdateTranslationSize,
            onToggleShowTranslation = onToggleShowTranslation
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickReaderSettingsSheet(
    settings: ReaderSettings,
    onDismiss: () -> Unit,
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

            Spacer(modifier = Modifier.height(16.dp))

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
