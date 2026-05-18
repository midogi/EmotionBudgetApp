package com.example.emotionbudgetapp.data

// 한 기록이 수입인지 지출인지 구분하는 타입.
// 화면과 통계는 이 값을 기준으로 수입 합계, 지출 합계, 잔액을 따로 계산한다.
enum class TransactionType(val label: String) {
    INCOME("수입"),
    EXPENSE("지출")
}

// 수입/지출 기록 1개를 표현하는 데이터 클래스.
// 화면, 통계, 검색 기능이 모두 이 모델을 기준으로 데이터를 읽는다.
data class Expense(
    // 목록에서 수정/삭제할 대상을 구분하기 위한 고유 번호.
    val id: Int,

    // 사용자가 입력한 금액. type이 INCOME이면 수입, EXPENSE이면 지출 금액이다.
    val amount: Int,

    // 식비, 교통, 급여, 용돈처럼 기록을 묶어 볼 때 쓰는 분류값.
    val category: String,

    // EmotionBudgetApp의 핵심 값. 지출 기록일 때 감정별 분석 계산에 사용된다.
    val emotion: String,

    // 사용자가 기록 상황을 기억하기 위해 남기는 짧은 설명.
    val memo: String,

    // 하루의 시작 시각을 millis로 저장한다. 월별/일별 필터와 날짜 표시의 기준이다.
    val dateMillis: Long,

    // 이 기록이 수입인지 지출인지 나타낸다. 기존 기록 호환을 위해 기본값은 지출이다.
    val type: TransactionType = TransactionType.EXPENSE
)
