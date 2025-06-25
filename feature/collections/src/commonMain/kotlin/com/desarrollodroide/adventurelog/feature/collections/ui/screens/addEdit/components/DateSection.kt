package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.data.CollectionFormData

@Composable
fun DateSection(
    formData: CollectionFormData,
    onFormDataChange: (CollectionFormData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    SectionCard(
        title = "Date Information",
        icon = Icons.Outlined.CalendarMonth,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledTextField(
                value = formData.startDate,
                onValueChange = { 
                    onFormDataChange(formData.copy(startDate = it))
                },
                label = "Start Date",
                icon = Icons.Outlined.CalendarToday,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            
            StyledTextField(
                value = formData.endDate,
                onValueChange = { 
                    onFormDataChange(formData.copy(endDate = it))
                },
                label = "End Date",
                icon = Icons.Outlined.CalendarToday,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
        }
        
        OutlinedButton(
            onClick = { /* TODO: Open date picker */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Date Range")
        }
    }
}