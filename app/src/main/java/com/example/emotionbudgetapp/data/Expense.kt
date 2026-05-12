package com.example.emotionbudgetapp.data

// 지출 기록 1개를 표현하는 데이터 클래스.
// 날짜를 함께 저장해서 월별/일별 분석과 검색의 기준으로 사용할 수 있다.
data class Expense(
    val id: Int,
    val amount: Int,
    val category: String,
    val emotion: String,
    val memo: String,
    val dateMillis: Long
)
