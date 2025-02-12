package com.desarrollodroide.adventurelog.feature.login.login

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.sp

@Composable
fun LinkableText(
    text: String,
    url: String,
    onClick: (String) -> Unit
) {
    Text(buildAnnotatedString {
        append("View instructions guide in ")
        withLink(LinkAnnotation.Url(url = url)) {
            append("website")
        }
    })
}
