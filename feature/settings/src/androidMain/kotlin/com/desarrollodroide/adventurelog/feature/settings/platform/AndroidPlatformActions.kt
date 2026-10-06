package com.desarrollodroide.adventurelog.feature.settings.platform

import android.content.Context
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager

class AndroidPlatformActions(private val context: Context) : PlatformActions {
    override fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // No browser at all (a work profile, a kiosk device): nothing to open, and no crash.
        }
    }

    /**
     * Starts the intent and lets Android say whether anything took it. The old check,
     * resolveActivity, needs a <queries> entry since Android 11 and without one answers null even
     * with Gmail installed - so the row did nothing at all (measured). The manifest declares the
     * query too, for anything that asks.
     */
    override fun sendFeedbackEmail(): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$FEEDBACK_ADDRESS")
            putExtra(Intent.EXTRA_SUBJECT, "AdventureLog feedback")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    override fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }
}
