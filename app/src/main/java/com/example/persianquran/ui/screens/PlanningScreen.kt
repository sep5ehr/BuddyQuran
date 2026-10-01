package com.example.persianquran.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.persianquran.ui.components.AutoSelectOutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.persianquran.data.model.PlanDaySchedule
import com.example.persianquran.data.model.PlanMethod
import com.example.persianquran.data.model.ReadingPlan
import com.example.persianquran.data.surah.QuranMetadata
import com.example.persianquran.data.surah.QuranPageMetadata
import com.example.persianquran.data.surah.toPersianDigits
import com.example.persianquran.data.surah.PersianDateHelper
import java.util.Calendar

@Composable
fun PlanningScreen(
    activePlan: ReadingPlan?,
    allPlans: List<ReadingPlan>,
    activeSchedule: List<PlanDaySchedule>,
    todayItem: PlanDaySchedule?,
    onCreatePlan: (String, PlanMethod, Int, Int, Int, Int, Int, Int, Int, Int, Long) -> Unit,
    onActivatePlan: (Long) -> Unit,
    onTogglePause: (Long, Boolean) -> Unit,
    onDeletePlan: (Long) -> Unit,
    onToggleDayCompletion: (Long, Int, Boolean) -> Unit,
    onOpenReader: (Int, Int) -> Unit,
    onOpenPlanDay: ((PlanDaySchedule) -> Unit)? = null,
    onNavigateToSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAllPlansDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("planning_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Title & Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "برنامه‌ریزی مطالعه قرآن",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "همراه قرآن - برنامه‌ریزی منظم و ختم کلام وحی",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("btn_create_plan")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "برنامه جدید", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (activePlan == null) {
            // Empty State
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "هنوز برنامه‌ای برای مطالعه تنظیم نشده است",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "با ایجاد یک برنامه هدفمند (بر اساس صفحات، آیات یا سوره‌ها)، تلاوت روزانه قرآن را در زندگی خود جاری کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { showCreateDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "تنظیم اولین برنامه مطالعه")
                        }
                    }
                }
            }
        } else {
            // Active Plan Dashboard Card
            val completedDays = activeSchedule.count { it.isCompleted }
            val totalDays = activePlan.totalDays
            val progressPercent = if (totalDays > 0) ((completedDays.toFloat() / totalDays.toFloat()) * 100).toInt() else 0

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activePlan.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (activePlan.isPaused) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.errorContainer)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "متوقف شده",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "روش: ${activePlan.method.titlePersian} • مدت: ${totalDays.toPersianDigits()} روز",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Quick pause / resume button
                            IconButton(
                                onClick = { onTogglePause(activePlan.id, activePlan.isPaused) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = if (activePlan.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (activePlan.isPaused) "ادامه برنامه" else "توقف موقت",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Progress Bar & Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "پیشرفت کل: ${progressPercent.toPersianDigits()}٪",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${completedDays.toPersianDigits()} از ${totalDays.toPersianDigits()} روز انجام شد",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { if (totalDays > 0) completedDays.toFloat() / totalDays.toFloat() else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Schedule & All plans buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToSchedule,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "جدول زمان‌بندی")
                            }

                            OutlinedButton(
                                onClick = { showAllPlansDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "فهرست برنامه‌ها")
                            }
                        }
                    }
                }
            }

            // Today's Reading Section (مطالعه امروز)
            if (todayItem != null) {
                item {
                    Text(
                        text = "مطالعه امروز",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (todayItem.isCompleted)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (todayItem.isCompleted) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.primaryContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (todayItem.isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else {
                                            Text(
                                                text = todayItem.dayNumber.toPersianDigits(),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = todayItem.dateFormatted,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = todayItem.rangeDescription,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (onOpenPlanDay != null) {
                                            onOpenPlanDay(todayItem)
                                        } else {
                                            onOpenReader(todayItem.startSurahId, todayItem.startVerse)
                                        }
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "شروع مطالعه")
                                }

                                OutlinedButton(
                                    onClick = { onToggleDayCompletion(todayItem.planId, todayItem.dayNumber, todayItem.isCompleted) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = if (todayItem.isCompleted)
                                        ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                    else
                                        ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Icon(
                                        imageVector = if (todayItem.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = if (todayItem.isCompleted) "انجام شد" else "ثبت مطالعه")
                                }
                            }
                        }
                    }
                }
            }

            // Up next preview
            val upcomingDays = activeSchedule.filter { !it.isCompleted && it.dayNumber != todayItem?.dayNumber }.take(3)
            if (upcomingDays.isNotEmpty()) {
                item {
                    Text(
                        text = "روزهای پیش‌رو",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(upcomingDays) { day ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.dayNumber.toPersianDigits(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = day.dateFormatted,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = day.rangeDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(onClick = { onOpenReader(day.startSurahId, day.startVerse) }) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = "مطالعه",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Create Plan
    if (showCreateDialog) {
        CreatePlanDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, method, days, startS, startV, endS, endV, startP, endP, dailyT, startMillis ->
                onCreatePlan(title, method, days, startS, startV, endS, endV, startP, endP, dailyT, startMillis)
                showCreateDialog = false
            }
        )
    }

    // Dialog: Manage All Plans
    if (showAllPlansDialog) {
        ManagePlansDialog(
            plans = allPlans,
            activePlanId = activePlan?.id,
            onSelectPlan = { onActivatePlan(it) },
            onDeletePlan = { onDeletePlan(it) },
            onDismiss = { showAllPlansDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        method: PlanMethod,
        calculatedTotalDays: Int,
        startSurahId: Int,
        startVerse: Int,
        endSurahId: Int,
        endVerse: Int,
        startPage: Int,
        endPage: Int,
        dailyTarget: Int,
        startDateMillis: Long
    ) -> Unit
) {
    var title by remember { mutableStateOf("برنامه مطالعه قرآن کریم") }
    var selectedMethod by remember { mutableStateOf(PlanMethod.PAGES) }

    var startSurahId by remember { mutableIntStateOf(1) }
    var startVerse by remember { mutableIntStateOf(1) }
    var endSurahId by remember { mutableIntStateOf(114) }
    var endVerse by remember { mutableIntStateOf(6) }

    var startPage by remember { mutableIntStateOf(1) }
    var endPage by remember { mutableIntStateOf(604) }

    // User defines: How much to read per day
    var dailyTarget by remember { mutableIntStateOf(20) }
    var customDailyTargetText by remember { mutableStateOf("20") }

    // User defines: Start date (default today 00:00:00)
    var startDateMillis by remember {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        mutableLongStateOf(cal.timeInMillis)
    }

    var showSchedulePreview by remember { mutableStateOf(false) }

    // Derived state: Total Units, Unit Name, Calculated Duration, and End Date
    val safeDailyTarget = maxOf(1, dailyTarget)
    val (totalUnits, unitName, calculatedDays) = remember(
        selectedMethod, startPage, endPage,
        startSurahId, startVerse, endSurahId, endVerse, safeDailyTarget
    ) {
        when (selectedMethod) {
            PlanMethod.PAGES -> {
                val total = QuranPageMetadata.calculateTotalPages(startPage, endPage)
                val days = (total + safeDailyTarget - 1) / safeDailyTarget
                Triple(total, "صفحه", maxOf(1, days))
            }
            PlanMethod.SURAHS -> {
                val sId = startSurahId.coerceIn(1, 114)
                val eId = endSurahId.coerceIn(sId, 114)
                val total = maxOf(1, eId - sId + 1)
                val days = (total + safeDailyTarget - 1) / safeDailyTarget
                Triple(total, "سوره", maxOf(1, days))
            }
            PlanMethod.VERSES -> {
                val gStart = QuranMetadata.getGlobalVerseIndex(startSurahId, startVerse)
                val gEnd = QuranMetadata.getGlobalVerseIndex(endSurahId, endVerse)
                val total = maxOf(1, gEnd - gStart + 1)
                val days = (total + safeDailyTarget - 1) / safeDailyTarget
                Triple(total, "آیه", maxOf(1, days))
            }
        }
    }

    // End date derived from: Start Date + Calculated Days - 1 (inclusive start day)
    val oneDayMillis = 24L * 60L * 60L * 1000L
    val calculatedEndDateMillis = remember(startDateMillis, calculatedDays) {
        startDateMillis + ((calculatedDays - 1) * oneDayMillis)
    }
    val startDateFormatted = remember(startDateMillis) {
        PersianDateHelper.formatPersianDateFull(startDateMillis)
    }
    val endDateFormatted = remember(calculatedEndDateMillis) {
        PersianDateHelper.formatPersianDateFull(calculatedEndDateMillis)
    }

    val remainingOnLastDay = remember(totalUnits, safeDailyTarget) {
        val rem = totalUnits % safeDailyTarget
        if (rem == 0) safeDailyTarget else rem
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تنظیم برنامه مطالعه جدید",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title Field
                AutoSelectOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نام برنامه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Title Suggestions with intelligent presets (Max 3 suggestions)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val suggestions = listOf(
                        "ختم کامل قرآن (۲۰ صفحه در روز)",
                        "ختم ۶۰ روزه (۱۰ صفحه در روز)",
                        "ختم جزء ۳۰ قرآن"
                    )
                    items(suggestions) { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    title = s
                                    if (s.contains("۲۰ صفحه")) {
                                        selectedMethod = PlanMethod.PAGES
                                        startPage = 1
                                        endPage = 604
                                        dailyTarget = 20
                                        customDailyTargetText = "20"
                                    } else if (s.contains("۱۰ صفحه")) {
                                        selectedMethod = PlanMethod.PAGES
                                        startPage = 1
                                        endPage = 604
                                        dailyTarget = 10
                                        customDailyTargetText = "10"
                                    } else if (s.contains("جزء ۳۰")) {
                                        selectedMethod = PlanMethod.PAGES
                                        startPage = 582
                                        endPage = 604
                                        dailyTarget = 2
                                        customDailyTargetText = "2"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = s, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Planning Method Selection
                Text(
                    text = "روش برنامه‌ریزی:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PlanMethod.entries.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedMethod = method
                                    when (method) {
                                        PlanMethod.PAGES -> {
                                            dailyTarget = 20
                                            customDailyTargetText = "20"
                                        }
                                        PlanMethod.SURAHS -> {
                                            dailyTarget = 2
                                            customDailyTargetText = "2"
                                        }
                                        PlanMethod.VERSES -> {
                                            dailyTarget = 20
                                            customDailyTargetText = "20"
                                        }
                                        else -> {}
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (selectedMethod == method) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (selectedMethod == method) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = method.titlePersian,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selectedMethod == method) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = method.descriptionPersian,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // Method Specific Range Inputs
                when (selectedMethod) {
                    PlanMethod.PAGES -> {
                        Text(
                            text = "صفحات تلاوت:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AutoSelectOutlinedTextField(
                                value = startPage.toString(),
                                onValueChange = { startPage = (it.toIntOrNull() ?: 1).coerceIn(1, 604) },
                                label = { Text("صفحه آغاز (۱-۶۰۴)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            AutoSelectOutlinedTextField(
                                value = endPage.toString(),
                                onValueChange = { endPage = (it.toIntOrNull() ?: 604).coerceIn(1, 604) },
                                label = { Text("صفحه پایان (۱-۶۰۴)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        // Quick Presets for Pages (Max 3 suggestions)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple(1, 604, "کل قرآن"),
                                Triple(1, 302, "نیمه اول"),
                                Triple(582, 604, "جزء ۳۰")
                            ).forEach { (s, e, lbl) ->
                                FilterChip(
                                    selected = startPage == s && endPage == e,
                                    onClick = {
                                        startPage = s
                                        endPage = e
                                    },
                                    label = { Text(lbl, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    PlanMethod.SURAHS -> {
                        Text(
                            text = "سوره‌های تلاوت:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AutoSelectOutlinedTextField(
                                value = startSurahId.toString(),
                                onValueChange = { startSurahId = (it.toIntOrNull() ?: 1).coerceIn(1, 114) },
                                label = { Text("سوره آغاز (۱-۱۱۴)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            AutoSelectOutlinedTextField(
                                value = endSurahId.toString(),
                                onValueChange = { endSurahId = (it.toIntOrNull() ?: 114).coerceIn(startSurahId, 114) },
                                label = { Text("سوره پایان (۱-۱۱۴)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        // Quick presets for Surahs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple(1, 114, "کل سوره‌ها"),
                                Triple(78, 114, "جزء ۳۰"),
                                Triple(109, 114, "چهار قل")
                            ).forEach { (s, e, lbl) ->
                                FilterChip(
                                    selected = startSurahId == s && endSurahId == e,
                                    onClick = {
                                        startSurahId = s
                                        endSurahId = e
                                    },
                                    label = { Text(lbl, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    PlanMethod.VERSES -> {
                        Text(
                            text = "آیات تلاوت:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AutoSelectOutlinedTextField(
                                value = startSurahId.toString(),
                                onValueChange = { startSurahId = (it.toIntOrNull() ?: 1).coerceIn(1, 114) },
                                label = { Text("سوره آغاز") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            AutoSelectOutlinedTextField(
                                value = startVerse.toString(),
                                onValueChange = { startVerse = (it.toIntOrNull() ?: 1).coerceAtLeast(1) },
                                label = { Text("آیه آغاز") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AutoSelectOutlinedTextField(
                                value = endSurahId.toString(),
                                onValueChange = { endSurahId = (it.toIntOrNull() ?: 114).coerceIn(startSurahId, 114) },
                                label = { Text("سوره پایان") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            AutoSelectOutlinedTextField(
                                value = endVerse.toString(),
                                onValueChange = { endVerse = (it.toIntOrNull() ?: 1).coerceAtLeast(1) },
                                label = { Text("آیه پایان") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        // Quick surah ranges (Max 3 suggestions)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                listOf(36, 1, 36, 83, "سوره یس"),
                                listOf(67, 1, 67, 30, "سوره ملک"),
                                listOf(18, 1, 18, 110, "سوره کهف")
                            ).forEach { item ->
                                val sId = item[0] as Int
                                val sV = item[1] as Int
                                val eId = item[2] as Int
                                val eV = item[3] as Int
                                val lbl = item[4] as String
                                FilterChip(
                                    selected = startSurahId == sId && startVerse == sV && endSurahId == eId && endVerse == eV,
                                    onClick = {
                                        startSurahId = sId
                                        startVerse = sV
                                        endSurahId = eId
                                        endVerse = eV
                                    },
                                    label = { Text(lbl, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Daily Target (How much to read per day - User defined)
                Text(
                    text = "سهمیه مطالعه روزانه ($unitName در روز):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                val targetPresets = when (selectedMethod) {
                    PlanMethod.PAGES -> listOf(1, 2, 5)
                    PlanMethod.SURAHS -> listOf(1, 2, 3)
                    PlanMethod.VERSES -> listOf(10, 20, 50)
                    else -> listOf(1, 2, 5)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    targetPresets.forEach { targetVal ->
                        FilterChip(
                            selected = dailyTarget == targetVal,
                            onClick = {
                                dailyTarget = targetVal
                                customDailyTargetText = targetVal.toString()
                            },
                            label = { Text("${targetVal.toPersianDigits()} $unitName") }
                        )
                    }
                }

                AutoSelectOutlinedTextField(
                    value = customDailyTargetText,
                    onValueChange = {
                        customDailyTargetText = it
                        val parsed = it.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            dailyTarget = parsed
                        }
                    },
                    label = { Text("مقدار مطالعه در هر روز ($unitName)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Start Date Selection
                Text(
                    text = "تاریخ آغاز برنامه:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val calToday = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val todayMillis = calToday.timeInMillis
                    val tomorrowMillis = todayMillis + oneDayMillis

                    // Saturday preset
                    val saturdayMillis = remember(todayMillis) {
                        val c = Calendar.getInstance().apply { timeInMillis = todayMillis }
                        while (c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                            c.add(Calendar.DAY_OF_YEAR, 1)
                        }
                        c.timeInMillis
                    }

                    FilterChip(
                        selected = startDateMillis == todayMillis,
                        onClick = { startDateMillis = todayMillis },
                        label = { Text("امروز") }
                    )
                    FilterChip(
                        selected = startDateMillis == tomorrowMillis,
                        onClick = { startDateMillis = tomorrowMillis },
                        label = { Text("فردا") }
                    )
                    FilterChip(
                        selected = startDateMillis == saturdayMillis,
                        onClick = { startDateMillis = saturdayMillis },
                        label = { Text("شنبه آینده") }
                    )
                }

                // =========================================================================
                // AUTOMATIC CALCULATION RESULT CARD (NON-EDITABLE)
                // "The user defines: What to read and How much to read per day.
                //  The application determines: How many days the plan takes and When the plan ends."
                // =========================================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "نتیجه محاسبه خودکار و هوشمند برنامه",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "کل حجم محتوای انتخابی:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${totalUnits.toPersianDigits()} $unitName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "سهمیه روزانه شما:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${safeDailyTarget.toPersianDigits()} $unitName در هر روز",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "مدت زمان کل برنامه (محاسبه خودکار):",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${calculatedDays.toPersianDigits()} روز",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "تاریخ آغاز برنامه:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = startDateFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "تاریخ پایان برنامه (محاسبه خودکار):",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = endDateFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Explanatory badge for final day
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "روز ${calculatedDays.toPersianDigits()} (پایان برنامه) شامل ${remainingOnLastDay.toPersianDigits()} $unitName باقیمانده است و هیچ روز خالی ایجاد نمی‌گردد.",
                                    style = MaterialTheme.typography.labelSmall,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Toggle Preview of Day Schedule
                        TextButton(
                            onClick = { showSchedulePreview = !showSchedulePreview },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = if (showSchedulePreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showSchedulePreview) "بستن پیش‌نمایش جدول روزانه" else "مشاهده پیش‌نمایش جدول روزها",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        if (showSchedulePreview) {
                            val previewItems = remember(
                                selectedMethod, calculatedDays, startDateMillis,
                                startSurahId, startVerse, endSurahId, endVerse,
                                startPage, endPage, safeDailyTarget
                            ) {
                                QuranMetadata.calculateScheduleForPlan(
                                    planId = 0L,
                                    method = selectedMethod,
                                    totalDays = calculatedDays,
                                    startDateMillis = startDateMillis,
                                    startSurahId = startSurahId,
                                    startVerse = startVerse,
                                    endSurahId = endSurahId,
                                    endVerse = endVerse,
                                    startPageInput = startPage,
                                    endPageInput = endPage,
                                    dailyTarget = safeDailyTarget
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val displayedPreview = if (previewItems.size <= 4) {
                                    previewItems
                                } else {
                                    listOf(previewItems.first(), previewItems[1], previewItems.last())
                                }

                                displayedPreview.forEach { item ->
                                    val isLast = item.dayNumber == calculatedDays
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isLast) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "روز ${item.dayNumber.toPersianDigits()}${if (isLast) " (روز پایانی)" else ""}:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = item.rangeDescription,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        title,
                        selectedMethod,
                        calculatedDays,
                        startSurahId,
                        startVerse,
                        endSurahId,
                        endVerse,
                        startPage,
                        endPage,
                        safeDailyTarget,
                        startDateMillis
                    )
                },
                modifier = Modifier.testTag("submit_create_plan_button")
            ) {
                Text("ثبت و شروع برنامه (${calculatedDays.toPersianDigits()} روزه)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun ManagePlansDialog(
    plans: List<ReadingPlan>,
    activePlanId: Long?,
    onSelectPlan: (Long) -> Unit,
    onDeletePlan: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "مدیریت برنامه‌های مطالعه",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (plans.isEmpty()) {
                Text("هنوز هیچ برنامه‌ای ذخیره نشده است.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(plans) { plan ->
                        val isActive = plan.id == activePlanId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectPlan(plan.id)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plan.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${plan.totalDays.toPersianDigits()} روز • ${plan.method.titlePersian}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isActive) {
                                        Text(
                                            text = "فعال",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeletePlan(plan.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )
}
