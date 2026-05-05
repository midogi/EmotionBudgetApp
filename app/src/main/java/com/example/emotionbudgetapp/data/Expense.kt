package com.example.emotionbudgetapp.data

// 지출 기록 1개를 표현하는 데이터 클래스
// 금액, 카테고리, 감정, 메모를 하나의 객체로 관리한다.
data class Expense(
    val id: Int,          // 각 기록을 구분하기 위한 고유 번호
    val amount: Int,      // 지출 금액
    val category: String, // 식비, 교통, 쇼핑 등 지출 분류
    val emotion: String,  // 지출 당시의 감정
    val memo: String      // 사용자가 남기는 간단한 메모
)
