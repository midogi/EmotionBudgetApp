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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.emotionbudgetapp.data.Expense
import com.example.emotionbudgetapp.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel) {
    // ViewModel의 StateFlow를 Compose State로 바꿔서 화면이 목록 변화를 자동으로 따라가게 한다.
    val expenses by viewModel.expenses.collectAsState()

    // 메인 화면 안에서 상세 통계 화면으로 넘어갈지 결정하는 단순 화면 전환 상태.
    var showReport by remember { mutableStateOf(false) }

    if (showReport) {
        LedgerReportScreen(
            expenses = expenses,
            onBack = { showReport = false }
        )
        return
    }

    // 입력 폼 상태. remember를 쓰면 화면이 다시 그려져도 사용자가 입력 중인 값이 유지된다.
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("식비") }
    var emotion by remember { mutableStateOf("기쁨") }
    var memo by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(todayMillis()) }
    var editingExpenseId by remember { mutableStateOf<Int?>(null) }

    // 검색/필터/다이얼로그처럼 화면 동작을 제어하는 상태들.
    var searchText by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("전체") }
    var emotionFilter by remember { mutableStateOf("전체") }
    var showDatePicker by remember { mutableStateOf(false) }
    var pendingDeleteExpense by remember { mutableStateOf<Expense?>(null) }

    // 입력 폼과 필터 드롭다운에서 같이 쓰는 기본 선택지.
    val categories = listOf("식비", "교통", "쇼핑", "카페", "문화", "기타")
    val emotions = listOf("기쁨", "슬픔", "스트레스", "외로움", "평온", "분노")

    // 상단 요약 카드와 카테고리 요약 카드에 쓰는 파생 데이터.
    val totalAmount = expenses.sumOf { it.amount }
    val topEmotion = expenses
        .groupingBy { it.emotion }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key ?: "기록 없음"
    val biggestAmount = expenses.maxOfOrNull { it.amount } ?: 0
    val categoryTotals = expenses
        .groupBy { it.category }
        .map { entry -> entry.key to entry.value.sumOf { it.amount } }
        .sortedByDescending { it.second }

    // 검색어, 카테고리 필터, 감정 필터를 모두 만족하는 기록만 목록에 보여준다.
    val filteredExpenses = expenses
        .filter { expense ->
            val query = searchText.trim()
            val matchesQuery = query.isBlank() ||
                expense.category.contains(query, ignoreCase = true) ||
                expense.emotion.contains(query, ignoreCase = true) ||
                expense.memo.contains(query, ignoreCase = true) ||
                expense.amount.toString().contains(query) ||
                formatDate(expense.dateMillis).contains(query)

            val matchesCategory = categoryFilter == "전체" || expense.category == categoryFilter
            val matchesEmotion = emotionFilter == "전체" || expense.emotion == emotionFilter

            matchesQuery && matchesCategory && matchesEmotion
        }
        .sortedWith(compareByDescending<Expense> { it.dateMillis }.thenByDescending { it.id })

    // 추가/수정이 끝난 뒤 입력 폼을 처음 상태로 되돌린다.
    fun resetForm() {
        amountText = ""
        category = categories.first()
        emotion = emotions.first()
        memo = ""
        selectedDateMillis = todayMillis()
        editingExpenseId = null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F7FA)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderCard(
                    totalAmount = totalAmount,
                    recordCount = expenses.size,
                    topEmotion = topEmotion,
                    biggestAmount = biggestAmount,
                    onReportClick = { showReport = true }
                )
            }

            if (categoryTotals.isNotEmpty()) {
                item {
                    CategoryTotalsCard(categoryTotals = categoryTotals)
                }
            }

            item {
                ExpenseInputCard(
                    amountText = amountText,
                    onAmountChange = { amountText = it.filter { char -> char.isDigit() } },
                    category = category,
                    categories = categories,
                    onCategoryChange = { category = it },
                    emotion = emotion,
                    emotions = emotions,
                    onEmotionChange = { emotion = it },
                    memo = memo,
                    onMemoChange = { memo = it },
                    selectedDateText = formatDate(selectedDateMillis),
                    onDateClick = { showDatePicker = true },
                    isEditing = editingExpenseId != null,
                    isSubmitEnabled = amountText.toIntOrNull()?.let { it > 0 } == true,
                    onCancelEdit = ::resetForm,
                    onSubmit = {
                        val amount = amountText.toIntOrNull()

                        if (amount != null && amount > 0) {
                            val editingId = editingExpenseId
                            if (editingId == null) {
                                // 수정 중인 id가 없으면 새 지출 기록을 추가한다.
                                viewModel.addExpense(
                                    amount = amount,
                                    category = category,
                                    emotion = emotion,
                                    memo = memo,
                                    dateMillis = selectedDateMillis
                                )
                            } else {
                                // 수정 중이면 같은 id의 기록만 새 입력값으로 교체한다.
                                viewModel.updateExpense(
                                    id = editingId,
                                    amount = amount,
                                    category = category,
                                    emotion = emotion,
                                    memo = memo,
                                    dateMillis = selectedDateMillis
                                )
                            }
                            resetForm()
                        }
                    }
                )
            }

            item {
                FilterCard(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    categoryFilter = categoryFilter,
                    categoryOptions = listOf("전체") + categories,
                    onCategoryFilterChange = { categoryFilter = it },
                    emotionFilter = emotionFilter,
                    emotionOptions = listOf("전체") + emotions,
                    onEmotionFilterChange = { emotionFilter = it },
                    onClearFilters = {
                        searchText = ""
                        categoryFilter = "전체"
                        emotionFilter = "전체"
                    }
                )
            }

            item {
                SectionTitle(
                    recordCount = filteredExpenses.size,
                    totalCount = expenses.size
                )
            }

            if (filteredExpenses.isEmpty()) {
                item {
                    if (expenses.isEmpty()) {
                        EmptyRecordCard(
                            title = "아직 기록이 없어요",
                            message = "금액과 감정을 입력하면 여기에 지출 기록이 쌓입니다."
                        )
                    } else {
                        EmptyRecordCard(
                            title = "조건에 맞는 기록이 없어요",
                            message = "검색어나 필터를 바꾸면 다른 기록을 볼 수 있습니다."
                        )
                    }
                }
            } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onEdit = {
                            // 선택한 기록의 값을 입력 폼에 다시 채워 수정 모드로 전환한다.
                            amountText = expense.amount.toString()
                            category = expense.category
                            emotion = expense.emotion
                            memo = expense.memo
                            selectedDateMillis = expense.dateMillis
                            editingExpenseId = expense.id
                        },
                        onDelete = {
                            // 바로 삭제하지 않고 확인 다이얼로그를 띄우기 위해 임시 상태에 담는다.
                            pendingDeleteExpense = expense
                        }
                    )
                }
            }
        }
    }

    pendingDeleteExpense?.let { expense ->
        AlertDialog(
            onDismissRequest = { pendingDeleteExpense = null },
            title = { Text("기록 삭제") },
            text = { Text("${formatWon(expense.amount)} 지출 기록을 삭제할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(expense)
                        if (editingExpenseId == expense.id) {
                            resetForm()
                        }
                        pendingDeleteExpense = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteExpense = null }) {
                    Text("취소")
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = toDatePickerUtcMillis(selectedDateMillis)
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Material DatePicker는 UTC 기준 millis를 돌려주기 때문에 로컬 날짜로 다시 변환한다.
                        selectedDateMillis = fromDatePickerUtcMillis(
                            datePickerState.selectedDateMillis ?: toDatePickerUtcMillis(selectedDateMillis)
                        )
                        showDatePicker = false
                    }
                ) {
                    Text("선택")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun HeaderCard(
    totalAmount: Int,
    recordCount: Int,
    topEmotion: String,
    biggestAmount: Int,
    onReportClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF172033)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "감정 가계부",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "소비한 순간의 감정까지 함께 기록해요.",
                color = Color(0xFFD6DEEB),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatWon(totalAmount),
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetric(
                    title = "기록",
                    value = "${recordCount}개",
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    title = "대표 감정",
                    value = topEmotion,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    title = "최대 지출",
                    value = formatWon(biggestAmount),
                    modifier = Modifier.weight(1f)
                )
            }
            Button(
                onClick = onReportClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("상세 통계 보기")
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF24324B),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFFB8C3D8),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CategoryTotalsCard(categoryTotals: List<Pair<String, Int>>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "카테고리별 합계",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )
            categoryTotals.forEach { (name, total) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF263244)
                    )
                    Text(
                        text = formatWon(total),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F766E)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseInputCard(
    amountText: String,
    onAmountChange: (String) -> Unit,
    category: String,
    categories: List<String>,
    onCategoryChange: (String) -> Unit,
    emotion: String,
    emotions: List<String>,
    onEmotionChange: (String) -> Unit,
    memo: String,
    onMemoChange: (String) -> Unit,
    selectedDateText: String,
    onDateClick: () -> Unit,
    isEditing: Boolean,
    isSubmitEnabled: Boolean,
    onCancelEdit: () -> Unit,
    onSubmit: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isEditing) "지출 기록 수정" else "새 지출 기록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = onAmountChange,
                label = { Text("금액") },
                suffix = { Text("원") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = onDateClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("날짜: $selectedDateText")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DropdownSelector(
                    label = "카테고리",
                    selectedValue = category,
                    options = categories,
                    onSelected = onCategoryChange,
                    modifier = Modifier.weight(1f)
                )
                DropdownSelector(
                    label = "감정",
                    selectedValue = emotion,
                    options = emotions,
                    onSelected = onEmotionChange,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = memo,
                onValueChange = onMemoChange,
                label = { Text("메모") },
                placeholder = { Text("예: 시험 끝나고 친구와 저녁") },
                modifier = Modifier.fillMaxWidth()
            )

            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = onSubmit,
                        enabled = isSubmitEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("수정 저장")
                    }
                }
            } else {
                Button(
                    onClick = onSubmit,
                    enabled = isSubmitEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("기록 추가")
                }
            }
        }
    }
}

@Composable
private fun FilterCard(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    categoryFilter: String,
    categoryOptions: List<String>,
    onCategoryFilterChange: (String) -> Unit,
    emotionFilter: String,
    emotionOptions: List<String>,
    onEmotionFilterChange: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "기록 찾기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )

            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                label = { Text("검색") },
                placeholder = { Text("메모, 금액, 날짜, 감정 검색") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DropdownSelector(
                    label = "카테고리",
                    selectedValue = categoryFilter,
                    options = categoryOptions,
                    onSelected = onCategoryFilterChange,
                    modifier = Modifier.weight(1f)
                )
                DropdownSelector(
                    label = "감정",
                    selectedValue = emotionFilter,
                    options = emotionOptions,
                    onSelected = onEmotionFilterChange,
                    modifier = Modifier.weight(1f)
                )
            }

            if (searchText.isNotBlank() || categoryFilter != "전체" || emotionFilter != "전체") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClearFilters) {
                        Text("필터 초기화")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(recordCount: Int, totalCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "최근 기록",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172033)
        )
        Text(
            text = if (recordCount == totalCount) "${recordCount}개" else "${recordCount}/${totalCount}개",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF5D6B82)
        )
    }
}

@Composable
private fun EmptyRecordCard(title: String, message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5D6B82)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Material3의 ExposedDropdownMenuBox는 TextField처럼 보이지만 클릭하면 메뉴가 펼쳐지는 UI다.
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatWon(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원"
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(millis))
}

private fun todayMillis(): Long {
    return normalizeDay(System.currentTimeMillis())
}

private fun normalizeDay(millis: Long): Long {
    // 시간/분/초를 0으로 맞춰 같은 날짜는 항상 같은 millis로 비교되게 한다.
    return Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun toDatePickerUtcMillis(localDayMillis: Long): Long {
    // DatePicker는 UTC 기준 날짜를 기대하므로 로컬 날짜의 연/월/일만 UTC 달력에 옮긴다.
    val local = Calendar.getInstance().apply { timeInMillis = localDayMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(
            local.get(Calendar.YEAR),
            local.get(Calendar.MONTH),
            local.get(Calendar.DAY_OF_MONTH),
            0,
            0,
            0
        )
    }.timeInMillis
}

private fun fromDatePickerUtcMillis(utcMillis: Long): Long {
    // DatePicker가 돌려준 UTC 날짜의 연/월/일을 다시 로컬 시간대의 하루 시작으로 변환한다.
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH),
            utc.get(Calendar.DAY_OF_MONTH),
            0,
            0,
            0
        )
    }.timeInMillis
}
