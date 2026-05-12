package com.example.emotionbudgetapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.emotionbudgetapp.viewmodel.ExpenseViewModel

@Composable
fun EmotionBudgetAppRoot(viewModel: ExpenseViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    var showEmotionAnalysis by remember { mutableStateOf(false) }

    if (showEmotionAnalysis) {
        EmotionAnalysisScreen(
            expenses = expenses,
            onBack = { showEmotionAnalysis = false }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ExpenseScreen(viewModel = viewModel)
        ExtendedFloatingActionButton(
            text = { Text("감정 분석") },
            onClick = { showEmotionAnalysis = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = Color(0xFFFF6651),
            contentColor = Color.White
        )
    }
}
