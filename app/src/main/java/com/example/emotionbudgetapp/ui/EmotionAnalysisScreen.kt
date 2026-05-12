package com.example.emotionbudgetapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.emotionbudgetapp.data.Expense
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class EmotionStat(
    val emotion: String,
    val totalAmount: Int,
    val count: Int,
    val averageAmount: Int,
    val previousCount: Int,
    val countChange: Int,
    val share: Float
)

@Composable
fun EmotionAnalysisScreen(
    expenses: List<Expense>,
    onBack: () -> Unit
) {
    var visibleMonthMillis by remember { mutableStateOf(startOfMonth(System.currentTimeMillis())) }

    val monthStart = startOfMonth(visibleMonthMillis)
    val monthEnd = addMonths(monthStart, 1)
    val previousMonthStart = addMonths(monthStart, -1)

    val monthExpenses = expenses.filter { it.dateMillis >= monthStart && it.dateMillis < monthEnd }
    val previousMonthExpenses = expenses.filter { it.dateMillis >= previousMonthStart && it.dateMillis < monthStart }
    val emotionStats = buildEmotionStats(monthExpenses, previousMonthExpenses)
    val monthTotal = monthExpenses.sumOf { it.amount }
    val topSpendingEmotion = emotionStats.maxByOrNull { it.totalAmount }
    val topCountEmotion = emotionStats.maxByOrNull { it.count }
    val stressStat = emotionStats.firstOrNull { it.emotion == "스트레스" }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                EmotionAnalysisHeader(
                    monthMillis = monthStart,
                    onBack = onBack,
                    onPreviousMonth = { visibleMonthMillis = addMonths(monthStart, -1) },
                    onNextMonth = { visibleMonthMillis = addMonths(monthStart, 1) }
                )
            }

            item {
                EmotionHeroCard(
                    monthTotal = monthTotal,
                    topSpendingEmotion = topSpendingEmotion,
                    stressStat = stressStat,
                    recordCount = monthExpenses.size
                )
            }

            item {
                EmotionInsightCard(
                    title = "이번 달 감정 신호",
                    message = buildStressInsight(stressStat)
                )
            }

            item {
                EmotionInsightCard(
                    title = "가장 자주 나온 감정",
                    message = topCountEmotion?.let { "${it.emotion} 감정으로 ${it.count}회 소비했어요." }
                        ?: "이번 달 감정 기록이 아직 없어요."
                )
            }

            if (emotionStats.none { it.count > 0 }) {
                item {
                    EmotionEmptyState()
                }
            } else {
                item {
                    EmotionSectionTitle(title = "감정별 상세 분석")
                }
                items(emotionStats.filter { it.count > 0 }, key = { it.emotion }) { stat ->
                    EmotionStatCard(stat = stat)
                }
            }
        }
    }
}

@Composable
private fun EmotionAnalysisHeader(
    monthMillis: Long,
    onBack: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("< 입력")
            }
            Text(
                text = "감정 분석",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )
            Text(
                text = "월간",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPreviousMonth) {
                Text("<")
            }
            Text(
                text = formatMonthTitle(monthMillis),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF263244)
            )
            TextButton(onClick = onNextMonth) {
                Text(">")
            }
        }
    }
}

@Composable
private fun EmotionHeroCard(
    monthTotal: Int,
    topSpendingEmotion: EmotionStat?,
    stressStat: EmotionStat?,
    recordCount: Int
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "소비 감정 리포트",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )
            Text(
                text = "감정별 소비 규모와 빈도를 한 달 단위로 비교해요.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF667085)
            )
            Text(
                text = formatWon(monthTotal),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6651)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EmotionMetricBox(
                    label = "최대 감정",
                    value = topSpendingEmotion?.emotion ?: "없음",
                    modifier = Modifier.weight(1f)
                )
                EmotionMetricBox(
                    label = "스트레스 평균",
                    value = formatWon(stressStat?.averageAmount ?: 0),
                    modifier = Modifier.weight(1f)
                )
                EmotionMetricBox(
                    label = "기록",
                    value = "${recordCount}개",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EmotionMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )
        }
    }
}

@Composable
private fun EmotionInsightCard(title: String, message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569)
            )
        }
    }
}

@Composable
private fun EmotionSectionTitle(title: String) {
    Text(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp),
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF172033)
    )
}

@Composable
private fun EmotionStatCard(stat: EmotionStat) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stat.emotion,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF172033)
                    )
                    Text(
                        text = "${stat.count}회 · 평균 ${formatWon(stat.averageAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatWon(stat.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6651)
                    )
                    Text(
                        text = formatCountChange(stat.countChange),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (stat.countChange > 0) Color(0xFFFF6651) else Color(0xFF64748B)
                    )
                }
            }
            EmotionShareBar(share = stat.share)
        }
    }
}

@Composable
private fun EmotionShareBar(share: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFE2E8F0),
            shape = RoundedCornerShape(8.dp)
        ) {}
        Surface(
            modifier = Modifier
                .fillMaxWidth(share.coerceIn(0.04f, 1f))
                .height(8.dp),
            color = Color(0xFFFF6651),
            shape = RoundedCornerShape(8.dp)
        ) {}
    }
}

@Composable
private fun EmotionEmptyState() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(18.dp),
            text = "이번 달 기록이 생기면 감정별 총액, 횟수, 평균 지출을 자동으로 분석해요.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B)
        )
    }
}

private fun buildEmotionStats(
    monthExpenses: List<Expense>,
    previousMonthExpenses: List<Expense>
): List<EmotionStat> {
    val defaultEmotions = listOf("기쁨", "슬픔", "스트레스", "외로움", "평온", "분노")
    val allEmotions = (defaultEmotions + monthExpenses.map { it.emotion } + previousMonthExpenses.map { it.emotion })
        .distinct()
    val totalAmount = monthExpenses.sumOf { it.amount }.takeIf { it > 0 } ?: 1

    return allEmotions.map { emotion ->
        val current = monthExpenses.filter { it.emotion == emotion }
        val previous = previousMonthExpenses.filter { it.emotion == emotion }
        val currentTotal = current.sumOf { it.amount }
        val currentCount = current.size
        EmotionStat(
            emotion = emotion,
            totalAmount = currentTotal,
            count = currentCount,
            averageAmount = if (currentCount == 0) 0 else currentTotal / currentCount,
            previousCount = previous.size,
            countChange = currentCount - previous.size,
            share = currentTotal.toFloat() / totalAmount.toFloat()
        )
    }.sortedWith(
        compareByDescending<EmotionStat> { it.totalAmount }
            .thenByDescending { it.count }
            .thenBy { defaultEmotions.indexOf(it.emotion).let { index -> if (index == -1) Int.MAX_VALUE else index } }
    )
}

private fun buildStressInsight(stressStat: EmotionStat?): String {
    if (stressStat == null || stressStat.count == 0 && stressStat.previousCount == 0) {
        return "스트레스 소비 기록이 생기면 지난달과 비교해서 변화를 알려줄게요."
    }

    return when {
        stressStat.countChange > 0 -> "스트레스 소비가 이번 달 ${stressStat.countChange}회 증가했어요. 평균은 ${formatWon(stressStat.averageAmount)}입니다."
        stressStat.countChange < 0 -> "스트레스 소비가 지난달보다 ${-stressStat.countChange}회 줄었어요."
        else -> "스트레스 소비 횟수는 지난달과 같아요. 이번 달 평균은 ${formatWon(stressStat.averageAmount)}입니다."
    }
}

private fun formatCountChange(change: Int): String {
    return when {
        change > 0 -> "지난달보다 +${change}회"
        change < 0 -> "지난달보다 ${change}회"
        else -> "지난달과 동일"
    }
}

private fun startOfMonth(millis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun addMonths(millis: Long, amount: Int): Long {
    return Calendar.getInstance().apply {
        timeInMillis = millis
        add(Calendar.MONTH, amount)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun formatWon(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원"
}

private fun formatMonthTitle(millis: Long): String {
    return SimpleDateFormat("yyyy년 M월", Locale.KOREA).format(Date(millis))
}
