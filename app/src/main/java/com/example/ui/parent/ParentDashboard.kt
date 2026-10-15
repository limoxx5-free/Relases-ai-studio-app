package com.example.ui.parent

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.GradeBadge
import com.example.ui.components.getGradeColor
import com.example.ui.theme.*
import com.example.ui.viewmodel.ParentTab
import com.example.ui.viewmodel.RegisterViewModel

@Composable
fun ParentDashboard(
    viewModel: RegisterViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val grades by viewModel.grades.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val circulars by viewModel.circulars.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val reportCard by viewModel.dynamicReportCard.collectAsState()

    // Filter data specifically for the parent's child (Marco Rossi, s1)
    val childGrades = remember(grades) {
        grades.filter { it.studentId == viewModel.childStudentId }
    }

    val childNotes = remember(notes) {
        notes.filter { it.studentId == viewModel.childStudentId || it.studentId == null }
    }

    val childAttendance = remember(attendance) {
        attendance.firstOrNull { it.studentId == viewModel.childStudentId }
    }

    var selectedCircularForDetail by remember { mutableStateOf<SchoolCircular?>(null) }
    var selectedNoteForConfirm by remember { mutableStateOf<SchoolNote?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                ParentTab.values().forEach { tab ->
                    val isSelected = uiState.parentTab == tab
                    val icon: androidx.compose.ui.graphics.vector.ImageVector = when (tab) {
                        ParentTab.VOTI -> Icons.Outlined.School
                        ParentTab.NOTE -> Icons.Outlined.Rule
                        ParentTab.PAGELLA -> Icons.Outlined.Description
                        ParentTab.AVVISI -> Icons.Outlined.Campaign
                    }
                    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector = when (tab) {
                        ParentTab.VOTI -> Icons.Filled.School
                        ParentTab.NOTE -> Icons.Filled.Rule
                        ParentTab.PAGELLA -> Icons.Filled.Description
                        ParentTab.AVVISI -> Icons.Filled.Campaign
                    }
                    val unreadBadgeCount = when (tab) {
                        ParentTab.NOTE -> childNotes.count { !it.isViewedByParent }
                        ParentTab.AVVISI -> circulars.count { it.requiresSignature && !it.isSignedByParent }
                        else -> 0
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setParentTab(tab) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (unreadBadgeCount > 0) {
                                        Badge(containerColor = AccentAmber) {
                                            Text("$unreadBadgeCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) selectedIcon else icon,
                                    contentDescription = tab.label
                                )
                            }
                        },
                        label = { Text(tab.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("parent_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.parentTab) {
                ParentTab.VOTI -> ParentGradesScreen(
                    student = viewModel.childStudent,
                    grades = childGrades,
                    todayAttendance = childAttendance,
                    onJustifyAbsence = { reason ->
                        childAttendance?.let { viewModel.justifyAbsence(it.id, reason) }
                    }
                )
                ParentTab.NOTE -> ParentNotesScreen(
                    student = viewModel.childStudent,
                    notes = childNotes,
                    onConfirmViewed = { noteId ->
                        viewModel.markNoteViewed(noteId)
                    }
                )
                ParentTab.PAGELLA -> ParentReportCardScreen(
                    reportCard = reportCard
                )
                ParentTab.AVVISI -> ParentCircularsScreen(
                    circulars = circulars,
                    onSignCircular = { id -> viewModel.signCircular(id) },
                    onSelectCircular = { circular -> selectedCircularForDetail = circular }
                )
            }
        }
    }

    // Modal detail for circular
    selectedCircularForDetail?.let { circ ->
        CircularDetailDialog(
            circular = circ,
            onDismiss = { selectedCircularForDetail = null },
            onSign = {
                viewModel.signCircular(circ.id)
                selectedCircularForDetail = null
            }
        )
    }
}

// ==========================================
// 1. VOTI DEL FIGLIO (Genitore)
// ==========================================
@Composable
fun ParentGradesScreen(
    student: Student,
    grades: List<Grade>,
    todayAttendance: AttendanceRecord?,
    onJustifyAbsence: (String) -> Unit
) {
    var selectedSubjectFilter by remember { mutableStateOf("Tutte") }
    var showJustifyDialog by remember { mutableStateOf(false) }

    val subjects = remember(grades) {
        listOf("Tutte") + grades.map { it.subject }.distinct()
    }

    val filteredGrades = remember(selectedSubjectFilter, grades) {
        if (selectedSubjectFilter == "Tutte") grades
        else grades.filter { it.subject == selectedSubjectFilter }
    }

    val overallAverage = remember(grades) {
        if (grades.isEmpty()) 0.0
        else grades.map { it.numericValue }.average()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Child Overview Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ScholarNavy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.fullName,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Classe ${student.className} • I.C. Pascoli",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        // Average Badge
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "MEDIA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                            Text(
                                text = if (grades.isEmpty()) "--" else String.format("%.2f", overallAverage),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real-time Push status pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF69F0AE))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notifiche push attive: aggiornamento voti istantaneo",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Attendance Alert if Absent or Late
        if (todayAttendance != null && todayAttendance.status != AttendanceStatus.PRESENTE) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentAmberContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AccentAmber
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Avviso Registro Presenze di Oggi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF6B3E00)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "L'alunno risulta registrato come: ${todayAttendance.status.label.uppercase()}${if (todayAttendance.timeDetail != null) " (${todayAttendance.timeDetail})" else ""}.",
                            fontSize = 13.sp,
                            color = Color(0xFF4A2B00)
                        )
                        if (!todayAttendance.isJustified) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showJustifyDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.BorderColor, null, Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Invia Giustificazione Genitore", fontSize = 12.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✓ Giustificata dalla famiglia: \"${todayAttendance.justificationReason ?: "Confermata"}\"",
                                fontSize = 11.sp,
                                color = GradeGood,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Filter chips for subjects
        item {
            Column {
                Text(
                    text = "Voti del Quadrimestre in Corso",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScholarNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjects) { subj ->
                        FilterChip(
                            selected = selectedSubjectFilter == subj,
                            onClick = { selectedSubjectFilter = subj },
                            label = { Text(subj, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        if (filteredGrades.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = null,
                            tint = ScholarNavyLight,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nessun voto registrato",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ScholarNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Non ci sono ancora voti registrati per Marco Rossi. Quando i docenti assegneranno una valutazione, apparirà qui con notifica push in tempo reale.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // List of Grades
        items(filteredGrades, key = { it.id }) { grade ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GradeBadge(
                        displayValue = grade.displayValue,
                        numericValue = grade.numericValue,
                        size = 46
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = grade.subject,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ScholarNavy
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = grade.date,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Prova ${grade.type.label}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = " • ${grade.teacher}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = grade.topic,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showJustifyDialog) {
        var reasonText by remember { mutableStateOf("Motivi familiari / salute") }
        AlertDialog(
            onDismissRequest = { showJustifyDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        onJustifyAbsence(reasonText)
                        showJustifyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy)
                ) {
                    Text("Firma Giustifica")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJustifyDialog = false }) { Text("Annulla") }
            },
            title = {
                Text("Giustificazione Assenza / Ritardo", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Il genitore o tutore legale dichiara che l'assenza o ritardo dell'alunno Marco Rossi è dovuta a:",
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Motivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

// ==========================================
// 2. NOTE DEL FIGLIO (Genitore)
// ==========================================
@Composable
fun ParentNotesScreen(
    student: Student,
    notes: List<SchoolNote>,
    onConfirmViewed: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Note e Provvedimenti Scolastici",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ScholarNavy
            )
            Text(
                text = "Registro delle annotazioni dei docenti per Marco Rossi e per la classe 2ª B",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (notes.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GradeGood,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nessuna nota disciplinare registrata!",
                            fontWeight = FontWeight.Bold,
                            color = ScholarNavy
                        )
                    }
                }
            }
        }

        items(notes, key = { it.id }) { note ->
            val isDisciplinary = note.type == NoteType.DISCIPLINARE
            val borderTint = if (isDisciplinary) AttendanceAbsent else ScholarNavyLight

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderTint.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = borderTint.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = note.type.label,
                                color = borderTint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = note.date,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (note.studentId == null) "Nota per la classe (2ª B)" else "Nota per: ${note.studentName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = note.text,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Inserita da: ${note.author}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.isViewedByParent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GradeGood,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Presa visione effettuata",
                                fontSize = 12.sp,
                                color = GradeGood,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Button(
                                onClick = { onConfirmViewed(note.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Conferma Presa Visione", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 3. PAGELLA SCOLASTICA (Genitore)
// ==========================================
@Composable
fun ParentReportCardScreen(
    reportCard: ReportCard?
) {
    if (reportCard == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = ScholarNavyLight,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Pagella non ancora disponibile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = ScholarNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Il documento di valutazione intermedia verrà elaborato automaticamente non appena i docenti registreranno i primi voti per Marco Rossi.",
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    val overallAverage = remember(reportCard) {
        if (reportCard.subjects.isEmpty()) 0.0 else reportCard.subjects.map { it.finalGrade }.average()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Official Header Certificate Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(2.dp, ScholarNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SCHEDA DI VALUTAZIONE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ScholarNavy,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${reportCard.termName} • A.S. ${reportCard.schoolYear}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Alunno:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(reportCard.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(reportCard.className, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Voto di Condotta:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(reportCard.conductGrade, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GradeGood)
                            Text("Media Generale: ${String.format("%.2f", overallAverage)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Quadro delle Discipline e Valutazioni",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ScholarNavy
            )
        }

        // Table Header
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ScholarBlueContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Materia", modifier = Modifier.weight(1.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ScholarNavy)
                    Text("Scritto", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = ScholarNavy)
                    Text("Orale", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = ScholarNavy)
                    Text("Finale", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = ScholarNavy)
                }
            }
        }

        // Subject list
        items(reportCard.subjects) { sub ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sub.subject,
                        modifier = Modifier.weight(1.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = sub.writtenGrade?.toString() ?: "-",
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = sub.oralGrade?.toString() ?: "-",
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = getGradeColor(sub.finalGrade).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = sub.finalGrade.toInt().toString(),
                                fontWeight = FontWeight.Bold,
                                color = getGradeColor(sub.finalGrade),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Global Judgment Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Giudizio Globale di Maturazione e Apprendimento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ScholarNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = reportCard.overallJudgment,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Il Consiglio di Classe • I.C. Pascoli",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 4. COMUNICAZIONI SCOLASTICHE (Genitore)
// ==========================================
@Composable
fun ParentCircularsScreen(
    circulars: List<SchoolCircular>,
    onSignCircular: (String) -> Unit,
    onSelectCircular: (SchoolCircular) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Bacheca Comunicazioni e Circolari",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ScholarNavy
            )
            Text(
                text = "Avvisi ufficiali dell'Istituto e della Dirigenza Scolastica in tempo reale",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (circulars.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Campaign,
                            contentDescription = null,
                            tint = ScholarNavyLight,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nessuna circolare in bacheca",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ScholarNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Non ci sono comunicazioni ufficiali o avvisi della Dirigenza al momento. Quando la scuola pubblicherà una circolare, apparirà qui con notifica push.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        items(circulars, key = { it.id }) { circ ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (circ.isUrgent) AccentAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCircular(circ) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ScholarNavy.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = circ.number,
                                color = ScholarNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (circ.isUrgent) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentAmber.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "URGENTE",
                                    color = AccentAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = circ.date,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = circ.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ScholarNavy
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = circ.content,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (circ.requiresSignature) {
                            if (circ.isSignedByParent) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, null, tint = GradeGood, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Firmata per adesione / presa visione", fontSize = 12.sp, color = GradeGood, fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Button(
                                    onClick = { onSignCircular(circ.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.BorderColor, null, Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Firma Presa Visione", fontSize = 11.sp)
                                }
                            }
                        } else {
                            Text("Informativa didattica (Nessuna firma richiesta)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Dettagli",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// Dialog: Dettaglio Circolare
// ==========================================
@Composable
fun CircularDetailDialog(
    circular: SchoolCircular,
    onDismiss: () -> Unit,
    onSign: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (circular.requiresSignature && !circular.isSignedByParent) {
                Button(
                    onClick = onSign,
                    colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy)
                ) {
                    Text("Conferma Presa Visione e Firma")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Chiudi") }
            }
        },
        dismissButton = {
            if (circular.requiresSignature && !circular.isSignedByParent) {
                TextButton(onClick = onDismiss) { Text("Annulla") }
            }
        },
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = circular.number,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ScholarNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = circular.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = circular.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = circular.content,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (circular.requiresSignature) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (circular.isSignedByParent) GradeGood.copy(alpha = 0.15f) else AccentAmberContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (circular.isSignedByParent) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (circular.isSignedByParent) GradeGood else AccentAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (circular.isSignedByParent) "Documento regolarmente visionato e firmato digitalmente dalla famiglia." else "È richiesta la firma digitale del genitore o tutore.",
                                fontSize = 12.sp,
                                color = if (circular.isSignedByParent) GradeGood else Color(0xFF6B3E00),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    )
}
