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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.persianquran.data.model.QuranFont
import com.example.persianquran.data.model.Surah
import com.example.persianquran.data.model.toFontFamily
import com.example.persianquran.data.surah.QuranHizb
import com.example.persianquran.data.surah.QuranHizbMetadata
import com.example.persianquran.data.surah.QuranMetadata
import com.example.persianquran.data.surah.QuranPageMetadata
import com.example.persianquran.data.surah.toPersianDigits

@Composable
fun SurahListScreen(
    surahs: List<Surah>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onOpenSurah: (Int) -> Unit,
    onPlaySurah: (Surah) -> Unit,
    onOpenSurahVerse: ((Int, Int) -> Unit)? = null,
    onOpenPage: ((Int) -> Unit)? = null,
    quranFont: QuranFont = QuranFont.OLD,
    modifier: Modifier = Modifier
) {
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0: سوره‌ها, 1: جزءها, 2: حزب‌ها
    var selectedFilter by remember { mutableStateOf("همه") }

    val filteredSurahs = remember(surahs, searchQuery, selectedFilter) {
        surahs.filter { surah ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim()
                surah.namePersian.contains(q, ignoreCase = true) ||
                        surah.nameArabic.contains(q, ignoreCase = true) ||
                        surah.id.toString() == q ||
                        surah.id.toPersianDigits() == q
            }
            val matchesFilter = when (selectedFilter) {
                "مکی" -> surah.revelationType == "مکی"
                "مدنی" -> surah.revelationType == "مدنی"
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    val filteredHizbs = remember(searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) {
            QuranHizbMetadata.ALL_HIZBS
        } else {
            QuranHizbMetadata.ALL_HIZBS.filter { h ->
                h.hizbNumber.toString() == q ||
                        h.hizbNumber.toPersianDigits() == q ||
                        h.startSurahNamePersian.contains(q, ignoreCase = true) ||
                        h.startSurahNameArabic.contains(q, ignoreCase = true) ||
                        h.startVerseSnippet.contains(q, ignoreCase = true) ||
                        h.juzNumber.toString() == q ||
                        h.juzNumber.toPersianDigits() == q
            }
        }
    }

    val juzList = remember { (1..30).toList() }
    val filteredJuz = remember(searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) {
            juzList
        } else {
            juzList.filter { j ->
                j.toString() == q || j.toPersianDigits() == q
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("surah_list_screen")
    ) {
        // Main Tab Switcher: سوره‌ها / جزءها / حزب‌ها
        TabRow(
            selectedTabIndex = selectedMainTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedMainTab == 0,
                onClick = { selectedMainTab = 0 },
                text = {
                    Text(
                        text = "سوره‌ها (${surahs.size.toPersianDigits()})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selectedMainTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedMainTab == 1,
                onClick = { selectedMainTab = 1 },
                text = {
                    Text(
                        text = "جزءها (۳۰)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selectedMainTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedMainTab == 2,
                onClick = { selectedMainTab = 2 },
                text = {
                    Text(
                        text = "حزب‌ها (۶۰)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selectedMainTab == 2) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("surah_search_input"),
            placeholder = {
                Text(
                    text = when (selectedMainTab) {
                        0 -> "جستجوی سوره (نام یا شماره سوره)..."
                        1 -> "جستجوی جزء (شماره جزء ۱ تا ۳۰)..."
                        else -> "جستجوی حزب (شماره ۱ تا ۶۰ یا نام سوره)..."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "جستجو")
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
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

        when (selectedMainTab) {
            0 -> {
                // Tab 0: Surahs
                val makkiCount = remember(surahs) { surahs.count { it.revelationType.contains("مکی") } }
                val madaniCount = remember(surahs) { surahs.count { it.revelationType.contains("مدنی") } }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("همه", "مکی", "مدنی")
                    items(filters) { f ->
                        val isSelected = selectedFilter == f
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = f },
                            label = {
                                Text(
                                    text = when (f) {
                                        "همه" -> "همه سوره‌ها (${surahs.size.toPersianDigits()})"
                                        "مکی" -> "سوره‌های مکی (${makkiCount.toPersianDigits()})"
                                        "مدنی" -> "سوره‌های مدنی (${madaniCount.toPersianDigits()})"
                                        else -> f
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (filteredSurahs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "سوره‌ای یافت نشد.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "عبارت دیگری را امتحان کنید یا فیلتر را تغییر دهید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSurahs, key = { it.id }) { surah ->
                            SurahListItemCard(
                                surah = surah,
                                onClick = { onOpenSurah(surah.id) },
                                onPlayClick = { onPlaySurah(surah) },
                                quranFont = quranFont
                            )
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: Juz' (30 Ajza)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredJuz, key = { it }) { juzNum ->
                        val startPage = (juzNum - 1) * 20 + 2
                        val safeStartPage = if (juzNum == 1) 1 else startPage
                        val endPage = if (juzNum == 30) 604 else safeStartPage + 19
                        val boundary = QuranPageMetadata.getPageBoundary(safeStartPage)
                        val surah = QuranMetadata.getSurahById(boundary.startSurahId)

                        JuzListItemCard(
                            juzNumber = juzNum,
                            startSurahName = surah?.namePersian ?: "سوره ${boundary.startSurahId}",
                            startVerseNumber = boundary.startVerse,
                            startPage = safeStartPage,
                            endPage = endPage,
                            onClick = {
                                if (onOpenPage != null) {
                                    onOpenPage(safeStartPage)
                                } else {
                                    onOpenSurah(boundary.startSurahId)
                                }
                            }
                        )
                    }
                }
            }

            2 -> {
                // Tab 2: Ahzab (60 Hizbs)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredHizbs, key = { it.hizbNumber }) { hizb ->
                        HizbListItemCard(
                            hizb = hizb,
                            quranFont = quranFont,
                            onClick = {
                                if (onOpenSurahVerse != null) {
                                    onOpenSurahVerse(hizb.startSurahId, hizb.startVerseNumber)
                                } else if (onOpenPage != null) {
                                    onOpenPage(hizb.startPageNumber)
                                } else {
                                    onOpenSurah(hizb.startSurahId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JuzListItemCard(
    juzNumber: Int,
    startSurahName: String,
    startVerseNumber: Int,
    startPage: Int,
    endPage: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("juz_card_$juzNumber"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = juzNumber.toPersianDigits(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "جزء ${juzNumber.toPersianDigits()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "آغاز: سوره $startSurahName، آیه ${startVerseNumber.toPersianDigits()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "صفحه ${startPage.toPersianDigits()} تا ${endPage.toPersianDigits()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun HizbListItemCard(
    hizb: QuranHizb,
    quranFont: QuranFont = QuranFont.OLD,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("hizb_card_${hizb.hizbNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hizb.hizbNumber.toPersianDigits(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "حزب ${hizb.hizbNumber.toPersianDigits()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "جزء ${hizb.juzNumber.toPersianDigits()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "سوره ${hizb.startSurahNamePersian} (${hizb.startSurahNameArabic}) • آیه ${hizb.startVerseNumber.toPersianDigits()} • صفحه ${hizb.startPageNumber.toPersianDigits()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Beginning words of the Hizb in Uthmani font
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = hizb.startVerseSnippet,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = quranFont.toFontFamily(),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Right
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun SurahItemCard(
    surah: Surah,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    quranFont: QuranFont = QuranFont.OLD,
    modifier: Modifier = Modifier
) {
    SurahListItemCard(surah, onClick, onPlayClick, quranFont, modifier)
}

@Composable
fun SurahListItemCard(
    surah: Surah,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    quranFont: QuranFont = QuranFont.OLD,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("surah_card_${surah.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right: Surah number badge & Persian details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Surah Number Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.id.toPersianDigits(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = surah.namePersian,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = surah.revelationType,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${surah.versesCount.toPersianDigits()} آیه • ${surah.startPage.toPersianDigits()} صفحه",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Left: Arabic Calligraphy name & Quick play button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = surah.nameArabic,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = quranFont.toFontFamily(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Left
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "پخش سوره",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
