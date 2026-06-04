package com.example.emotionbudgetapp.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
internal fun ExpenseInputCard(
    amountText: String,
    onAmountChange: (String) -> Unit,
    dateMillis: Long,
    onDateChange: (Long) -> Unit,
    category: String,
    categories: List<String>,
    onCategoryChange: (String) -> Unit,
    emotion: String,
    emotions: List<String>,
    onEmotionChange: (String) -> Unit,
    memo: String,
    onMemoChange: (String) -> Unit,
    emotionRecommendation: EmotionRecommendation?,
    onApplyEmotionRecommendation: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    val calendar = remember(dateMillis) {
        Calendar.getInstance().apply {
            timeInMillis = dateMillis
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "새 지출 기록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )

            OutlinedButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onDateChange(dateMillisOf(year, month, dayOfMonth))
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("날짜  ${formatDate(dateMillis)}")
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = onAmountChange,
                label = { Text("금액") },
                suffix = { Text("원") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ChipSelector(
                title = "카테고리",
                selectedValue = category,
                options = categories,
                onSelected = onCategoryChange
            )

            OutlinedTextField(
                value = memo,
                onValueChange = onMemoChange,
                label = { Text("메모") },
                placeholder = { Text("예: 시험 끝나고 친구와 저녁") },
                modifier = Modifier.fillMaxWidth()
            )

            EmotionRecommendationChip(
                recommendation = emotionRecommendation,
                selectedEmotion = emotion,
                onApply = onApplyEmotionRecommendation
            )

            ChipSelector(
                title = "감정",
                selectedValue = emotion,
                options = emotions,
                onSelected = onEmotionChange
            )

            Button(
                onClick = onAddClick,
                enabled = amountText.toIntOrNull()?.let { it > 0 } == true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("기록 추가")
            }
        }
    }
}

@Composable
private fun ChipSelector(
    title: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF172033)
        )

        options.chunked(3).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowOptions.forEach { option ->
                    FilterChip(
                        selected = selectedValue == option,
                        onClick = { onSelected(option) },
                        label = { Text(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmotionRecommendationChip(
    recommendation: EmotionRecommendation?,
    selectedEmotion: String,
    onApply: (String) -> Unit
) {
    if (recommendation == null) {
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "메모 추천",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF172033)
        )
        FilterChip(
            selected = selectedEmotion == recommendation.emotion,
            onClick = { onApply(recommendation.emotion) },
            label = {
                Text("${recommendation.emotion} · ${recommendation.keyword}")
            }
        )
    }
}
