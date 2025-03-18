package com.desarrollodroide.adventurelog.theme

import android.os.Build

// Dynamic colors are supported on Android 12 (API 31) and above
actual val isDynamicColorSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
