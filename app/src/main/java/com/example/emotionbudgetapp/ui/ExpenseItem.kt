package com.example.emotionbudgetapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.emotionbudgetapp.data.Expense

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
