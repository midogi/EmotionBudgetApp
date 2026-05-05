package com.example.emotionbudgetapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.emotionbudgetapp.viewmodel.ExpenseViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("감정 가계부", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(12.dp))

        // 현재 입력된 전체 지출 합계를 보여준다.
        Text(
            text = "총 지출: ${viewModel.getTotalAmount()}원",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("금액") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        DropdownSelector("카테고리", category, categories) {
            category = it
        }

        Spacer(modifier = Modifier.height(8.dp))

        DropdownSelector("감정", emotion, emotions) {
            emotion = it
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = memo,
            onValueChange = { memo = it },
            label = { Text("메모") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // 금액 입력값이 숫자로 변환될 때만 기록을 추가한다.
                val amount = amountText.toIntOrNull()

                if (amount != null && amount > 0) {
                    viewModel.addExpense(amount, category, emotion, memo)

                    // 입력 후 금액과 메모 칸을 비운다.
                    amountText = ""
                    memo = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("기록 추가")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 지출 목록을 스크롤 가능한 리스트로 표시한다.
        LazyColumn {
            items(expenses) { expense ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
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
