package com.desarrollodroide.adventurelog.feature.settings.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager

class AndroidPlatformActions(private val context: Context) : PlatformActions {
    override fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun sendFeedbackEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:desarrollodroide@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Feedback para AdventureLog")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: PackageManager.NameNotFoundException) {
            "Desconocida"
        }
    }
}
