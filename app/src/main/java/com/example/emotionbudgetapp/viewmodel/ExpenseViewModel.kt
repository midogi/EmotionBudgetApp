package com.example.emotionbudgetapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.emotionbudgetapp.data.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpenseViewModel : ViewModel() {

    // Compose 화면이 관찰하는 지출 목록 상태.
    // MutableStateFlow 값이 바뀌면 collectAsState()를 쓰는 화면이 자동으로 다시 그려진다.
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    // 새 지출을 추가할 때마다 id가 겹치지 않도록 증가시키는 임시 번호값.
    // 나중에 Room DB를 붙이면 DB의 자동 증가 키로 대체할 수 있다.
    private var nextId = 1

    fun addExpense(
        amount: Int,
        category: String,
        emotion: String,
        memo: String,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        // 입력 폼에서 넘어온 값을 앱의 표준 데이터 모델인 Expense로 묶는다.
        val newExpense = Expense(
            id = nextId++,
            amount = amount,
            category = category,
            emotion = emotion,
            memo = memo,
            dateMillis = dateMillis
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
        dateMillis: Long
    ) {
        // 같은 id를 가진 기록만 copy로 바꾸고, 나머지 기록은 그대로 둔다.
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
        // 삭제할 id와 다른 기록만 남기는 방식으로 목록을 갱신한다.
        _expenses.value = _expenses.value.filter { it.id != expense.id }
    }

    fun getTotalAmount(): Int {
        // 모든 지출 금액을 더해 메인 요약 카드의 총지출로 사용한다.
        return _expenses.value.sumOf { it.amount }
    }

    fun getCategoryTotals(): Map<String, Int> {
        // 카테고리별 합계를 계산한다. 통계 화면이나 테스트에서 재사용하기 좋은 형태다.
        return _expenses.value
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
}
