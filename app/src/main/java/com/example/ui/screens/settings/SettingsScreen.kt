package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.ExpenseCategoryEntity
import com.example.data.preferences.ContractorInfo
import com.example.ui.components.common.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.SignageViewModel
import com.example.util.BackupMetadata
import com.example.util.FormatUtils
import java.util.UUID

@Composable
fun SettingsScreen(
    viewModel: SignageViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currencyUnit by viewModel.currencyUnit.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val primaryColorKey by viewModel.primaryColorKey.collectAsState()
    val contractorInfo by viewModel.contractorInfo.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAuthEnabled by viewModel.isAuthEnabled.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val categories by viewModel.expenseCategories.collectAsState()
    val backupList by viewModel.backupList.collectAsState()

    // Dialog states
    var showContractorDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategoryForEdit by remember { mutableStateOf<ExpenseCategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<ExpenseCategoryEntity?>(null) }

    var backupToRestore by remember { mutableStateOf<BackupMetadata?>(null) }
    var showClearSampleDialog by remember { mutableStateOf(false) }
    var showSeedSampleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "تنظیمات نرم‌افزار",
                subtitle = "پیکربندی تابلوسازی، امنیت، تم و پشتیبان‌گیری",
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Contractor & Business Details
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("اطلاعات پیمانکار و کارگاه تابلوسازی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                AppCard {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contractorInfo.businessName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("مدیریت: ${contractorInfo.name} • ${contractorInfo.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("نشانی: ${contractorInfo.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("کارت: ${contractorInfo.cardNumber} (بانک ملی)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { showContractorDialog = true }, modifier = Modifier.testTag("edit_contractor_btn")) {
                                Icon(Icons.Default.Edit, contentDescription = "ویرایش مشخصات", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Section 2: Currency & Appearance (Theme, Colors)
            item {
                Text("واحد پول و ظاهر برنامه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                AppCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Currency Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("واحد پول محاسبات:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = currencyUnit == "TOMAN",
                                    onClick = { viewModel.setCurrencyUnit("TOMAN") },
                                    label = { Text("تومان") }
                                )
                                FilterChip(
                                    selected = currencyUnit == "RIAL",
                                    onClick = { viewModel.setCurrencyUnit("RIAL") },
                                    label = { Text("ریال") }
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Theme Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("حالت تم:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = themeMode == "SYSTEM",
                                    onClick = { viewModel.setThemeMode("SYSTEM") },
                                    label = { Text("سیستم") }
                                )
                                FilterChip(
                                    selected = themeMode == "LIGHT",
                                    onClick = { viewModel.setThemeMode("LIGHT") },
                                    label = { Text("روشن") }
                                )
                                FilterChip(
                                    selected = themeMode == "DARK",
                                    onClick = { viewModel.setThemeMode("DARK") },
                                    label = { Text("تاریک") }
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Primary Color Palette
                        Column {
                            Text("رنگ اصلی برنامه:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            val colorOptions = listOf(
                                "AMBER" to ("کهربایی" to PrimaryAmber),
                                "BLUE" to ("آبی" to PrimaryBlue),
                                "CYAN" to ("فیروزه‌ای" to PrimaryCyan),
                                "EMERALD" to ("زمردی" to PrimaryEmerald),
                                "ROSE" to ("زرشکی" to PrimaryRose),
                                "PURPLE" to ("بنفش" to PrimaryPurple)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(colorOptions) { (key, pair) ->
                                    val (label, col) = pair
                                    val isSelected = primaryColorKey == key
                                    Surface(
                                        color = if (isSelected) col else col.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.setPrimaryColorKey(key) }
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Expense Categories Management
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مدیریت عنوان هزینه‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showCategoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن عنوان")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                AppCard {
                    Column(modifier = Modifier.padding(10.dp)) {
                        categories.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cat.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    if (cat.defaultAmount > 0) {
                                        Text("مبلغ پیش‌فرض: ${FormatUtils.formatCurrency(cat.defaultAmount, currencyUnit)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { selectedCategoryForEdit = cat }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "ویرایش", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { categoryToDelete = cat }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            // Section 4: Security & User Profile (PIN / Biometric)
            item {
                Text("امنیت و کنترل دسترسی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                AppCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("قفل ورود به برنامه با رمز / PIN", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("در صورت فعال‌بودن، هنگام باز شدن برنامه رمز خواسته می‌شود", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isAuthEnabled,
                                onCheckedChange = { checked ->
                                    val user = currentUser
                                    viewModel.updateSecuritySettings(
                                        pinEnabled = checked,
                                        biometricEnabled = isBiometricEnabled,
                                        newPasswordRaw = null,
                                        hint = user?.securityHint ?: "1234",
                                        fullName = user?.fullName ?: "امیرحسین حسنخانی",
                                        username = user?.username ?: "admin"
                                    )
                                }
                            )
                        }

                        if (isAuthEnabled) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ورود با اثر انگشت (بیومتریک)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("استفاده از حسگر اثر انگشت گوشی برای ورود سریع", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = isBiometricEnabled,
                                    onCheckedChange = { checked ->
                                        val user = currentUser
                                        viewModel.updateSecuritySettings(
                                            pinEnabled = isAuthEnabled,
                                            biometricEnabled = checked,
                                            newPasswordRaw = null,
                                            hint = user?.securityHint ?: "1234",
                                            fullName = user?.fullName ?: "امیرحسین حسنخانی",
                                            username = user?.username ?: "admin"
                                        )
                                    }
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Button(
                            onClick = { showUserDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_security_profile_btn")
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تغییر رمز عبور و راهنمای یادآوری (Hint)")
                        }
                    }
                }
            }

            // Section 5: Backup & Restore
            item {
                Text("پشتیبان‌گیری و بازیابی اطلاعات (Backup / Restore)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                AppCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "پشتیبان‌گیری خودکار اطلاعات در پوشه داخلی گوشی با ساختار استاندارد JSON جهت جلوگیری از تخریب یا از دست رفتن اطلاعات.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                viewModel.performBackup { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("create_backup_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ایجاد فایل پشتیبان جدید (هم‌اکنون)")
                        }

                        if (backupList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("لیست فایل‌های پشتیبان قابل بازیابی:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            backupList.forEach { bkp ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(bkp.fileName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("حجم: ${bkp.fileSizeFormatted}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        FilledTonalButton(
                                            onClick = { backupToRestore = bkp },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("بازیابی", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section: Database & Sample Data Management
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("مدیریت دیتابیس و داده‌های فرضی (کارگاه)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                AppCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("وضعیت دیتابیس و آماده‌سازی برای کار واقعی", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "نرم‌افزار با اطلاعات فرضی جامع (مشتریان، سفارشات چلنیوم و کامپوزیت، فاکتورها، چک‌ها و قراردادها) راه‌اندازی شده است تا بتوانید گزارشات و محیط برنامه را تست کنید.\nبرای شروع کار واقعی، با زدن دکمه زیر اطلاعات فرضی پاک شده و دیتابیس کاملاً آماده ثبت اطلاعات واقعی شما می‌گردد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { showClearSampleDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("clear_sample_data_btn")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("پاک‌سازی اطلاعات فرضی و راه‌اندازی کارگاه")
                        }

                        OutlinedButton(
                            onClick = { showSeedSampleDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("seed_sample_data_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("بارگذاری مجدد اطلاعات نمونه کارگاه")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Contractor Info Dialog
    if (showContractorDialog) {
        ContractorEditDialog(
            currentInfo = contractorInfo,
            onDismiss = { showContractorDialog = false },
            onSave = { updated ->
                viewModel.updateContractorInfo(updated)
                showContractorDialog = false
                Toast.makeText(context, "مشخصات تابلوسازی ذخیره شد.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // User Profile Dialog
    if (showUserDialog) {
        UserProfileDialog(
            currentUser = currentUser,
            onDismiss = { showUserDialog = false },
            onSave = { name, user, newPass, hint ->
                viewModel.updateSecuritySettings(
                    pinEnabled = isAuthEnabled,
                    biometricEnabled = isBiometricEnabled,
                    newPasswordRaw = newPass,
                    hint = hint,
                    fullName = name,
                    username = user
                )
                showUserDialog = false
                Toast.makeText(context, "اطلاعات امنیتی بروزرسانی شد.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Expense Category Dialogs
    if (showCategoryDialog) {
        ExpenseCategoryDialog(
            category = null,
            currencyUnit = currencyUnit,
            onDismiss = { showCategoryDialog = false },
            onSave = { entity, isNew ->
                viewModel.saveExpenseCategory(entity, isNew) {
                    showCategoryDialog = false
                }
            }
        )
    }

    selectedCategoryForEdit?.let { cat ->
        ExpenseCategoryDialog(
            category = cat,
            currencyUnit = currencyUnit,
            onDismiss = { selectedCategoryForEdit = null },
            onSave = { entity, isNew ->
                viewModel.saveExpenseCategory(entity, isNew) {
                    selectedCategoryForEdit = null
                }
            }
        )
    }

    DeleteConfirmationDialog(
        show = categoryToDelete != null,
        title = "حذف عنوان هزینه",
        message = "آیا از حذف عنوان هزینه «${categoryToDelete?.title}» اطمینان دارید؟",
        onConfirm = {
            categoryToDelete?.let { cat ->
                viewModel.deleteExpenseCategory(cat) {
                    categoryToDelete = null
                }
            }
        },
        onDismiss = { categoryToDelete = null }
    )

    // Restore Confirmation Dialog
    DeleteConfirmationDialog(
        show = backupToRestore != null,
        title = "تأیید بازیابی اطلاعات (Restore)",
        message = "هشدار: با بازیابی این فایل، داده‌های پایگاه داده با اطلاعات نسخه پشتیبان همگام خواهند شد. آیا ادامه می‌دهید؟",
        itemDetail = backupToRestore?.fileName,
        onConfirm = {
            backupToRestore?.let { b ->
                viewModel.performRestore(b.filePath) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    backupToRestore = null
                }
            }
        },
        onDismiss = { backupToRestore = null }
    )

    // Clear Sample Data Dialog
    DeleteConfirmationDialog(
        show = showClearSampleDialog,
        title = "پاک‌سازی اطلاعات فرضی و راه‌اندازی کارگاه",
        message = "آیا مطمئن هستید؟ تمامی داده‌های تستی شامل مشتریان، پروژه‌ها، فاکتورها، چک‌ها و قراردادهای فرضی حذف خواهند شد و دیتابیس برای ورود اطلاعات واقعی کارگاه آماده می‌شود.\n(اطلاعات حساب کاربری، مشخصات کارگاه و دسته‌های هزینه محفوظ می‌مانند)",
        onConfirm = {
            viewModel.clearSampleData {
                showClearSampleDialog = false
                Toast.makeText(context, "اطلاعات فرضی با موفقیت پاک شد و دیتابیس آماده کار است.", Toast.LENGTH_LONG).show()
            }
        },
        onDismiss = { showClearSampleDialog = false }
    )

    // Seed Sample Data Dialog
    DeleteConfirmationDialog(
        show = showSeedSampleDialog,
        title = "بارگذاری مجدد اطلاعات نمونه",
        message = "آیا می‌خواهید داده‌های فرضی تابلوسازی (۳ مشتری، ۳ سفارش، فاکتورها، هزینه‌ها و چک‌ها) مجدداً در دیتابیس بارگذاری شوند؟",
        onConfirm = {
            viewModel.seedSampleData {
                showSeedSampleDialog = false
                Toast.makeText(context, "اطلاعات فرضی تابلوسازی با موفقیت بارگذاری شد.", Toast.LENGTH_SHORT).show()
            }
        },
        onDismiss = { showSeedSampleDialog = false }
    )
}

@Composable
fun ContractorEditDialog(
    currentInfo: ContractorInfo,
    onDismiss: () -> Unit,
    onSave: (ContractorInfo) -> Unit
) {
    var name by remember { mutableStateOf(currentInfo.name) }
    var businessName by remember { mutableStateOf(currentInfo.businessName) }
    var address by remember { mutableStateOf(currentInfo.address) }
    var phone by remember { mutableStateOf(currentInfo.phone) }
    var accountNumber by remember { mutableStateOf(currentInfo.accountNumber) }
    var shabaNumber by remember { mutableStateOf(currentInfo.shabaNumber) }
    var cardNumber by remember { mutableStateOf(currentInfo.cardNumber) }
    var bankName by remember { mutableStateOf(currentInfo.bankName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ویرایش مشخصات پیمانکار و کارگاه", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PersianTextField(value = name, onValueChange = { name = it }, label = "نام پیمانکار")
                PersianTextField(value = businessName, onValueChange = { businessName = it }, label = "نام مجموعه / کارگاه")
                PersianTextField(value = phone, onValueChange = { phone = it }, label = "تلفن تماس")
                PersianTextField(value = address, onValueChange = { address = it }, label = "نشانی کارگاه")
                PersianTextField(value = accountNumber, onValueChange = { accountNumber = it }, label = "شماره حساب بانک ملی")
                PersianTextField(value = cardNumber, onValueChange = { cardNumber = it }, label = "شماره کارت")
                PersianTextField(value = shabaNumber, onValueChange = { shabaNumber = it }, label = "شماره شبا (IR)")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        currentInfo.copy(
                            name = name.trim(),
                            businessName = businessName.trim(),
                            address = address.trim(),
                            phone = phone.trim(),
                            accountNumber = accountNumber.trim(),
                            shabaNumber = shabaNumber.trim(),
                            cardNumber = cardNumber.trim(),
                            bankName = bankName.trim()
                        )
                    )
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("ذخیره تغییرات")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("انصراف")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun UserProfileDialog(
    currentUser: com.example.data.local.entities.UserEntity?,
    onDismiss: () -> Unit,
    onSave: (fullName: String, username: String, newPass: String?, hint: String) -> Unit
) {
    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "امیرحسین حسنخانی") }
    var username by remember { mutableStateOf(currentUser?.username ?: "admin") }
    var newPassword by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf(currentUser?.securityHint ?: "رمز پیش‌فرض: 1234") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تنظیمات رمز عبور و پروفایل", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PersianTextField(value = fullName, onValueChange = { fullName = it }, label = "نام کاربر")
                PersianTextField(value = username, onValueChange = { username = it }, label = "نام کاربری")
                PersianTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "رمز عبور جدید (در صورت تمایل به تغییر)",
                    placeholder = "خالی بگذارید اگر تغییر نمی‌کند"
                )
                PersianTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = "راهنمای یادآوری رمز (Hint / پیامک)",
                    placeholder = "متنی که در صورت فراموشی به عنوان راهنما نشان داده می‌شود"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        fullName.trim(),
                        username.trim(),
                        if (newPassword.isNotBlank()) newPassword.trim() else null,
                        hint.trim()
                    )
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("انصراف")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ExpenseCategoryDialog(
    category: ExpenseCategoryEntity?,
    currencyUnit: String,
    onDismiss: () -> Unit,
    onSave: (ExpenseCategoryEntity, Boolean) -> Unit
) {
    val isNew = category == null
    var title by remember { mutableStateOf(category?.title ?: "") }
    var defaultAmount by remember { mutableStateOf(category?.defaultAmount ?: 0L) }
    var description by remember { mutableStateOf(category?.description ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "افزودن عنوان هزینه جدید" else "ویرایش عنوان هزینه", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PersianTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = "عنوان هزینه *",
                    placeholder = "مثال: کرایه بالابر، ورق کامپوزیت",
                    isError = titleError != null,
                    errorMessage = titleError
                )
                CurrencyInputField(
                    amount = defaultAmount,
                    onAmountChange = { defaultAmount = it },
                    label = "مبلغ پیش‌فرض (اختیاری)",
                    currencyUnit = currencyUnit
                )
                PersianTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "توضیحات"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isEmpty()) {
                        titleError = "عنوان هزینه الزامی است."
                    } else {
                        val entity = category?.copy(
                            title = title.trim(),
                            defaultAmount = defaultAmount,
                            description = description.trim()
                        ) ?: ExpenseCategoryEntity(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            defaultAmount = defaultAmount,
                            description = description.trim()
                        )
                        onSave(entity, isNew)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("انصراف")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
