package com.example.emotionbudgetapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.emotionbudgetapp.viewmodel.ExpenseViewModel

private enum class AppDestination(val label: String) {
    Ledger("기록"),
    Report("통계"),
    Emotion("감정")
}

@Composable
fun EmotionBudgetAppRoot(viewModel: ExpenseViewModel) {
    // Root 화면은 ViewModel의 수입/지출 목록을 읽어서 하위 화면들에 전달한다.
    val expenses by viewModel.expenses.collectAsState()

    // 앱의 주요 화면은 하단 내비게이션으로 전환한다.
    // 버튼이 화면 위에 떠서 목록을 가리는 문제를 줄이고, 제출 영상에서도 구조가 더 명확해진다.
    var currentDestination by remember { mutableStateOf(AppDestination.Ledger) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestination.values().forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination,
                        onClick = { currentDestination = destination },
                        icon = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.Ledger -> ExpenseScreen(viewModel = viewModel)
                AppDestination.Report -> LedgerReportScreen(
                    expenses = expenses,
                    onBack = { currentDestination = AppDestination.Ledger }
                )
                AppDestination.Emotion -> EmotionAnalysisScreen(
                    expenses = expenses,
                    onBack = { currentDestination = AppDestination.Ledger }
                )
            }
        }
    }
}
