package com.example.emotionbudgetapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.emotionbudgetapp.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel) {
    // ViewModel의 지출 목록을 화면 상태로 관찰한다.
    val expenses by viewModel.expenses.collectAsState()

    // 사용자가 입력 중인 값들을 저장하는 화면 상태
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("식비") }
    var emotion by remember { mutableStateOf("기쁨") }
    var memo by remember { mutableStateOf("") }

    val categories = listOf("식비", "교통", "쇼핑", "카페", "문화", "기타")
    val emotions = listOf("기쁨", "슬픔", "스트레스", "외로움", "평온", "분노")
    val totalAmount = expenses.sumOf { it.amount }
    val topEmotion = expenses
        .groupingBy { it.emotion }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key ?: "기록 없음"
    val biggestAmount = expenses.maxOfOrNull { it.amount } ?: 0

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F7FA)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderCard(
                    totalAmount = totalAmount,
                    recordCount = expenses.size,
                    topEmotion = topEmotion,
                    biggestAmount = biggestAmount
                )
            }

            item {
                ExpenseInputCard(
                    amountText = amountText,
                    onAmountChange = { amountText = it.filter { char -> char.isDigit() } },
                    category = category,
                    categories = categories,
                    onCategoryChange = { category = it },
                    emotion = emotion,
                    emotions = emotions,
                    onEmotionChange = { emotion = it },
                    memo = memo,
                    onMemoChange = { memo = it },
                    onAddClick = {
                        val amount = amountText.toIntOrNull()

                        if (amount != null && amount > 0) {
                            viewModel.addExpense(amount, category, emotion, memo)
                            amountText = ""
                            memo = ""
                        }
                    }
                )
            }

            item {
                SectionTitle(recordCount = expenses.size)
            }

            if (expenses.isEmpty()) {
                item {
                    EmptyRecordCard()
                }
            } else {
                items(expenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onDelete = {
                            viewModel.deleteExpense(expense)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(
    totalAmount: Int,
    recordCount: Int,
    topEmotion: String,
    biggestAmount: Int
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF172033)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "감정 가계부",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "소비한 순간의 감정까지 함께 기록해요.",
                color = Color(0xFFD6DEEB),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatWon(totalAmount),
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetric(
                    title = "기록",
                    value = "${recordCount}개",
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    title = "대표 감정",
                    value = topEmotion,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    title = "최대 지출",
                    value = formatWon(biggestAmount),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF24324B),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFFB8C3D8),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ExpenseInputCard(
    amountText: String,
    onAmountChange: (String) -> Unit,
    category: String,
    categories: List<String>,
    onCategoryChange: (String) -> Unit,
    emotion: String,
    emotions: List<String>,
    onEmotionChange: (String) -> Unit,
    memo: String,
    onMemoChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
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

            OutlinedTextField(
                value = amountText,
                onValueChange = onAmountChange,
                label = { Text("금액") },
                suffix = { Text("원") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DropdownSelector(
                    label = "카테고리",
                    selectedValue = category,
                    options = categories,
                    onSelected = onCategoryChange,
                    modifier = Modifier.weight(1f)
                )
                DropdownSelector(
                    label = "감정",
                    selectedValue = emotion,
                    options = emotions,
                    onSelected = onEmotionChange,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = memo,
                onValueChange = onMemoChange,
                label = { Text("메모") },
                placeholder = { Text("예: 시험 끝나고 친구와 저녁") },
                modifier = Modifier.fillMaxWidth()
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
private fun SectionTitle(recordCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "최근 기록",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172033)
        )
        Text(
            text = "${recordCount}개",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF5D6B82)
        )
    }
}

@Composable
private fun EmptyRecordCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "아직 기록이 없어요",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )
            Text(
                text = "금액과 감정을 입력하면 여기에 지출 기록이 쌓입니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5D6B82)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatWon(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원"
}
