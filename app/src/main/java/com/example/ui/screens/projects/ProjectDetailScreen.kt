package com.example.ui.screens.projects

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.data.local.entities.*
import com.example.ui.components.common.*
import com.example.ui.screens.contracts.InvoiceItemEditDialog
import com.example.ui.theme.MoneyNegative
import com.example.ui.theme.MoneyPositive
import com.example.ui.viewmodels.SignageViewModel
import com.example.util.FormatUtils
import com.example.util.JalaliDate
import com.example.util.PdfGenerator
import java.util.UUID

@Composable
fun ProjectDetailScreen(
    projectId: String,
    viewModel: SignageViewModel,
    onBack: () -> Unit,
    onNavigateToContractEditor: (String) -> Unit
) {
    val context = LocalContext.current
    val project by viewModel.repository.getProjectByIdFlow(projectId).collectAsState(initial = null)
    val specs by viewModel.getSpecsForProject(projectId).collectAsState(initial = emptyList())
    val invoice by viewModel.getInvoiceForProject(projectId).collectAsState(initial = null)
    val contract by viewModel.getContractForProject(projectId).collectAsState(initial = null)
    val currencyUnit by viewModel.currencyUnit.collectAsState()
    val contractorInfo by viewModel.contractorInfo.collectAsState()

    var customer by remember { mutableStateOf<CustomerEntity?>(null) }
    LaunchedEffect(project?.customerId) {
        project?.customerId?.let { cid ->
            customer = viewModel.repository.getCustomerById(cid)
        }
    }

    val invoiceItems = remember(invoice) {
        invoice?.let { inv -> viewModel.getInvoiceItems(inv.id) }
    }?.collectAsState(initial = emptyList())?.value ?: emptyList()

    val projectExpenses = remember(invoice) {
        invoice?.let { inv -> viewModel.repository.getExpensesForInvoice(inv.id) }
    }?.collectAsState(initial = emptyList())?.value ?: emptyList()

    // Dialog states
    var showProjectEditDialog by remember { mutableStateOf(false) }
    var selectedSpecForEdit by remember { mutableStateOf<ProjectSpecEntity?>(null) }
    var showCreateSpecDialog by remember { mutableStateOf(false) }
    var specToDelete by remember { mutableStateOf<ProjectSpecEntity?>(null) }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var selectedItemForEdit by remember { mutableStateOf<InvoiceItemEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<InvoiceItemEntity?>(null) }

    val customers by viewModel.customers.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = project?.title ?: "جزئیات پروژه",
                subtitle = customer?.name,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showProjectEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش پروژه")
                    }
                }
            )
        }
    ) { padding ->
        if (project == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val proj = project!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Project Summary Card with prominent "Print Contract" button
                item {
                    AppCard(borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(proj.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("کارفرما: ${customer?.name ?: "نامشخص"} (${customer?.phone ?: ""})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("تاریخ شروع: ${proj.startDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusBadge(status = proj.status)
                            }

                            if (proj.description.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "توضیحات: ${proj.description}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons: "Print Contract" & "Invoice PDF"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (customer == null) {
                                            Toast.makeText(context, "اطلاعات کارفرما بارگذاری نشده است", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val con = contract ?: ContractEntity(
                                            customerId = proj.customerId,
                                            projectId = proj.id,
                                            contractDate = JalaliDate.today().formatShort(),
                                            durationDays = 15,
                                            totalAmount = invoiceItems.sumOf { it.calculatedTotal },
                                            prepayment = (invoiceItems.sumOf { it.calculatedTotal } * 0.5).toLong(),
                                            balanceAmount = (invoiceItems.sumOf { it.calculatedTotal } * 0.5).toLong()
                                        )
                                        try {
                                            val pdfFile = PdfGenerator.generateContractPdf(
                                                context = context,
                                                contractor = contractorInfo,
                                                customer = customer!!,
                                                project = proj,
                                                specs = specs,
                                                durationDays = con.durationDays,
                                                totalAmount = con.totalAmount,
                                                prepayment = con.prepayment,
                                                balanceAmount = con.balanceAmount,
                                                contractDate = con.contractDate,
                                                arbitratorName = con.arbitratorName
                                            )
                                            PdfGenerator.sharePdf(context, pdfFile, "قرارداد رسمی تابلوسازی - ${proj.title}")
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "خطا در چاپ قرارداد: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("print_contract_btn")
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("چاپ قرارداد", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { onNavigateToContractEditor(proj.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تنظیم بندها")
                                }
                            }
                        }
                    }
                }

                // Section 1: Signage Technical Specs (مشخصات فنی و زیرمجموعه‌ها)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مشخصات تابلو و زیرمجموعه‌ها (${specs.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FilledTonalButton(
                            onClick = { showCreateSpecDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("افزودن تابلو")
                        }
                    }
                }

                if (specs.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "مشخصاتی ثبت نشده است",
                            message = "برای صدور دقیق قرارداد و محاسبات متراژ، ابعاد و نوع تابلو را ثبت کنید.",
                            icon = Icons.Default.Straighten,
                            actionButtonText = "افزودن مشخصات تابلو",
                            onActionClick = { showCreateSpecDialog = true }
                        )
                    }
                } else {
                    items(specs, key = { it.id }) { spec ->
                        SpecItemCard(
                            spec = spec,
                            onEdit = { selectedSpecForEdit = spec },
                            onDelete = { specToDelete = spec }
                        )
                    }
                }

                // Section 2: Invoice & Items (فاکتور و ردیف‌های خدمات)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "فاکتور و ردیف‌های خدمات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Print invoice PDF
                            if (invoiceItems.isNotEmpty()) {
                                IconButton(onClick = {
                                    if (customer != null && invoice != null) {
                                        try {
                                            val expensesSum = projectExpenses.sumOf { it.amount }
                                            val pdf = PdfGenerator.generateInvoicePdf(
                                                context = context,
                                                contractor = contractorInfo,
                                                customer = customer!!,
                                                project = proj,
                                                invoice = invoice!!,
                                                items = invoiceItems,
                                                expensesTotal = expensesSum
                                            )
                                            PdfGenerator.sharePdf(context, pdf, "فاکتور تابلوسازی - ${proj.title}")
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "خطا در چاپ فاکتور: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = "چاپ فاکتور", tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    if (invoice == null) {
                                        // auto create invoice for project
                                        val newInvoice = InvoiceEntity(
                                            id = UUID.randomUUID().toString(),
                                            projectId = proj.id,
                                            issueDate = JalaliDate.today().formatShort()
                                        )
                                        viewModel.saveInvoice(newInvoice, isNew = true) {
                                            showAddItemDialog = true
                                        }
                                    } else {
                                        showAddItemDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ردیف فاکتور")
                            }
                        }
                    }
                }

                if (invoiceItems.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "ردیف فاکتور ثبت نشده است",
                            message = "خدمات و متریال تابلوسازی (برش لیزر، چلنیوم، کامپوزیت، نصب) را اضافه کنید.",
                            icon = Icons.Default.Receipt
                        )
                    }
                } else {
                    items(invoiceItems, key = { it.id }) { item ->
                        AppCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.serviceType, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "متراژ/تعداد: ${item.quantity} • قیمت واحد: ${FormatUtils.formatCurrency(item.unitPrice, currencyUnit)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (item.description.isNotEmpty()) {
                                        Text("توضیح: ${item.description}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = FormatUtils.formatCurrency(item.calculatedTotal, currencyUnit),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row {
                                        IconButton(onClick = { selectedItemForEdit = item }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "ویرایش", modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { itemToDelete = item }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Invoice Financial totals summary
                    item {
                        val itemsSum = invoiceItems.sumOf { it.calculatedTotal }
                        val discount = invoice?.discount ?: 0L
                        val expensesSum = projectExpenses.sumOf { it.amount }
                        val finalNet = itemsSum - discount + expensesSum

                        AppCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("جمع اقلام خدمات و ساخت:", style = MaterialTheme.typography.bodyMedium)
                                    Text(FormatUtils.formatCurrency(itemsSum, currencyUnit), fontWeight = FontWeight.Bold)
                                }
                                if (discount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("تخفیف:", style = MaterialTheme.typography.bodyMedium)
                                        Text("- ${FormatUtils.formatCurrency(discount, currencyUnit)}", fontWeight = FontWeight.Bold, color = MoneyPositive)
                                    }
                                }
                                if (expensesSum > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("هزینه‌های متصل به فاکتور:", style = MaterialTheme.typography.bodyMedium)
                                        Text("+ ${FormatUtils.formatCurrency(expensesSum, currencyUnit)}", fontWeight = FontWeight.Bold, color = MoneyNegative)
                                    }
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("مبلغ نهایی قابل پرداخت فاکتور:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(FormatUtils.formatCurrency(finalNet, currencyUnit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // Spec Dialogs
    if (showCreateSpecDialog) {
        SpecEditDialog(
            projectId = projectId,
            spec = null,
            onDismiss = { showCreateSpecDialog = false },
            onSave = { entity, isNew ->
                viewModel.saveSpec(entity, isNew) {
                    showCreateSpecDialog = false
                }
            }
        )
    }

    selectedSpecForEdit?.let { sp ->
        SpecEditDialog(
            projectId = projectId,
            spec = sp,
            onDismiss = { selectedSpecForEdit = null },
            onSave = { entity, isNew ->
                viewModel.saveSpec(entity, isNew) {
                    selectedSpecForEdit = null
                }
            }
        )
    }

    DeleteConfirmationDialog(
        show = specToDelete != null,
        title = "حذف مشخصات تابلو",
        message = "آیا از حذف این ردیف مشخصات تابلو اطمینان دارید؟",
        itemDetail = specToDelete?.subTitle,
        onConfirm = {
            specToDelete?.let { sp ->
                viewModel.deleteSpec(sp) {
                    specToDelete = null
                }
            }
        },
        onDismiss = { specToDelete = null }
    )

    // Invoice Item Dialogs
    if (showAddItemDialog && invoice != null) {
        InvoiceItemEditDialog(
            invoiceId = invoice!!.id,
            item = null,
            currencyUnit = currencyUnit,
            onDismiss = { showAddItemDialog = false },
            onSave = { entity, isNew ->
                viewModel.saveInvoiceItem(entity, isNew) {
                    showAddItemDialog = false
                }
            }
        )
    }

    selectedItemForEdit?.let { itm ->
        InvoiceItemEditDialog(
            invoiceId = itm.invoiceId,
            item = itm,
            currencyUnit = currencyUnit,
            onDismiss = { selectedItemForEdit = null },
            onSave = { entity, isNew ->
                viewModel.saveInvoiceItem(entity, isNew) {
                    selectedItemForEdit = null
                }
            }
        )
    }

    DeleteConfirmationDialog(
        show = itemToDelete != null,
        title = "حذف ردیف فاکتور",
        message = "آیا از حذف این ردیف خدمت اطمینان دارید؟",
        itemDetail = itemToDelete?.serviceType,
        onConfirm = {
            itemToDelete?.let { itm ->
                viewModel.deleteInvoiceItem(itm) {
                    itemToDelete = null
                }
            }
        },
        onDismiss = { itemToDelete = null }
    )

    if (showProjectEditDialog && project != null) {
        ProjectEditDialog(
            project = project,
            customers = customers,
            onDismiss = { showProjectEditDialog = false },
            onSave = { entity, isNew ->
                viewModel.saveProject(entity, isNew) {
                    showProjectEditDialog = false
                }
            }
        )
    }
}

@Composable
private fun SpecItemCard(
    spec: ProjectSpecEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AppCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(spec.subTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("نوع تابلو: ${spec.signType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("طول: ${spec.lengthCm} cm", style = MaterialTheme.typography.labelMedium)
                    Text("عرض: ${spec.widthCm} cm", style = MaterialTheme.typography.labelMedium)
                    Text("عمق: ${spec.depthCm} cm", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (spec.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("توضیحات: ${spec.description}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Attached photo previews
            val uris = remember(spec.attachmentUris) {
                spec.attachmentUris.split(",").filter { it.isNotBlank() }
            }
            if (uris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(uris) { u ->
                        Image(
                            painter = rememberAsyncImagePainter(u),
                            contentDescription = "طرح تابلو",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }
}
