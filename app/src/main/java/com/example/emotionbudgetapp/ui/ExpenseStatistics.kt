package com.example.emotionbudgetapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.emotionbudgetapp.data.Expense

@Composable
internal fun StatisticsScreen(
    recordCount: Int,
    totalAmount: Int,
    emotionStats: List<ExpenseStat>,
    categoryStats: List<ExpenseStat>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StatisticsSummaryCard(
            recordCount = recordCount,
            totalAmount = totalAmount,
            topEmotion = emotionStats.firstOrNull()?.label ?: "기록 없음",
            topCategory = categoryStats.firstOrNull()?.label ?: "기록 없음"
        )

        StatGroupCard(
            title = "감정별 지출",
            description = "어떤 감정에서 지출이 많이 생겼는지 보여줘요.",
            stats = emotionStats,
            accentColor = Color(0xFF7C3AED)
        )

        StatGroupCard(
            title = "카테고리별 지출",
            description = "가장 큰 비중을 차지하는 소비 항목을 확인해요.",
            stats = categoryStats,
            accentColor = Color(0xFF0F766E)
        )
    }
}

@Composable
private fun StatisticsSummaryCard(
    recordCount: Int,
    totalAmount: Int,
    topEmotion: String,
    topCategory: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "통계 요약",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryTile(
                    title = "총 기록",
                    value = "${recordCount}개",
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    title = "총 지출",
                    value = formatWon(totalAmount),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryTile(
                    title = "지출 1위 감정",
                    value = topEmotion,
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    title = "지출 1위 항목",
                    value = topCategory,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF3F6FA),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF5D6B82),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = Color(0xFF172033),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatGroupCard(
    title: String,
    description: String,
    stats: List<ExpenseStat>,
    accentColor: Color
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172033)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5D6B82)
                )
            }

            if (stats.isEmpty()) {
                Text(
                    text = "기록을 추가하면 통계가 표시됩니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5D6B82)
                )
            } else {
                stats.forEachIndexed { index, stat ->
                    StatRow(
                        stat = stat,
                        rank = index + 1,
                        accentColor = statColor(index, accentColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    stat: ExpenseStat,
    rank: Int,
    accentColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.14f),
                    shape = CircleShape
                ) {
                    Text(
                        text = rank.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Text(
                    text = stat.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF172033)
                )
            }
            Text(
                text = formatWon(stat.totalAmount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF172033)
            )
        }

        ChartBar(
            ratio = stat.ratio,
            color = accentColor
        )

        Text(
            text = "${stat.count}건 · ${formatPercent(stat.ratio)}",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF5D6B82)
        )
    }
}

@Composable
private fun ChartBar(
    ratio: Float,
    color: Color
) {
    val barRatio = ratio.coerceIn(0.04f, 1f)
    val remainingRatio = 1f - barRatio

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(Color(0xFFE7ECF3), RoundedCornerShape(99.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(barRatio)
                .background(color, RoundedCornerShape(99.dp))
        )

        if (remainingRatio > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(remainingRatio)
            )
        }
    }
}

internal data class ExpenseStat(
    val label: String,
    val totalAmount: Int,
    val count: Int,
    val ratio: Float
)

internal fun buildStats(
    expenses: List<Expense>,
    labelSelector: (Expense) -> String
): List<ExpenseStat> {
    val totalAmount = expenses.sumOf { it.amount }

    if (totalAmount == 0) {
        return emptyList()
    }

    return expenses
        .groupBy(labelSelector)
        .map { (label, group) ->
            val groupTotal = group.sumOf { it.amount }

            ExpenseStat(
                label = label,
                totalAmount = groupTotal,
                count = group.size,
                ratio = groupTotal / totalAmount.toFloat()
            )
        }
        .sortedByDescending { it.totalAmount }
}

private fun statColor(index: Int, fallback: Color): Color {
    val palette = listOf(
        Color(0xFF7C3AED),
        Color(0xFF0F766E),
        Color(0xFF2563EB),
        Color(0xFFE11D48),
        Color(0xFFD97706),
        Color(0xFF16A34A)
    )

    return palette.getOrElse(index) { fallback }
}
