package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.runtime.Composable

/** Opens the legal page on the privacy policy; the other is one tab away. */
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) = LegalScreen(initial = LegalDoc.PRIVACY, onBack = onBack)
