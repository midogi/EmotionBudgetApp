package com.example.emotionbudget.viewmodel

import androidx.lifecycle.ViewModel
import com.example.emotionbudget.data.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpenseViewModel : ViewModel() {

    // 화면에서 관찰할 지출 목록 데이터
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    // 새 기록을 추가할 때마다 id가 겹치지 않도록 증가시킨다.
    private var nextId = 1

    fun addExpense(
        amount: Int,
        category: String,
        emotion: String,
        memo: String
    ) {
        // 사용자가 입력한 값을 Expense 객체로 만든다.
        val newExpense = Expense(
            id = nextId++,
            amount = amount,
            category = category,
            emotion = emotion,
            memo = memo
        )

        // 기존 목록 뒤에 새 지출 기록을 추가한다.
        // StateFlow 값이 바뀌면 Compose 화면이 자동으로 다시 그려진다.
        _expenses.value = _expenses.value + newExpense
    }

    fun deleteExpense(expense: Expense) {
        // 선택한 id와 다른 기록만 남겨서 삭제 효과를 만든다.
        _expenses.value = _expenses.value.filter { it.id != expense.id }
    }

    fun getTotalAmount(): Int {
        // 모든 지출 금액을 더해서 총 지출을 계산한다.
        return _expenses.value.sumOf { it.amount }
    }
}
