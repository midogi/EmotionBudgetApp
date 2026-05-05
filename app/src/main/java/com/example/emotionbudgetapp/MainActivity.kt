package com.example.emotionbudgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emotionbudgetapp.ui.ExpenseScreen
import com.example.emotionbudgetapp.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // ViewModel을 생성하고 화면에 전달한다.
            // 화면은 ViewModel의 데이터를 보고 UI를 갱신한다.
            val expenseViewModel: ExpenseViewModel = viewModel()

            ExpenseScreen(viewModel = expenseViewModel)
        }
    }
}
