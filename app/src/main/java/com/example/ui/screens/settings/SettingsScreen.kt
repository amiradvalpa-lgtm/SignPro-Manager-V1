package com.example.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.data.local.entities.*
import com.example.data.preferences.ContractorInfo
import com.example.ui.components.common.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.SignageViewModel
import com.example.util.BackupMetadata
import com.example.util.FormatUtils
import com.example.util.JalaliDate
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
    val stages by viewModel.stages.collectAsState()
    val trashItems by viewModel.trashItems.collectAsState()
    val recentAudits by viewModel.recentAudits.collectAsState()
    val warnUnsavedChanges by viewModel.warnUnsavedChanges.collectAsState()

    // Dialog states
    var showContractorDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategoryForEdit by remember { mutableStateOf<ExpenseCategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<ExpenseCategoryEntity?>(null) }

    // Project Stages dialogs
    var showStageManagerDialog by remember { mutableStateOf(false) }
    var selectedStageForEdit by remember { mutableStateOf<ProjectStageEntity?>(null) }
    var showAddStageDialog by remember { mutableStateOf(false) }
    var stageToDelete by remember { mutableStateOf<ProjectStageEntity?>(null) }

    // Trash & Audits dialogs
    var showTrashDialog by remember { mutableStateOf(false) }
    var itemToRestore by remember { mutableStateOf<TrashItem?>(null) }
    var showAuditLogsDialog by remember { mutableStateOf(false) }

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

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Warn on unsaved changes switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("هشدار تغییرات ذخیره‌نشده", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("هنگام خروج از فرم‌ها قبل از ذخیره، پیام هشدار نمایش داده شود", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = warnUnsavedChanges,
                                onCheckedChange = { viewModel.setWarnUnsavedChanges(it) },
                                modifier = Modifier.testTag("switch_warn_unsaved")
                            )
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

            // Section: Project Stages Management
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مدیریت مراحل پروژه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showAddStageDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن مرحله")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                AppCard {
                    Column(modifier = Modifier.padding(10.dp)) {
                        val sortedStages = stages.sortedBy { it.displayOrder }
                        if (sortedStages.isEmpty()) {
                            Text("مرحله‌ای تعریف نشده است", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                        } else {
                            sortedStages.forEachIndexed { index, stage ->
                                val stageColor = try {
                                    Color(android.graphics.Color.parseColor(stage.colorHex))
                                } catch (_: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(stageColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${stage.displayOrder}. ${stage.title}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Move up button
                                        IconButton(
                                            onClick = {
                                                if (index > 0) {
                                                    val mutable = sortedStages.toMutableList()
                                                    val prev = mutable[index - 1]
                                                    mutable[index - 1] = stage
                                                    mutable[index] = prev
                                                    viewModel.reorderStages(mutable)
                                                }
                                            },
                                            enabled = index > 0,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "انتقال به بالا", modifier = Modifier.size(20.dp))
                                        }

                                        // Move down button
                                        IconButton(
                                            onClick = {
                                                if (index < sortedStages.size - 1) {
                                                    val mutable = sortedStages.toMutableList()
                                                    val next = mutable[index + 1]
                                                    mutable[index + 1] = stage
                                                    mutable[index] = next
                                                    viewModel.reorderStages(mutable)
                                                }
                                            },
                                            enabled = index < sortedStages.size - 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "انتقال به پایین", modifier = Modifier.size(20.dp))
                                        }

                                        IconButton(onClick = { selectedStageForEdit = stage }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "ویرایش", modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { stageToDelete = stage }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                if (index < sortedStages.size - 1) {
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }

            // Section: Trash & Activity Log
            item {
                Text("سطل زباله و سابقه فعالیت‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                AppCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Trash Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTrashDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("سطل زباله", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("مشاهده و بازیابی موارد حذف‌شده (${trashItems.size} مورد)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Audit Log Option (Moved from Dashboard)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAuditLogsDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("سابقه فعالیت‌ها و رویدادها", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("مشاهده آخرین تغییرات و ثبت اطلاعات (${recentAudits.size} رویداد)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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

    // Stage Dialogs
    if (showAddStageDialog) {
        val nextOrder = (stages.maxOfOrNull { it.displayOrder } ?: 0) + 1
        StageEditDialog(
            stage = null,
            nextOrder = nextOrder,
            onDismiss = { showAddStageDialog = false },
            onSave = { entity, isNew ->
                viewModel.saveStage(entity, isNew) {
                    showAddStageDialog = false
                }
            }
        )
    }

    selectedStageForEdit?.let { stg ->
        StageEditDialog(
            stage = stg,
            nextOrder = stg.displayOrder,
            onDismiss = { selectedStageForEdit = null },
            onSave = { entity, isNew ->
                viewModel.saveStage(entity, isNew) {
                    selectedStageForEdit = null
                }
            }
        )
    }

    DeleteConfirmationDialog(
        show = stageToDelete != null,
        title = "حذف مرحله کارگاهی",
        message = "آیا از حذف مرحله «${stageToDelete?.title}» اطمینان دارید؟",
        onConfirm = {
            stageToDelete?.let { stg ->
                viewModel.deleteStage(stg) {
                    stageToDelete = null
                }
            }
        },
        onDismiss = { stageToDelete = null }
    )

    // Trash Dialog
    if (showTrashDialog) {
        TrashDialog(
            trashItems = trashItems,
            onDismiss = { showTrashDialog = false },
            onRequestRestore = { item -> itemToRestore = item }
        )
    }

    // Pre-restore confirmation dialog (Rule: confirm before restore)
    DeleteConfirmationDialog(
        show = itemToRestore != null,
        title = "تأیید بازیابی از سطل زباله",
        message = "آیا از بازیابی «${itemToRestore?.title}» (${itemToRestore?.categoryLabel}) به فهرست فعال اطمینان دارید؟",
        confirmButtonText = "بازیابی",
        onConfirm = {
            itemToRestore?.let { itm ->
                viewModel.restoreTrashItem(itm) {
                    itemToRestore = null
                    Toast.makeText(context, "با موفقیت بازیابی شد.", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onDismiss = { itemToRestore = null }
    )

    // Audit Logs Dialog
    if (showAuditLogsDialog) {
        AuditLogsDialog(
            logs = recentAudits,
            onDismiss = { showAuditLogsDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractorEditDialog(
    currentInfo: ContractorInfo,
    onDismiss: () -> Unit,
    onSave: (ContractorInfo) -> Unit,
    warnUnsavedChanges: Boolean = true
) {
    var name by remember { mutableStateOf(currentInfo.name) }
    var businessName by remember { mutableStateOf(currentInfo.businessName) }
    var address by remember { mutableStateOf(currentInfo.address) }
    var phone by remember { mutableStateOf(currentInfo.phone) }
    var accountNumber by remember { mutableStateOf(currentInfo.accountNumber) }
    var shabaNumber by remember { mutableStateOf(currentInfo.shabaNumber) }
    var cardNumber by remember { mutableStateOf(currentInfo.cardNumber) }
    var bankName by remember { mutableStateOf(currentInfo.bankName) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val isDirty = remember(name, businessName, address, phone, accountNumber, shabaNumber, cardNumber, bankName) {
        name != currentInfo.name ||
                businessName != currentInfo.businessName ||
                address != currentInfo.address ||
                phone != currentInfo.phone ||
                accountNumber != currentInfo.accountNumber ||
                shabaNumber != currentInfo.shabaNumber ||
                cardNumber != currentInfo.cardNumber ||
                bankName != currentInfo.bankName
    }

    val attemptDismiss = {
        if (warnUnsavedChanges && isDirty) {
            showUnsavedDialog = true
        } else {
            onDismiss()
        }
    }

    val executeSave = {
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
    }

    AppFormBottomSheet(
        title = "مشخصات پیمانکار و کارگاه",
        subtitle = "اطلاعات ثبتی، نشانی کارگاه و شماره حساب‌های بانکی",
        onDismissRequest = onDismiss,
        isDirty = isDirty,
        warnUnsavedChanges = warnUnsavedChanges,
        onAttemptDismiss = attemptDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PersianTextField(value = name, onValueChange = { name = it }, label = "نام پیمانکار")
            PersianTextField(value = businessName, onValueChange = { businessName = it }, label = "نام مجموعه یا کارگاه")
            PersianTextField(value = phone, onValueChange = { phone = it }, label = "تلفن تماس")
            PersianTextField(value = address, onValueChange = { address = it }, label = "نشانی کارگاه")
            PersianTextField(value = accountNumber, onValueChange = { accountNumber = it }, label = "شماره حساب بانک ملی")
            PersianTextField(value = cardNumber, onValueChange = { cardNumber = it }, label = "شماره کارت")
            PersianTextField(value = shabaNumber, onValueChange = { shabaNumber = it }, label = "شماره شبا (IR)")
        }

        StickyBottomActionBar(
            entityName = "مشخصات پیمانکار",
            saveButtonText = "ذخیره مشخصات پیمانکار",
            onSave = executeSave,
            onCancel = onDismiss,
            isDirty = isDirty,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            show = true,
            onSaveAndExit = {
                showUnsavedDialog = false
                executeSave()
            },
            onDiscardAndExit = {
                showUnsavedDialog = false
                onDismiss()
            },
            onDismiss = { showUnsavedDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileDialog(
    currentUser: com.example.data.local.entities.UserEntity?,
    onDismiss: () -> Unit,
    onSave: (fullName: String, username: String, newPass: String?, hint: String) -> Unit,
    warnUnsavedChanges: Boolean = true
) {
    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "امیرحسین حسنخانی") }
    var username by remember { mutableStateOf(currentUser?.username ?: "admin") }
    var newPassword by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf(currentUser?.securityHint ?: "رمز پیش‌فرض: 1234") }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val isDirty = remember(fullName, username, newPassword, hint) {
        fullName != (currentUser?.fullName ?: "") ||
                username != (currentUser?.username ?: "") ||
                newPassword.isNotBlank() ||
                hint != (currentUser?.securityHint ?: "")
    }

    val attemptDismiss = {
        if (warnUnsavedChanges && isDirty) {
            showUnsavedDialog = true
        } else {
            onDismiss()
        }
    }

    val executeSave = {
        onSave(
            fullName.trim(),
            username.trim(),
            if (newPassword.isNotBlank()) newPassword.trim() else null,
            hint.trim()
        )
    }

    AppFormBottomSheet(
        title = "تنظیمات رمز عبور و پروفایل",
        subtitle = "مدیریت حساب کاربری ادمین و راهنمای یادآوری رمز",
        onDismissRequest = onDismiss,
        isDirty = isDirty,
        warnUnsavedChanges = warnUnsavedChanges,
        onAttemptDismiss = attemptDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                label = "راهنمای یادآوری رمز (Hint)",
                placeholder = "متنی که در صورت فراموشی به عنوان راهنما نشان داده می‌شود"
            )
        }

        StickyBottomActionBar(
            entityName = "اطلاعات کاربر",
            saveButtonText = "ذخیره اطلاعات کاربر",
            onSave = executeSave,
            onCancel = onDismiss,
            isDirty = isDirty,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            show = true,
            onSaveAndExit = {
                showUnsavedDialog = false
                executeSave()
            },
            onDiscardAndExit = {
                showUnsavedDialog = false
                onDismiss()
            },
            onDismiss = { showUnsavedDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCategoryDialog(
    category: ExpenseCategoryEntity?,
    currencyUnit: String,
    onDismiss: () -> Unit,
    onSave: (ExpenseCategoryEntity, Boolean) -> Unit,
    warnUnsavedChanges: Boolean = true
) {
    val isNew = category == null
    var title by remember { mutableStateOf(category?.title ?: "") }
    var defaultAmount by remember { mutableStateOf(category?.defaultAmount ?: 0L) }
    var description by remember { mutableStateOf(category?.description ?: "") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val isDirty = remember(title, defaultAmount, description) {
        if (isNew) {
            title.isNotBlank() || defaultAmount > 0L
        } else {
            title != (category?.title ?: "") ||
                    defaultAmount != (category?.defaultAmount ?: 0L) ||
                    description != (category?.description ?: "")
        }
    }

    val attemptDismiss = {
        if (warnUnsavedChanges && isDirty) {
            showUnsavedDialog = true
        } else {
            onDismiss()
        }
    }

    val executeSave = {
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
    }

    AppFormBottomSheet(
        title = if (isNew) "افزودن عنوان هزینه جدید" else "ویرایش عنوان هزینه",
        subtitle = "تعریف عناوین پرکاربرد هزینه‌های کارگاهی و مبلغ پیش‌فرض",
        onDismissRequest = onDismiss,
        isDirty = isDirty,
        warnUnsavedChanges = warnUnsavedChanges,
        onAttemptDismiss = attemptDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PersianTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = "عنوان هزینه",
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

        StickyBottomActionBar(
            entityName = "عنوان هزینه",
            saveButtonText = "ذخیره عنوان هزینه",
            onSave = executeSave,
            onCancel = onDismiss,
            isDirty = isDirty,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            show = true,
            onSaveAndExit = {
                showUnsavedDialog = false
                executeSave()
            },
            onDiscardAndExit = {
                showUnsavedDialog = false
                onDismiss()
            },
            onDismiss = { showUnsavedDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageEditDialog(
    stage: com.example.data.local.entities.ProjectStageEntity?,
    nextOrder: Int,
    onDismiss: () -> Unit,
    onSave: (com.example.data.local.entities.ProjectStageEntity, Boolean) -> Unit,
    warnUnsavedChanges: Boolean = true
) {
    val isNew = stage == null
    var title by remember { mutableStateOf(stage?.title ?: "") }
    var selectedColor by remember { mutableStateOf(stage?.colorHex ?: "#3B82F6") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val presetColors = listOf(
        "#3B82F6", "#10B981", "#F59E0B", "#EF4444",
        "#8B5CF6", "#EC4899", "#14B8A6", "#6366F1"
    )

    val isDirty = remember(title, selectedColor) {
        if (isNew) {
            title.isNotBlank()
        } else {
            title != (stage?.title ?: "") || selectedColor != (stage?.colorHex ?: "")
        }
    }

    val attemptDismiss = {
        if (warnUnsavedChanges && isDirty) {
            showUnsavedDialog = true
        } else {
            onDismiss()
        }
    }

    val executeSave = {
        if (title.trim().isEmpty()) {
            titleError = "عنوان مرحله الزامی است."
        } else {
            val entity = stage?.copy(
                name = title.trim(),
                colorHex = selectedColor
            ) ?: ProjectStageEntity(
                id = UUID.randomUUID().toString(),
                name = title.trim(),
                colorHex = selectedColor,
                displayOrder = nextOrder
            )
            onSave(entity, isNew)
        }
    }

    AppFormBottomSheet(
        title = if (isNew) "افزودن مرحله جدید" else "ویرایش مرحله کارگاهی",
        subtitle = "نام مرحله و نشانگر رنگی در پرونده پروژه",
        onDismissRequest = onDismiss,
        isDirty = isDirty,
        warnUnsavedChanges = warnUnsavedChanges,
        onAttemptDismiss = attemptDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PersianTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = "عنوان مرحله کارگاهی",
                placeholder = "مثال: خم‌کاری و جوشکاری",
                isError = titleError != null,
                errorMessage = titleError
            )

            Text("رنگ نشانگر مرحله:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(presetColors) { hex ->
                    val isSelected = selectedColor.equals(hex, ignoreCase = true)
                    val color = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (_: Exception) { Color.Gray }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        StickyBottomActionBar(
            entityName = "مرحله کارگاهی",
            saveButtonText = "ذخیره مرحله کارگاهی",
            onSave = executeSave,
            onCancel = onDismiss,
            isDirty = isDirty,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            show = true,
            onSaveAndExit = {
                showUnsavedDialog = false
                executeSave()
            },
            onDiscardAndExit = {
                showUnsavedDialog = false
                onDismiss()
            },
            onDismiss = { showUnsavedDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashDialog(
    trashItems: List<TrashItem>,
    onDismiss: () -> Unit,
    onRequestRestore: (TrashItem) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    AppFormBottomSheet(
        title = "سطل زباله",
        subtitle = "${trashItems.size} مورد حذف‌شده قابل بازیابی",
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("فهرست یکپارچه") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("گروه‌بندی دسته‌ها") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (trashItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "سطل زباله خالی است.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (selectedTab == 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                ) {
                    items(trashItems, key = { it.id }) { item ->
                        TrashItemRow(item = item, onRestore = { onRequestRestore(item) })
                    }
                }
            } else {
                val grouped = remember(trashItems) { trashItems.groupBy { it.categoryLabel } }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                ) {
                    grouped.forEach { (categoryName, items) ->
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "$categoryName (${items.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        items(items, key = { it.id }) { item ->
                            TrashItemRow(item = item, onRestore = { onRequestRestore(item) })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text("بستن")
            }
        }
    }
}

@Composable
fun TrashItemRow(
    item: TrashItem,
    onRestore: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.categoryLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(item.details, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            FilledTonalButton(
                onClick = onRestore,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("بازیابی", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsDialog(
    logs: List<com.example.data.local.entities.AuditLogEntity>,
    onDismiss: () -> Unit
) {
    AppFormBottomSheet(
        title = "سابقه فعالیت‌ها و رویدادها",
        subtitle = "${logs.size} رویداد ثبت‌شده در سیستم",
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هیچ رویدادی ثبت نشده است.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        val actionColor = when (log.action) {
                            "INSERT" -> MoneyPositive
                            "UPDATE" -> MaterialTheme.colorScheme.primary
                            "DELETE" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }
                        val actionLabel = when (log.action) {
                            "INSERT" -> "ثبت جدید"
                            "UPDATE" -> "ویرایش"
                            "DELETE" -> "حذف"
                            "RESTORE" -> "بازیابی"
                            else -> log.action
                        }

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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = actionColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = actionLabel,
                                                color = actionColor,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(log.entityType, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    if (log.details.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(log.details, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(
                                    text = JalaliDate.fromMillis(log.timestamp).formatShort(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text("بستن")
            }
        }
    }
}

@Preview(name = "Contractor Dialog Preview", showBackground = true)
@Composable
fun ContractorEditDialogPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ContractorEditDialog(
                currentInfo = ContractorInfo(
                    name = "امیرحسین حسنخانی",
                    businessName = "تابلوسازی آلپا",
                    address = "تهران، شهرک صنعتی چهاردانگه",
                    phone = "09121234567"
                ),
                onDismiss = {},
                onSave = {}
            )
        }
    }
}

@Preview(name = "Stage Edit Preview", showBackground = true)
@Composable
fun StageEditDialogPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StageEditDialog(
                stage = ProjectStageEntity(
                    id = "s1",
                    name = "خم‌کاری و جوشکاری شاسی",
                    colorHex = "#F59E0B",
                    displayOrder = 2
                ),
                nextOrder = 3,
                onDismiss = {},
                onSave = { _, _ -> }
            )
        }
    }
}


