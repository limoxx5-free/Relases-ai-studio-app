package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

fun getGradeColor(value: Double): Color {
    return when {
        value >= 8.0 -> GradeExcellent
        value >= 7.0 -> GradeGood
        value >= 6.0 -> GradeSufficient
        else -> GradeInsufficient
    }
}

fun getAttendanceColor(status: AttendanceStatus): Color {
    return when (status) {
        AttendanceStatus.PRESENTE -> AttendancePresent
        AttendanceStatus.ASSENTE -> AttendanceAbsent
        AttendanceStatus.RITARDO_BREVE -> AttendanceLate
        AttendanceStatus.USCITA_ANTICIPATA -> AttendanceEarlyExit
    }
}

@Composable
fun GradeBadge(
    displayValue: String,
    numericValue: Double,
    modifier: Modifier = Modifier,
    size: Int = 42
) {
    val bgColor = getGradeColor(numericValue)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayValue,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = if (size > 40) 18.sp else 14.sp
        )
    }
}

@Composable
fun AttendanceBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    val color = getAttendanceColor(status)
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.label,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun HeadsUpPushBanner(
    notification: PushNotification?,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(300)) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(250)) + fadeOut(),
        modifier = modifier
    ) {
        if (notification != null) {
            val icon: ImageVector = when (notification.category) {
                NotificationCategory.VOTO -> Icons.Filled.School
                NotificationCategory.NOTA -> Icons.Filled.Warning
                NotificationCategory.PRESENZA -> Icons.Filled.AccessTime
                NotificationCategory.CIRCOLARE -> Icons.Filled.Campaign
            }
            val tint: Color = when (notification.category) {
                NotificationCategory.VOTO -> AccentAmber
                NotificationCategory.NOTA -> AttendanceAbsent
                NotificationCategory.PRESENZA -> AttendanceLate
                NotificationCategory.CIRCOLARE -> ScholarNavyLight
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clickable { onClick() }
                    .testTag("push_notification_banner"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(tint.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Notifica",
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NOTIFICA PUSH IN TEMPO REALE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tint,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Adesso",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = notification.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = notification.message,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopRegisterAppBar(
    currentRole: UserRole,
    unreadNotifCount: Int,
    onSwitchRoleClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onResetToEmpty: () -> Unit = {},
    onLoadSampleData: () -> Unit = {},
    onAddCircularClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showConfirmResetDialog by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ScholarNavy,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Registro Scuola",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "I.C. Pascoli • Secondaria I Grado",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        },
        actions = {
            // Profile & Role Chip button
            Surface(
                onClick = onSwitchRoleClick,
                shape = RoundedCornerShape(20.dp),
                color = if (currentRole == UserRole.DOCENTE) Color(0xFF1E5B94) else Color(0xFF00695C),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("switch_role_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentRole == UserRole.DOCENTE) Icons.Default.School else Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentRole == UserRole.DOCENTE) "Docente" else "Genitore",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Cambia Profilo",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(14.dp)
                    )
                }
            }

            // Notification Bell with badge
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("notification_bell_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotifCount > 0) {
                            Badge(
                                containerColor = AccentAmber,
                                contentColor = Color.Black
                            ) {
                                Text(
                                    text = if (unreadNotifCount > 9) "9+" else unreadNotifCount.toString(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifiche",
                        tint = Color.White
                    )
                }
            }

            // Overflow Options Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opzioni", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (currentRole == UserRole.DOCENTE) {
                        DropdownMenuItem(
                            text = { Text("+ Pubblica Circolare") },
                            leadingIcon = { Icon(Icons.Default.Campaign, null) },
                            onClick = {
                                showMenu = false
                                onAddCircularClick()
                            }
                        )
                        HorizontalDivider()
                    }
                    DropdownMenuItem(
                        text = { Text("Azzera tutto (Vuoto)") },
                        leadingIcon = { Icon(Icons.Default.DeleteSweep, null, tint = AttendanceAbsent) },
                        onClick = {
                            showMenu = false
                            showConfirmResetDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Carica dati di esempio") },
                        leadingIcon = { Icon(Icons.Default.AutoFixHigh, null) },
                        onClick = {
                            showMenu = false
                            onLoadSampleData()
                        }
                    )
                }
            }
        }
    )

    if (showConfirmResetDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmResetDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToEmpty()
                        showConfirmResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AttendanceAbsent)
                ) {
                    Text("Conferma e Azzera")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmResetDialog = false }) {
                    Text("Annulla")
                }
            },
            title = { Text("Svuotare tutto il registro?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Tutti i voti, note, circolari e notifiche inserite verranno cancellati e il registro tornerà completamente vuoto come al primo avvio.")
            }
        )
    }
}

@Composable
fun AddCircularDialog(
    onDismiss: () -> Unit,
    onConfirm: (number: String, title: String, category: String, date: String, content: String, requiresSignature: Boolean, isUrgent: Boolean) -> Unit
) {
    var number by remember { mutableStateOf("Circ. n. 1") }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Circolare Dirigenza") }
    var content by remember { mutableStateOf("") }
    var requiresSignature by remember { mutableStateOf(true) }
    var isUrgent by remember { mutableStateOf(false) }

    val categories = listOf("Circolare Dirigenza", "Avviso Didattico", "Viaggi d'istruzione", "Sciopero / Variazione")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onConfirm(
                            number,
                            title,
                            category,
                            "14 Ottobre 2026",
                            content,
                            requiresSignature,
                            isUrgent
                        )
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy)
            ) {
                Text("Pubblica e Notifica")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, null, tint = ScholarNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pubblica Nuova Comunicazione", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Numero Circolare (es. Circ. n. 50)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titolo dell'avviso") },
                    placeholder = { Text("Es. Uscita didattica a teatro...") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector
                Text("Categoria:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Testo della circolare") },
                    placeholder = { Text("Inserisci il testo completo della comunicazione...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = requiresSignature,
                        onCheckedChange = { requiresSignature = it }
                    )
                    Text("Richiede firma / presa visione del genitore", fontSize = 12.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isUrgent,
                        onCheckedChange = { isUrgent = it }
                    )
                    Text("Contrassegna come URGENTE", fontSize = 12.sp)
                }
            }
        }
    )
}

@Composable
fun RoleSwitcherModal(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = ScholarNavy
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Seleziona Profilo Dispositivo", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Come richiesto, il registro adatta l'interfaccia in base al dispositivo/account connesso:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Teacher profile card
                Surface(
                    onClick = { onRoleSelected(UserRole.DOCENTE) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentRole == UserRole.DOCENTE) ScholarBlueContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = if (currentRole == UserRole.DOCENTE) androidx.compose.foundation.BorderStroke(2.dp, ScholarNavy) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_teacher_role")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ScholarNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Account Docente",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (currentRole == UserRole.DOCENTE) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ATTIVO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ScholarNavy
                                    )
                                }
                            }
                            Text(
                                text = "Prof.ssa Maria Bianchi (Lettere - 2ª B)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "✓ Voti, Orario, Note, Registro Giornaliero",
                                fontSize = 11.sp,
                                color = ScholarNavy,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Parent profile card
                Surface(
                    onClick = { onRoleSelected(UserRole.GENITORE) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentRole == UserRole.GENITORE) Color(0xFFE0F2F1) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = if (currentRole == UserRole.GENITORE) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00796B)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_parent_role")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00796B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FamilyRestroom,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Account Genitore",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (currentRole == UserRole.GENITORE) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ATTIVO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00796B)
                                    )
                                }
                            }
                            Text(
                                text = "Famiglia Rossi (Alunno: Marco Rossi - 2ª B)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "✓ Pagelle, Circolari, Voti e Note del figlio, Push",
                                fontSize = 11.sp,
                                color = Color(0xFF00796B),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDrawerSheet(
    notifications: List<PushNotification>,
    onDismiss: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onClearAll: () -> Unit,
    onTriggerDemo: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = ScholarNavy
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Centro Notifiche Push",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("Cancella tutte", fontSize = 12.sp)
                    }
                }
            }

            Text(
                text = "Ricevi notifiche istantanee per ogni voto, assenza o avviso registrato dai docenti.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Quick Simulation Bar
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Simula Notifica Push in Tempo Reale:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ScholarNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssistChip(
                            onClick = { onTriggerDemo("voto") },
                            label = { Text("+ Voto", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.School, null, Modifier.size(14.dp)) }
                        )
                        AssistChip(
                            onClick = { onTriggerDemo("ritardo") },
                            label = { Text("+ Ritardo", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.AccessTime, null, Modifier.size(14.dp)) }
                        )
                        AssistChip(
                            onClick = { onTriggerDemo("circolare") },
                            label = { Text("+ Circolare", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Campaign, null, Modifier.size(14.dp)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nessuna notifica presente",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxHeight(0.6f)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        val icon: ImageVector = when (notif.category) {
                            NotificationCategory.VOTO -> Icons.Filled.School
                            NotificationCategory.NOTA -> Icons.Filled.Warning
                            NotificationCategory.PRESENZA -> Icons.Filled.AccessTime
                            NotificationCategory.CIRCOLARE -> Icons.Filled.Campaign
                        }
                        val tint: Color = when (notif.category) {
                            NotificationCategory.VOTO -> AccentAmber
                            NotificationCategory.NOTA -> AttendanceAbsent
                            NotificationCategory.PRESENZA -> AttendanceLate
                            NotificationCategory.CIRCOLARE -> ScholarNavyLight
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (!notif.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMarkAsRead(notif.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(tint.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = notif.title,
                                            fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (!notif.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(ScholarNavy)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = notif.message,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
