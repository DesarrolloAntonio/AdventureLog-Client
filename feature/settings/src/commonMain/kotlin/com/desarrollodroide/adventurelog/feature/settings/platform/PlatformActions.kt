package com.desarrollodroide.adventurelog.feature.settings.platform

/** Where "Send feedback" writes to, and what the screen shows when there is no email app. */
const val FEEDBACK_ADDRESS = "desarrollodroide@gmail.com"

interface PlatformActions {
    /** Opens a URL in the device's default browser
     */
    fun openUrlInBrowser(url: String)
    
    /** Opens the device's email client to send feedback.
     * @return false when there is no email app to open, so the screen can say where to write instead
     */
    fun sendFeedbackEmail(): Boolean
    
    /** Gets the app version
     */
    fun getAppVersion(): String
}
