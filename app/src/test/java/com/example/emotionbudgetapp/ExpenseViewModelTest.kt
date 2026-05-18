package com.example.emotionbudgetapp

import com.example.emotionbudgetapp.data.TransactionType
import com.example.emotionbudgetapp.viewmodel.ExpenseViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseViewModelTest {

    @Test
    fun addExpense_recordsAllFields() {
        val viewModel = ExpenseViewModel()

        // 지출을 추가했을 때 금액/카테고리/감정/메모/날짜/타입이 모두 저장되는지 확인한다.
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
        assertEquals(TransactionType.EXPENSE, expense.type)
    }

    @Test
    fun addExpense_separatesIncomeAndExpenseTotals() {
        val viewModel = ExpenseViewModel()

        viewModel.addExpense(
            amount = 3000000,
            category = "급여",
            emotion = "평온",
            memo = "월급",
            dateMillis = 1000L,
            type = TransactionType.INCOME
        )
        viewModel.addExpense(
            amount = 12000,
            category = "식비",
            emotion = "기쁨",
            memo = "점심",
            dateMillis = 2000L,
            type = TransactionType.EXPENSE
        )

        // 수입과 지출은 합계가 섞이면 안 되고, 잔액은 수입에서 지출을 뺀 값이어야 한다.
        assertEquals(3000000, viewModel.getIncomeTotal())
        assertEquals(12000, viewModel.getExpenseTotal())
        assertEquals(2988000, viewModel.getBalance())
        assertEquals(12000, viewModel.getTotalAmount())
        assertEquals(3000000, viewModel.getCategoryTotals(TransactionType.INCOME)["급여"])
        assertEquals(12000, viewModel.getCategoryTotals(TransactionType.EXPENSE)["식비"])
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
            category = "용돈",
            emotion = "평온",
            memo = "용돈 받음",
            dateMillis = 2000L,
            type = TransactionType.INCOME
        )

        val expense = viewModel.expenses.value.single()
        assertEquals(id, expense.id)
        assertEquals(7000, expense.amount)
        assertEquals("용돈", expense.category)
        assertEquals("평온", expense.emotion)
        assertEquals("용돈 받음", expense.memo)
        assertEquals(2000L, expense.dateMillis)
        assertEquals(TransactionType.INCOME, expense.type)
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
        viewModel.addExpense(3000000, "급여", "평온", "", 4000L, TransactionType.INCOME)

        // 기본 카테고리 합계는 지출 기준이고, 수입은 별도 타입을 넘겼을 때만 포함된다.
        val expenseTotals = viewModel.getCategoryTotals()
        val incomeTotals = viewModel.getCategoryTotals(TransactionType.INCOME)

        assertEquals(8000, expenseTotals["카페"])
        assertEquals(7000, expenseTotals["교통"])
        assertEquals(3000000, incomeTotals["급여"])
        assertEquals(15000, viewModel.getTotalAmount())
    }
}
