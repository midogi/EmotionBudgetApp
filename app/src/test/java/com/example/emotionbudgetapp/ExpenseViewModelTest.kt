package com.example.emotionbudgetapp

import com.example.emotionbudgetapp.viewmodel.ExpenseViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseViewModelTest {

    @Test
    fun addExpense_recordsAllFields() {
        val viewModel = ExpenseViewModel()

        // 지출을 추가했을 때 금액/카테고리/감정/메모/날짜가 모두 저장되는지 확인한다.
        viewModel.addExpense(
            amount = 12000,
            category = "식비",
            emotion = "기쁨",
            memo = "친구와 점심",
            dateMillis = 1000L
        )

        val expense = viewModel.expenses.value.single()
        assertEquals(12000, expense.amount)
        assertEquals("식비", expense.category)
        assertEquals("기쁨", expense.emotion)
        assertEquals("친구와 점심", expense.memo)
        assertEquals(1000L, expense.dateMillis)
    }

    @Test
    fun updateExpense_changesExistingRecord() {
        val viewModel = ExpenseViewModel()
        viewModel.addExpense(5000, "카페", "평온", "커피", 1000L)
        val id = viewModel.expenses.value.single().id

        // 같은 id의 기록을 수정하면 새 값으로 교체되고 id는 유지되어야 한다.
        viewModel.updateExpense(
            id = id,
            amount = 7000,
            category = "문화",
            emotion = "기쁨",
            memo = "영화",
            dateMillis = 2000L
        )

        val expense = viewModel.expenses.value.single()
        assertEquals(id, expense.id)
        assertEquals(7000, expense.amount)
        assertEquals("문화", expense.category)
        assertEquals("기쁨", expense.emotion)
        assertEquals("영화", expense.memo)
        assertEquals(2000L, expense.dateMillis)
    }

    @Test
    fun deleteExpense_removesOnlySelectedRecord() {
        val viewModel = ExpenseViewModel()
        viewModel.addExpense(3000, "교통", "평온", "버스", 1000L)
        viewModel.addExpense(9000, "식비", "스트레스", "야식", 2000L)
        val firstExpense = viewModel.expenses.value.first()

        // 삭제한 기록만 빠지고 나머지 기록은 유지되는지 확인한다.
        viewModel.deleteExpense(firstExpense)

        assertEquals(1, viewModel.expenses.value.size)
        assertEquals("식비", viewModel.expenses.value.single().category)
    }

    @Test
    fun getCategoryTotals_sumsExpensesByCategory() {
        val viewModel = ExpenseViewModel()
        viewModel.addExpense(3000, "카페", "평온", "", 1000L)
        viewModel.addExpense(5000, "카페", "기쁨", "", 2000L)
        viewModel.addExpense(7000, "교통", "평온", "", 3000L)

        // 같은 카테고리끼리 금액을 묶는 집계 로직을 검증한다.
        val totals = viewModel.getCategoryTotals()

        assertEquals(8000, totals["카페"])
        assertEquals(7000, totals["교통"])
        assertEquals(15000, viewModel.getTotalAmount())
    }
}
