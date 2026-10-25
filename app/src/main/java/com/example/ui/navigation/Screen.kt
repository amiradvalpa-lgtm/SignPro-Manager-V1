package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "داشبورد", Icons.Default.Dashboard)
    object Customers : Screen("customers", "مشتریان", Icons.Default.People)
    object Projects : Screen("projects", "پروژه‌ها", Icons.Default.Work)
    object Finance : Screen("finance", "امور مالی", Icons.Default.AccountBalanceWallet)
    object Contracts : Screen("contracts", "قراردادها", Icons.Default.Description)
    object Settings : Screen("settings", "تنظیمات", Icons.Default.Settings)

    // Sub-screens
    object CustomerDetail : Screen("customer_detail/{customerId}", "جزئیات مشتری") {
        fun createRoute(customerId: String) = "customer_detail/$customerId"
    }

    object ProjectDetail : Screen("project_detail/{projectId}", "جزئیات پروژه") {
        fun createRoute(projectId: String) = "project_detail/$projectId"
    }

    object ContractEditor : Screen("contract_editor/{projectId}", "تنظیم قرارداد") {
        fun createRoute(projectId: String) = "contract_editor/$projectId"
    }

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(
                Dashboard,
                Customers,
                Projects,
                Finance,
                Contracts
            )
    }
}
