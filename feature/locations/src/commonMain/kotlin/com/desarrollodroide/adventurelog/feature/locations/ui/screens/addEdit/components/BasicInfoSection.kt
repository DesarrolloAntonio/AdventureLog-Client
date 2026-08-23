package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import com.desarrollodroide.adventurelog.core.model.Currencies
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.feature.ui.components.DescriptionSection
import com.desarrollodroide.adventurelog.feature.ui.components.PrimaryButton
import com.desarrollodroide.adventurelog.feature.ui.components.RatingBar
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.StyledTextField

@Composable
fun BasicInfoSection(
    formData: LocationFormData,
    categories: List<Category>,
    onFormDataChange: (LocationFormData) -> Unit,
    onNavigateBack: () -> Unit = {},
    onGenerateDescription: () -> Unit = {},
    isGeneratingDescription: Boolean = false,
    onAddCategory: (name: String, icon: String) -> Unit = { _, _ -> }
) {
    var expanded by remember { mutableStateOf(true) }
    var linkError by remember { mutableStateOf("") }
    
    fun validateUrl(url: String): Boolean {
        if (url.isBlank()) {
            linkError = ""
            return true
        }
        
        val hasProtocol = url.startsWith("http://", ignoreCase = true) || 
                         url.startsWith("https://", ignoreCase = true)
        
        return if (!hasProtocol) {
            linkError = "URL must include http:// or https://"
            false
        } else {
            linkError = ""
            true
        }
    }
    
    SectionCard(
        title = "Basic Information",
        icon = Icons.Outlined.Description,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        leadingContent = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledTextField(
                value = formData.name,
                onValueChange = {
                    onFormDataChange(formData.copy(name = it))
                },
                label = "Location Name",
                icon = Icons.Outlined.Title,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            
            CategoryDropdown(
                categories = categories,
                selectedCategory = formData.category,
                onCategorySelected = {
                    onFormDataChange(formData.copy(category = it))
                },
                onAddCategory = onAddCategory
            )

            PriceField(
                price = formData.price,
                currency = formData.priceCurrency,
                onPriceChange = { onFormDataChange(formData.copy(price = it)) },
                onCurrencyChange = { onFormDataChange(formData.copy(priceCurrency = it)) }
            )

            RatingBar(
                rating = formData.rating,
                onRatingChanged = {
                    onFormDataChange(formData.copy(rating = it))
                }
            )

            StyledTextField(
                value = formData.link,
                onValueChange = { newValue ->
                    onFormDataChange(formData.copy(link = newValue))
                    validateUrl(newValue)
                },
                label = "Website Link",
                icon = Icons.Outlined.Link,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                isError = linkError.isNotEmpty(),
                errorMessage = linkError
            )
            
            DescriptionSection(
                description = formData.description,
                onDescriptionChange = {
                    onFormDataChange(formData.copy(description = it))
                }
            )
            
            AnimatedVisibility(
                visible = formData.name.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (isGeneratingDescription) {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = false
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating...")
                        }
                    } else {
                        PrimaryButton(
                            onClick = onGenerateDescription,
                            text = "Generate Description from Wikipedia",
                            enabled = !isGeneratingDescription
                        )
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Public Location",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = formData.isPublic,
                        onCheckedChange = {
                            onFormDataChange(formData.copy(isPublic = it))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    }
}

/**
 * What the place cost, and in which currency. The amount is typed rather than stepped, and only
 * digits and a single separator are accepted so a stray character cannot reach the server.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceField(
    price: String,
    currency: String,
    onPriceChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currencyMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StyledTextField(
            value = price,
            onValueChange = { typed ->
                val cleaned = typed.filter { it.isDigit() || it == '.' || it == ',' }
                    .replace(',', '.')
                if (cleaned.count { it == '.' } <= 1) onPriceChange(cleaned)
            },
            label = "Price",
            icon = Icons.Outlined.Payments,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            TextButton(onClick = { currencyMenuOpen = true }) {
                Text(text = currency)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Choose currency"
                )
            }
            DropdownMenu(
                expanded = currencyMenuOpen,
                onDismissRequest = { currencyMenuOpen = false }
            ) {
                Currencies.options.forEach { (code, label) ->
                    DropdownMenuItem(
                        text = { Text("$code - $label") },
                        onClick = {
                            onCurrencyChange(code)
                            currencyMenuOpen = false
                        }
                    )
                }
            }
        }
    }
}
