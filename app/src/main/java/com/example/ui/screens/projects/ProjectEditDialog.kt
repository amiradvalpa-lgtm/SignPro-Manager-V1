package com.example.ui.screens.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.ProjectEntity
import com.example.ui.components.common.PersianDateField
import com.example.ui.components.common.PersianTextField
import com.example.util.JalaliDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectEditDialog(
    project: ProjectEntity?,
    customers: List<CustomerEntity>,
    preselectedCustomerId: String? = null,
    onDismiss: () -> Unit,
    onSave: (ProjectEntity, Boolean) -> Unit
) {
    val isNew = project == null

    var title by remember { mutableStateOf(project?.title ?: "") }
    var selectedCustomerId by remember {
        mutableStateOf(project?.customerId ?: preselectedCustomerId ?: customers.firstOrNull()?.id ?: "")
    }
    var startDate by remember {
        mutableStateOf(project?.startDate ?: JalaliDate.today().formatShort())
    }
    var status by remember { mutableStateOf(project?.status ?: "در حال ساخت") }
    var description by remember { mutableStateOf(project?.description ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var customerError by remember { mutableStateOf<String?>(null) }

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf(
        "در حال مذاکره",
        "طراحی",
        "در حال ساخت",
        "نصب",
        "تکمیل شده",
        "تسویه شده"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "تعریف پروژه جدید" else "ویرایش پروژه",
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
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = "عنوان پروژه تابلوسازی *",
                    placeholder = "مثال: تابلوی چلنیوم فروشگاه الماس",
                    leadingIcon = Icons.Default.Business,
                    isError = titleError != null,
                    errorMessage = titleError,
                    testTag = "project_title_input"
                )

                // Customer Selection Dropdown
                val selectedCustomerName = customers.firstOrNull { it.id == selectedCustomerId }?.name ?: "انتخاب مشتری"
                ExposedDropdownMenuBox(
                    expanded = customerDropdownExpanded,
                    onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCustomerName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("مشتری / کارفرمای مرتبط *") },
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

                // Start Date
                PersianDateField(
                    dateString = startDate,
                    onDateSelected = { startDate = it },
                    label = "تاریخ شروع پروژه *"
                )

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("وضعیت پروژه") },
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
                                text = { Text(opt) },
                                onClick = {
                                    status = opt
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                PersianTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "توضیحات و نیازمندی‌های پروژه",
                    placeholder = "مشخصات محل نصب، ارتفاع، هماهنگی‌های ویژه و ...",
                    singleLine = false,
                    testTag = "project_description_input"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (title.trim().isEmpty()) {
                        titleError = "عنوان پروژه الزامی است."
                        hasError = true
                    }
                    if (selectedCustomerId.isEmpty()) {
                        customerError = "انتخاب مشتری الزامی است."
                        hasError = true
                    }

                    if (!hasError) {
                        val entity = project?.copy(
                            title = title.trim(),
                            customerId = selectedCustomerId,
                            startDate = startDate,
                            status = status,
                            description = description.trim()
                        ) ?: ProjectEntity(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            customerId = selectedCustomerId,
                            startDate = startDate,
                            status = status,
                            description = description.trim()
                        )
                        onSave(entity, isNew)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_project_btn")
            ) {
                Text("ذخیره پروژه", fontWeight = FontWeight.Bold)
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
