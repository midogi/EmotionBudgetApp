package com.example.emotionbudgetapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.emotionbudgetapp.data.Expense
import com.example.emotionbudgetapp.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseItem(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = expense.type == TransactionType.INCOME
    val amountColor = if (isIncome) Color(0xFF3B82F6) else Color(0xFFFF6651)
    val typeLabel = expense.type.label

    // 목록에 보이는 수입/지출 기록 1개를 카드 형태로 표시한다.
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 왼쪽에는 타입/분류와 날짜/감정을 보여줘 기록을 빠르게 구분할 수 있게 한다.
                    Text(
                        text = "$typeLabel · ${expense.category}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF172033)
                    )
                    Text(
                        text = if (isIncome) {
                            "${formatDate(expense.dateMillis)} · 수입 기록"
                        } else {
                            "${formatDate(expense.dateMillis)} · 감정: ${expense.emotion}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5D6B82)
                    )
                }
                // 수입은 +, 지출은 -로 보여줘 목록에서 흐름이 바로 읽히게 한다.
                Text(
                    text = formatSignedAmount(expense.amount, expense.type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }

            // 수입/지출 타입, 카테고리, 감정은 통계의 기준이 되는 값이라 배지로 한 번 더 노출한다.
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoBadge(text = typeLabel, color = if (isIncome) Color(0xFFEAF2FF) else Color(0xFFFFEDE9))
                InfoBadge(text = expense.category)
                if (!isIncome) {
                    InfoBadge(text = expense.emotion)
                }
            }

            if (expense.memo.isNotBlank()) {
                Text(
                    text = expense.memo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF263244)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("수정")
                }
                TextButton(onClick = onDelete) {
                    Text("삭제")
                }
            }
        }
    }
}

@Composable
private fun InfoBadge(text: String, color: Color = Color(0xFFE8F0F2)) {
    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF2F5D62)
            )
        }
    }
}

private fun formatSignedAmount(amount: Int, type: TransactionType): String {
    val prefix = if (type == TransactionType.INCOME) "+" else "-"
    return prefix + NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원"
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(millis))
}
