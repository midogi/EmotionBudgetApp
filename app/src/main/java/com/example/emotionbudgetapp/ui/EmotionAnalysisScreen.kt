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
import com.example.emotionbudgetapp.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 감정 분석 화면에서 한 감정에 대해 보여줄 계산 결과를 한곳에 모은 모델.
private data class EmotionStat(
    val emotion: String,
    val totalAmount: Int,
    val count: Int,
    val averageAmount: Int,
    val previousCount: Int,
    val countChange: Int,
    val share: Float
)

// 앱의 차별점이 되는 감정 소비 진단 카드용 모델.
// 단순 합계가 아니라 위험도, 주요 감정, 추천 행동까지 한 문장으로 해석한다.
private data class EmotionDiagnosis(
    val riskLabel: String,
    val riskScore: Int,
    val title: String,
    val message: String,
    val primaryEmotion: String,
    val stressSignal: String,
    val recommendation: String
)

@Composable
fun EmotionAnalysisScreen(
    expenses: List<Expense>,
    onBack: () -> Unit
) {
    // 분석 기준 월. 이전/다음 버튼을 누르면 이 값이 바뀌고 통계가 다시 계산된다.
    var visibleMonthMillis by remember { mutableStateOf(startOfMonth(System.currentTimeMillis())) }

    // 수입 기록은 감정 소비 분석에서 제외한다. 감정 분석은 지출 습관을 설명하는 화면이기 때문이다.
    val expenseOnlyRecords = expenses.filter { it.type == TransactionType.EXPENSE }

    // 현재 월과 지난달의 범위를 millis로 계산한다.
    val monthStart = startOfMonth(visibleMonthMillis)
    val monthEnd = addMonths(monthStart, 1)
    val previousMonthStart = addMonths(monthStart, -1)

    // 이번 달 지출과 지난달 지출을 나눠야 "지난달보다 몇 회 증가" 같은 문장을 만들 수 있다.
    val monthExpenses = expenseOnlyRecords.filter { it.dateMillis >= monthStart && it.dateMillis < monthEnd }
    val previousMonthExpenses = expenseOnlyRecords.filter { it.dateMillis >= previousMonthStart && it.dateMillis < monthStart }
    val emotionStats = buildEmotionStats(monthExpenses, previousMonthExpenses)
    val activeEmotionStats = emotionStats.filter { it.count > 0 }
    val monthTotal = monthExpenses.sumOf { it.amount }
    val topSpendingEmotion = activeEmotionStats.maxByOrNull { it.totalAmount }
    val topCountEmotion = activeEmotionStats.maxByOrNull { it.count }
    val stressStat = emotionStats.firstOrNull { it.emotion == "스트레스" }
    val diagnosis = buildEmotionDiagnosis(
        monthExpenses = monthExpenses,
        emotionStats = emotionStats,
        stressStat = stressStat
    )

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

            if (activeEmotionStats.isNotEmpty()) {
                item {
                    EmotionPatternGraphCard(stats = activeEmotionStats)
                }
            }

            item {
                EmotionDiagnosisCard(diagnosis = diagnosis)
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
                        ?: "이번 달 감정 지출 기록이 아직 없어요."
                )
            }

            if (emotionStats.none { it.count > 0 }) {
                item {
                    EmotionEmptyState(onRecordClick = onBack)
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
                text = "감정별 지출 규모와 빈도를 한 달 단위로 비교해요.",
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
                    label = "지출 기록",
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
private fun EmotionDiagnosisCard(diagnosis: EmotionDiagnosis) {
    val accent = diagnosisRiskColor(diagnosis.riskLabel)
    val riskFraction = (diagnosis.riskScore / 100f).coerceIn(0f, 1f)

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "감정 소비 진단",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172033)
                )
                Surface(
                    color = accent.copy(alpha = 0.13f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        text = diagnosis.riskLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }

            Text(
                text = diagnosis.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF263244)
            )
            Text(
                text = diagnosis.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569)
            )

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
                        .fillMaxWidth(riskFraction)
                        .height(8.dp),
                    color = accent,
                    shape = RoundedCornerShape(8.dp)
                ) {}
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiagnosisMetricBox(
                    label = "주요 감정",
                    value = diagnosis.primaryEmotion,
                    modifier = Modifier.weight(1f)
                )
                DiagnosisMetricBox(
                    label = "스트레스 신호",
                    value = diagnosis.stressSignal,
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "추천 행동",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = diagnosis.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF263244)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmotionPatternGraphCard(stats: List<EmotionStat>) {
    val amountStats = stats
        .sortedWith(compareByDescending<EmotionStat> { it.totalAmount }.thenByDescending { it.count })
        .take(5)
    val countStats = stats
        .sortedWith(compareByDescending<EmotionStat> { it.count }.thenByDescending { it.totalAmount })
        .take(5)
    val maxAmount = amountStats.maxOfOrNull { it.totalAmount } ?: 1
    val maxCount = countStats.maxOfOrNull { it.count } ?: 1

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "소비 패턴 분석",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172033)
                )
                Text(
                    text = "감정별 지출 금액 및 빈도를 그래프로 시각화해요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }

            EmotionGraphGroup(
                title = "감정별 지출 금액",
                stats = amountStats,
                maxValue = maxAmount,
                valueText = { formatWon(it.totalAmount) },
                value = { it.totalAmount },
                barColor = Color(0xFFFF6651)
            )

            EmotionGraphGroup(
                title = "감정별 소비 빈도",
                stats = countStats,
                maxValue = maxCount,
                valueText = { "${it.count}회" },
                value = { it.count },
                barColor = Color(0xFF2F5D62)
            )
        }
    }
}

@Composable
private fun EmotionGraphGroup(
    title: String,
    stats: List<EmotionStat>,
    maxValue: Int,
    valueText: (EmotionStat) -> String,
    value: (EmotionStat) -> Int,
    barColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF263244)
        )
        stats.forEach { stat ->
            EmotionGraphBarRow(
                emotion = stat.emotion,
                valueText = valueText(stat),
                fraction = (value(stat).toFloat() / maxValue.coerceAtLeast(1).toFloat()).coerceIn(0.06f, 1f),
                barColor = barColor
            )
        }
    }
}

@Composable
private fun EmotionGraphBarRow(
    emotion: String,
    valueText: String,
    fraction: Float,
    barColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emotion,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF172033)
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(8.dp)
            ) {}
            Surface(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(12.dp),
                color = barColor,
                shape = RoundedCornerShape(8.dp)
            ) {}
        }
    }
}

@Composable
private fun DiagnosisMetricBox(
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
                style = MaterialTheme.typography.labelMedium,
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
                // share는 이번 달 총지출 중 해당 감정이 차지한 비율이다.
                .fillMaxWidth(share.coerceIn(0.04f, 1f))
                .height(8.dp),
            color = Color(0xFFFF6651),
            shape = RoundedCornerShape(8.dp)
        ) {}
    }
}

@Composable
private fun EmotionEmptyState(onRecordClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "분석할 지출 기록이 없어요",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )
            Text(
                text = "이번 달 지출 기록이 생기면 감정별 총액, 횟수, 평균 지출과 소비 패턴 그래프가 자동으로 채워집니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B)
            )
            TextButton(onClick = onRecordClick) {
                Text("기록 화면으로 이동")
            }
        }
    }
}

private fun buildEmotionStats(
    monthExpenses: List<Expense>,
    previousMonthExpenses: List<Expense>
): List<EmotionStat> {
    // 기본 감정은 기록이 없어도 순서 기준으로 유지하고, 새 감정이 생기면 뒤에 자연스럽게 포함한다.
    val defaultEmotions = listOf("기쁨", "슬픔", "스트레스", "외로움", "평온", "분노")
    val allEmotions = (defaultEmotions + monthExpenses.map { it.emotion } + previousMonthExpenses.map { it.emotion })
        .distinct()

    // 총지출이 0원일 때도 share 계산에서 0으로 나누지 않도록 1을 임시 기준값으로 사용한다.
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
        // 화면에서는 돈을 많이 쓴 감정부터 보이게 정렬한다.
        compareByDescending<EmotionStat> { it.totalAmount }
            .thenByDescending { it.count }
            .thenBy { defaultEmotions.indexOf(it.emotion).let { index -> if (index == -1) Int.MAX_VALUE else index } }
    )
}

private fun buildEmotionDiagnosis(
    monthExpenses: List<Expense>,
    emotionStats: List<EmotionStat>,
    stressStat: EmotionStat?
): EmotionDiagnosis {
    if (monthExpenses.isEmpty()) {
        return EmotionDiagnosis(
            riskLabel = "대기",
            riskScore = 0,
            title = "아직 진단할 지출 기록이 없어요",
            message = "지출과 감정을 함께 기록하면 이번 달 감정 소비 위험도를 자동으로 계산해요.",
            primaryEmotion = "없음",
            stressSignal = "기록 없음",
            recommendation = "시연할 때는 스트레스, 기쁨, 평온처럼 서로 다른 감정의 지출을 3개 이상 넣어보세요. 진단 카드가 더 또렷하게 보입니다."
        )
    }

    val monthTotal = monthExpenses.sumOf { it.amount }.takeIf { it > 0 } ?: 1
    val activeStats = emotionStats.filter { it.count > 0 }
    val primaryStat = activeStats.maxByOrNull { it.totalAmount }
    val primaryEmotion = primaryStat?.emotion ?: "없음"
    val primaryCategory = monthExpenses
        .filter { it.emotion == primaryEmotion }
        .groupingBy { it.category }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key ?: "기록 없음"
    val stressCount = stressStat?.count ?: 0
    val stressAmount = stressStat?.totalAmount ?: 0
    val stressShare = stressAmount.toFloat() / monthTotal.toFloat()
    val stressChange = stressStat?.countChange ?: 0

    // 위험도 점수는 각 신호를 독립 점수로 나눈 뒤 합산한다.
    // 이렇게 두면 발표 때도 "횟수, 증가 여부, 비중, 대표 감정" 기준을 바로 설명할 수 있다.
    val baseScore = 20
    val stressCountScore = (stressCount * 12).coerceAtMost(30)
    val stressIncreaseScore = if (stressChange > 0) 20 else 0
    val stressShareScore = if (stressShare >= 0.3f) 25 else 0
    val primaryStressScore = if (primaryEmotion == "스트레스") 15 else 0
    val riskScore = (
        baseScore +
            stressCountScore +
            stressIncreaseScore +
            stressShareScore +
            primaryStressScore
        ).coerceIn(0, 100)

    val riskLabel = when {
        riskScore >= 70 -> "높음"
        riskScore >= 40 -> "보통"
        else -> "낮음"
    }
    val title = when (riskLabel) {
        "높음" -> "감정 소비 주의 단계예요"
        "보통" -> "감정 소비를 관찰할 단계예요"
        else -> "감정 소비가 안정적인 편이에요"
    }
    val message = "${primaryEmotion} 감정에서 ${formatWon(primaryStat?.totalAmount ?: 0)}을 사용했고, ${primaryCategory} 지출과 가장 자주 연결됐어요."
    val stressSignal = when {
        stressCount == 0 -> "스트레스 기록 없음"
        stressChange > 0 -> "${stressCount}회, +${stressChange}회"
        stressChange < 0 -> "${stressCount}회, ${stressChange}회"
        else -> "${stressCount}회, 변화 없음"
    }
    val recommendation = when (riskLabel) {
        "높음" -> "${primaryCategory} 지출 전에는 메모에 소비 이유를 한 줄 적고 10분 뒤 결제해보세요. 감정성 지출을 줄이는 장치가 됩니다."
        "보통" -> "${primaryEmotion} 감정일 때 ${primaryCategory} 지출이 반복되는지 며칠 더 관찰해보세요. 반복되면 월 예산을 따로 잡는 게 좋아요."
        else -> "현재 패턴은 안정적이에요. 다음 목표는 감정별 평균 지출을 유지하면서 불필요한 반복 지출을 줄이는 것입니다."
    }

    return EmotionDiagnosis(
        riskLabel = riskLabel,
        riskScore = riskScore,
        title = title,
        message = message,
        primaryEmotion = primaryEmotion,
        stressSignal = stressSignal,
        recommendation = recommendation
    )
}

private fun buildStressInsight(stressStat: EmotionStat?): String {
    // 스트레스 소비는 앱의 핵심 인사이트라 별도 문장으로 뽑아 상단에 보여준다.
    if (stressStat == null || (stressStat.count == 0 && stressStat.previousCount == 0)) {
        return "스트레스 지출 기록이 생기면 지난달과 비교해서 변화를 알려줄게요."
    }

    return when {
        stressStat.countChange > 0 -> "스트레스 소비가 이번 달 ${stressStat.countChange}회 증가했어요. 평균은 ${formatWon(stressStat.averageAmount)}입니다."
        stressStat.countChange < 0 -> "스트레스 소비가 지난달보다 ${-stressStat.countChange}회 줄었어요."
        else -> "스트레스 소비 횟수는 지난달과 같아요. 이번 달 평균은 ${formatWon(stressStat.averageAmount)}입니다."
    }
}

private fun diagnosisRiskColor(riskLabel: String): Color {
    return when (riskLabel) {
        "높음" -> Color(0xFFFF6651)
        "보통" -> Color(0xFFF59E0B)
        "낮음" -> Color(0xFF0F766E)
        else -> Color(0xFF64748B)
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
