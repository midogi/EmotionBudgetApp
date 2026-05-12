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
            val expenseViewModel: ExpenseViewModel = viewModel()

            EmotionBudgetAppRoot(viewModel = expenseViewModel)
        }
    }
}
