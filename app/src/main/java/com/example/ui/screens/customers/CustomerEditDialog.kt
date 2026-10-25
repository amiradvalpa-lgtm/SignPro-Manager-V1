package com.example.ui.screens.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.CustomerEntity
import com.example.ui.components.common.PersianTextField
import java.util.UUID

@Composable
fun CustomerEditDialog(
    customer: CustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity, Boolean) -> Unit
) {
    val isNew = customer == null

    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var nationalCode by remember { mutableStateOf(customer?.nationalCode ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var description by remember { mutableStateOf(customer?.description ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "ثبت مشتری جدید" else "ویرایش اطلاعات مشتری",
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
                PersianTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = "نام مشتری / شرکت *",
                    placeholder = "مثال: حاج محمد رضایی",
                    leadingIcon = Icons.Default.Person,
                    isError = nameError != null,
                    errorMessage = nameError,
                    testTag = "customer_name_input"
                )

                PersianTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = null
                    },
                    label = "شماره تلفن همراه *",
                    placeholder = "۰۹۱۲۳۴۵۶۷۸۹",
                    leadingIcon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    isError = phoneError != null,
                    errorMessage = phoneError,
                    testTag = "customer_phone_input"
                )

                PersianTextField(
                    value = nationalCode,
                    onValueChange = { nationalCode = it },
                    label = "کد ملی / شناسه ملی",
                    placeholder = "۱۰ رقم برای صدور قرارداد رسمی",
                    leadingIcon = Icons.Default.Badge,
                    keyboardType = KeyboardType.Number,
                    testTag = "customer_national_code_input"
                )

                PersianTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "نشانی محل کسب / پروژه",
                    placeholder = "خیابان، پلاک، طبقه",
                    leadingIcon = Icons.Default.LocationOn,
                    testTag = "customer_address_input"
                )

                PersianTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "توضیحات تکمیلی",
                    placeholder = "یادداشت‌های فنی، نحوه آشنایی و سوابق مشتری",
                    singleLine = false,
                    testTag = "customer_description_input"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (name.trim().isEmpty()) {
                        nameError = "نام مشتری الزامی است."
                        hasError = true
                    }
                    if (phone.trim().isEmpty()) {
                        phoneError = "شماره تلفن الزامی است."
                        hasError = true
                    }

                    if (!hasError) {
                        val entity = customer?.copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            nationalCode = nationalCode.trim(),
                            address = address.trim(),
                            description = description.trim()
                        ) ?: CustomerEntity(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            phone = phone.trim(),
                            nationalCode = nationalCode.trim(),
                            address = address.trim(),
                            description = description.trim()
                        )
                        onSave(entity, isNew)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_customer_btn")
            ) {
                Text("ذخیره اطلاعات", fontWeight = FontWeight.Bold)
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
