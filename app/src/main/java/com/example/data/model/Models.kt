package com.example.data.model

enum class UserRole(val label: String, val subtitle: String) {
    DOCENTE("Prof.ssa Maria Bianchi", "Docente di Lettere - Coord. 2ª B"),
    GENITORE("Fam. Rossi (Mario Rossi)", "Genitore di Marco Rossi - 2ª B")
}

data class Student(
    val id: String,
    val firstName: String,
    val lastName: String,
    val rollNumber: Int,
    val className: String = "2ª B",
    val parentName: String = "Famiglia $lastName"
) {
    val fullName: String get() = "$lastName $firstName"
}

enum class GradeType(val label: String) {
    SCRITTO("Scritto"),
    ORALE("Orale"),
    PRATICO("Pratico")
}

data class Grade(
    val id: String,
    val studentId: String,
    val studentName: String,
    val subject: String,
    val numericValue: Double,
    val displayValue: String, // e.g. "8", "7½", "6+", "5½", "9"
    val type: GradeType,
    val date: String,
    val topic: String,
    val teacher: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AttendanceStatus(val label: String, val shortLabel: String) {
    PRESENTE("Presente", "P"),
    ASSENTE("Assente", "A"),
    RITARDO_BREVE("Ritardo Breve", "R"),
    USCITA_ANTICIPATA("Uscita Anticipata", "U")
}

data class AttendanceRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val date: String,
    val status: AttendanceStatus,
    val timeDetail: String? = null, // e.g. "Entrato alle 09:05 (2ª ora)"
    val isJustified: Boolean = false,
    val justificationReason: String? = null
)

enum class NoteType(val label: String) {
    DISCIPLINARE("Nota Disciplinare"),
    GENERICA("Nota Didattica / Generica"),
    COMPITI("Mancanza Compiti / Materiale")
}

data class SchoolNote(
    val id: String,
    val studentId: String?, // null if for whole class
    val studentName: String?,
    val author: String,
    val date: String,
    val type: NoteType,
    val text: String,
    val isViewedByParent: Boolean = false
)

data class LessonLog(
    val hour: Int,
    val timeRange: String,
    val subject: String,
    val teacher: String,
    val className: String = "2ª B",
    val topic: String,
    val homework: String,
    val date: String,
    val isSigned: Boolean = true
)

data class TimetableSlot(
    val dayOfWeek: String, // Lunedì, Martedì, etc.
    val hour: Int,
    val timeRange: String,
    val className: String,
    val subject: String,
    val room: String
)

data class SchoolCircular(
    val id: String,
    val number: String,
    val title: String,
    val category: String, // "Circolare DS", "Avviso Didattico", "Viaggio d'istruzione"
    val date: String,
    val content: String,
    val requiresSignature: Boolean = true,
    val isSignedByParent: Boolean = false,
    val isUrgent: Boolean = false
)

data class ReportCardSubject(
    val subject: String,
    val writtenGrade: Double?,
    val oralGrade: Double?,
    val finalGrade: Double,
    val absencesCount: Int
)

data class ReportCard(
    val studentName: String,
    val className: String,
    val termName: String, // "1° Quadrimestre (Valutazione Intermedia)"
    val schoolYear: String = "2026/2027",
    val subjects: List<ReportCardSubject>,
    val conductGrade: String = "9 / 10",
    val overallJudgment: String
)

enum class NotificationCategory {
    VOTO,
    NOTA,
    PRESENZA,
    CIRCOLARE
}

data class PushNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val category: NotificationCategory,
    val targetRole: UserRole,
    val isRead: Boolean = false
)
