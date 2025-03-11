package com.desarrollodroide.adventurelog.feature.settings.platform

interface PlatformActions {
    /** Opens a URL in the device's default browser
     */
    fun openUrlInBrowser(url: String)
    
    /** Opens the device's email client to send feedback
     */
    fun sendFeedbackEmail()
    
    /** Gets the app version
     */
    fun getAppVersion(): String
}
