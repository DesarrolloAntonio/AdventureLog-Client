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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Currencies
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.ui.components.DescriptionSection
import com.desarrollodroide.adventurelog.feature.ui.components.PrimaryButton
import com.desarrollodroide.adventurelog.feature.ui.components.AddPhotoTile
import com.desarrollodroide.adventurelog.feature.ui.components.RatingBar
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.StyledTextField

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BasicInfoSection(
    formData: LocationFormData,
    categories: List<Category>,
    onFormDataChange: (LocationFormData) -> Unit,
    /** The old in-card back arrow; the form's own header has Close now, so callers leave it out. */
    onNavigateBack: (() -> Unit)? = null,
    onGenerateDescription: () -> Unit = {},
    isGeneratingDescription: Boolean = false,
    onAddCategory: (name: String, icon: String) -> Unit = { _, _ -> }
) {
    var expanded by remember { mutableStateOf(true) }
    val motion = MaterialTheme.motionScheme
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
        leadingContent = onNavigateBack?.let { back -> {
            IconButton(
                onClick = back,
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        } }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledTextField(
                value = formData.name,
                onValueChange = {
                    onFormDataChange(formData.copy(name = it))
                },
                label = "Name of the place",
                icon = Icons.Outlined.Title,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            
            CategoryChips(
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

            AddPhotoTile(
                photoCount = formData.images.size,
                onPhotoPicked = { picked ->
                    // The first photo of a place is its cover, as the Images section does it.
                    onFormDataChange(
                        formData.copy(images = formData.images + picked.copy(isPrimary = formData.images.isEmpty()))
                    )
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
                enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec())
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (isGeneratingDescription) {
                        // PrimaryButton's height and shape, so the swap does not make the form jump.
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shapes = ButtonDefaults.shapes(),
                            enabled = false
                        ) {
                            LoadingIndicator(
                                modifier = Modifier.size(24.dp),
                                color = LocalContentColor.current
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
            
            PublicPlaceRow(
                checked = formData.isPublic,
                onCheckedChange = { onFormDataChange(formData.copy(isPublic = it)) }
            )
        }
    }
}

/**
 * Whether the place is public, as a settings row: the whole row toggles it, not only the switch
 * (the collections' Public rows learnt this in QA CO-11).
 */
@Composable
private fun PublicPlaceRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Switch)
                .heightIn(min = 64.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                text = "Public place",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = null,
                thumbContent = if (checked) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                } else null
            )
        }
    }
}

/**
 * What the place cost, and in which currency. The amount is typed rather than stepped, and only
 * digits and a single separator are accepted so a stray character cannot reach the server.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
            TextButton(onClick = { currencyMenuOpen = true }, shapes = ButtonDefaults.shapes()) {
                Text(text = currency)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Choose currency"
                )
            }
            DropdownMenu(
                expanded = currencyMenuOpen,
                onDismissRequest = { currencyMenuOpen = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Currencies.options.forEach { (code, label) ->
                    DropdownMenuItem(
                        text = { Text("$code - $label") },
                        // The one in use carries a check, so an open menu says what is chosen.
                        trailingIcon = if (code == currency) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        } else null,
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
