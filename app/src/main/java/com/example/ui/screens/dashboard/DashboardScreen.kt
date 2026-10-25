package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.AuditLogEntity
import com.example.ui.components.common.AppCard
import com.example.ui.components.common.AppTopBar
import com.example.ui.components.common.StatCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodels.SignageViewModel
import com.example.util.FormatUtils
import com.example.util.JalaliDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: SignageViewModel,
    onNavigateTo: (String) -> Unit,
    onOpenCustomerDialog: () -> Unit,
    onOpenProjectDialog: () -> Unit,
    onOpenPaymentDialog: () -> Unit,
    onOpenCheckDialog: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val checks by viewModel.checks.collectAsState()
    val contracts by viewModel.contracts.collectAsState()
    val recentAudits by viewModel.recentAudits.collectAsState()
    val currencyUnit by viewModel.currencyUnit.collectAsState()
    val contractorInfo by viewModel.contractorInfo.collectAsState()

    // Calculated metrics
    val activeProjectsCount = remember(projects) {
        projects.count { it.status != "تکمیل شده" && it.status != "تسویه شده" }
    }

    val totalContractsAmount = remember(contracts) {
        contracts.sumOf { it.totalAmount - it.discount }
    }

    val totalReceipts = remember(payments) {
        payments.filter { it.type == "RECEIPT" }.sumOf { it.amount }
    }

    val pendingChecks = remember(checks) {
        checks.filter { it.status == "PENDING" }
    }
    val pendingChecksAmount = remember(pendingChecks) {
        pendingChecks.sumOf { it.amount }
    }

    val todayStr = remember { JalaliDate.today().formatShort() }
    val overdueChecksCount = remember(checks, todayStr) {
        checks.count { it.status == "PENDING" && it.dueDate <= todayStr }
    }

    // Customer balance calculation
    val totalCustomerDebt = remember(contracts, payments, checks) {
        val billed = contracts.sumOf { it.totalAmount - it.discount }
        val paid = payments.filter { it.type == "RECEIPT" }.sumOf { it.amount }
        val clearedChecks = checks.filter { it.status == "CLEARED" }.sumOf { it.amount }
        (billed - paid - clearedChecks).coerceAtLeast(0L)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = contractorInfo.businessName,
                subtitle = "مدیریت سفارشات، پروژه‌ها و امور مالی",
                actions = {
                    IconButton(onClick = { onNavigateTo(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "تنظیمات")
                    }
                }
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
            // Quick action chips
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "دسترسی سریع",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickActionButton(
                            title = "مشتری جدید",
                            icon = Icons.Default.PersonAdd,
                            color = PrimaryBlue,
                            onClick = onOpenCustomerDialog
                        )
                    }
                    item {
                        QuickActionButton(
                            title = "پروژه جدید",
                            icon = Icons.Default.AddBusiness,
                            color = PrimaryAmber,
                            onClick = onOpenProjectDialog
                        )
                    }
                    item {
                        QuickActionButton(
                            title = "ثبت دریافتی",
                            icon = Icons.Default.Payments,
                            color = PrimaryEmerald,
                            onClick = onOpenPaymentDialog
                        )
                    }
                    item {
                        QuickActionButton(
                            title = "ثبت چک",
                            icon = Icons.Default.ReceiptLong,
                            color = PrimaryPurple,
                            onClick = onOpenCheckDialog
                        )
                    }
                }
            }

            // Stat Cards Grid (2 columns)
            item {
                Text(
                    text = "خلاصه وضعیت کارگاه و مالی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "پروژه‌های فعال",
                        value = "$activeProjectsCount پروژه",
                        subtitle = "از مجموع ${projects.size} سفارش",
                        icon = Icons.Default.Engineering,
                        iconColor = PrimaryAmber,
                        onClick = { onNavigateTo(Screen.Projects.route) },
                        modifier = Modifier.weight(1f).testTag("stat_card_active_projects")
                    )
                    StatCard(
                        title = "تعداد مشتریان",
                        value = "${customers.size} نفر",
                        subtitle = "مشتریان ثبت‌شده",
                        icon = Icons.Default.People,
                        iconColor = PrimaryBlue,
                        onClick = { onNavigateTo(Screen.Customers.route) },
                        modifier = Modifier.weight(1f).testTag("stat_card_customers")
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "کل مبالغ قراردادها",
                        value = FormatUtils.formatCurrency(totalContractsAmount, currencyUnit),
                        subtitle = "${contracts.size} قرارداد رسمی",
                        icon = Icons.Default.Assignment,
                        iconColor = PrimaryPurple,
                        onClick = { onNavigateTo(Screen.Contracts.route) },
                        modifier = Modifier.weight(1f).testTag("stat_card_contracts")
                    )
                    StatCard(
                        title = "کل دریافتی‌های نقدی",
                        value = FormatUtils.formatCurrency(totalReceipts, currencyUnit),
                        subtitle = "تسویه‌شده از کارفرما",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = MoneyPositive,
                        onClick = { onNavigateTo(Screen.Finance.route) },
                        modifier = Modifier.weight(1f).testTag("stat_card_receipts")
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "مانده طلب از مشتریان",
                        value = FormatUtils.formatCurrency(totalCustomerDebt, currencyUnit),
                        subtitle = "بدهی تسویه‌نشده",
                        icon = Icons.Default.TrendingUp,
                        iconColor = if (totalCustomerDebt > 0) MoneyWarning else MoneyPositive,
                        onClick = { onNavigateTo(Screen.Finance.route) },
                        modifier = Modifier.weight(1f).testTag("stat_card_debt")
                    )
                    StatCard(
                        title = "چک‌های در جریان وصول",
                        value = FormatUtils.formatCurrency(pendingChecksAmount, currencyUnit),
                        subtitle = if (overdueChecksCount > 0) "$overdueChecksCount چک سررسیدشده!" else "${pendingChecks.size} برگ چک",
                        icon = Icons.Default.ReceiptLong,
                        iconColor = if (overdueChecksCount > 0) MoneyNegative else PrimaryCyan,
                        onClick = { onNavigateTo(Screen.Finance.route) },
                        modifier = Modifier.weight(1f).testTag("stat_card_checks")
                    )
                }
            }

            // Recent Audit Logs / Activities
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "آخرین فعالیت‌ها و سوابق تغییرات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${recentAudits.size} رویداد",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (recentAudits.isEmpty()) {
                item {
                    AppCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هنوز فعالیتی ثبت نشده است. با افزودن مشتری یا پروژه جدید شروع کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(recentAudits.take(8)) { audit ->
                    AuditLogItem(audit)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AuditLogItem(audit: AuditLogEntity) {
    val (actionIcon, actionColor) = when (audit.action) {
        "CREATE" -> Icons.Default.AddCircle to MoneyPositive
        "UPDATE" -> Icons.Default.Edit to PrimaryBlue
        "DELETE" -> Icons.Default.Delete to MoneyNegative
        else -> Icons.Default.Info to PrimaryAmber
    }

    val timeString = remember(audit.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        sdf.format(Date(audit.timestamp))
    }

    AppCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = actionColor.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(actionIcon, contentDescription = null, tint = actionColor, modifier = Modifier.size(18.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audit.details,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "کاربر: ${audit.userId} • ساعت $timeString",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
