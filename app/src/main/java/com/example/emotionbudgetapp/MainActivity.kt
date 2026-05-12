package com.example.emotionbudgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emotionbudgetapp.ui.EmotionBudgetAppRoot
import com.example.emotionbudgetapp.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // 앱 전체에서 공유할 지출 ViewModel을 Compose 생명주기에 맞춰 생성한다.
            val expenseViewModel: ExpenseViewModel = viewModel()

            // 실제 화면 전환과 메인 UI 구성은 Root Composable이 담당한다.
            EmotionBudgetAppRoot(viewModel = expenseViewModel)
        }
    }
}
