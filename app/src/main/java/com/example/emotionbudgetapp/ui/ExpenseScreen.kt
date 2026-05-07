package com.example.emotionbudgetapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.emotionbudgetapp.data.Expense
import com.example.emotionbudgetapp.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("식비") }
    var emotion by remember { mutableStateOf("기쁨") }
    var memo by remember { mutableStateOf("") }
    var editingExpenseId by remember { mutableStateOf<Int?>(null) }

    var filterType by remember { mutableStateOf("이번달") }
    var customStart by remember { mutableStateOf("") }
    var customEnd by remember { mutableStateOf("") }

    val categories = listOf("식비", "교통", "쇼핑", "카페", "문화", "기타")
    val emotions = listOf("기쁨", "슬픔", "스트레스", "외로움", "평온", "분노")
    val filteredExpenses = expenses.filter { isInFilter(it.createdAt, filterType, customStart, customEnd) }

    val totalAmount = filteredExpenses.sumOf { it.amount }
    val topEmotion = filteredExpenses.groupingBy { it.emotion }.eachCount().maxByOrNull { it.value }?.key ?: "기록 없음"
    val biggestAmount = filteredExpenses.maxOfOrNull { it.amount } ?: 0

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F7FA)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeaderCard(totalAmount, filteredExpenses.size, topEmotion, biggestAmount) }
            item {
                FilterCard(
                    filterType = filterType,
                    onFilterChange = { filterType = it },
                    customStart = customStart,
                    onCustomStartChange = { customStart = it },
                    customEnd = customEnd,
                    onCustomEndChange = { customEnd = it }
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
                    buttonText = if (editingExpenseId == null) "기록 추가" else "기록 수정",
                    onAddClick = {
                        val amount = amountText.toIntOrNull()
                        if (amount != null && amount > 0) {
                            if (editingExpenseId == null) {
                                viewModel.addExpense(amount, category, emotion, memo)
                            } else {
                                val original = expenses.firstOrNull { it.id == editingExpenseId } ?: return@ExpenseInputCard
                                viewModel.updateExpense(original.copy(amount = amount, category = category, emotion = emotion, memo = memo))
                            }
                            amountText = ""; memo = ""; editingExpenseId = null
                        }
                    }
                )
            }

            item { SectionTitle(filteredExpenses.size) }
            if (filteredExpenses.isEmpty()) item { EmptyRecordCard() } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onEdit = {
                            editingExpenseId = expense.id
                            amountText = expense.amount.toString()
                            category = expense.category
                            emotion = expense.emotion
                            memo = expense.memo
                        },
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }
        }
    }
}

@Composable private fun FilterCard(filterType: String,onFilterChange: (String) -> Unit,customStart: String,onCustomStartChange: (String) -> Unit,customEnd: String,onCustomEndChange: (String) -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DropdownSelector("기간", filterType, listOf("오늘", "이번주", "이번달", "직접 선택"), onFilterChange)
            if (filterType == "직접 선택") {
                OutlinedTextField(value = customStart, onValueChange = onCustomStartChange, label = { Text("시작일 (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = customEnd, onValueChange = onCustomEndChange, label = { Text("종료일 (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun isInFilter(timeMillis: Long, filterType: String, customStart: String, customEnd: String): Boolean {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timeMillis }
    return when (filterType) {
        "오늘" -> now.get(Calendar.YEAR) == target.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
        "이번주" -> now.get(Calendar.YEAR) == target.get(Calendar.YEAR) && now.get(Calendar.WEEK_OF_YEAR) == target.get(Calendar.WEEK_OF_YEAR)
        "이번달" -> now.get(Calendar.YEAR) == target.get(Calendar.YEAR) && now.get(Calendar.MONTH) == target.get(Calendar.MONTH)
        "직접 선택" -> {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            runCatching {
                val start = fmt.parse(customStart)?.time ?: Long.MIN_VALUE
                val end = (fmt.parse(customEnd)?.time ?: Long.MAX_VALUE) + 86_399_999
                timeMillis in start..end
            }.getOrDefault(true)
        }
        else -> true
    }
}

@Composable private fun HeaderCard(totalAmount: Int,recordCount: Int,topEmotion: String,biggestAmount: Int) { /* same */
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF172033))) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("감정 가계부", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("소비한 순간의 감정까지 함께 기록해요.", color = Color(0xFFD6DEEB))
            Text(formatWon(totalAmount), color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryMetric("기록", "${recordCount}개", Modifier.weight(1f)); SummaryMetric("대표 감정", topEmotion, Modifier.weight(1f)); SummaryMetric("최대 지출", formatWon(biggestAmount), Modifier.weight(1f))
            }
        }
    }
}
@Composable private fun SummaryMetric(title: String,value: String,modifier: Modifier = Modifier) { Surface(modifier = modifier, color = Color(0xFF24324B), shape = RoundedCornerShape(8.dp)) { Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) { Text(title, color = Color(0xFFB8C3D8)); Text(value, color = Color.White, fontWeight = FontWeight.SemiBold) } } }
@Composable
private fun ExpenseInputCard(amountText: String,onAmountChange: (String) -> Unit,category: String,categories: List<String>,onCategoryChange: (String) -> Unit,emotion: String,emotions: List<String>,onEmotionChange: (String) -> Unit,memo: String,onMemoChange: (String) -> Unit,buttonText: String,onAddClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = amountText, onValueChange = onAmountChange, label = { Text("금액") }, suffix = { Text("원") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DropdownSelector("카테고리", category, categories, onCategoryChange, Modifier.weight(1f)); DropdownSelector("감정", emotion, emotions, onEmotionChange, Modifier.weight(1f))
            }
            OutlinedTextField(value = memo, onValueChange = onMemoChange, label = { Text("메모") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = onAddClick, enabled = amountText.toIntOrNull()?.let { it > 0 } == true, modifier = Modifier.fillMaxWidth()) { Text(buttonText) }
        }
    }
}
@Composable private fun SectionTitle(recordCount: Int) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("최근 기록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("${recordCount}개") } }
@Composable private fun EmptyRecordCard() { Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(8.dp)) { Text("아직 기록이 없어요", modifier = Modifier.padding(18.dp)) } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(label: String, selectedValue: String, options: List<String>, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(value = selectedValue, onValueChange = {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false }) } }
    }
}

private fun formatWon(amount: Int): String = NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원"
