package me.tomasan7.jecnamobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.tomasan7.jecnamobile.gradenotifications.GradeCheckerWorker

class SystemBootBroadcastReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context, intent: Intent)
    {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED)
            return

        GradeCheckerWorker.scheduleWorker(context)
    }
}
