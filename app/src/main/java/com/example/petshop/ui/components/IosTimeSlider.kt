package com.example.petshop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.NumberPicker

@Composable
fun IosTimeSlider(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHour by remember(hour) { mutableIntStateOf(hour.coerceIn(0, 23)) }
    var selectedMinute by remember(minute) { mutableIntStateOf(minute.coerceIn(0, 59)) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            String.format("%02d:%02d", selectedHour, selectedMinute),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeNumberPicker(
                    label = "Hour",
                    value = selectedHour,
                    range = 0..23,
                    formatter = { String.format("%02d", it) },
                    onValueChange = {
                        selectedHour = it
                        onTimeChange(selectedHour, selectedMinute)
                    }
                )

                Text(":", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                TimeNumberPicker(
                    label = "Minute",
                    value = selectedMinute,
                    range = 0..59,
                    formatter = { String.format("%02d", it) },
                    onValueChange = {
                        selectedMinute = it
                        onTimeChange(selectedHour, selectedMinute)
                    }
                )
            }
        }
    }
}

@Composable
private fun TimeNumberPicker(
    label: String,
    value: Int,
    range: IntRange,
    formatter: (Int) -> String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        AndroidView(
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last
                    wrapSelectorWheel = true
                    descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                    setOnValueChangedListener { _, _, newValue -> onValueChange(newValue) }
                }
            },
            update = { picker ->
                picker.displayedValues = null
                picker.minValue = range.first
                picker.maxValue = range.last
                picker.setFormatter { formatter(it) }
                if (picker.value != value) picker.value = value
            },
            modifier = Modifier.size(width = 110.dp, height = 150.dp)
        )
    }
}

/*
private fun TimeWheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    label: String
) {
    // Deprecated custom wheel retained only as a reference while moving to NumberPicker.
}
*/






