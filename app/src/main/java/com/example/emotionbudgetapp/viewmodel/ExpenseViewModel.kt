package com.example.emotionbudgetapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.emotionbudgetapp.data.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpenseViewModel : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private var nextId = 1

    fun addExpense(
        amount: Int,
        category: String,
        emotion: String,
        memo: String,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        val newExpense = Expense(
            id = nextId++,
            amount = amount,
            category = category,
            emotion = emotion,
            memo = memo,
            dateMillis = dateMillis
        )

        _expenses.value = _expenses.value + newExpense
    }

    fun updateExpense(
        id: Int,
        amount: Int,
        category: String,
        emotion: String,
        memo: String,
        dateMillis: Long
    ) {
        _expenses.value = _expenses.value.map { expense ->
            if (expense.id == id) {
                expense.copy(
                    amount = amount,
                    category = category,
                    emotion = emotion,
                    memo = memo,
                    dateMillis = dateMillis
                )
            } else {
                expense
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        _expenses.value = _expenses.value.filter { it.id != expense.id }
    }

    fun getTotalAmount(): Int {
        return _expenses.value.sumOf { it.amount }
    }

    fun getCategoryTotals(): Map<String, Int> {
        return _expenses.value
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
}
