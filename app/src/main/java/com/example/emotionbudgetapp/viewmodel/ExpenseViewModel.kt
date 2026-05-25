package com.example.emotionbudgetapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.emotionbudgetapp.data.Expense
import com.example.emotionbudgetapp.data.TransactionType
import java.util.Calendar
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

    fun loadSampleData(referenceMillis: Long = System.currentTimeMillis()) {
        val today = normalizeDay(referenceMillis)
        val yesterday = addDays(today, -1)
        val twoDaysAgo = addDays(today, -2)
        val fourDaysAgo = addDays(today, -4)
        val previousMonth = addMonths(startOfMonth(referenceMillis), -1)

        // 발표/시연용 데이터는 현재 입력값과 섞이지 않도록 목록을 교체한다.
        nextId = 1
        _expenses.value = listOf(
            Expense(
                id = nextId++,
                amount = 3000000,
                category = "급여",
                emotion = "평온",
                memo = "월급",
                dateMillis = today,
                type = TransactionType.INCOME
            ),
            Expense(
                id = nextId++,
                amount = 4800,
                category = "카페",
                emotion = "스트레스",
                memo = "과제하다가 커피",
                dateMillis = today,
                type = TransactionType.EXPENSE
            ),
            Expense(
                id = nextId++,
                amount = 23000,
                category = "식비",
                emotion = "기쁨",
                memo = "친구와 저녁",
                dateMillis = yesterday,
                type = TransactionType.EXPENSE
            ),
            Expense(
                id = nextId++,
                amount = 59000,
                category = "쇼핑",
                emotion = "스트레스",
                memo = "기분 전환 쇼핑",
                dateMillis = twoDaysAgo,
                type = TransactionType.EXPENSE
            ),
            Expense(
                id = nextId++,
                amount = 14500,
                category = "교통",
                emotion = "평온",
                memo = "지하철/버스",
                dateMillis = fourDaysAgo,
                type = TransactionType.EXPENSE
            ),
            Expense(
                id = nextId++,
                amount = 12000,
                category = "카페",
                emotion = "스트레스",
                memo = "시험 준비",
                dateMillis = addDays(previousMonth, 12),
                type = TransactionType.EXPENSE
            ),
            Expense(
                id = nextId++,
                amount = 18000,
                category = "식비",
                emotion = "외로움",
                memo = "혼밥",
                dateMillis = addDays(previousMonth, 16),
                type = TransactionType.EXPENSE
            )
        )
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

    private fun normalizeDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun startOfMonth(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun addDays(millis: Long, amount: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            add(Calendar.DAY_OF_MONTH, amount)
        }.timeInMillis
    }

    private fun addMonths(millis: Long, amount: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            add(Calendar.MONTH, amount)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
