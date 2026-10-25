package com.example.ui.screens.contracts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.InvoiceItemEntity
import com.example.ui.components.common.CurrencyInputField
import com.example.ui.components.common.PersianTextField
import com.example.util.FormatUtils
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceItemEditDialog(
    invoiceId: String,
    item: InvoiceItemEntity?,
    currencyUnit: String = "TOMAN",
    onDismiss: () -> Unit,
    onSave: (InvoiceItemEntity, Boolean) -> Unit
) {
    val isNew = item == null

    var serviceType by remember { mutableStateOf(item?.serviceType ?: "ساخت و مونتاژ حروف چلنیوم") }
    var itemDescription by remember { mutableStateOf(item?.itemDescription ?: "") }
    var quantityStr by remember { mutableStateOf(if (item != null) item.quantity.toString() else "1") }
    var unitPrice by remember { mutableStateOf(item?.unitPrice ?: 0L) }
    var description by remember { mutableStateOf(item?.description ?: "") }

    var serviceDropdownExpanded by remember { mutableStateOf(false) }
    var serviceTypeError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var unitPriceError by remember { mutableStateOf<String?>(null) }

    val servicePresets = listOf(
        "ساخت و مونتاژ حروف چلنیوم (متر محیط)",
        "حروف برجسته استیل طلایی / نقره‌ای",
        "حروف پلاستیک لبه سوئدی و دوبل",
        "نئون فلکسی روی پلکسی شفاف",
        "زیرسازی و ورق کامپوزیت نما (متر مربع)",
        "چوب ترمووود فنلاندی شاسی‌کشی شده",
        "طراحی و ساخت لایت‌باکس فریم‌لس",
        "نصب، سیم‌کشی و راه‌اندازی در محل",
        "منبع تغذیه (ترانس سوئیچینگ ضدآب)",
        "برش لیزر پلکسی و آهن"
    )

    val quantity = quantityStr.toDoubleOrNull() ?: 0.0
    val calculatedTotal = (quantity * unitPrice).toLong()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "افزودن ردیف فاکتور" else "ویرایش ردیف فاکتور",
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
                // Service Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = serviceDropdownExpanded,
                    onExpandedChange = { serviceDropdownExpanded = !serviceDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = serviceType,
                        onValueChange = {
                            serviceType = it
                            serviceTypeError = null
                        },
                        label = { Text("نوع خدمت یا کالا *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceDropdownExpanded) },
                        isError = serviceTypeError != null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = serviceDropdownExpanded,
                        onDismissRequest = { serviceDropdownExpanded = false }
                    ) {
                        servicePresets.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset) },
                                onClick = {
                                    serviceType = preset
                                    serviceTypeError = null
                                    serviceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                if (serviceTypeError != null) {
                    Text(text = serviceTypeError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                PersianTextField(
                    value = itemDescription,
                    onValueChange = { itemDescription = it },
                    label = "شرح تفصیلی ردیف",
                    placeholder = "مثال: قطر قلم ۳۰ سانتیمتر با پلکسی تایوان و اس‌ام‌دی لنزدار کره"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PersianTextField(
                        value = quantityStr,
                        onValueChange = {
                            quantityStr = it
                            quantityError = null
                        },
                        label = "متراژ / تعداد *",
                        placeholder = "مثال: ۱۲.۵",
                        keyboardType = KeyboardType.Number,
                        isError = quantityError != null,
                        errorMessage = quantityError,
                        modifier = Modifier.weight(1f),
                        testTag = "item_quantity_input"
                    )

                    CurrencyInputField(
                        amount = unitPrice,
                        onAmountChange = {
                            unitPrice = it
                            unitPriceError = null
                        },
                        label = "قیمت واحد",
                        currencyUnit = currencyUnit,
                        isError = unitPriceError != null,
                        errorMessage = unitPriceError,
                        modifier = Modifier.weight(1.5f),
                        testTag = "item_unit_price_input"
                    )
                }

                // Automatic calculation card
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "مبلغ کل محاسبه‌شده ردیف (متراژ × فی):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtils.formatCurrency(calculatedTotal, currencyUnit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                PersianTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "توضیحات تکمیلی",
                    placeholder = "گارانتی، نحوه شاسی‌کشی و ...",
                    singleLine = false,
                    testTag = "item_description_input"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (serviceType.trim().isEmpty()) {
                        serviceTypeError = "نوع خدمت الزامی است."
                        hasError = true
                    }
                    val q = quantityStr.toDoubleOrNull()
                    if (q == null || q <= 0) {
                        quantityError = "متراژ یا تعداد معتبر نیست."
                        hasError = true
                    }
                    if (unitPrice <= 0) {
                        unitPriceError = "قیمت واحد الزامی است."
                        hasError = true
                    }

                    if (!hasError) {
                        val entity = item?.copy(
                            serviceType = serviceType.trim(),
                            itemDescription = itemDescription.trim(),
                            quantity = q ?: 1.0,
                            unitPrice = unitPrice,
                            calculatedTotal = ((q ?: 1.0) * unitPrice).toLong(),
                            description = description.trim()
                        ) ?: InvoiceItemEntity(
                            id = UUID.randomUUID().toString(),
                            invoiceId = invoiceId,
                            serviceType = serviceType.trim(),
                            itemDescription = itemDescription.trim(),
                            quantity = q ?: 1.0,
                            unitPrice = unitPrice,
                            calculatedTotal = ((q ?: 1.0) * unitPrice).toLong(),
                            description = description.trim()
                        )
                        onSave(entity, isNew)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_invoice_item_btn")
            ) {
                Text("ثبت در فاکتور", fontWeight = FontWeight.Bold)
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
