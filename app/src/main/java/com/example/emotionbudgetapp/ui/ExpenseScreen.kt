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
import androidx.compose.material3.MenuAnchorType
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
import com.example.emotionbudgetapp.data.TransactionType
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
    // ViewModel의 StateFlow를 Compose State로 바꿔서 기록 추가/수정/삭제가 바로 화면에 반영되게 한다.
    val expenses by viewModel.expenses.collectAsState()

    // 입력 폼 상태. remember를 쓰면 화면이 다시 그려져도 입력 중인 값이 유지된다.
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("식비") }
    var emotion by remember { mutableStateOf("기쁨") }
    var memo by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(todayMillis()) }
    var editingExpenseId by remember { mutableStateOf<Int?>(null) }

    // 기록 찾기 상태. 검색어는 검색하기를 눌렀을 때 적용하고, 필터는 선택 즉시 다시 계산한다.
    var searchText by remember { mutableStateOf("") }
    var appliedSearchText by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }
    var periodFilter by remember { mutableStateOf("전체") }
    var customStartText by remember { mutableStateOf("") }
    var customEndText by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf("전체") }
    var categoryFilter by remember { mutableStateOf("전체") }
    var emotionFilter by remember { mutableStateOf("전체") }
    var showDatePicker by remember { mutableStateOf(false) }
    var pendingDeleteExpense by remember { mutableStateOf<Expense?>(null) }

    // 입력 폼의 기본 선택지. 필터 선택지는 실제 기록 데이터까지 합쳐서 아래에서 다시 만든다.
    val expenseCategories = listOf("식비", "교통", "쇼핑", "카페", "문화", "기타")
    val incomeCategories = listOf("급여", "용돈", "부수입", "환급", "기타")
    val baseEmotions = listOf("기쁨", "슬픔", "우울", "스트레스", "외로움", "평온", "분노")
    val activeCategories = if (transactionType == TransactionType.INCOME) incomeCategories else expenseCategories
    val emotions = (baseEmotions + expenses
        .filter { it.type == TransactionType.EXPENSE }
        .map { it.emotion })
        .distinct()
    val expenseFilterCategories = (expenseCategories + expenses
        .filter { it.type == TransactionType.EXPENSE }
        .map { it.category })
        .distinct()
    val incomeFilterCategories = (incomeCategories + expenses
        .filter { it.type == TransactionType.INCOME }
        .map { it.category })
        .distinct()
    val allCategories = (expenseFilterCategories + incomeFilterCategories).distinct()
    val visibleFilterCategories = when (typeFilter) {
        TransactionType.INCOME.label -> incomeFilterCategories
        TransactionType.EXPENSE.label -> expenseFilterCategories
        else -> allCategories
    }
    val transactionTypeOptions = listOf(TransactionType.EXPENSE.label, TransactionType.INCOME.label)
    val periodOptions = listOf("전체", "오늘", "이번 주", "이번 달", "직접 선택")

    val filteredExpenses = expenses
        .filter { expense ->
            val query = appliedSearchText.trim()
            val compactQuery = query
                .replace(",", "")
                .replace("원", "")
                .replace("+", "")
                .replace("-", "")
                .trim()

            // 검색어는 유형, 카테고리, 감정, 메모, 날짜, 금액 표기까지 모두 대상으로 삼는다.
            val matchesQuery = query.isBlank() ||
                expense.type.label.contains(query, ignoreCase = true) ||
                expense.category.contains(query, ignoreCase = true) ||
                expense.emotion.contains(query, ignoreCase = true) ||
                expense.memo.contains(query, ignoreCase = true) ||
                formatDate(expense.dateMillis).contains(query) ||
                formatWon(expense.amount).contains(query) ||
                formatSignedSearchWon(expense).contains(query) ||
                (compactQuery.isNotBlank() && expense.amount.toString().contains(compactQuery))

            val matchesPeriod = matchesPeriodFilter(
                millis = expense.dateMillis,
                periodFilter = periodFilter,
                customStartText = customStartText,
                customEndText = customEndText
            )
            val matchesType = typeFilter == "전체" || expense.type.label == typeFilter
            val matchesCategory = categoryFilter == "전체" || expense.category == categoryFilter
            val matchesEmotion = emotionFilter == "전체" ||
                (expense.type == TransactionType.EXPENSE && expense.emotion == emotionFilter)

            matchesQuery && matchesPeriod && matchesType && matchesCategory && matchesEmotion
        }
        .sortedWith(compareByDescending<Expense> { it.dateMillis }.thenByDescending { it.id })

    val incomeRecords = filteredExpenses.filter { it.type == TransactionType.INCOME }
    val expenseRecords = filteredExpenses.filter { it.type == TransactionType.EXPENSE }
    val incomeTotal = incomeRecords.sumOf { it.amount }
    val expenseTotal = expenseRecords.sumOf { it.amount }
    val balance = incomeTotal - expenseTotal
    val topEmotion = expenseRecords
        .groupingBy { it.emotion }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key ?: "지출 기록 없음"
    val biggestExpenseAmount = expenseRecords.maxOfOrNull { it.amount } ?: 0
    val categoryTotals = expenseRecords
        .groupBy { it.category }
        .map { entry -> entry.key to entry.value.sumOf { it.amount } }
        .sortedByDescending { it.second }

    fun resetForm() {
        transactionType = TransactionType.EXPENSE
        amountText = ""
        category = expenseCategories.first()
        emotion = baseEmotions.first()
        memo = ""
        selectedDateMillis = todayMillis()
        editingExpenseId = null
    }

    fun clearRecordFilters() {
        searchText = ""
        appliedSearchText = ""
        hasSearched = false
        periodFilter = "전체"
        customStartText = ""
        customEndText = ""
        typeFilter = "전체"
        categoryFilter = "전체"
        emotionFilter = "전체"
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
                    incomeTotal = incomeTotal,
                    expenseTotal = expenseTotal,
                    balance = balance,
                    recordCount = filteredExpenses.size,
                    topEmotion = topEmotion,
                    biggestExpenseAmount = biggestExpenseAmount
                )
            }

            if (categoryTotals.isNotEmpty()) {
                item {
                    CategoryTotalsCard(categoryTotals = categoryTotals)
                }
            }

            item {
                ExpenseInputCard(
                    transactionType = transactionType,
                    transactionTypeOptions = transactionTypeOptions,
                    onTransactionTypeChange = { selectedLabel ->
                        transactionType = transactionTypeFromLabel(selectedLabel)
                        category = if (transactionType == TransactionType.INCOME) {
                            incomeCategories.first()
                        } else {
                            expenseCategories.first()
                        }
                        if (transactionType == TransactionType.INCOME) {
                            emotion = "평온"
                        }
                    },
                    amountText = amountText,
                    onAmountChange = { amountText = it.filter { char -> char.isDigit() } },
                    category = category,
                    categories = activeCategories,
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
                            val savedEmotion = if (transactionType == TransactionType.INCOME) "평온" else emotion
                            if (editingId == null) {
                                viewModel.addExpense(
                                    amount = amount,
                                    category = category,
                                    emotion = savedEmotion,
                                    memo = memo,
                                    dateMillis = selectedDateMillis,
                                    type = transactionType
                                )
                            } else {
                                viewModel.updateExpense(
                                    id = editingId,
                                    amount = amount,
                                    category = category,
                                    emotion = savedEmotion,
                                    memo = memo,
                                    dateMillis = selectedDateMillis,
                                    type = transactionType
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
                    appliedSearchText = appliedSearchText,
                    hasSearched = hasSearched,
                    onSearchSubmit = {
                        appliedSearchText = searchText.trim()
                        hasSearched = true
                    },
                    periodFilter = periodFilter,
                    periodOptions = periodOptions,
                    onPeriodFilterChange = { periodFilter = it },
                    customStartText = customStartText,
                    onCustomStartTextChange = { customStartText = it },
                    customEndText = customEndText,
                    onCustomEndTextChange = { customEndText = it },
                    typeFilter = typeFilter,
                    typeOptions = listOf("전체") + transactionTypeOptions,
                    onTypeFilterChange = { selectedType ->
                        typeFilter = selectedType
                        categoryFilter = "전체"
                        if (selectedType == TransactionType.INCOME.label) {
                            emotionFilter = "전체"
                        }
                    },
                    categoryFilter = categoryFilter,
                    categoryOptions = listOf("전체") + visibleFilterCategories,
                    onCategoryFilterChange = { categoryFilter = it },
                    emotionFilter = emotionFilter,
                    emotionOptions = listOf("전체") + emotions,
                    onEmotionFilterChange = { emotionFilter = it },
                    resultCount = filteredExpenses.size,
                    totalCount = expenses.size,
                    onClearFilters = ::clearRecordFilters
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
                            message = "샘플 데이터를 불러오거나 수입/지출을 직접 입력하면 여기에 기록이 쌓입니다.",
                            actionLabel = "샘플 데이터 불러오기",
                            onAction = viewModel::loadSampleData
                        )
                    } else {
                        EmptyRecordCard(
                            title = "조건에 맞는 기록이 없어요",
                            message = "검색어와 필터 조건을 넓히거나 초기화하면 다른 기록을 다시 볼 수 있습니다.",
                            actionLabel = "필터 초기화",
                            onAction = ::clearRecordFilters
                        )
                    }
                }
            } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onEdit = {
                            transactionType = expense.type
                            amountText = expense.amount.toString()
                            category = expense.category
                            emotion = expense.emotion
                            memo = expense.memo
                            selectedDateMillis = expense.dateMillis
                            editingExpenseId = expense.id
                        },
                        onDelete = {
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
            text = { Text("${expense.type.label} ${formatWon(expense.amount)} 기록을 삭제할까요?") },
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
    incomeTotal: Int,
    expenseTotal: Int,
    balance: Int,
    recordCount: Int,
    topEmotion: String,
    biggestExpenseAmount: Int
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
                text = "수입과 지출, 소비한 순간의 감정까지 함께 기록해요.",
                color = Color(0xFFD6DEEB),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatSignedWon(balance),
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetric(
                    title = "수입",
                    value = formatWon(incomeTotal),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    title = "지출",
                    value = formatWon(expenseTotal),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    title = "기록",
                    value = "${recordCount}개",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetric(
                    title = "대표 감정",
                    value = topEmotion,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    title = "최대 지출",
                    value = formatWon(biggestExpenseAmount),
                    modifier = Modifier.weight(1f)
                )
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
                text = "지출 카테고리별 합계",
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
                        color = Color(0xFFFF6651)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseInputCard(
    transactionType: TransactionType,
    transactionTypeOptions: List<String>,
    onTransactionTypeChange: (String) -> Unit,
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
    val isIncome = transactionType == TransactionType.INCOME

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
                text = if (isEditing) "기록 수정" else "새 기록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )

            DropdownSelector(
                label = "유형",
                selectedValue = transactionType.label,
                options = transactionTypeOptions,
                onSelected = onTransactionTypeChange,
                modifier = Modifier.fillMaxWidth()
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

            if (isIncome) {
                DropdownSelector(
                    label = "수입 카테고리",
                    selectedValue = category,
                    options = categories,
                    onSelected = onCategoryChange,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DropdownSelector(
                        label = "지출 카테고리",
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
            }

            OutlinedTextField(
                value = memo,
                onValueChange = onMemoChange,
                label = { Text("메모") },
                placeholder = { Text(if (isIncome) "예: 알바비, 용돈" else "예: 시험 끝나고 친구와 저녁") },
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
    appliedSearchText: String,
    hasSearched: Boolean,
    onSearchSubmit: () -> Unit,
    periodFilter: String,
    periodOptions: List<String>,
    onPeriodFilterChange: (String) -> Unit,
    customStartText: String,
    onCustomStartTextChange: (String) -> Unit,
    customEndText: String,
    onCustomEndTextChange: (String) -> Unit,
    typeFilter: String,
    typeOptions: List<String>,
    onTypeFilterChange: (String) -> Unit,
    categoryFilter: String,
    categoryOptions: List<String>,
    onCategoryFilterChange: (String) -> Unit,
    emotionFilter: String,
    emotionOptions: List<String>,
    onEmotionFilterChange: (String) -> Unit,
    resultCount: Int,
    totalCount: Int,
    onClearFilters: () -> Unit
) {
    val showEmotionFilter = typeFilter != TransactionType.INCOME.label
    val trimmedSearchText = searchText.trim()
    val hasPendingSearch = trimmedSearchText != appliedSearchText
    val hasNonSearchFilters = periodFilter != "전체" ||
        customStartText.isNotBlank() ||
        customEndText.isNotBlank() ||
        typeFilter != "전체" ||
        categoryFilter != "전체" ||
        emotionFilter != "전체"
    val hasActiveFilters = appliedSearchText.isNotBlank() || hasNonSearchFilters
    val resultText = if (totalCount == 0) {
        "기록 없음"
    } else if (hasActiveFilters) {
        "${resultCount}/${totalCount}개"
    } else {
        "${totalCount}개"
    }
    val searchStatusText = when {
        hasPendingSearch && trimmedSearchText.isNotBlank() -> "'$trimmedSearchText' 검색은 검색하기를 누르면 적용돼요."
        hasSearched && appliedSearchText.isBlank() && totalCount == 0 -> "검색할 기록이 아직 없어요."
        hasSearched && appliedSearchText.isBlank() && hasNonSearchFilters && resultCount == 0 -> "현재 필터 조건에 맞는 기록이 없어요."
        hasSearched && appliedSearchText.isBlank() && hasNonSearchFilters -> "현재 필터 조건으로 ${resultCount}개를 찾았어요."
        hasSearched && appliedSearchText.isBlank() -> "전체 기록 ${resultCount}개를 보여주고 있어요."
        hasSearched && resultCount > 0 -> "'$appliedSearchText' 검색 결과 ${resultCount}개를 찾았어요."
        hasSearched -> "'$appliedSearchText'에 해당하는 기록이 없어요."
        else -> "검색어를 입력하고 검색하기를 누르면 결과가 표시돼요."
    }
    val searchStatusColor = if (hasSearched && !hasPendingSearch && resultCount == 0) {
        Color(0xFFB91C1C)
    } else {
        Color(0xFF2F5D62)
    }
    val searchStatusBackground = if (hasSearched && !hasPendingSearch && resultCount == 0) {
        Color(0xFFFFF1F2)
    } else {
        Color(0xFFF1F5F9)
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "기록 찾기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172033)
                )
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2F5D62)
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasActiveFilters) "현재 조건 결과" else "전체 기록",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5D6B82)
                    )
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF172033)
                    )
                }
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                label = { Text("검색") },
                placeholder = { Text("메모, 금액, 날짜, 유형, 감정 검색") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onSearchSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("검색하기")
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = searchStatusBackground,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = searchStatusText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = searchStatusColor
                )
            }

            DropdownSelector(
                label = "기간",
                selectedValue = periodFilter,
                options = periodOptions,
                onSelected = onPeriodFilterChange,
                modifier = Modifier.fillMaxWidth()
            )

            if (periodFilter == "직접 선택") {
                OutlinedTextField(
                    value = customStartText,
                    onValueChange = onCustomStartTextChange,
                    label = { Text("시작일") },
                    placeholder = { Text("예: 2026.06.01") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customEndText,
                    onValueChange = onCustomEndTextChange,
                    label = { Text("종료일") },
                    placeholder = { Text("예: 2026.06.30") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            DropdownSelector(
                label = "유형",
                selectedValue = typeFilter,
                options = typeOptions,
                onSelected = onTypeFilterChange,
                modifier = Modifier.fillMaxWidth()
            )

            if (showEmotionFilter) {
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
                        label = "지출 감정",
                        selectedValue = emotionFilter,
                        options = emotionOptions,
                        onSelected = onEmotionFilterChange,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                DropdownSelector(
                    label = "카테고리",
                    selectedValue = categoryFilter,
                    options = categoryOptions,
                    onSelected = onCategoryFilterChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (hasActiveFilters) {
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
private fun EmptyRecordCard(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(actionLabel)
                }
            }
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

    // TextField처럼 보이지만 클릭하면 선택 메뉴가 펼쳐지는 Material3 기본 드롭다운이다.
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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
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

private fun transactionTypeFromLabel(label: String): TransactionType {
    return if (label == TransactionType.INCOME.label) TransactionType.INCOME else TransactionType.EXPENSE
}

private fun matchesPeriodFilter(
    millis: Long,
    periodFilter: String,
    customStartText: String,
    customEndText: String
): Boolean {
    val dayMillis = normalizeDay(millis)
    val today = todayMillis()
    return when (periodFilter) {
        "오늘" -> dayMillis == today
        "이번 주" -> dayMillis >= startOfWeek(today) && dayMillis < addDays(startOfWeek(today), 7)
        "이번 달" -> {
            val monthStart = startOfMonth(today)
            dayMillis >= monthStart && dayMillis < addMonths(monthStart, 1)
        }
        "직접 선택" -> matchesCustomDateRange(dayMillis, customStartText, customEndText)
        else -> true
    }
}

private fun matchesCustomDateRange(dayMillis: Long, customStartText: String, customEndText: String): Boolean {
    val startMillis = parseFilterDate(customStartText)?.let { normalizeDay(it) }
    val endMillis = parseFilterDate(customEndText)?.let { normalizeDay(it) }
    return (startMillis == null || dayMillis >= startMillis) &&
        (endMillis == null || dayMillis <= endMillis)
}

private fun parseFilterDate(text: String): Long? {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return null
    return runCatching {
        SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).apply {
            isLenient = false
        }.parse(trimmed)?.time
    }.getOrNull()
}

private fun formatWon(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원"
}

private fun formatSignedWon(amount: Int): String {
    return if (amount < 0) "-${formatWon(-amount)}" else formatWon(amount)
}

private fun formatSignedSearchWon(expense: Expense): String {
    val prefix = if (expense.type == TransactionType.INCOME) "+" else "-"
    return prefix + formatWon(expense.amount)
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(millis))
}

private fun todayMillis(): Long {
    return normalizeDay(System.currentTimeMillis())
}

private fun normalizeDay(millis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun startOfWeek(millis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = normalizeDay(millis)
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
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

private fun addDays(millis: Long, amount: Int): Long {
    return Calendar.getInstance().apply {
        timeInMillis = millis
        add(Calendar.DAY_OF_MONTH, amount)
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

private fun toDatePickerUtcMillis(localDayMillis: Long): Long {
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
