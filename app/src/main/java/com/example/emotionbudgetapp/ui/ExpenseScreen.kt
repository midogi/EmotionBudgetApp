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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.unit.dp
import com.example.emotionbudgetapp.data.Expense
import com.example.emotionbudgetapp.viewmodel.ExpenseViewModel

@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel) {
    // ViewModel의 지출 목록을 화면 상태로 관찰한다.
    val expenses by viewModel.expenses.collectAsState()

    // 사용자가 입력 중인 값들을 저장하는 화면 상태
    var amountText by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(startOfTodayMillis()) }
    var category by remember { mutableStateOf("식비") }
    var emotion by remember { mutableStateOf("기쁨") }
    var memo by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(ExpenseTab.RECORDS) }
    var autoEmotionEnabled by remember { mutableStateOf(true) }

    val categories = listOf("식비", "교통", "쇼핑", "카페", "문화", "기타")
    val emotions = listOf("기쁨", "슬픔", "스트레스", "외로움", "평온", "분노")
    val emotionRecommendation = recommendEmotionFromMemo(memo, emotions)
    val sortedExpenses = expenses.sortedWith(
        compareByDescending<Expense> { it.dateMillis }.thenByDescending { it.id }
    )
    val totalAmount = expenses.sumOf { it.amount }
    val emotionStats = buildStats(expenses) { it.emotion }
    val categoryStats = buildStats(expenses) { it.category }
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
                ExpenseTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            if (selectedTab == ExpenseTab.RECORDS) {
                item {
                    ExpenseInputCard(
                        amountText = amountText,
                        onAmountChange = { amountText = it.filter { char -> char.isDigit() } },
                        dateMillis = selectedDateMillis,
                        onDateChange = { selectedDateMillis = it },
                        category = category,
                        categories = categories,
                        onCategoryChange = { category = it },
                        emotion = emotion,
                        emotions = emotions,
                        onEmotionChange = { selectedEmotion ->
                            emotion = selectedEmotion
                            autoEmotionEnabled = emotionRecommendation?.emotion == selectedEmotion
                        },
                        memo = memo,
                        onMemoChange = { newMemo ->
                            memo = newMemo

                            if (newMemo.isBlank()) {
                                autoEmotionEnabled = true
                            }

                            val recommendation = recommendEmotionFromMemo(newMemo, emotions)

                            if (autoEmotionEnabled && recommendation != null) {
                                emotion = recommendation.emotion
                            }
                        },
                        emotionRecommendation = emotionRecommendation,
                        onApplyEmotionRecommendation = { recommendedEmotion ->
                            emotion = recommendedEmotion
                            autoEmotionEnabled = true
                        },
                        onAddClick = {
                            val amount = amountText.toIntOrNull()

                            if (amount != null && amount > 0) {
                                viewModel.addExpense(selectedDateMillis, amount, category, emotion, memo)
                                amountText = ""
                                memo = ""
                                selectedDateMillis = startOfTodayMillis()
                                autoEmotionEnabled = true
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
                    items(sortedExpenses, key = { it.id }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onDelete = {
                                viewModel.deleteExpense(expense)
                            }
                        )
                    }
                }
            } else {
                item {
                    StatisticsScreen(
                        recordCount = expenses.size,
                        totalAmount = totalAmount,
                        emotionStats = emotionStats,
                        categoryStats = categoryStats
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
private fun ExpenseTabRow(
    selectedTab: ExpenseTab,
    onTabSelected: (ExpenseTab) -> Unit
) {
    val tabs = listOf(ExpenseTab.RECORDS, ExpenseTab.STATISTICS)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        TabRow(
            selectedTabIndex = tabs.indexOf(selectedTab),
            containerColor = Color.White,
            contentColor = Color(0xFF172033)
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(tab.title) }
                )
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
                text = "날짜, 금액, 감정을 입력하면 여기에 지출 기록이 쌓입니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5D6B82)
            )
        }
    }
}

private enum class ExpenseTab(val title: String) {
    RECORDS("기록"),
    STATISTICS("통계")
}
