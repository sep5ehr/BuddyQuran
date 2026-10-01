package com.example.persianquran.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyPlanReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val prefs = context.getSharedPreferences(DailyPlanReminderManager.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(DailyPlanReminderManager.KEY_REMINDER_ENABLED, false)

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            if (isEnabled) {
                val hour = prefs.getInt(DailyPlanReminderManager.KEY_REMINDER_HOUR, 20)
                val minute = prefs.getInt(DailyPlanReminderManager.KEY_REMINDER_MINUTE, 0)
                DailyPlanReminderManager.scheduleDailyReminder(context, hour, minute)
            }
            return
        }

        // Trigger the check and notification
        DailyPlanReminderManager.checkAndSendReminder(context, isManualTest = false)

        // Reschedule for next day if enabled
        if (isEnabled) {
            val hour = prefs.getInt(DailyPlanReminderManager.KEY_REMINDER_HOUR, 20)
            val minute = prefs.getInt(DailyPlanReminderManager.KEY_REMINDER_MINUTE, 0)
            DailyPlanReminderManager.scheduleDailyReminder(context, hour, minute)
        }
    }
}
