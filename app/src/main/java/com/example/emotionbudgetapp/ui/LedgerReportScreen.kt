package com.example.emotionbudgetapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.emotionbudgetapp.data.Expense
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 상세 통계 화면의 상단 탭 종류.
// 각 탭은 같은 월 데이터(monthExpenses)를 다른 방식으로 보여준다.
private enum class ReportTab(val label: String) {
    Daily("일일"),
    Calendar("달력"),
    Weekly("주별"),
    Monthly("월별"),
    Summary("요약")
}

// 하루 단위로 묶은 지출 목록과 그날의 합계를 함께 들고 다니는 화면용 모델.
private data class DailyExpenseGroup(
    val dayMillis: Long,
    val expenses: List<Expense>,
    val totalAmount: Int
)

// 달력 칸 하나를 표현한다. 빈 칸은 dayNumber가 null이다.
private data class CalendarDayCell(
    val dayNumber: Int?,
    val dayMillis: Long?,
    val totalAmount: Int
)

@Composable
fun LedgerReportScreen(
    expenses: List<Expense>,
    onBack: () -> Unit
) {
    // 현재 보고 있는 달. 이전/다음 버튼을 누르면 이 값이 바뀌고 화면 전체 통계가 다시 계산된다.
    var visibleMonthMillis by remember { mutableStateOf(startOfMonth(System.currentTimeMillis())) }
    var selectedTab by remember { mutableStateOf(ReportTab.Daily) }

    // 월 시작~다음 달 시작 사이의 기록만 골라서 이번 화면의 기준 데이터로 사용한다.
    val monthStart = startOfMonth(visibleMonthMillis)
    val monthEnd = addMonths(monthStart, 1)
    val monthExpenses = expenses
        .filter { it.dateMillis >= monthStart && it.dateMillis < monthEnd }
        .sortedWith(compareByDescending<Expense> { it.dateMillis }.thenByDescending { it.id })
    val monthExpenseTotal = monthExpenses.sumOf { it.amount }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                ReportHeader(
                    monthMillis = monthStart,
                    incomeTotal = 0,
                    expenseTotal = monthExpenseTotal,
                    onBack = onBack,
                    onPreviousMonth = { visibleMonthMillis = addMonths(monthStart, -1) },
                    onNextMonth = { visibleMonthMillis = addMonths(monthStart, 1) }
                )
            }

            item {
                ReportTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // 선택된 탭에 따라 같은 월 데이터를 다른 Composable로 전달한다.
            when (selectedTab) {
                ReportTab.Daily -> {
                    val groups = buildDailyGroups(monthExpenses)
                    if (groups.isEmpty()) {
                        item {
                            ReportEmptyState(message = "이번 달에 등록된 지출 기록이 없어요.")
                        }
                    } else {
                        items(groups, key = { it.dayMillis }) { group ->
                            DailyGroupCard(group = group)
                        }
                    }
                }

                ReportTab.Calendar -> {
                    item {
                        CalendarMonthView(
                            monthMillis = monthStart,
                            expenses = monthExpenses
                        )
                    }
                }

                ReportTab.Weekly -> {
                    item {
                        WeeklyReportView(monthMillis = monthStart, expenses = monthExpenses)
                    }
                }

                ReportTab.Monthly -> {
                    item {
                        MonthlyReportView(expenses = monthExpenses)
                    }
                }

                ReportTab.Summary -> {
                    item {
                        SummaryReportView(
                            monthMillis = monthStart,
                            expenses = monthExpenses
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportHeader(
    monthMillis: Long,
    incomeTotal: Int,
    expenseTotal: Int,
    onBack: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("< 입력")
            }
            Text(
                text = "가계부",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2933)
            )
            Text(
                text = "통계",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPreviousMonth) {
                Text("<")
            }
            Text(
                text = formatMonthTitle(monthMillis),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF263244)
            )
            TextButton(onClick = onNextMonth) {
                Text(">")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ReportAmountMetric(
                label = "수입",
                value = formatWon(incomeTotal),
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            ReportAmountMetric(
                label = "지출",
                value = formatWon(expenseTotal),
                color = Color(0xFFFF6651),
                modifier = Modifier.weight(1f)
            )
            ReportAmountMetric(
                label = "합계",
                value = formatSignedWon(incomeTotal - expenseTotal),
                color = Color(0xFF1F2933),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReportAmountMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8792A2)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ReportTabs(
    selectedTab: ReportTab,
    onTabSelected: (ReportTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ReportTab.values().forEach { tab ->
            val selected = selectedTab == tab
            val background = if (selected) Color(0xFFFF6651) else Color(0xFFF2F4F7)
            val content = if (selected) Color.White else Color(0xFF8A94A3)
            Surface(
                modifier = Modifier.weight(1f),
                color = background,
                shape = RoundedCornerShape(8.dp)
            ) {
                TextButton(onClick = { onTabSelected(tab) }) {
                    Text(
                        text = tab.label,
                        color = content,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyGroupCard(group: DailyExpenseGroup) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = dayOfMonth(group.dayMillis),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = formatMonthDot(group.dayMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9AA3AE)
                    )
                    Surface(
                        color = dayBadgeColor(group.dayMillis),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            text = dayOfWeekLabel(group.dayMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    text = "0원",
                    color = Color(0xFF3B82F6),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatWon(group.totalAmount),
                    color = Color(0xFFFF6651),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        group.expenses.forEach { expense ->
            DailyExpenseRow(expense = expense)
        }
    }
}

@Composable
private fun DailyExpenseRow(expense: Expense) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = expense.category,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFA1A8B3)
            )
            Text(
                text = expense.emotion,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB4BBC5)
            )
        }
        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = expense.memo.ifBlank { expense.category },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF404854)
            )
            Text(
                text = expense.emotion,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB4BBC5)
            )
        }
        Text(
            text = formatWon(expense.amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6651)
        )
    }
}

@Composable
private fun CalendarMonthView(
    monthMillis: Long,
    expenses: List<Expense>
) {
    // 달력 화면은 먼저 한 달의 모든 칸을 만들고, 7개씩 잘라 한 주로 표현한다.
    val weeks = buildCalendarCells(monthMillis, expenses).chunked(7)

    Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach { label ->
                Text(
                    modifier = Modifier.weight(1f),
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8A94A3),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                week.forEach { cell ->
                    CalendarDayCellView(
                        cell = cell,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCellView(
    cell: CalendarDayCell,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (cell.dayNumber == null) Color.Transparent else Color(0xFFF7F8FA),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = cell.dayNumber?.toString().orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2933)
            )
            Text(
                text = if (cell.totalAmount > 0) formatCompactWon(cell.totalAmount) else "",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFF6651)
            )
        }
    }
}

@Composable
private fun WeeklyReportView(monthMillis: Long, expenses: List<Expense>) {
    // Calendar.WEEK_OF_MONTH 값으로 묶어 같은 달의 몇 주차 소비인지 계산한다.
    val weekGroups = expenses
        .groupBy { weekOfMonth(it.dateMillis) }
        .toSortedMap()

    Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (weekGroups.isEmpty()) {
            ReportEmptyState(message = "이번 달 주별 통계가 아직 없어요.")
        } else {
            weekGroups.forEach { (week, weekExpenses) ->
                ReportSummaryRow(
                    title = "${week}주차",
                    subtitle = weekRangeLabel(monthMillis, week),
                    amount = weekExpenses.sumOf { it.amount },
                    accent = Color(0xFFFF6651)
                )
            }
        }
    }
}

@Composable
private fun MonthlyReportView(expenses: List<Expense>) {
    // 같은 월 데이터 안에서 카테고리별/감정별 합계를 따로 계산한다.
    val categoryTotals = expenses
        .groupBy { it.category }
        .map { it.key to it.value.sumOf { expense -> expense.amount } }
        .sortedByDescending { it.second }
    val emotionTotals = expenses
        .groupBy { it.emotion }
        .map { it.key to it.value.sumOf { expense -> expense.amount } }
        .sortedByDescending { it.second }

    Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ReportBreakdownCard(title = "카테고리별 지출", rows = categoryTotals)
        ReportBreakdownCard(title = "감정별 지출", rows = emotionTotals)
    }
}

@Composable
private fun SummaryReportView(monthMillis: Long, expenses: List<Expense>) {
    // 기록이 있는 날짜 수를 기준으로 하루 평균 지출을 계산한다.
    val spendDays = expenses.map { startOfDay(it.dateMillis) }.distinct().size
    val total = expenses.sumOf { it.amount }
    val avg = if (spendDays == 0) 0 else total / spendDays
    val topCategory = expenses.groupingBy { it.category }.eachCount().maxByOrNull { it.value }?.key ?: "없음"
    val topEmotion = expenses.groupingBy { it.emotion }.eachCount().maxByOrNull { it.value }?.key ?: "없음"
    val highest = expenses.maxByOrNull { it.amount }

    Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ReportSummaryRow(
            title = "${formatMonthTitle(monthMillis)} 총지출",
            subtitle = "${expenses.size}개 기록, ${spendDays}일 소비",
            amount = total,
            accent = Color(0xFFFF6651)
        )
        ReportSummaryRow(
            title = "하루 평균 지출",
            subtitle = "기록이 있는 날짜 기준",
            amount = avg,
            accent = Color(0xFF0F766E)
        )
        InsightTextCard(title = "가장 자주 나온 카테고리", value = topCategory)
        InsightTextCard(title = "대표 소비 감정", value = topEmotion)
        InsightTextCard(
            title = "가장 큰 지출",
            value = highest?.let { "${it.memo.ifBlank { it.category }} · ${formatWon(it.amount)}" } ?: "없음"
        )
    }
}

@Composable
private fun ReportBreakdownCard(title: String, rows: List<Pair<String, Int>>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF7F8FA),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF263244)
            )
            if (rows.isEmpty()) {
                Text(
                    text = "아직 표시할 기록이 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8A94A3)
                )
            } else {
                rows.forEach { (label, amount) ->
                    ReportSummaryRow(
                        title = label,
                        subtitle = "월간 합계",
                        amount = amount,
                        accent = Color(0xFFFF6651)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryRow(title: String, subtitle: String, amount: Int, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF263244)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9AA3AE)
            )
        }
        Text(
            text = formatWon(amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
}

@Composable
private fun InsightTextCard(title: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF7F8FA),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF263244),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReportEmptyState(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        color = Color(0xFFF7F8FA),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(18.dp),
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )
    }
}

private fun buildDailyGroups(expenses: List<Expense>): List<DailyExpenseGroup> {
    // dateMillis를 하루 시작값으로 정규화한 뒤 같은 날짜끼리 묶는다.
    return expenses
        .groupBy { startOfDay(it.dateMillis) }
        .map { (dayMillis, dayExpenses) ->
            DailyExpenseGroup(
                dayMillis = dayMillis,
                expenses = dayExpenses.sortedByDescending { it.id },
                totalAmount = dayExpenses.sumOf { it.amount }
            )
        }
        .sortedByDescending { it.dayMillis }
}

private fun buildCalendarCells(monthMillis: Long, expenses: List<Expense>): List<CalendarDayCell> {
    val calendar = Calendar.getInstance().apply { timeInMillis = startOfMonth(monthMillis) }
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalsByDay = expenses
        .groupBy { startOfDay(it.dateMillis) }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val cells = mutableListOf<CalendarDayCell>()

    // 월의 1일이 시작되기 전 요일 칸은 빈 셀로 채운다.
    repeat(firstDayOfWeek - 1) {
        cells.add(CalendarDayCell(dayNumber = null, dayMillis = null, totalAmount = 0))
    }

    for (day in 1..daysInMonth) {
        val dayMillis = Calendar.getInstance().apply {
            timeInMillis = monthMillis
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        cells.add(
            CalendarDayCell(
                dayNumber = day,
                dayMillis = dayMillis,
                totalAmount = totalsByDay[dayMillis] ?: 0
            )
        )
    }

    // 마지막 주가 7칸이 되도록 빈 셀을 추가한다.
    while (cells.size % 7 != 0) {
        cells.add(CalendarDayCell(dayNumber = null, dayMillis = null, totalAmount = 0))
    }

    return cells
}

private fun startOfDay(millis: Long): Long {
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

private fun addMonths(millis: Long, amount: Int): Long {
    return Calendar.getInstance().apply {
        timeInMillis = millis
        add(Calendar.MONTH, amount)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun weekOfMonth(millis: Long): Int {
    return Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.WEEK_OF_MONTH)
}

private fun weekRangeLabel(monthMillis: Long, week: Int): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = monthMillis
        set(Calendar.WEEK_OF_MONTH, week)
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    }
    val start = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_MONTH, 6)
    val end = calendar.timeInMillis
    return "${formatDateShort(start)} - ${formatDateShort(end)}"
}

private fun dayOfMonth(millis: Long): String {
    return Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.DAY_OF_MONTH).toString()
}

private fun dayOfWeekLabel(millis: Long): String {
    return when (Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "일요일"
        Calendar.MONDAY -> "월요일"
        Calendar.TUESDAY -> "화요일"
        Calendar.WEDNESDAY -> "수요일"
        Calendar.THURSDAY -> "목요일"
        Calendar.FRIDAY -> "금요일"
        else -> "토요일"
    }
}

private fun dayBadgeColor(millis: Long): Color {
    return when (Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> Color(0xFFFF6651)
        Calendar.SATURDAY -> Color(0xFF60A5FA)
        else -> Color(0xFFB4BBC5)
    }
}

private fun formatWon(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원"
}

private fun formatSignedWon(amount: Int): String {
    return if (amount < 0) "-${formatWon(-amount)}" else formatWon(amount)
}

private fun formatCompactWon(amount: Int): String {
    return if (amount >= 10000) {
        "${NumberFormat.getNumberInstance(Locale.KOREA).format(amount / 10000)}만"
    } else {
        formatWon(amount)
    }
}

private fun formatMonthTitle(millis: Long): String {
    return SimpleDateFormat("yyyy년 M월", Locale.KOREA).format(Date(millis))
}

private fun formatMonthDot(millis: Long): String {
    return SimpleDateFormat("yyyy.MM", Locale.KOREA).format(Date(millis))
}

private fun formatDateShort(millis: Long): String {
    return SimpleDateFormat("M.d", Locale.KOREA).format(Date(millis))
}
