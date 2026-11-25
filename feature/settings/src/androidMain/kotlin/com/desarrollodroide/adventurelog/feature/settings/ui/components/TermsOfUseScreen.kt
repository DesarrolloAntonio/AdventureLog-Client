package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.runtime.Composable

/** Opens the legal page on the terms; the other is one tab away. */
@Composable
fun TermsOfUseScreen(onBack: () -> Unit) = LegalScreen(initial = LegalDoc.TERMS, onBack = onBack)
