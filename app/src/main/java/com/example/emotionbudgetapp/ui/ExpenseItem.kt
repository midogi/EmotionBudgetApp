package com.example.emotionbudget.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.emotionbudget.data.Expense

@Composable
fun ExpenseItem(
    expense: Expense,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(text = "${expense.amount}원")
            Text(text = "카테고리: ${expense.category}")
            Text(text = "감정: ${expense.emotion}")

            if (expense.memo.isNotBlank()) {
                Text(text = "메모: ${expense.memo}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("삭제")
            }
        }
    }
}
