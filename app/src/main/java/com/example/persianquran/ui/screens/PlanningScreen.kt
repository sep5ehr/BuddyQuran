package com.example.persianquran.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.persianquran.data.model.PlanDaySchedule
import com.example.persianquran.data.model.PlanMethod
import com.example.persianquran.data.model.ReadingPlan
import com.example.persianquran.data.surah.QuranMetadata
import com.example.persianquran.data.surah.toPersianDigits

@Composable
fun PlanningScreen(
    activePlan: ReadingPlan?,
    allPlans: List<ReadingPlan>,
    activeSchedule: List<PlanDaySchedule>,
    todayItem: PlanDaySchedule?,
    onCreatePlan: (String, PlanMethod, Int, Int, Int, Int, Int, Int, Int, Int) -> Unit,
    onActivatePlan: (Long) -> Unit,
    onTogglePause: (Long, Boolean) -> Unit,
    onDeletePlan: (Long) -> Unit,
    onToggleDayCompletion: (Long, Int, Boolean) -> Unit,
    onOpenReader: (Int, Int) -> Unit,
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
                            text = "با ایجاد یک برنامه هدفمند (بر اساس صفحات، آیات، سوره‌ها یا محدوده دلخواه)، تلاوت روزانه قرآن را در زندگی خود جاری کنید.",
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
                                    onClick = { onOpenReader(todayItem.startSurahId, todayItem.startVerse) },
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
            onConfirm = { title, method, days, startS, startV, endS, endV, startP, endP, dailyT ->
                onCreatePlan(title, method, days, startS, startV, endS, endV, startP, endP, dailyT)
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
    onConfirm: (String, PlanMethod, Int, Int, Int, Int, Int, Int, Int, Int) -> Unit
) {
    var title by remember { mutableStateOf("برنامه مطالعه قرآن کریم") }
    var selectedMethod by remember { mutableStateOf(PlanMethod.PAGES) }
    var durationDays by remember { mutableIntStateOf(30) }
    var customDaysText by remember { mutableStateOf("30") }

    var startSurahId by remember { mutableIntStateOf(1) }
    var startVerse by remember { mutableIntStateOf(1) }
    var endSurahId by remember { mutableIntStateOf(114) }
    var endVerse by remember { mutableIntStateOf(6) }

    var startPage by remember { mutableIntStateOf(1) }
    var endPage by remember { mutableIntStateOf(604) }
    var dailyTarget by remember { mutableIntStateOf(1) }

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
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نام برنامه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Title Suggestions
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val suggestions = listOf("ختم ۳۰ روزه قرآن", "ختم ۶۰ روزه", "مطالعه جزء به جزء", "مرور سوره بقره")
                    items(suggestions) { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    title = s
                                    if (s.contains("۳۰")) {
                                        durationDays = 30
                                        customDaysText = "30"
                                    } else if (s.contains("۶۰")) {
                                        durationDays = 60
                                        customDaysText = "60"
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
                                .clickable { selectedMethod = method }
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

                // Method Specific Inputs
                when (selectedMethod) {
                    PlanMethod.PAGES -> {
                        Text(
                            text = "محدوده صفحات:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = startPage.toString(),
                                onValueChange = { startPage = (it.toIntOrNull() ?: 1).coerceIn(1, 604) },
                                label = { Text("صفحه آغاز (۱-۶۰۴)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = endPage.toString(),
                                onValueChange = { endPage = (it.toIntOrNull() ?: 604).coerceIn(startPage, 604) },
                                label = { Text("صفحه پایان (۱-۶۰۴)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Text(
                            text = "تعداد صفحات روزانه:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 4, 10, 20).forEach { p ->
                                FilterChip(
                                    selected = dailyTarget == p,
                                    onClick = { dailyTarget = p },
                                    label = { Text("${p.toPersianDigits()} صفحه") }
                                )
                            }
                        }
                    }

                    PlanMethod.SURAHS -> {
                        Text(
                            text = "محدوده سوره‌ها:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = startSurahId.toString(),
                                onValueChange = { startSurahId = (it.toIntOrNull() ?: 1).coerceIn(1, 114) },
                                label = { Text("سوره آغاز (۱-۱۱۴)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = endSurahId.toString(),
                                onValueChange = { endSurahId = (it.toIntOrNull() ?: 114).coerceIn(startSurahId, 114) },
                                label = { Text("سوره پایان (۱-۱۱۴)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Text(
                            text = "تعداد سوره در هر روز:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3, 5).forEach { s ->
                                FilterChip(
                                    selected = dailyTarget == s,
                                    onClick = { dailyTarget = s },
                                    label = { Text("${s.toPersianDigits()} سوره") }
                                )
                            }
                        }
                    }

                    PlanMethod.RANGE -> {
                        Text(
                            text = "محدوده تلاوت (سوره و آیه):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = startSurahId.toString(),
                                onValueChange = { startSurahId = (it.toIntOrNull() ?: 1).coerceIn(1, 114) },
                                label = { Text("سوره آغاز") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
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
                            OutlinedTextField(
                                value = endSurahId.toString(),
                                onValueChange = { endSurahId = (it.toIntOrNull() ?: 114).coerceIn(startSurahId, 114) },
                                label = { Text("سوره پایان") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = endVerse.toString(),
                                onValueChange = { endVerse = (it.toIntOrNull() ?: 1).coerceAtLeast(1) },
                                label = { Text("آیه پایان") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }

                    PlanMethod.VERSES -> {
                        Text(
                            text = "تعداد آیات روزانه:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 20, 50).forEach { v ->
                                FilterChip(
                                    selected = dailyTarget == v,
                                    onClick = { dailyTarget = v },
                                    label = { Text("${v.toPersianDigits()} آیه") }
                                )
                            }
                        }
                    }
                }

                // Duration Selection for Pages / Range / Surahs
                Text(
                    text = "مدت زمان برنامه (روز):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 60, 120).forEach { days ->
                        FilterChip(
                            selected = durationDays == days,
                            onClick = {
                                durationDays = days
                                customDaysText = days.toString()
                            },
                            label = { Text("${days.toPersianDigits()} روز") }
                        )
                    }
                }

                OutlinedTextField(
                    value = customDaysText,
                    onValueChange = {
                        customDaysText = it
                        val parsed = it.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            durationDays = parsed
                        }
                    },
                    label = { Text("تعداد روز دلخواه") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        title,
                        selectedMethod,
                        durationDays,
                        startSurahId,
                        startVerse,
                        endSurahId,
                        endVerse,
                        startPage,
                        endPage,
                        dailyTarget
                    )
                }
            ) {
                Text("ثبت و شروع برنامه")
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
