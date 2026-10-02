package com.example.persianquran.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.core.content.ContextCompat
import com.example.persianquran.data.local.BookmarkEntity
import com.example.persianquran.data.model.QuranFont
import com.example.persianquran.data.model.QuranTheme
import com.example.persianquran.data.model.ReaderSettings
import com.example.persianquran.data.model.SearchResultItem
import com.example.persianquran.data.model.toFontFamily
import com.example.persianquran.data.surah.QuranMetadata
import com.example.persianquran.data.surah.toPersianDigits
import com.example.ui.components.sanitizeQuranText

// ======================= SEARCH SCREEN =======================
@Composable
fun SearchScreen(
    query: String,
    results: List<SearchResultItem>,
    isSearching: Boolean,
    onQueryChanged: (String) -> Unit,
    onResultClick: (Int, Int) -> Unit,
    quranFont: QuranFont = QuranFont.OLD,
    modifier: Modifier = Modifier
) {
    val sampleTopics = listOf("صبر", "نماز", "رحمت", "تقوا", "شکر", "بهشت", "هدایت")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Search Input
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("search_input_field"),
            placeholder = {
                Text(
                    text = "جستجو در متن و ترجمه قرآن کریم...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "جستجو")
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "پاک کردن")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        // Topic chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleTopics) { topic ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (query == topic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onQueryChanged(topic) }
                ) {
                    Text(
                        text = topic,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        color = if (query == topic) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Status / Count
        if (query.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearching) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "در حال جستجو...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "${results.size.toPersianDigits()} نتیجه برای «$query»",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Search Results List
        if (query.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "کلمه، عبارت یا موضوع مورد نظر خود را جستجو نمایید.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (!isSearching && results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "موردی برای «$query» یافت نشد.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results, key = { it.verseKey }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onResultClick(item.surahNumber, item.verseNumber) }
                            .testTag("search_result_${item.verseKey}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "سوره ${item.surahNamePersian} - آیه ${item.verseNumber.toPersianDigits()}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "باز کردن",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.textUthmani.sanitizeQuranText(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 17.sp,
                                    lineHeight = 26.sp,
                                    textAlign = TextAlign.Right,
                                    fontFamily = quranFont.toFontFamily()
                                )
                            )
                            if (item.translation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.translation,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        lineHeight = 20.sp,
                                        textAlign = TextAlign.Right
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================= BOOKMARKS SCREEN =======================
@Composable
fun BookmarksScreen(
    bookmarks: List<BookmarkEntity>,
    onBookmarkClick: (Int, Int) -> Unit,
    onRemoveBookmark: (BookmarkEntity) -> Unit,
    onClearAll: () -> Unit,
    quranFont: QuranFont = QuranFont.OLD,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("bookmarks_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "نشان‌شده‌ها",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${bookmarks.size.toPersianDigits()} آیه نشان‌شده",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (bookmarks.isNotEmpty()) {
                TextButton(onClick = { showClearConfirmDialog = true }) {
                    Text(
                        text = "پاکسازی همه",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        if (bookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "هنوز آیه‌ای را نشان نکرده‌اید.",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "در هنگام قرائت قرآن با لمس آیکون نشان، آیات دلخواه را ذخیره کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bookmarks, key = { it.id }) { b ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBookmarkClick(b.surahNumber, b.verseNumber) }
                            .testTag("bookmark_item_${b.verseKey}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "سوره ${b.surahNamePersian} - آیه ${b.verseNumber.toPersianDigits()}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = b.arabicText.sanitizeQuranText(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 17.sp,
                                        lineHeight = 24.sp,
                                        fontFamily = quranFont.toFontFamily(),
                                        textAlign = TextAlign.Right
                                    )
                                )
                                if (b.translationText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = b.translationText,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textAlign = TextAlign.Right
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            IconButton(onClick = { onRemoveBookmark(b) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف نشان",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(text = "پاکسازی تمام نشان‌ها") },
            text = { Text(text = "آیا مطمئن هستید که می‌خواهید تمام آیات نشان‌شده را حذف کنید؟") },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    showClearConfirmDialog = false
                }) {
                    Text(text = "بله، پاک شود", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(text = "انصراف")
                }
            }
        )
    }
}

// ======================= SETTINGS SCREEN =======================
@Composable
fun SettingsScreen(
    settings: ReaderSettings,
    onUpdateTheme: (QuranTheme) -> Unit,
    onUpdateQuranFont: (QuranFont) -> Unit = {},
    onUpdateArabicSize: (Float) -> Unit,
    onUpdateTranslationSize: (Float) -> Unit,
    onUpdateLineSpacing: (Float) -> Unit,
    onToggleShowTranslation: (Boolean) -> Unit,
    onUpdateReciter: (Int) -> Unit,
    onUpdateSpeed: (Float) -> Unit,
    onToggleAutoAdvance: (Boolean) -> Unit,
    onToggleRepeatAyah: (Boolean) -> Unit,
    onTogglePlayEntryAudio: (Boolean) -> Unit = {},
    onTogglePlayExitAudio: (Boolean) -> Unit = {},
    onToggleDailyReminder: (Boolean) -> Unit = {},
    onUpdateReminderTime: (Int, Int) -> Unit = { _, _ -> },
    onTestDailyReminder: () -> Unit = {},
    onClearAllBookmarks: () -> Unit = {},
    onClearAllPlans: () -> Unit = {},
    onClearAllChecklist: () -> Unit = {},
    onResetAllSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "تنظیمات برنامه",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Theme Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ظاهر",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val themes = listOf(
                        Pair(QuranTheme.SYSTEM, "تم دستگاه"),
                        Pair(QuranTheme.LIGHT, "روشن"),
                        Pair(QuranTheme.DARK, "تیره")
                    )

                    themes.forEach { (t, label) ->
                        val isSelected = settings.theme == t
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateTheme(t) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onUpdateTheme(t) },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // Font & Reader Appearance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "اندازه و قلم نمایش آیات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Font changer (جدید vs قدیمی - default قدیمی)
                    Text(
                        text = "نوع قلم قرآن کریم",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val fontOptions = listOf(
                        Pair(QuranFont.OLD, "قدیمی"),
                        Pair(QuranFont.NEW, "جدید")
                    )

                    fontOptions.forEach { (fontOption, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateQuranFont(fontOption) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.quranFont == fontOption,
                                onClick = { onUpdateQuranFont(fontOption) },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Arabic font size
                    Text(
                        text = "اندازه متن قرآن: ${settings.arabicFontSize.toInt().toPersianDigits()} واحد",
                        style = MaterialTheme.typography.bodyMedium
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

                    // Live preview Arabic
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = settings.arabicFontSize.sp,
                                fontFamily = settings.quranFont.toFontFamily(),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Translation font size
                    Text(
                        text = "اندازه متن ترجمه: ${settings.translationFontSize.toInt().toPersianDigits()} واحد",
                        style = MaterialTheme.typography.bodyMedium
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Show translation toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "نمایش ترجمه فارسی زیر آیات", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = settings.showTranslation,
                            onCheckedChange = onToggleShowTranslation,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Reciter Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "انتخاب قاری قرآن",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    QuranMetadata.reciters.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateReciter(r.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.selectedReciterId == r.id,
                                onClick = { onUpdateReciter(r.id) },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = r.namePersian,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (settings.selectedReciterId == r.id) FontWeight.Bold else FontWeight.Normal
                                )
                                if (r.style != null) {
                                    Text(
                                        text = r.style,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Playback Options
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تنظیمات پخش صوت",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto advance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "پخش خودکار آیه بعد", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = settings.autoAdvance,
                            onCheckedChange = onToggleAutoAdvance,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Repeat Ayah
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "تکرار مداوم آیه جاری", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = settings.repeatAyah,
                            onCheckedChange = onToggleRepeatAyah,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }

        // App Entry and Exit Audio Controls (Requirements: Bismillah on enter, Sadaqallah on exit)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_audio_effects_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "صوت ورود و خروج از برنامه",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "پخش نوای تبرک در آغاز و پایان استفاده از برنامه",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Entry audio switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "پخش صوت هنگام ورود",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "«بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ» در هنگام باز کردن برنامه",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = settings.playEntryAudio,
                            onCheckedChange = onTogglePlayEntryAudio,
                            modifier = Modifier.testTag("switch_play_entry_audio"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Exit audio switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "پخش صوت هنگام خروج",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "«صَدَقَ اللَّهُ الْعَلِيُّ الْعَظِيمُ» در هنگام ترک یا خروج از برنامه",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = settings.playExitAudio,
                            onCheckedChange = onTogglePlayExitAudio,
                            modifier = Modifier.testTag("switch_play_exit_audio"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Daily Quran Plan Notification / Reminder (Requirements #96, #97, #98, #99)
        item {
            var showTimePickerDialog by remember { mutableStateOf(false) }
            var showPermissionRationaleDialog by remember { mutableStateOf(false) }
            val context = LocalContext.current

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    onToggleDailyReminder(true)
                } else {
                    Toast.makeText(
                        context,
                        "اجازه ارسال اعلان صادر نشد. برای دریافت یادآوری روزانه، این مجوز را فعال نمایید.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_reminder_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "اعلان‌ها و یادآوری روزانه",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "یادآوری انجام برنامه مطالعه روزانه قرآن کریم",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "یادآوری برنامه روزانه",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (settings.dailyReminderEnabled) "فعال" else "غیرفعال",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (settings.dailyReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = settings.dailyReminderEnabled,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPermission) {
                                            onToggleDailyReminder(true)
                                        } else {
                                            showPermissionRationaleDialog = true
                                        }
                                    } else {
                                        onToggleDailyReminder(true)
                                    }
                                } else {
                                    onToggleDailyReminder(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("toggle_daily_reminder_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Time Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "زمان یادآوری روزانه",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "ساعت ارسال اعلان در هر روز",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        FilledTonalButton(
                            onClick = { showTimePickerDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("select_reminder_time_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d", settings.reminderHour, settings.reminderMinute).toPersianDigits(),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick presets chips (Max 3 suggestions)
                    Text(
                        text = "انتخاب سریع زمان:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            Triple(7, 0, "۰۷:۰۰"),
                            Triple(13, 0, "۱۳:۰۰"),
                            Triple(20, 0, "۲۰:۰۰")
                        )
                        presets.forEach { (h, m, label) ->
                            val isSelected = settings.reminderHour == h && settings.reminderMinute == m
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateReminderTime(h, m) },
                                label = {
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Text(text = label, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Explanatory note (Requirements #96, #97, #99)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "اعلان یادآوری فقط در صورتی ارسال می‌گردد که یک برنامه مطالعه فعال و متوقف‌نشده برای روز جاری داشته باشید. در صورت عدم وجود برنامه فعال، هیچ اعلانی ارسال نخواهد شد.",
                                style = MaterialTheme.typography.labelSmall,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Permission explanation dialog (Requirement #98)
            if (showPermissionRationaleDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionRationaleDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "اجازه ارسال اعلان یادآوری",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "برای دریافت یادآوری برنامه روزانه، اجازه ارسال اعلان را فعال کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showPermissionRationaleDialog = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onToggleDailyReminder(true)
                                }
                            }
                        ) {
                            Text(text = "اعطای دسترسی")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPermissionRationaleDialog = false }) {
                            Text(text = "انصراف")
                        }
                    }
                )
            }

            // Time Picker Dialog
            if (showTimePickerDialog) {
                PersianTimePickerDialog(
                    initialHour = settings.reminderHour,
                    initialMinute = settings.reminderMinute,
                    onConfirm = { h, m ->
                        onUpdateReminderTime(h, m)
                        showTimePickerDialog = false
                    },
                    onDismiss = { showTimePickerDialog = false }
                )
            }
        }

        // Data Management Card
        item {
            var showConfirmResetDialog by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "مدیریت داده‌ها و بازنشانی",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClearAllBookmarks() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "پاکسازی کلیه نشان‌ها", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "پاک کردن", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClearAllPlans() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "حذف کلیه برنامه‌های مطالعه", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "حذف", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClearAllChecklist() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "پاک کردن کلیه موارد چک‌لیست", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "پاک کردن", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showConfirmResetDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "بازگردانی تنظیمات به حالت اولیه", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "پیش‌فرض", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (showConfirmResetDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmResetDialog = false },
                    title = { Text("بازنشانی تنظیمات") },
                    text = { Text("آیا مایلید تمام اندازه‌های قلم، رنگ پوسته و ترجیحات قرائت به حالت پیش‌فرض برگردد؟") },
                    confirmButton = {
                        TextButton(onClick = {
                            onResetAllSettings()
                            showConfirmResetDialog = false
                        }) {
                            Text("تأیید بازنشانی")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmResetDialog = false }) {
                            Text("انصراف")
                        }
                    }
                )
            }
        }

        // About & Developer Attribution (درباره برنامه)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "درباره برنامه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "همراه قرآن",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "ساخته شده توسط سپهر قلعه سفیدی",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "توسعه‌دهنده: سپهر قلعه سفیدی",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "«همراه قرآن» یک نرم‌افزار جامع، انسان‌طراحی‌شده و متمرکز بر آرامش و تدبر در کلام الهی است. این اپلیکیشن با هدف تسهیل تلاوت پیوسته، برنامه‌ریزی ختم قرآن کریم، ثبت وظایف قرآنی روزانه و دسترسی سریع به تلاوت و ترجمه سلیس فارسی بدون هرگونه تبلیغات و مزاحمت بصری طراحی شده است.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "نسخه 2.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun PersianTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تنظیم زمان یادآوری روزانه",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ساعت و دقیقه مورد نظر برای دریافت اعلان روزانه را انتخاب کنید:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Time Display Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = String.format(Locale.US, "%02d", selectedHour).toPersianDigits(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " : ",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = String.format(Locale.US, "%02d", selectedMinute).toPersianDigits(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Hour Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ساعت (۰ تا ۲۳):",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedHour = if (selectedHour > 0) selectedHour - 1 else 23 }) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "کاهش ساعت")
                        }
                        Text(
                            text = selectedHour.toPersianDigits(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = { selectedHour = if (selectedHour < 23) selectedHour + 1 else 0 }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "افزایش ساعت")
                        }
                    }
                }

                // Minute Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "دقیقه (۰ تا ۵۹):",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedMinute = (selectedMinute - 5 + 60) % 60 }) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "کاهش دقیقه")
                        }
                        Text(
                            text = String.format("%02d", selectedMinute).toPersianDigits(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = { selectedMinute = (selectedMinute + 5) % 60 }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "افزایش دقیقه")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedHour, selectedMinute) }) {
                Text(text = "تأیید و ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "انصراف")
            }
        }
    )
}
