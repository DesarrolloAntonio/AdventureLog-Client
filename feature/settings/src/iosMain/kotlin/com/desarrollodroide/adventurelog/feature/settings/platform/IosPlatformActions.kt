package com.desarrollodroide.adventurelog.feature.settings.platform

import platform.Foundation.NSURL
import platform.Foundation.NSBundle
import platform.UIKit.UIApplication

class IosPlatformActions : PlatformActions {
    override fun openUrlInBrowser(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }

    override fun sendFeedbackEmail() {
        // Implementación para iOS - puedes usar MFMailComposeViewController o similar
        // Esta es una implementación simplificada
        val emailUrl = NSURL.URLWithString("mailto:feedback@yourdomain.com")
        if (emailUrl != null) {
            UIApplication.sharedApplication.openURL(emailUrl)
        }
    }
    
    override fun getAppVersion(): String {
        val bundle = NSBundle.mainBundle
        val version = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        val build = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
        return if (version != null && build != null) {
            "$version ($build)"
        } else {
            version ?: build ?: "Unknown"
        }
    }
}
