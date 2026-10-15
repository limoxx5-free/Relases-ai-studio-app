package com.example.ui.teacher

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AttendanceBadge
import com.example.ui.components.GradeBadge
import com.example.ui.components.getAttendanceColor
import com.example.ui.components.getGradeColor
import com.example.ui.theme.*
import com.example.ui.viewmodel.RegisterViewModel
import com.example.ui.viewmodel.TeacherTab

@Composable
fun TeacherDashboard(
    viewModel: RegisterViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val lessonLogs by viewModel.lessonLogs.collectAsState()
    val grades by viewModel.grades.collectAsState()
    val notes by viewModel.notes.collectAsState()

    var showAddGradeDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var editingLessonLog by remember { mutableStateOf<LessonLog?>(null) }
    var attendanceDetailStudent by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                TeacherTab.values().forEach { tab ->
                    val isSelected = uiState.teacherTab == tab
                    val icon: androidx.compose.ui.graphics.vector.ImageVector = when (tab) {
                        TeacherTab.REGISTRO -> Icons.Outlined.EditCalendar
                        TeacherTab.VOTI -> Icons.Outlined.School
                        TeacherTab.ORARIO -> Icons.Outlined.Schedule
                        TeacherTab.NOTE -> Icons.Outlined.Rule
                    }
                    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector = when (tab) {
                        TeacherTab.REGISTRO -> Icons.Filled.EditCalendar
                        TeacherTab.VOTI -> Icons.Filled.School
                        TeacherTab.ORARIO -> Icons.Filled.Schedule
                        TeacherTab.NOTE -> Icons.Filled.Rule
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTeacherTab(tab) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) selectedIcon else icon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("teacher_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            when (uiState.teacherTab) {
                TeacherTab.VOTI -> {
                    ExtendedFloatingActionButton(
                        onClick = { showAddGradeDialog = true },
                        containerColor = ScholarNavy,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Add, null) },
                        text = { Text("Inserisci Voto", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("add_grade_fab")
                    )
                }
                TeacherTab.NOTE -> {
                    ExtendedFloatingActionButton(
                        onClick = { showAddNoteDialog = true },
                        containerColor = ScholarNavy,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.PostAdd, null) },
                        text = { Text("Scrivi Nota", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("add_note_fab")
                    )
                }
                else -> {}
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.teacherTab) {
                TeacherTab.REGISTRO -> DailyClassRegisterScreen(
                    attendanceList = attendance,
                    lessonLogs = lessonLogs,
                    students = viewModel.students,
                    onUpdateAttendance = { id, status ->
                        viewModel.updateAttendance(id, status)
                    },
                    onOpenAttendanceDetails = { student ->
                        attendanceDetailStudent = student
                    },
                    onEditLesson = { log ->
                        editingLessonLog = log
                    }
                )
                TeacherTab.VOTI -> TeacherGradesManagementScreen(
                    grades = grades,
                    students = viewModel.students,
                    onAddGradeClick = { showAddGradeDialog = true }
                )
                TeacherTab.ORARIO -> TeacherTimetableScreen(
                    timetable = viewModel.teacherTimetable
                )
                TeacherTab.NOTE -> TeacherNotesManagementScreen(
                    notes = notes,
                    onAddNoteClick = { showAddNoteDialog = true }
                )
            }
        }
    }

    // Dialog for adding a new grade
    if (showAddGradeDialog) {
        AddGradeDialog(
            students = viewModel.students,
            onDismiss = { showAddGradeDialog = false },
            onConfirm = { studentId, subject, numVal, dispVal, type, date, topic ->
                viewModel.addGrade(studentId, subject, numVal, dispVal, type, date, topic)
                showAddGradeDialog = false
            }
        )
    }

    // Dialog for adding a new note
    if (showAddNoteDialog) {
        AddNoteDialog(
            students = viewModel.students,
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { studentId, type, text ->
                viewModel.addNote(studentId, type, text)
                showAddNoteDialog = false
            }
        )
    }

    // Sheet for signing / editing a lesson log hour
    editingLessonLog?.let { log ->
        EditLessonDialog(
            lessonLog = log,
            onDismiss = { editingLessonLog = null },
            onConfirm = { topic, homework ->
                viewModel.signLesson(log.hour, topic, homework)
                editingLessonLog = null
            }
        )
    }

    // Quick attendance details modal
    attendanceDetailStudent?.let { student ->
        val record = attendance.firstOrNull { it.studentId == student.id }
        AttendanceDetailModal(
            student = student,
            currentRecord = record,
            onDismiss = { attendanceDetailStudent = null },
            onSave = { status, detail ->
                viewModel.updateAttendance(student.id, status, detail)
                attendanceDetailStudent = null
            }
        )
    }
}

// ==========================================
// 1. REGISTRO DI CLASSE GIORNALIERO (Docente)
// ==========================================
@Composable
fun DailyClassRegisterScreen(
    attendanceList: List<AttendanceRecord>,
    lessonLogs: List<LessonLog>,
    students: List<Student>,
    onUpdateAttendance: (String, AttendanceStatus) -> Unit,
    onOpenAttendanceDetails: (Student) -> Unit,
    onEditLesson: (LessonLog) -> Unit
) {
    var registerSection by remember { mutableIntStateOf(0) } // 0: Appello / Presenze, 1: Firma d'Ora

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Class & Date Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ScholarBlueContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "REGISTRO DI CLASSE • 2ª B",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScholarNavy
                        )
                        Text(
                            text = "Mercoledì 14 Ottobre 2026",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScholarOnBlueContainer
                        )
                        Text(
                            text = "Docente in servizio: Prof.ssa Maria Bianchi",
                            fontSize = 12.sp,
                            color = ScholarOnBlueContainer.copy(alpha = 0.8f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ScholarNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Segmented Switch: Appello vs Firma d'Ora
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = registerSection == 0,
                    onClick = { registerSection = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Appello e Presenze (${attendanceList.count { it.status == AttendanceStatus.PRESENTE }}/${students.size})")
                }
                SegmentedButton(
                    selected = registerSection == 1,
                    onClick = { registerSection = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Diario e Firma d'Ora")
                }
            }
        }

        if (registerSection == 0) {
            // APPELLO SECTION
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Elenco Alunni 2ª B",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Tocca per stato o dettagli",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(students, key = { it.id }) { student ->
                val record = attendanceList.firstOrNull { it.studentId == student.id }
                val currentStatus = record?.status ?: AttendanceStatus.PRESENTE

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAttendanceDetails(student) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Roll number
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${student.rollNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ScholarNavy
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.fullName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            if (record?.timeDetail != null) {
                                Text(
                                    text = record.timeDetail,
                                    fontSize = 11.sp,
                                    color = getAttendanceColor(currentStatus),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Quick Toggle Chips: P, A, R, U
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AttendanceStatus.values().forEach { st ->
                                val isSelected = currentStatus == st
                                val activeColor = getAttendanceColor(st)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { onUpdateAttendance(student.id, st) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = st.shortLabel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // FIRMA D'ORA SECTION
            item {
                Text(
                    text = "Firma Registro Giornaliero (Ore di lezione)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(lessonLogs, key = { it.hour }) { log ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (log.isSigned) ScholarNavy.copy(alpha = 0.4f) else AccentAmber
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ScholarBlueContainer
                            ) {
                                Text(
                                    text = "${log.hour}ª ORA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ScholarNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = log.timeRange,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (log.isSigned) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = GradeGood,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Firmata",
                                        fontSize = 12.sp,
                                        color = GradeGood,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Da Firmare",
                                    fontSize = 12.sp,
                                    color = AccentAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = log.subject,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ScholarNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${log.teacher}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Argomento: ${log.topic}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (log.homework.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Compiti: ${log.homework}",
                                fontSize = 12.sp,
                                color = AccentAmber.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onEditLesson(log) },
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (log.isSigned) "Modifica Firma" else "Firma Ora", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ==========================================
// 2. GESTIONE VOTI (Docente)
// ==========================================
@Composable
fun TeacherGradesManagementScreen(
    grades: List<Grade>,
    students: List<Student>,
    onAddGradeClick: () -> Unit
) {
    var selectedFilterSubject by remember { mutableStateOf("Tutte") }
    val subjects = listOf("Tutte", "Italiano", "Storia", "Geografia", "Matematica", "Scienze", "Inglese", "Musica", "Arte e Immagine")

    val filteredGrades = remember(selectedFilterSubject, grades) {
        if (selectedFilterSubject == "Tutte") grades
        else grades.filter { it.subject.contains(selectedFilterSubject, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Gestione Valutazioni",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScholarNavy
                    )
                    Text(
                        text = "Voti orali, scritti e pratici inseriti per la classe 2ª B",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Subject Filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subject ->
                    FilterChip(
                        selected = selectedFilterSubject == subject,
                        onClick = { selectedFilterSubject = subject },
                        label = { Text(subject, fontSize = 12.sp) }
                    )
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
                        .padding(vertical = 20.dp)
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
                            text = "Il registro delle valutazioni è attualmente vuoto. Tocca '+ Inserisci Voto' per assegnare una valutazione agli alunni della 2ª B.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

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
                                text = grade.studentName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = grade.date,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = grade.subject,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = ScholarNavy
                            )
                            Text(
                                text = " • ${grade.type.label}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = grade.topic,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ==========================================
// 3. GESTIONE ORARIO (Docente)
// ==========================================
@Composable
fun TeacherTimetableScreen(
    timetable: List<TimetableSlot>
) {
    var selectedDay by remember { mutableStateOf("Mercoledì") }
    val days = listOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì")

    val slotsForDay = remember(selectedDay, timetable) {
        timetable.filter { it.dayOfWeek == selectedDay }.sortedBy { it.hour }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Orario delle Lezioni Docente",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ScholarNavy
        )
        Text(
            text = "Prof.ssa Maria Bianchi • Cattedra di Lettere",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Day Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = days.indexOf(selectedDay).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = Color.Transparent
        ) {
            days.forEach { day ->
                val isSelected = selectedDay == day
                Tab(
                    selected = isSelected,
                    onClick = { selectedDay = day },
                    text = {
                        Text(
                            text = if (day == "Mercoledì") "$day (Oggi)" else day,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(slotsForDay) { slot ->
                val isClass2B = slot.className.contains("2ª B")
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isClass2B) ScholarBlueContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isClass2B) ScholarNavy.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isClass2B) ScholarNavy else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${slot.hour}ª ora",
                                color = if (isClass2B) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = slot.subject,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ScholarNavy
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = slot.className,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isClass2B) ScholarNavy else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = slot.timeRange,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• ${slot.room}",
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

// ==========================================
// 4. GESTIONE NOTE (Docente)
// ==========================================
@Composable
fun TeacherNotesManagementScreen(
    notes: List<SchoolNote>,
    onAddNoteClick: () -> Unit
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
                text = "Registro Note e Provvedimenti",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ScholarNavy
            )
            Text(
                text = "Note disciplinari, didattiche e richiami per la classe 2ª B",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (notes.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Rule,
                            contentDescription = null,
                            tint = ScholarNavyLight,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nessuna nota registrata",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ScholarNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Non ci sono ancora note disciplinari o didattiche per la classe 2ª B. Tocca '+ Scrivi Nota' per inviare una nota.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        items(notes, key = { it.id }) { note ->
            val (badgeColor, icon) = when (note.type) {
                NoteType.DISCIPLINARE -> AttendanceAbsent to Icons.Filled.Warning
                NoteType.GENERICA -> ScholarNavy to Icons.Filled.Info
                NoteType.COMPITI -> AccentAmber to Icons.Filled.AssignmentLate
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, null, tint = badgeColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = note.type.label,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
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
                        text = "Destinatario: ${note.studentName ?: "Intera Classe"}",
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
                        text = "Autore: ${note.author}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ==========================================
// Dialog: Inserisci Voto
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGradeDialog(
    students: List<Student>,
    onDismiss: () -> Unit,
    onConfirm: (studentId: String, subject: String, numVal: Double, dispVal: String, type: GradeType, date: String, topic: String) -> Unit
) {
    var selectedStudentId by remember { mutableStateOf(students.first().id) }
    var selectedSubject by remember { mutableStateOf("Italiano") }
    var selectedGradeDisplay by remember { mutableStateOf("8") }
    var selectedGradeNum by remember { mutableDoubleStateOf(8.0) }
    var selectedType by remember { mutableStateOf(GradeType.SCRITTO) }
    var topicText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("14 Ottobre") }

    val gradePresets = listOf(
        "4" to 4.0, "4½" to 4.5, "5" to 5.0, "5½" to 5.5,
        "6-" to 5.75, "6" to 6.0, "6+" to 6.25, "6½" to 6.5,
        "7-" to 6.75, "7" to 7.0, "7+" to 7.25, "7½" to 7.5,
        "8-" to 7.75, "8" to 8.0, "8½" to 8.5, "9" to 9.0, "10" to 10.0
    )

    val subjects = listOf("Italiano", "Storia", "Geografia", "Matematica", "Scienze", "Inglese", "Musica", "Arte", "Ed. Fisica")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val finalTopic = if (topicText.isBlank()) "Valutazione formativa delle competenze" else topicText
                    onConfirm(
                        selectedStudentId,
                        selectedSubject,
                        selectedGradeNum,
                        selectedGradeDisplay,
                        selectedType,
                        dateText,
                        finalTopic
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                modifier = Modifier.testTag("confirm_add_grade_button")
            ) {
                Text("Registra e Notifica")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, null, tint = ScholarNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Inserisci Nuovo Voto", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Alunno Selector
                item {
                    Text("Alunno:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    var expandedStudent by remember { mutableStateOf(false) }
                    val currentStudentName = students.firstOrNull { it.id == selectedStudentId }?.fullName ?: ""
                    OutlinedButton(
                        onClick = { expandedStudent = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(currentStudentName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(
                        expanded = expandedStudent,
                        onDismissRequest = { expandedStudent = false }
                    ) {
                        students.forEach { st ->
                            DropdownMenuItem(
                                text = { Text("${st.rollNumber}. ${st.fullName}") },
                                onClick = {
                                    selectedStudentId = st.id
                                    expandedStudent = false
                                }
                            )
                        }
                    }
                }

                // Materia Selector
                item {
                    Text("Materia:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subjects) { subj ->
                            FilterChip(
                                selected = selectedSubject == subj,
                                onClick = { selectedSubject = subj },
                                label = { Text(subj, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Tipo Prova
                item {
                    Text("Tipo di Prova:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GradeType.values().forEach { t ->
                            FilterChip(
                                selected = selectedType == t,
                                onClick = { selectedType = t },
                                label = { Text(t.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Voto Grid
                item {
                    Text("Voto assegnato:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(gradePresets) { (label, value) ->
                            val isSel = selectedGradeDisplay == label
                            val color = getGradeColor(value)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) color else color.copy(alpha = 0.15f),
                                border = if (isSel) androidx.compose.foundation.BorderStroke(2.dp, Color.Black.copy(alpha = 0.4f)) else null,
                                modifier = Modifier
                                    .size(width = 46.dp, height = 38.dp)
                                    .clickable {
                                        selectedGradeDisplay = label
                                        selectedGradeNum = value
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        color = if (isSel) Color.White else color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Argomento
                item {
                    OutlinedTextField(
                        value = topicText,
                        onValueChange = { topicText = it },
                        label = { Text("Argomento della verifica") },
                        placeholder = { Text("Es. Verifica di grammatica sui complementi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Data") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}

// ==========================================
// Dialog: Inserisci Nota
// ==========================================
@Composable
fun AddNoteDialog(
    students: List<Student>,
    onDismiss: () -> Unit,
    onConfirm: (studentId: String?, type: NoteType, text: String) -> Unit
) {
    var isWholeClass by remember { mutableStateOf(false) }
    var selectedStudentId by remember { mutableStateOf(students.first().id) }
    var selectedType by remember { mutableStateOf(NoteType.DISCIPLINARE) }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (noteText.isNotBlank()) {
                        onConfirm(
                            if (isWholeClass) null else selectedStudentId,
                            selectedType,
                            noteText
                        )
                    }
                },
                enabled = noteText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                modifier = Modifier.testTag("confirm_add_note_button")
            ) {
                Text("Registra Nota")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = AttendanceAbsent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scrivi Nota Scolastica", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Whole class toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isWholeClass,
                        onCheckedChange = { isWholeClass = it }
                    )
                    Text("Nota indirizzata all'intera classe 2ª B", fontSize = 13.sp)
                }

                if (!isWholeClass) {
                    var expanded by remember { mutableStateOf(false) }
                    val currentStudentName = students.firstOrNull { it.id == selectedStudentId }?.fullName ?: ""
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Alunno: $currentStudentName")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        students.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.fullName) },
                                onClick = {
                                    selectedStudentId = st.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Type
                Text("Tipo di nota:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NoteType.values().forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t.label, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Descrizione della nota") },
                    placeholder = { Text("Descrivi il comportamento o la motivazione del richiamo...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

// ==========================================
// Dialog: Firma / Modifica Ora Registro
// ==========================================
@Composable
fun EditLessonDialog(
    lessonLog: LessonLog,
    onDismiss: () -> Unit,
    onConfirm: (topic: String, homework: String) -> Unit
) {
    var topic by remember { mutableStateOf(lessonLog.topic) }
    var homework by remember { mutableStateOf(lessonLog.homework) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(topic, homework) },
                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy)
            ) {
                Text("Firma e Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
        title = {
            Text("Firma ${lessonLog.hour}ª Ora (${lessonLog.subject})", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Argomento della Lezione") },
                    placeholder = { Text("Es. Spiegazione Capitolo IV...") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = homework,
                    onValueChange = { homework = it },
                    label = { Text("Compiti per Casa") },
                    placeholder = { Text("Es. Esercizi pag. 45 n. 1-4 per mercoledì...") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

// ==========================================
// Dialog: Dettaglio Presenza / Ritardo / Uscita
// ==========================================
@Composable
fun AttendanceDetailModal(
    student: Student,
    currentRecord: AttendanceRecord?,
    onDismiss: () -> Unit,
    onSave: (AttendanceStatus, String?) -> Unit
) {
    var status by remember { mutableStateOf(currentRecord?.status ?: AttendanceStatus.PRESENTE) }
    var detailText by remember { mutableStateOf(currentRecord?.timeDetail ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(status, detailText.ifBlank { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy)
            ) {
                Text("Aggiorna Stato")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
        title = {
            Text("Stato Presenza: ${student.fullName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AttendanceStatus.values().forEach { st ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (status == st) getAttendanceColor(st).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (status == st) androidx.compose.foundation.BorderStroke(1.5.dp, getAttendanceColor(st)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                status = st
                                if (st == AttendanceStatus.RITARDO_BREVE && detailText.isBlank()) {
                                    detailText = "Entrato alle 08:30 (1ª ora)"
                                } else if (st == AttendanceStatus.USCITA_ANTICIPATA && detailText.isBlank()) {
                                    detailText = "Uscita alle 12:00 per visita medica"
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(getAttendanceColor(st))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(st.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }

                if (status != AttendanceStatus.PRESENTE) {
                    OutlinedTextField(
                        value = detailText,
                        onValueChange = { detailText = it },
                        label = { Text("Dettaglio orario / motivazione") },
                        placeholder = { Text("Es. Entrato alle 08:25...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}
