package com.example.emotionbudgetapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.emotionbudgetapp.data.Expense
import com.example.emotionbudgetapp.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpenseViewModel : ViewModel() {

    // Compose 화면이 관찰하는 수입/지출 목록 상태.
    // MutableStateFlow 값이 바뀌면 collectAsState()를 쓰는 화면이 자동으로 다시 그려진다.
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    // 새 기록을 추가할 때마다 id가 겹치지 않도록 증가시키는 임시 번호값.
    // 나중에 Room DB를 붙이면 DB의 자동 증가 키로 대체할 수 있다.
    private var nextId = 1

    fun addExpense(
        amount: Int,
        category: String,
        emotion: String,
        memo: String,
        dateMillis: Long = System.currentTimeMillis(),
        type: TransactionType = TransactionType.EXPENSE
    ) {
        // 입력 폼에서 넘어온 값을 앱의 표준 데이터 모델인 Expense로 묶는다.
        val newExpense = Expense(
            id = nextId++,
            amount = amount,
            category = category,
            emotion = emotion,
            memo = memo,
            dateMillis = dateMillis,
            type = type
        )

        // 기존 리스트를 직접 수정하지 않고 새 리스트를 만들어 StateFlow에 넣는다.
        _expenses.value = _expenses.value + newExpense
    }

    fun updateExpense(
        id: Int,
        amount: Int,
        category: String,
        emotion: String,
        memo: String,
        dateMillis: Long,
        type: TransactionType = TransactionType.EXPENSE
    ) {
        // 같은 id를 가진 기록만 copy로 바꾸고, 나머지 기록은 그대로 둔다.
        _expenses.value = _expenses.value.map { expense ->
            if (expense.id == id) {
                expense.copy(
                    amount = amount,
                    category = category,
                    emotion = emotion,
                    memo = memo,
                    dateMillis = dateMillis,
                    type = type
                )
            } else {
                expense
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        // 삭제할 id와 다른 기록만 남기는 방식으로 목록을 갱신한다.
        _expenses.value = _expenses.value.filter { it.id != expense.id }
    }

    fun getTotalAmount(): Int {
        // 기존 호출 호환을 위해 지출 총액을 반환한다.
        return getExpenseTotal()
    }

    fun getIncomeTotal(): Int {
        return _expenses.value
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }

    fun getExpenseTotal(): Int {
        return _expenses.value
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }

    fun getBalance(): Int {
        return getIncomeTotal() - getExpenseTotal()
    }

    fun getCategoryTotals(type: TransactionType = TransactionType.EXPENSE): Map<String, Int> {
        // 타입별 카테고리 합계를 계산한다. 기본값은 지출 카테고리 합계다.
        return _expenses.value
            .filter { it.type == type }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
}
