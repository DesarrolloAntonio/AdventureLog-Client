package com.desarrollodroide.adventurelog.feature.login.login

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink

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
