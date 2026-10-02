package com.example.persianquran.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.persianquran.ui.components.AutoSelectOutlinedTextField
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.persianquran.data.model.ChecklistItem
import com.example.persianquran.data.model.PlanDaySchedule
import com.example.persianquran.data.model.ReadingPlan
import com.example.persianquran.data.surah.toPersianDigits
import java.util.Calendar

enum class DayStatus(val label: String) {
    COMPLETED("انجام شده"),
    TODAY("امروز"),
    OVERDUE("عقب‌افتاده"),
    PAUSED("توقف موقت برنامه"),
    FUTURE("آینده")
}

enum class ScheduleFilter(val title: String) {
    UNCOMPLETED("انجام نشده"),
    TODAY("امروز"),
    ALL("همه روزها"),
    OVERDUE("عقب‌افتاده"),
    COMPLETED("انجام شده")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    activePlan: ReadingPlan?,
    schedule: List<PlanDaySchedule>,
    items: List<ChecklistItem>,
    allPlans: List<ReadingPlan> = emptyList(),
    onActivatePlan: ((Long) -> Unit)? = null,
    onToggleDayCompletion: (Long, Int, Boolean) -> Unit,
    onOpenReader: (Int, Int) -> Unit,
    onOpenPlanDay: ((PlanDaySchedule) -> Unit)? = null,
    onAddItem: (String, String) -> Unit,
    onUpdateItem: (Long, String, String) -> Unit,
    onToggleItem: (Long, Boolean) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onResetDaily: () -> Unit,
    onClearCompleted: () -> Unit,
    onClearAll: () -> Unit,
    onNavigateToPlanning: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(ScheduleFilter.UNCOMPLETED) }
    var showSwitchPlanDialog by remember { mutableStateOf(false) }

    val startOfTodayMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val startOfTomorrowMillis = remember(startOfTodayMillis) {
        startOfTodayMillis + (24L * 60L * 60L * 1000L)
    }

    fun getDayStatus(day: PlanDaySchedule): DayStatus {
        if (day.isCompleted) return DayStatus.COMPLETED
        if (activePlan?.isPaused == true) return DayStatus.PAUSED
        if (day.dateMillis in startOfTodayMillis until startOfTomorrowMillis) return DayStatus.TODAY
        if (day.dateMillis < startOfTodayMillis) return DayStatus.OVERDUE
        return DayStatus.FUTURE
    }

    val filteredSchedule by remember(schedule, selectedFilter, activePlan) {
        derivedStateOf {
            when (selectedFilter) {
                ScheduleFilter.ALL -> schedule
                ScheduleFilter.TODAY -> schedule.filter {
                    it.dateMillis in startOfTodayMillis until startOfTomorrowMillis ||
                    (!it.isCompleted && schedule.indexOf(it) == schedule.indexOfFirst { d -> !d.isCompleted })
                }
                ScheduleFilter.UNCOMPLETED -> schedule.filter { !it.isCompleted }
                ScheduleFilter.OVERDUE -> schedule.filter { !it.isCompleted && it.dateMillis < startOfTodayMillis }
                ScheduleFilter.COMPLETED -> schedule.filter { it.isCompleted }
            }
        }
    }

    val completedScheduleCount = schedule.count { it.isCompleted }
    val totalScheduleCount = schedule.size
    val scheduleProgress = if (totalScheduleCount > 0) (completedScheduleCount.toFloat() / totalScheduleCount.toFloat()) else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("checklist_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "جدول برنامه مطالعه",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "پیگیری جامع روزهای تلاوت و برنامه ختم",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Active Plan Quick Switcher (تغییر برنامه فعال)
        if (allPlans.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تغییر برنامه فعال:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (allPlans.size > 1 && onActivatePlan != null) {
                                TextButton(
                                    onClick = { showSwitchPlanDialog = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "مشاهده همه (${allPlans.size.toPersianDigits()})",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(allPlans, key = { it.id }) { plan ->
                                val isCurrent = plan.id == activePlan?.id
                                FilterChip(
                                    selected = isCurrent,
                                    onClick = {
                                        if (!isCurrent && onActivatePlan != null) {
                                            onActivatePlan(plan.id)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = plan.title + if (isCurrent) " (فعال)" else "",
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (isCurrent) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                            item {
                                OutlinedButton(
                                    onClick = onNavigateToPlanning,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "برنامه جدید", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Full Plan Schedule
        if (activePlan == null || schedule.isEmpty()) {
                // Empty state when no plan is active
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "هیچ برنامه مطالعه‌ای فعال نیست",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "برای مشاهده جدول کامل روزهای مطالعه، ابتدا یک برنامه ختم یا مطالعه تنظیم نمایید.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                                Spacer(modifier = Modifier.height(18.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onNavigateToPlanning,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "ایجاد برنامه جدید")
                                }
                                if (allPlans.isNotEmpty() && onActivatePlan != null) {
                                    OutlinedButton(
                                        onClick = { showSwitchPlanDialog = true },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "تغییر برنامه")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Active Plan Header Card with Overall Progress
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activePlan.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "روش: ${activePlan.method.titlePersian}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (allPlans.isNotEmpty() && onActivatePlan != null) {
                                        OutlinedButton(
                                            onClick = { showSwitchPlanDialog = true },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SwapHoriz,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "تغییر برنامه",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (activePlan.isPaused) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "توقف موقت برنامه",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "پیشرفت کل: ${completedScheduleCount.toPersianDigits()} از ${totalScheduleCount.toPersianDigits()} روز",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${((scheduleProgress * 100).toInt()).toPersianDigits()}٪",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { scheduleProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Filter Chips (همه، امروز، انجام نشده، عقب‌افتاده، انجام شده)
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(ScheduleFilter.entries) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter.title) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Schedule Days List
                if (filteredSchedule.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedFilter == ScheduleFilter.UNCOMPLETED)
                                        "تمام برنامه‌های تا به امروز با موفقیت انجام شده‌اند."
                                    else
                                        "موردی با این فیلتر یافت نشد.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredSchedule, key = { it.id }) { day ->
                        val status = getDayStatus(day)
                        ScheduleDayCard(
                            day = day,
                            status = status,
                            onToggleCompletion = { onToggleDayCompletion(day.planId, day.dayNumber, day.isCompleted) },
                            onStartReading = {
                                if (onOpenPlanDay != null) {
                                    onOpenPlanDay(day)
                                } else {
                                    onOpenReader(day.startSurahId, day.startVerse)
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showSwitchPlanDialog) {
            SwitchPlanDialog(
                allPlans = allPlans,
                activePlanId = activePlan?.id,
                onDismiss = { showSwitchPlanDialog = false },
                onSelectPlan = { planId ->
                    onActivatePlan?.invoke(planId)
                },
                onNavigateToPlanning = {
                    showSwitchPlanDialog = false
                    onNavigateToPlanning()
                }
            )
        }
    }

@Composable
fun ScheduleDayCard(
    day: PlanDaySchedule,
    status: DayStatus,
    onToggleCompletion: () -> Unit,
    onStartReading: () -> Unit
) {
    val isToday = status == DayStatus.TODAY
    val isCompleted = status == DayStatus.COMPLETED
    val isOverdue = status == DayStatus.OVERDUE

    val cardBorder = when {
        isToday -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
        isOverdue -> Modifier.border(1.dp, Color(0xFFE65100).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        else -> Modifier
    }

    val cardBg = when {
        isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        isToday -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardBorder),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 2.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Day Number + Status Badge + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) MaterialTheme.colorScheme.primary
                                else if (isToday) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayNumber.toPersianDigits(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) MaterialTheme.colorScheme.onPrimary
                            else if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "روز ${day.dayNumber.toPersianDigits()}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = day.dateFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                StatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Range Description (Surah / Verses / Pages)
            Text(
                text = day.rangeDescription,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: "شروع مطالعه" & "انجام شد"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartReading,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "شروع مطالعه", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onToggleCompletion,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                        contentDescription = null,
                        tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCompleted) "انجام شد" else "ثبت انجام",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: DayStatus) {
    val (bgColor, textColor, icon) = when (status) {
        DayStatus.COMPLETED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.Check)
        DayStatus.TODAY -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, Icons.Default.CalendarMonth)
        DayStatus.OVERDUE -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.Warning)
        DayStatus.PAUSED -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Pause)
        DayStatus.FUTURE -> Triple(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.colorScheme.onSurfaceVariant, null)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun AddEditChecklistDialog(
    initialTitle: String,
    initialCategory: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var category by remember { mutableStateOf(initialCategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTitle.isEmpty()) "افزودن به چک‌لیست" else "ویرایش مورد",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AutoSelectOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان تکلیف یا ذکر") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                AutoSelectOutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("دسته‌بندی (مثلاً: روزانه، تدبر، حفظ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick presets
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val presets = listOf("تلاوت قرآن", "صلوات", "تدبر در آیات", "زیارت عاشورا", "آیت الکرسی")
                    items(presets) { p ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { title = p }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = p, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), category.trim().ifEmpty { "روزانه" })
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("تأیید")
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
fun SwitchPlanDialog(
    allPlans: List<ReadingPlan>,
    activePlanId: Long?,
    onDismiss: () -> Unit,
    onSelectPlan: (Long) -> Unit,
    onNavigateToPlanning: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "انتخاب برنامه فعال",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (allPlans.isEmpty()) {
                    Text(
                        text = "هیچ برنامه‌ای ذخیره نشده است.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allPlans, key = { it.id }) { plan ->
                            val isCurrent = plan.id == activePlanId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectPlan(plan.id)
                                        onDismiss()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = if (isCurrent)
                                    androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = plan.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isCurrent) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(MaterialTheme.colorScheme.primary)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "فعال",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${plan.method.titlePersian} • ${plan.totalDays.toPersianDigits()} روز",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Icon(
                                        imageVector = if (isCurrent) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onNavigateToPlanning()
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("برنامه جدید")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )
}
