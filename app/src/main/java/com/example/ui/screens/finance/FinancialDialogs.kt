package com.example.ui.screens.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.*
import com.example.ui.components.common.CurrencyInputField
import com.example.ui.components.common.PersianDateField
import com.example.ui.components.common.PersianTextField
import com.example.util.JalaliDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentEditDialog(
    payment: PaymentEntity?,
    customers: List<CustomerEntity>,
    preselectedCustomerId: String? = null,
    currencyUnit: String = "TOMAN",
    onDismiss: () -> Unit,
    onSave: (PaymentEntity, Boolean) -> Unit
) {
    val isNew = payment == null

    var selectedCustomerId by remember {
        mutableStateOf(payment?.customerId ?: preselectedCustomerId ?: customers.firstOrNull()?.id ?: "")
    }
    var type by remember { mutableStateOf(payment?.type ?: "RECEIPT") } // RECEIPT or PAYMENT
    var amount by remember { mutableStateOf(payment?.amount ?: 0L) }
    var date by remember { mutableStateOf(payment?.date ?: JalaliDate.today().formatShort()) }
    var method by remember { mutableStateOf(payment?.method ?: "کارت به کارت") }
    var trackingCode by remember { mutableStateOf(payment?.trackingCode ?: "") }
    var description by remember { mutableStateOf(payment?.description ?: "") }

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var customerError by remember { mutableStateOf<String?>(null) }

    val methodOptions = listOf("کارت به کارت", "دستگاه پوز (POS)", "نقدی", "حواله پایا / ساتنا", "چک بانکی")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "ثبت پرداخت / دریافت جدید" else "ویرایش تراکنش مالی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Operation type switch (دریافت از مشتری / پرداخت/استرداد به مشتری)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "RECEIPT",
                        onClick = { type = "RECEIPT" },
                        label = { Text("دریافتی از مشتری", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "PAYMENT",
                        onClick = { type = "PAYMENT" },
                        label = { Text("پرداخت / استرداد", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Customer selection
                val selectedCustomerName = customers.firstOrNull { it.id == selectedCustomerId }?.name ?: "انتخاب مشتری"
                ExposedDropdownMenuBox(
                    expanded = customerDropdownExpanded,
                    onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCustomerName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("مشتری مرتبط *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                        isError = customerError != null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = customerDropdownExpanded,
                        onDismissRequest = { customerDropdownExpanded = false }
                    ) {
                        customers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text("${cust.name} (${cust.phone})") },
                                onClick = {
                                    selectedCustomerId = cust.id
                                    customerError = null
                                    customerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                if (customerError != null) {
                    Text(text = customerError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                CurrencyInputField(
                    amount = amount,
                    onAmountChange = {
                        amount = it
                        amountError = null
                    },
                    label = "مبلغ تراکنش *",
                    currencyUnit = currencyUnit,
                    isError = amountError != null,
                    errorMessage = amountError,
                    testTag = "payment_amount_input"
                )

                PersianDateField(
                    dateString = date,
                    onDateSelected = { date = it },
                    label = "تاریخ تراکنش *"
                )

                // Method
                ExposedDropdownMenuBox(
                    expanded = methodDropdownExpanded,
                    onExpandedChange = { methodDropdownExpanded = !methodDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = method,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("روش پرداخت / دریافت *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = methodDropdownExpanded,
                        onDismissRequest = { methodDropdownExpanded = false }
                    ) {
                        methodOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    method = opt
                                    methodDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                PersianTextField(
                    value = trackingCode,
                    onValueChange = { trackingCode = it },
                    label = "شماره پیگیری / ارجاع",
                    placeholder = "شماره پیگیری فیش واریزی یا پوز",
                    testTag = "payment_tracking_code_input"
                )

                PersianTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "توضیحات تراکنش",
                    placeholder = "بابت پیش‌پرداخت، تسویه نهایی، واریز کارت به کارت و ...",
                    singleLine = false,
                    testTag = "payment_description_input"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (selectedCustomerId.isEmpty()) {
                        customerError = "انتخاب مشتری الزامی است."
                        hasError = true
                    }
                    if (amount <= 0) {
                        amountError = "مبلغ تراکنش باید بیشتر از صفر باشد."
                        hasError = true
                    }

                    if (!hasError) {
                        val entity = payment?.copy(
                            customerId = selectedCustomerId,
                            type = type,
                            amount = amount,
                            date = date,
                            method = method,
                            trackingCode = trackingCode.trim(),
                            description = description.trim()
                        ) ?: PaymentEntity(
                            id = UUID.randomUUID().toString(),
                            customerId = selectedCustomerId,
                            type = type,
                            amount = amount,
                            date = date,
                            method = method,
                            trackingCode = trackingCode.trim(),
                            description = description.trim()
                        )
                        onSave(entity, isNew)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_payment_btn")
            ) {
                Text("ثبت تراکنش", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("انصراف")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckEditDialog(
    check: CheckEntity?,
    customers: List<CustomerEntity>,
    preselectedCustomerId: String? = null,
    currencyUnit: String = "TOMAN",
    onDismiss: () -> Unit,
    onSave: (CheckEntity, Boolean) -> Unit
) {
    val isNew = check == null

    var selectedCustomerId by remember {
        mutableStateOf(check?.customerId ?: preselectedCustomerId ?: customers.firstOrNull()?.id ?: "")
    }
    var bankName by remember { mutableStateOf(check?.bankName ?: "ملی") }
    var branch by remember { mutableStateOf(check?.branch ?: "") }
    var checkNumber by remember { mutableStateOf(check?.checkNumber ?: "") }
    var sayadNumber by remember { mutableStateOf(check?.sayadNumber ?: "") }
    var amount by remember { mutableStateOf(check?.amount ?: 0L) }
    var dueDate by remember { mutableStateOf(check?.dueDate ?: JalaliDate.today().formatShort()) }
    var status by remember { mutableStateOf(check?.status ?: "PENDING") } // PENDING, CLEARED, BOUNCED, RETURNED
    var description by remember { mutableStateOf(check?.description ?: "") }

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var customerError by remember { mutableStateOf<String?>(null) }

    val statusOptions = listOf(
        "PENDING" to "در جریان وصول",
        "CLEARED" to "وصول شده",
        "BOUNCED" to "برگشت خورده",
        "RETURNED" to "عودت داده شده"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "ثبت چک صیادی جدید" else "ویرایش چک",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Customer selection
                val selectedCustomerName = customers.firstOrNull { it.id == selectedCustomerId }?.name ?: "انتخاب مشتری"
                ExposedDropdownMenuBox(
                    expanded = customerDropdownExpanded,
                    onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCustomerName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("مشتری صادرکننده چک *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                        isError = customerError != null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = customerDropdownExpanded,
                        onDismissRequest = { customerDropdownExpanded = false }
                    ) {
                        customers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text("${cust.name} (${cust.phone})") },
                                onClick = {
                                    selectedCustomerId = cust.id
                                    customerError = null
                                    customerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                if (customerError != null) {
                    Text(text = customerError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                CurrencyInputField(
                    amount = amount,
                    onAmountChange = {
                        amount = it
                        amountError = null
                    },
                    label = "مبلغ چک *",
                    currencyUnit = currencyUnit,
                    isError = amountError != null,
                    errorMessage = amountError,
                    testTag = "check_amount_input"
                )

                PersianDateField(
                    dateString = dueDate,
                    onDateSelected = { dueDate = it },
                    label = "تاریخ سررسید چک *"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PersianTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = "نام بانک",
                        placeholder = "ملی، ملت، صادرات...",
                        modifier = Modifier.weight(1f),
                        testTag = "check_bank_input"
                    )
                    PersianTextField(
                        value = branch,
                        onValueChange = { branch = it },
                        label = "شعبه",
                        placeholder = "کد یا نام شعبه",
                        modifier = Modifier.weight(1f),
                        testTag = "check_branch_input"
                    )
                }

                PersianTextField(
                    value = sayadNumber,
                    onValueChange = { sayadNumber = it },
                    label = "شناسه ۱۶ رقمی صیاد",
                    placeholder = "شماره صیادی مندرج روی چک",
                    testTag = "check_sayad_input"
                )

                // Status dropdown
                val statusLabel = statusOptions.firstOrNull { it.first == status }?.second ?: "در جریان وصول"
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = statusLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("وضعیت وصول چک *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        statusOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt.second) },
                                onClick = {
                                    status = opt.first
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                PersianTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "توضیحات چک",
                    placeholder = "در وجه، بابت قرارداد تابلوسازی، نام ضامن و ...",
                    singleLine = false,
                    testTag = "check_description_input"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (selectedCustomerId.isEmpty()) {
                        customerError = "انتخاب مشتری الزامی است."
                        hasError = true
                    }
                    if (amount <= 0) {
                        amountError = "مبلغ چک باید بیشتر از صفر باشد."
                        hasError = true
                    }

                    if (!hasError) {
                        val entity = check?.copy(
                            customerId = selectedCustomerId,
                            bankName = bankName.trim(),
                            branch = branch.trim(),
                            checkNumber = checkNumber.trim(),
                            sayadNumber = sayadNumber.trim(),
                            amount = amount,
                            dueDate = dueDate,
                            status = status,
                            description = description.trim()
                        ) ?: CheckEntity(
                            id = UUID.randomUUID().toString(),
                            customerId = selectedCustomerId,
                            bankName = bankName.trim(),
                            branch = branch.trim(),
                            checkNumber = checkNumber.trim(),
                            sayadNumber = sayadNumber.trim(),
                            amount = amount,
                            dueDate = dueDate,
                            status = status,
                            description = description.trim()
                        )
                        onSave(entity, isNew)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_check_btn")
            ) {
                Text("ثبت چک", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("انصراف")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditDialog(
    expense: ExpenseEntity?,
    categories: List<ExpenseCategoryEntity>,
    invoices: List<InvoiceEntity> = emptyList(),
    currencyUnit: String = "TOMAN",
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity, Boolean) -> Unit,
    onQuickAddCategory: (String, Long) -> Unit
) {
    val isNew = expense == null

    var category by remember { mutableStateOf(expense?.category ?: categories.firstOrNull()?.title ?: "ورق پلکسی گلاس") }
    var amount by remember { mutableStateOf(expense?.amount ?: categories.firstOrNull { it.title == category }?.defaultAmount ?: 0L) }
    var date by remember { mutableStateOf(expense?.date ?: JalaliDate.today().formatShort()) }
    var invoiceId by remember { mutableStateOf(expense?.invoiceId) }
    var description by remember { mutableStateOf(expense?.description ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    var showNewCategoryInput by remember { mutableStateOf(false) }
    var newCategoryTitle by remember { mutableStateOf("") }
    var newCategoryDefaultAmt by remember { mutableStateOf(0L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "ثبت هزینه جدید کارگاه / پروژه" else "ویرایش هزینه",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Category Selector with quick add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("عنوان هزینه *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showNewCategoryInput = !showNewCategoryInput }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showNewCategoryInput) "انتخاب از لیست" else "تعریف عنوان جدید")
                    }
                }

                if (showNewCategoryInput) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PersianTextField(
                                value = newCategoryTitle,
                                onValueChange = { newCategoryTitle = it },
                                label = "عنوان هزینه جدید",
                                placeholder = "مثال: کرایه بالابر"
                            )
                            Button(
                                onClick = {
                                    if (newCategoryTitle.trim().isNotEmpty()) {
                                        onQuickAddCategory(newCategoryTitle.trim(), 0L)
                                        category = newCategoryTitle.trim()
                                        showNewCategoryInput = false
                                        newCategoryTitle = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ثبت و انتخاب این عنوان")
                            }
                        }
                    }
                } else {
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {
                                category = it
                                categoryError = null
                            },
                            label = { Text("انتخاب عنوان هزینه") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.title) },
                                    onClick = {
                                        category = cat.title
                                        if (amount == 0L && cat.defaultAmount > 0) {
                                            amount = cat.defaultAmount
                                        }
                                        categoryError = null
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                CurrencyInputField(
                    amount = amount,
                    onAmountChange = {
                        amount = it
                        amountError = null
                    },
                    label = "مبلغ هزینه *",
                    currencyUnit = currencyUnit,
                    isError = amountError != null,
                    errorMessage = amountError,
                    testTag = "expense_amount_input"
                )

                PersianDateField(
                    dateString = date,
                    onDateSelected = { date = it },
                    label = "تاریخ پرداخت هزینه *"
                )

                PersianTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "توضیحات هزینه",
                    placeholder = "نام فروشنده، شماره فاکتور خرید، متراژ ورق و ...",
                    singleLine = false,
                    testTag = "expense_description_input"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (category.trim().isEmpty()) {
                        categoryError = "عنوان هزینه الزامی است."
                        hasError = true
                    }
                    if (amount <= 0) {
                        amountError = "مبلغ هزینه باید بیشتر از صفر باشد."
                        hasError = true
                    }

                    if (!hasError) {
                        val entity = expense?.copy(
                            invoiceId = invoiceId,
                            category = category.trim(),
                            amount = amount,
                            date = date,
                            description = description.trim()
                        ) ?: ExpenseEntity(
                            id = UUID.randomUUID().toString(),
                            invoiceId = invoiceId,
                            category = category.trim(),
                            amount = amount,
                            date = date,
                            description = description.trim()
                        )
                        onSave(entity, isNew)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_expense_btn")
            ) {
                Text("ثبت هزینه", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("انصراف")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
