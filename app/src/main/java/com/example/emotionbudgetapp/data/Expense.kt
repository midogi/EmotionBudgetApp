package com.example.emotionbudgetapp.data

// 지출 기록 1개를 표현하는 데이터 클래스.
// 화면, 통계, 검색 기능이 모두 이 모델을 기준으로 데이터를 읽는다.
data class Expense(
    // 목록에서 수정/삭제할 대상을 구분하기 위한 고유 번호.
    val id: Int,

    // 사용자가 입력한 지출 금액. 현재 앱은 수입 없이 지출만 저장한다.
    val amount: Int,

    // 식비, 교통, 쇼핑처럼 지출을 묶어 볼 때 쓰는 분류값.
    val category: String,

    // EmotionBudgetApp의 핵심 값. 감정별 분석 화면에서 총액/횟수/평균 계산에 사용된다.
    val emotion: String,

    // 사용자가 지출 상황을 기억하기 위해 남기는 짧은 설명.
    val memo: String,

    // 하루의 시작 시각을 millis로 저장한다. 월별/일별 필터와 날짜 표시의 기준이다.
    val dateMillis: Long
)
