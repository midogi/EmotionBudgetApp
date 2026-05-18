package com.example.emotionbudgetapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    // Root 화면은 ViewModel의 지출 목록을 읽어서 하위 화면들에 전달한다.
    val expenses by viewModel.expenses.collectAsState()

    // 현재는 Navigation 라이브러리 없이 Boolean 상태로 화면 전환을 처리한다.
    var showEmotionAnalysis by remember { mutableStateOf(false) }

    if (showEmotionAnalysis) {
        // 감정 분석 화면은 목록을 보기만 하므로 expenses List와 뒤로가기 콜백만 받는다.
        EmotionAnalysisScreen(
            expenses = expenses,
            onBack = { showEmotionAnalysis = false }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 기본 가계부 입력/목록 화면.
        ExpenseScreen(viewModel = viewModel)

        // 어디서든 감정 분석으로 넘어갈 수 있게 메인 화면 위에 버튼을 띄운다.
        Button(
            onClick = { showEmotionAnalysis = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6651),
                contentColor = Color.White
            )
        ) {
            Text("감정 분석")
        }
    }
}
