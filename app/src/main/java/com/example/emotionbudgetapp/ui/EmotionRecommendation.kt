package com.example.emotionbudgetapp.ui

import java.util.Locale

internal data class EmotionRecommendation(
    val emotion: String,
    val keyword: String
)

private data class EmotionRule(
    val emotion: String,
    val keywords: List<String>
)

private val emotionRules = listOf(
    EmotionRule("분노", listOf("화나", "화남", "분노", "싸움", "억울", "짜증", "빡")),
    EmotionRule("스트레스", listOf("스트레스", "피곤", "야근", "과제", "시험", "불안", "걱정", "압박", "충동", "보상", "폭식", "힘들")),
    EmotionRule("외로움", listOf("외로", "혼자", "쓸쓸", "공허", "허전", "심심")),
    EmotionRule("슬픔", listOf("슬픔", "슬퍼", "우울", "속상", "눈물", "이별", "상실", "아쉽")),
    EmotionRule("평온", listOf("평온", "여유", "산책", "휴식", "명상", "조용", "차분", "쉼")),
    EmotionRule("기쁨", listOf("기쁨", "행복", "좋아", "신남", "축하", "기념", "합격", "성공", "월급", "데이트", "여행", "맛있"))
)

internal fun recommendEmotionFromMemo(
    memo: String,
    emotions: List<String>
): EmotionRecommendation? {
    val normalizedMemo = memo.lowercase(Locale.KOREA).trim()

    if (normalizedMemo.length < 2) {
        return null
    }

    return emotionRules.firstNotNullOfOrNull { rule ->
        if (rule.emotion !in emotions) {
            null
        } else {
            rule.keywords
                .firstOrNull { keyword -> normalizedMemo.contains(keyword.lowercase(Locale.KOREA)) }
                ?.let { keyword ->
                    EmotionRecommendation(
                        emotion = rule.emotion,
                        keyword = keyword
                    )
                }
        }
    }
}
