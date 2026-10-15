package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRole
import com.example.ui.components.AddCircularDialog
import com.example.ui.components.HeadsUpPushBanner
import com.example.ui.components.NotificationDrawerSheet
import com.example.ui.components.RoleSwitcherModal
import com.example.ui.components.TopRegisterAppBar
import com.example.ui.parent.ParentDashboard
import com.example.ui.teacher.TeacherDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.RegisterViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: RegisterViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                TopRegisterAppBar(
                    currentRole = uiState.currentRole,
                    unreadNotifCount = unreadCount,
                    onSwitchRoleClick = { viewModel.setRoleSwitcherOpen(true) },
                    onNotificationsClick = { viewModel.setNotificationDrawerOpen(true) },
                    onResetToEmpty = { viewModel.resetToEmpty() },
                    onLoadSampleData = { viewModel.loadSampleData() },
                    onAddCircularClick = { viewModel.setAddCircularDialogOpen(true) }
                )
                // Real-time Push Notification Heads-up Banner
                HeadsUpPushBanner(
                    notification = uiState.currentHeadsup,
                    onDismiss = { viewModel.dismissHeadsup() },
                    onClick = {
                        viewModel.dismissHeadsup()
                        viewModel.setNotificationDrawerOpen(true)
                    }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentRole) {
                UserRole.DOCENTE -> TeacherDashboard(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                UserRole.GENITORE -> ParentDashboard(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Role switcher dialog
    if (uiState.showRoleSwitcherDialog) {
        RoleSwitcherModal(
            currentRole = uiState.currentRole,
            onRoleSelected = { role ->
                viewModel.setRole(role)
            },
            onDismiss = { viewModel.setRoleSwitcherOpen(false) }
        )
    }

    // Push notification drawer sheet
    if (uiState.isNotificationDrawerOpen) {
        NotificationDrawerSheet(
            notifications = notifications,
            onDismiss = { viewModel.setNotificationDrawerOpen(false) },
            onMarkAsRead = { id -> viewModel.markNotificationRead(id) },
            onClearAll = { viewModel.clearAllNotifications() },
            onTriggerDemo = { type -> viewModel.triggerDemoNotification(type) }
        )
    }

    // Publish circular dialog
    if (uiState.showAddCircularDialog) {
        AddCircularDialog(
            onDismiss = { viewModel.setAddCircularDialogOpen(false) },
            onConfirm = { number, title, category, date, content, requiresSignature, isUrgent ->
                viewModel.addCircular(number, title, category, date, content, requiresSignature, isUrgent)
                viewModel.setAddCircularDialogOpen(false)
            }
        )
    }
}
