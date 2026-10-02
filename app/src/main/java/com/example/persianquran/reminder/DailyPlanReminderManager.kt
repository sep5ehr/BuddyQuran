package com.example.persianquran.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.persianquran.data.local.QuranDatabase
import com.example.persianquran.data.surah.PersianDateHelper
import kotlinx.coroutines.runBlocking
import java.util.Calendar

sealed class ReminderCheckResult {
    data class Success(val message: String, val details: String) : ReminderCheckResult()
    data class NoActivePlan(val message: String) : ReminderCheckResult()
    data class PlanPaused(val message: String) : ReminderCheckResult()
    data class NoAssignmentToday(val message: String) : ReminderCheckResult()
    data class Disabled(val message: String) : ReminderCheckResult()
    data class PermissionDenied(val message: String) : ReminderCheckResult()
    data class AlreadyNotified(val message: String) : ReminderCheckResult()
}

object DailyPlanReminderManager {
    const val PREFS_NAME = "quran_prefs"
    const val KEY_REMINDER_ENABLED = "daily_reminder_enabled"
    const val KEY_REMINDER_HOUR = "reminder_hour"
    const val KEY_REMINDER_MINUTE = "reminder_minute"
    const val KEY_LAST_NOTIFIED_KEY = "last_notified_plan_key"
    const val CHANNEL_ID = "quran_daily_plan_channel"
    const val NOTIFICATION_ID = 786

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyPlanReminderReceiver::class.java).apply {
            action = "com.example.persianquran.ACTION_DAILY_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2026,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyPlanReminderReceiver::class.java).apply {
            action = "com.example.persianquran.ACTION_DAILY_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2026,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun checkAndSendReminder(context: Context, isManualTest: Boolean = false): ReminderCheckResult {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_REMINDER_ENABLED, false)

        if (!isEnabled && !isManualTest) {
            return ReminderCheckResult.Disabled("یادآوری برنامه روزانه غیرفعال است.")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return ReminderCheckResult.PermissionDenied("برای دریافت یادآوری برنامه روزانه، اجازه ارسال اعلان را فعال کنید.")
            }
        }

        val db = QuranDatabase.getInstance(context)
        val activePlan = runBlocking { db.quranDao().getActivePlanOnce() }

        // Rule 96 & 99: Only send when the user has an active daily reading plan.
        // If user does NOT have an active reading plan: DO NOT SEND THE NOTIFICATION.
        if (activePlan == null || !activePlan.isActive) {
            return ReminderCheckResult.NoActivePlan("هیچ برنامه مطالعه فعالی وجود ندارد؛ بنابراین اعلانی ارسال نمی‌شود.")
        }

        // Avoid sending notifications for paused plans
        if (activePlan.isPaused) {
            return ReminderCheckResult.PlanPaused("برنامه مطالعه در وضعیت توقف موقت قرار دارد؛ اعلانی ارسال نمی‌شود.")
        }

        val schedules = runBlocking { db.quranDao().getScheduleForPlanOnce(activePlan.id) }
        if (schedules.isEmpty()) {
            return ReminderCheckResult.NoAssignmentToday("جدول مطالعه برای این برنامه یافت نشد.")
        }

        val todayFormatted = PersianDateHelper.formatPersianDate(System.currentTimeMillis())
        val oneDayMillis = 24L * 60L * 60L * 1000L
        val dayIndexFromStart = ((System.currentTimeMillis() - activePlan.startDateMillis) / oneDayMillis).toInt() + 1

        val todayAssignment = schedules.find { it.dateFormatted == todayFormatted }
            ?: schedules.find { it.dayNumber == dayIndexFromStart }

        // Rule 99: If there is no daily reading assignment for the current day, DO NOT send notification.
        if (todayAssignment == null) {
            return ReminderCheckResult.NoAssignmentToday("برای روز جاری تکلیفی در برنامه «${activePlan.title}» وجود ندارد.")
        }

        val todayKey = "${activePlan.id}_${todayAssignment.dayNumber}_${todayFormatted}"
        val lastNotified = prefs.getString(KEY_LAST_NOTIFIED_KEY, null)
        if (!isManualTest && lastNotified == todayKey) {
            return ReminderCheckResult.AlreadyNotified("اعلان یادآوری امروز پیش‌تر ارسال گردیده است.")
        }

        // Prepare Notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "یادآوری مطالعه قرآن",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "اعلان روزانه جهت مطالعه سهمیه قرآنی برنامه ختم"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "schedule")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifTitle = "وقت مطالعه همراه قرآن رسیده است"
        val notifBody = "مطالعه این برنامه را انجام دهید."
        val notifDetail = "برنامه: ${activePlan.title}\nسهمیه: ${todayAssignment.rangeDescription}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.R.mipmap.ic_launcher)
            .setContentTitle(notifTitle)
            .setContentText(notifBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$notifBody\n${todayAssignment.rangeDescription}"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        prefs.edit().putString(KEY_LAST_NOTIFIED_KEY, todayKey).apply()

        return ReminderCheckResult.Success("اعلان یادآوری با موفقیت ارسال شد.", notifDetail)
    }
}
