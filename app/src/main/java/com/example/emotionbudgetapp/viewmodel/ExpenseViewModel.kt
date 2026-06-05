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

    fun loadSampleData() {
        loadSampleData(System.currentTimeMillis())
    }

    @Suppress("UNUSED_PARAMETER")
    fun loadSampleData(referenceMillis: Long) {
        // 발표/시연용 데이터는 현재 입력값과 섞이지 않도록 목록을 교체한다.
        // referenceMillis는 기존 테스트/호출 호환을 위해 유지하고, 제출 영상 기준인 2026년 데이터로 고정한다.
        nextId = 1
        fun sample(
            amount: Int,
            category: String,
            emotion: String,
            memo: String,
            month: Int,
            day: Int,
            type: TransactionType = TransactionType.EXPENSE
        ): Expense {
            return Expense(
                id = nextId++,
                amount = amount,
                category = category,
                emotion = emotion,
                memo = memo,
                dateMillis = fixedDateMillis(year = 2026, month = month, day = day),
                type = type
            )
        }

        _expenses.value = listOf(
            sample(
                amount = 3000000,
                category = "급여",
                emotion = "평온",
                memo = "6월 월급",
                month = 6,
                day = 1,
                type = TransactionType.INCOME
            ),
            sample(
                amount = 200000,
                category = "용돈",
                emotion = "평온",
                memo = "중간 용돈",
                month = 6,
                day = 15,
                type = TransactionType.INCOME
            ),
            sample(
                amount = 2800000,
                category = "급여",
                emotion = "평온",
                memo = "4월 월급",
                month = 4,
                day = 1,
                type = TransactionType.INCOME
            ),
            sample(
                amount = 2900000,
                category = "급여",
                emotion = "평온",
                memo = "5월 월급",
                month = 5,
                day = 1,
                type = TransactionType.INCOME
            ),
            sample(
                amount = 3000000,
                category = "급여",
                emotion = "평온",
                memo = "7월 월급",
                month = 7,
                day = 1,
                type = TransactionType.INCOME
            ),

            // 2026년 4월: 이전 달 비교와 월 이동 시연용 기록.
            sample(22000, "식비", "기쁨", "개강 후 점심", 4, 3),
            sample(14500, "교통", "평온", "정기 이동", 4, 9),
            sample(18000, "카페", "스트레스", "과제 커피", 4, 18),
            sample(42000, "문화", "기쁨", "영화 관람", 4, 25),

            // 2026년 5월: 전월 대비 감정 소비 비교용 기록.
            sample(18000, "식비", "외로움", "혼밥", 5, 4),
            sample(27000, "카페", "스트레스", "시험 준비", 5, 12),
            sample(69000, "쇼핑", "분노", "충동구매", 5, 20),
            sample(13200, "교통", "평온", "버스와 지하철", 5, 28),

            // 2026년 6월: 주별/일별 통계가 확실히 보이도록 주차별로 나눈 기록.
            sample(
                amount = 4800,
                category = "카페",
                emotion = "스트레스",
                memo = "과제하다가 커피",
                month = 6,
                day = 2
            ),
            sample(
                amount = 55555,
                category = "식비",
                emotion = "기쁨",
                memo = "친구와 저녁",
                month = 6,
                day = 5
            ),
            sample(14500, "교통", "평온", "버스와 지하철", 6, 6),
            sample(
                amount = 59000,
                category = "카페",
                emotion = "스트레스",
                memo = "스트레스 디저트",
                month = 6,
                day = 9
            ),
            sample(21000, "식비", "외로움", "혼밥", 6, 11),
            sample(32000, "문화", "기쁨", "전시 관람", 6, 14),
            sample(76000, "쇼핑", "스트레스", "기분 전환 쇼핑", 6, 16),
            sample(17000, "식비", "평온", "가벼운 점심", 6, 18),
            sample(13500, "카페", "분노", "답답해서 커피", 6, 20),
            sample(12500, "교통", "평온", "약속 이동", 6, 23),
            sample(
                amount = 23000,
                category = "식비",
                emotion = "기쁨",
                memo = "친구와 점심",
                month = 6,
                day = 25
            ),
            sample(31000, "카페", "스트레스", "마감 전 카페", 6, 27),
            sample(
                amount = 42000,
                category = "문화",
                emotion = "우울",
                memo = "기분 전환 공연",
                month = 6,
                day = 30
            ),

            // 2026년 7월: 다음 달 이동 시연용 기록.
            sample(26000, "식비", "기쁨", "방학 점심", 7, 3),
            sample(22000, "카페", "스트레스", "계획 정리", 7, 8),
            sample(88000, "쇼핑", "평온", "필요한 물건 구매", 7, 15),
            sample(35000, "문화", "기쁨", "친구와 영화", 7, 24)
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

    private fun fixedDateMillis(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
