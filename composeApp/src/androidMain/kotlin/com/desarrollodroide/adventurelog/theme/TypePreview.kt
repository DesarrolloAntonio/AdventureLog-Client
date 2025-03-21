package com.desarrollodroide.adventurelog.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TypeScaleItem(
    title: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header with title and weight/size info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${style.fontWeight} - ${style.fontSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Normal text sample
            Text(
                text = "Quicksand Normal",
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Bold text if not already bold
            if (style.fontWeight != FontWeight.Bold) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quicksand Bold",
                    style = style.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Italic text sample
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Quicksand Italic (simulated)",
                style = style.copy(fontStyle = FontStyle.Italic),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Preview(showBackground = true)
@Composable
fun DisplayStylesPreview() {
    PreviewContainer("Display Styles") {
        val typography = getAppTypography()
        TypeScaleItem("Display Large", typography.displayLarge)
        TypeScaleItem("Display Medium", typography.displayMedium)
        TypeScaleItem("Display Small", typography.displaySmall)
    }
}

@Preview(showBackground = true)
@Composable
fun HeadlineStylesPreview() {
    PreviewContainer("Headline Styles") {
        val typography = getAppTypography()
        TypeScaleItem("Headline Large", typography.headlineLarge)
        TypeScaleItem("Headline Medium", typography.headlineMedium)
        TypeScaleItem("Headline Small", typography.headlineSmall)
    }
}

@Preview(showBackground = true)
@Composable
fun TitleStylesPreview() {
    PreviewContainer("Title Styles") {
        val typography = getAppTypography()
        TypeScaleItem("Title Large", typography.titleLarge)
        TypeScaleItem("Title Medium", typography.titleMedium)
        TypeScaleItem("Title Small", typography.titleSmall)
    }
}

@Preview(showBackground = true)
@Composable
fun BodyStylesPreview() {
    PreviewContainer("Body Styles") {
        val typography = getAppTypography()
        TypeScaleItem("Body Large", typography.bodyLarge)
        TypeScaleItem("Body Medium", typography.bodyMedium)
        TypeScaleItem("Body Small", typography.bodySmall)
    }
}

@Preview(showBackground = true)
@Composable
fun LabelStylesPreview() {
    PreviewContainer("Label Styles") {
        val typography = getAppTypography()
        TypeScaleItem("Label Large", typography.labelLarge)
        TypeScaleItem("Label Medium", typography.labelMedium)
        TypeScaleItem("Label Small", typography.labelSmall)
    }
}

@Composable
private fun PreviewContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Typography - $title",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}