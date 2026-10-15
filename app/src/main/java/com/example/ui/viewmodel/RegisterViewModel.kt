package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.RegisterRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TeacherTab(val label: String) {
    REGISTRO("Registro"),
    VOTI("Voti"),
    ORARIO("Orario"),
    NOTE("Note")
}

enum class ParentTab(val label: String) {
    VOTI("Voti"),
    NOTE("Note"),
    PAGELLA("Pagella"),
    AVVISI("Avvisi")
}

data class RegisterUiState(
    val currentRole: UserRole = UserRole.DOCENTE,
    val teacherTab: TeacherTab = TeacherTab.REGISTRO,
    val parentTab: ParentTab = ParentTab.VOTI,
    val selectedClass: String = "2ª B",
    val selectedSubjectFilter: String? = null,
    val currentHeadsup: PushNotification? = null,
    val isNotificationDrawerOpen: Boolean = false,
    val showRoleSwitcherDialog: Boolean = false,
    val showAddCircularDialog: Boolean = false
)

class RegisterViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: RegisterRepository = RegisterRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        RegisterUiState(currentRole = repository.savedRole.value)
    )
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    val students: List<Student> = repository.getStudents()
    val grades: StateFlow<List<Grade>> = repository.grades
    val attendance: StateFlow<List<AttendanceRecord>> = repository.attendance
    val notes: StateFlow<List<SchoolNote>> = repository.notes
    val lessonLogs: StateFlow<List<LessonLog>> = repository.lessonLogs
    val circulars: StateFlow<List<SchoolCircular>> = repository.circulars
    val notifications: StateFlow<List<PushNotification>> = repository.notifications
    val teacherTimetable: List<TimetableSlot> = repository.teacherTimetable

    // Marco Rossi's child ID for the parent view
    val childStudentId = "s1"
    val childStudent: Student = students.first { it.id == childStudentId }

    // Dynamic Report Card computed from actual recorded grades for Marco Rossi
    val dynamicReportCard: StateFlow<ReportCard?> = grades.map { allGrades ->
        val marcoGrades = allGrades.filter { it.studentId == childStudentId }
        if (marcoGrades.isEmpty()) {
            null
        } else {
            val bySubject = marcoGrades.groupBy { it.subject }
            val subjectsReport = bySubject.map { (subj, gList) ->
                val written = gList.filter { it.type == GradeType.SCRITTO }.map { it.numericValue }
                val oral = gList.filter { it.type != GradeType.SCRITTO }.map { it.numericValue }
                val avgFinal = gList.map { it.numericValue }.average()
                ReportCardSubject(
                    subject = subj,
                    writtenGrade = if (written.isNotEmpty()) written.average() else null,
                    oralGrade = if (oral.isNotEmpty()) oral.average() else null,
                    finalGrade = (Math.round(avgFinal * 2) / 2.0),
                    absencesCount = 0
                )
            }
            val overallAvg = marcoGrades.map { it.numericValue }.average()
            val judgment = when {
                overallAvg >= 8.5 -> "L'alunno Marco Rossi ha dimostrato eccellente maturazione globale, impegno lodevole e brillante padronanza in tutte le discipline."
                overallAvg >= 7.0 -> "L'alunno Marco Rossi ha evidenziato un percorso scolastico costante, serio e responsabile, con buona partecipazione alle attività didattiche."
                overallAvg >= 6.0 -> "L'alunno ha conseguito una preparazione complessivamente sufficiente, dimostrando disponibilità al lavoro e rispetto delle consegne."
                else -> "L'alunno evidenzia diffuse lacune nelle discipline di base; si raccomanda un piano di recupero e maggiore costanza nello studio domestico."
            }
            ReportCard(
                studentName = "Marco Rossi",
                className = "Classe 2ª B",
                termName = "1° Quadrimestre (Valutazione Intermedia)",
                schoolYear = "2026/2027",
                subjects = subjectsReport,
                conductGrade = if (overallAvg >= 7.0) "9 / 10" else "8 / 10",
                overallJudgment = judgment
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val unreadNotificationsCount: StateFlow<Int> = notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        // Synchronize saved role
        _uiState.update { it.copy(currentRole = repository.savedRole.value) }

        // Collect real-time heads-up alerts
        viewModelScope.launch {
            repository.headsupNotification.collect { notification ->
                _uiState.update { it.copy(currentHeadsup = notification) }
                delay(4500)
                _uiState.update { current ->
                    if (current.currentHeadsup?.id == notification.id) {
                        current.copy(currentHeadsup = null)
                    } else current
                }
            }
        }
    }

    fun setRole(role: UserRole) {
        repository.saveActiveRole(role)
        _uiState.update { it.copy(currentRole = role, showRoleSwitcherDialog = false) }
    }

    fun setTeacherTab(tab: TeacherTab) {
        _uiState.update { it.copy(teacherTab = tab) }
    }

    fun setParentTab(tab: ParentTab) {
        _uiState.update { it.copy(parentTab = tab) }
    }

    fun setSelectedSubjectFilter(subject: String?) {
        _uiState.update { it.copy(selectedSubjectFilter = subject) }
    }

    fun setNotificationDrawerOpen(open: Boolean) {
        _uiState.update { it.copy(isNotificationDrawerOpen = open) }
    }

    fun setRoleSwitcherOpen(open: Boolean) {
        _uiState.update { it.copy(showRoleSwitcherDialog = open) }
    }

    fun setAddCircularDialogOpen(open: Boolean) {
        _uiState.update { it.copy(showAddCircularDialog = open) }
    }

    fun dismissHeadsup() {
        _uiState.update { it.copy(currentHeadsup = null) }
    }

    // Teacher actions
    fun addGrade(
        studentId: String,
        subject: String,
        numericValue: Double,
        displayValue: String,
        type: GradeType,
        date: String,
        topic: String
    ) {
        repository.addGrade(
            studentId = studentId,
            subject = subject,
            numericValue = numericValue,
            displayValue = displayValue,
            type = type,
            date = date,
            topic = topic,
            teacher = "Prof.ssa Maria Bianchi"
        )
    }

    fun updateAttendance(studentId: String, status: AttendanceStatus, timeDetail: String? = null) {
        repository.updateAttendance(studentId, status, timeDetail)
    }

    fun addNote(
        studentId: String?,
        type: NoteType,
        text: String
    ) {
        repository.addSchoolNote(
            studentId = studentId,
            author = "Prof.ssa Maria Bianchi (Docente)",
            date = "14 Ottobre",
            type = type,
            text = text
        )
    }

    fun signLesson(hour: Int, topic: String, homework: String) {
        repository.signLessonLog(hour, topic, homework)
    }

    fun addCircular(
        number: String,
        title: String,
        category: String,
        date: String,
        content: String,
        requiresSignature: Boolean,
        isUrgent: Boolean
    ) {
        repository.addCircular(number, title, category, date, content, requiresSignature, isUrgent)
    }

    // Parent actions
    fun signCircular(circularId: String) {
        repository.signCircular(circularId)
    }

    fun justifyAbsence(attendanceId: String, reason: String) {
        repository.justifyAbsence(attendanceId, reason)
    }

    fun markNoteViewed(noteId: String) {
        repository.markNoteAsViewed(noteId)
    }

    fun markNotificationRead(notifId: String) {
        repository.markNotificationAsRead(notifId)
    }

    fun clearAllNotifications() {
        repository.clearAllNotifications()
    }

    // Reset everything back to completely empty state
    fun resetToEmpty() {
        repository.resetToEmpty()
    }

    // Optional demo helper
    fun loadSampleData() {
        repository.loadSampleData()
    }

    // Quick demo triggers to showcase real-time alerts
    fun triggerDemoNotification(type: String) {
        when (type) {
            "voto" -> repository.simulateIncomingNotification(
                title = "Nuovo Voto registrato",
                message = "Prof. Verdi ha inserito un 9 (Scritto) in Matematica per Marco Rossi: \"Verifica di geometria su triangoli e perimetri\"",
                category = NotificationCategory.VOTO
            )
            "ritardo" -> repository.simulateIncomingNotification(
                title = "Avviso Ingresso in Ritardo",
                message = "Marco Rossi è entrato alle ore 08:35 alla 1ª ora. Giustificazione richiesta.",
                category = NotificationCategory.PRESENZA
            )
            "circolare" -> repository.simulateIncomingNotification(
                title = "Nuova Circolare n. 49",
                message = "Chiusura della scuola per allerta meteo e sanificazione locali didattici.",
                category = NotificationCategory.CIRCOLARE
            )
            "nota" -> repository.simulateIncomingNotification(
                title = "Nota Didattica",
                message = "Prof.ssa Bianchi: \"Marco ha completato con lode la ricerca su Leonardo Da Vinci.\"",
                category = NotificationCategory.NOTA
            )
        }
    }
}
