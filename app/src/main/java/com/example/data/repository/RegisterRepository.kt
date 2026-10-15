package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class RegisterRepository(private val context: Context? = null) {

    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("registro_scuola_data_v1", Context.MODE_PRIVATE)

    // Preloaded Class Roster (Classe 2ª B)
    private val _students = listOf(
        Student("s1", "Marco", "Rossi", 1),
        Student("s2", "Sofia", "Bianchi", 2),
        Student("s3", "Lorenzo", "Ferrari", 3),
        Student("s4", "Chiara", "Esposito", 4),
        Student("s5", "Leonardo", "Romano", 5),
        Student("s6", "Giulia", "Colombo", 6),
        Student("s7", "Matteo", "Ricci", 7),
        Student("s8", "Francesca", "Conti", 8),
        Student("s9", "Alessandro", "De Luca", 9),
        Student("s10", "Elena", "Marino", 10)
    )

    private val _grades = MutableStateFlow<List<Grade>>(emptyList())
    val grades: StateFlow<List<Grade>> = _grades.asStateFlow()

    private val _attendance = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendance: StateFlow<List<AttendanceRecord>> = _attendance.asStateFlow()

    private val _notes = MutableStateFlow<List<SchoolNote>>(emptyList())
    val notes: StateFlow<List<SchoolNote>> = _notes.asStateFlow()

    private val _lessonLogs = MutableStateFlow<List<LessonLog>>(emptyList())
    val lessonLogs: StateFlow<List<LessonLog>> = _lessonLogs.asStateFlow()

    private val _circulars = MutableStateFlow<List<SchoolCircular>>(emptyList())
    val circulars: StateFlow<List<SchoolCircular>> = _circulars.asStateFlow()

    private val _notifications = MutableStateFlow<List<PushNotification>>(emptyList())
    val notifications: StateFlow<List<PushNotification>> = _notifications.asStateFlow()

    private val _savedRole = MutableStateFlow(UserRole.DOCENTE)
    val savedRole: StateFlow<UserRole> = _savedRole.asStateFlow()

    private val _headsupNotification = MutableSharedFlow<PushNotification>(extraBufferCapacity = 5)
    val headsupNotification: SharedFlow<PushNotification> = _headsupNotification.asSharedFlow()

    // Teacher's Timetable (Orario Settimanale Prof.ssa Bianchi)
    val teacherTimetable: List<TimetableSlot> = listOf(
        // Lunedì
        TimetableSlot("Lunedì", 1, "08:00 - 09:00", "2ª B", "Italiano (Grammatica)", "Aula 12"),
        TimetableSlot("Lunedì", 2, "09:00 - 10:00", "2ª B", "Italiano (Antologia)", "Aula 12"),
        TimetableSlot("Lunedì", 3, "10:05 - 11:00", "3ª A", "Italiano (Letteratura)", "Aula 18"),
        TimetableSlot("Lunedì", 4, "11:00 - 12:00", "3ª A", "Storia", "Aula 18"),
        // Martedì
        TimetableSlot("Martedì", 1, "08:00 - 09:00", "2ª B", "Geografia", "Aula 12"),
        TimetableSlot("Martedì", 2, "09:00 - 10:00", "1ª C", "Italiano", "Aula 04"),
        TimetableSlot("Martedì", 3, "10:05 - 11:00", "1ª C", "Italiano", "Aula 04"),
        TimetableSlot("Martedì", 5, "12:05 - 13:00", "2ª B", "Storia", "Aula 12"),
        // Mercoledì
        TimetableSlot("Mercoledì", 1, "08:00 - 09:00", "2ª B", "Italiano", "Aula 12"),
        TimetableSlot("Mercoledì", 2, "09:00 - 10:00", "2ª B", "Storia", "Aula 12"),
        TimetableSlot("Mercoledì", 3, "10:05 - 11:00", "3ª A", "Geografia", "Aula 18"),
        TimetableSlot("Mercoledì", 4, "11:00 - 12:00", "Docenti", "Ricevimento Genitori", "Sala Colloqui"),
        // Giovedì
        TimetableSlot("Giovedì", 1, "08:00 - 09:00", "1ª C", "Storia e Geo", "Aula 04"),
        TimetableSlot("Giovedì", 2, "09:00 - 10:00", "3ª A", "Italiano (Scrittura)", "Aula 18"),
        TimetableSlot("Giovedì", 3, "10:05 - 11:00", "2ª B", "Italiano", "Aula 12"),
        TimetableSlot("Giovedì", 4, "11:00 - 12:00", "2ª B", "Italiano", "Aula 12"),
        // Venerdì
        TimetableSlot("Venerdì", 1, "08:00 - 09:00", "3ª A", "Italiano", "Aula 18"),
        TimetableSlot("Venerdì", 2, "09:00 - 10:00", "3ª A", "Storia", "Aula 18"),
        TimetableSlot("Venerdì", 3, "10:05 - 11:00", "2ª B", "Geografia", "Aula 12"),
        TimetableSlot("Venerdì", 4, "11:00 - 12:00", "1ª C", "Italiano", "Aula 04")
    )

    init {
        loadDataFromStorage()
    }

    private fun defaultEmptyLessons(): List<LessonLog> = listOf(
        LessonLog(1, "08:00 - 09:00", "Italiano", "Prof.ssa Maria Bianchi", "2ª B", "", "", "Oggi - 14 Ottobre", isSigned = false),
        LessonLog(2, "09:00 - 10:00", "Storia", "Prof.ssa Maria Bianchi", "2ª B", "", "", "Oggi - 14 Ottobre", isSigned = false),
        LessonLog(3, "10:05 - 11:00", "Matematica", "Prof. Paolo Verdi", "2ª B", "", "", "Oggi - 14 Ottobre", isSigned = false),
        LessonLog(4, "11:00 - 12:00", "Inglese", "Prof.ssa Sarah Smith", "2ª B", "", "", "Oggi - 14 Ottobre", isSigned = false),
        LessonLog(5, "12:05 - 13:00", "Scienze", "Prof.ssa Laura Neri", "2ª B", "", "", "Oggi - 14 Ottobre", isSigned = false)
    )

    private fun defaultAttendance(): List<AttendanceRecord> = _students.map {
        AttendanceRecord(
            id = "att_${it.id}",
            studentId = it.id,
            studentName = it.fullName,
            date = "14 Ottobre",
            status = AttendanceStatus.PRESENTE,
            timeDetail = null,
            isJustified = true,
            justificationReason = null
        )
    }

    fun getStudents(): List<Student> = _students

    // ==========================================
    // PERSISTENCE: LOAD & SAVE VIA SHARED_PREFS
    // ==========================================
    private fun loadDataFromStorage() {
        if (prefs == null) {
            // Memory-only fallback (e.g. during tests without context)
            _grades.value = emptyList()
            _notes.value = emptyList()
            _circulars.value = emptyList()
            _notifications.value = emptyList()
            _lessonLogs.value = defaultEmptyLessons()
            _attendance.value = defaultAttendance()
            return
        }

        val isInitialized = prefs.getBoolean("is_initialized", false)
        if (!isInitialized) {
            // First run: strictly empty at the beginning!
            _grades.value = emptyList()
            _notes.value = emptyList()
            _circulars.value = emptyList()
            _notifications.value = emptyList()
            _lessonLogs.value = defaultEmptyLessons()
            _attendance.value = defaultAttendance()
            _savedRole.value = UserRole.DOCENTE
            saveAllToStorage()
            prefs.edit().putBoolean("is_initialized", true).apply()
            return
        }

        // Load persisted state
        try {
            // Grades
            val gradesJson = prefs.getString("grades_json", null)
            if (!gradesJson.isNullOrEmpty()) {
                val array = JSONArray(gradesJson)
                val list = mutableListOf<Grade>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Grade(
                            id = obj.getString("id"),
                            studentId = obj.getString("studentId"),
                            studentName = obj.getString("studentName"),
                            subject = obj.getString("subject"),
                            numericValue = obj.getDouble("numericValue"),
                            displayValue = obj.getString("displayValue"),
                            type = GradeType.valueOf(obj.getString("type")),
                            date = obj.getString("date"),
                            topic = obj.getString("topic"),
                            teacher = obj.getString("teacher"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _grades.value = list
            } else {
                _grades.value = emptyList()
            }

            // Attendance
            val attJson = prefs.getString("attendance_json", null)
            if (!attJson.isNullOrEmpty()) {
                val array = JSONArray(attJson)
                val list = mutableListOf<AttendanceRecord>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        AttendanceRecord(
                            id = obj.getString("id"),
                            studentId = obj.getString("studentId"),
                            studentName = obj.getString("studentName"),
                            date = obj.getString("date"),
                            status = AttendanceStatus.valueOf(obj.getString("status")),
                            timeDetail = if (obj.has("timeDetail") && !obj.isNull("timeDetail")) obj.getString("timeDetail") else null,
                            isJustified = obj.optBoolean("isJustified", false),
                            justificationReason = if (obj.has("justificationReason") && !obj.isNull("justificationReason")) obj.getString("justificationReason") else null
                        )
                    )
                }
                _attendance.value = list
            } else {
                _attendance.value = defaultAttendance()
            }

            // Notes
            val notesJson = prefs.getString("notes_json", null)
            if (!notesJson.isNullOrEmpty()) {
                val array = JSONArray(notesJson)
                val list = mutableListOf<SchoolNote>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SchoolNote(
                            id = obj.getString("id"),
                            studentId = if (obj.has("studentId") && !obj.isNull("studentId")) obj.getString("studentId") else null,
                            studentName = if (obj.has("studentName") && !obj.isNull("studentName")) obj.getString("studentName") else null,
                            author = obj.getString("author"),
                            date = obj.getString("date"),
                            type = NoteType.valueOf(obj.getString("type")),
                            text = obj.getString("text"),
                            isViewedByParent = obj.optBoolean("isViewedByParent", false)
                        )
                    )
                }
                _notes.value = list
            } else {
                _notes.value = emptyList()
            }

            // Lesson logs
            val lessonsJson = prefs.getString("lessons_json", null)
            if (!lessonsJson.isNullOrEmpty()) {
                val array = JSONArray(lessonsJson)
                val list = mutableListOf<LessonLog>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        LessonLog(
                            hour = obj.getInt("hour"),
                            timeRange = obj.getString("timeRange"),
                            subject = obj.getString("subject"),
                            teacher = obj.getString("teacher"),
                            className = obj.optString("className", "2ª B"),
                            topic = obj.getString("topic"),
                            homework = obj.getString("homework"),
                            date = obj.getString("date"),
                            isSigned = obj.getBoolean("isSigned")
                        )
                    )
                }
                _lessonLogs.value = list
            } else {
                _lessonLogs.value = defaultEmptyLessons()
            }

            // Circulars
            val circJson = prefs.getString("circulars_json", null)
            if (!circJson.isNullOrEmpty()) {
                val array = JSONArray(circJson)
                val list = mutableListOf<SchoolCircular>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SchoolCircular(
                            id = obj.getString("id"),
                            number = obj.getString("number"),
                            title = obj.getString("title"),
                            category = obj.getString("category"),
                            date = obj.getString("date"),
                            content = obj.getString("content"),
                            requiresSignature = obj.optBoolean("requiresSignature", true),
                            isSignedByParent = obj.optBoolean("isSignedByParent", false),
                            isUrgent = obj.optBoolean("isUrgent", false)
                        )
                    )
                }
                _circulars.value = list
            } else {
                _circulars.value = emptyList()
            }

            // Notifications
            val notifJson = prefs.getString("notifications_json", null)
            if (!notifJson.isNullOrEmpty()) {
                val array = JSONArray(notifJson)
                val list = mutableListOf<PushNotification>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        PushNotification(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            message = obj.getString("message"),
                            timestamp = obj.getLong("timestamp"),
                            category = NotificationCategory.valueOf(obj.getString("category")),
                            targetRole = UserRole.valueOf(obj.getString("targetRole")),
                            isRead = obj.optBoolean("isRead", false)
                        )
                    )
                }
                _notifications.value = list
            } else {
                _notifications.value = emptyList()
            }

            // Role
            val roleName = prefs.getString("active_role", UserRole.DOCENTE.name)
            _savedRole.value = try {
                UserRole.valueOf(roleName ?: UserRole.DOCENTE.name)
            } catch (e: Exception) {
                UserRole.DOCENTE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveAllToStorage() {
        if (prefs == null) return
        val editor = prefs.edit()

        // Grades
        val gradesArr = JSONArray()
        for (g in _grades.value) {
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("studentId", g.studentId)
            obj.put("studentName", g.studentName)
            obj.put("subject", g.subject)
            obj.put("numericValue", g.numericValue)
            obj.put("displayValue", g.displayValue)
            obj.put("type", g.type.name)
            obj.put("date", g.date)
            obj.put("topic", g.topic)
            obj.put("teacher", g.teacher)
            obj.put("timestamp", g.timestamp)
            gradesArr.put(obj)
        }
        editor.putString("grades_json", gradesArr.toString())

        // Attendance
        val attArr = JSONArray()
        for (a in _attendance.value) {
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("studentId", a.studentId)
            obj.put("studentName", a.studentName)
            obj.put("date", a.date)
            obj.put("status", a.status.name)
            obj.put("timeDetail", a.timeDetail)
            obj.put("isJustified", a.isJustified)
            obj.put("justificationReason", a.justificationReason)
            attArr.put(obj)
        }
        editor.putString("attendance_json", attArr.toString())

        // Notes
        val notesArr = JSONArray()
        for (n in _notes.value) {
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("studentId", n.studentId)
            obj.put("studentName", n.studentName)
            obj.put("author", n.author)
            obj.put("date", n.date)
            obj.put("type", n.type.name)
            obj.put("text", n.text)
            obj.put("isViewedByParent", n.isViewedByParent)
            notesArr.put(obj)
        }
        editor.putString("notes_json", notesArr.toString())

        // Lesson logs
        val lessonsArr = JSONArray()
        for (l in _lessonLogs.value) {
            val obj = JSONObject()
            obj.put("hour", l.hour)
            obj.put("timeRange", l.timeRange)
            obj.put("subject", l.subject)
            obj.put("teacher", l.teacher)
            obj.put("className", l.className)
            obj.put("topic", l.topic)
            obj.put("homework", l.homework)
            obj.put("date", l.date)
            obj.put("isSigned", l.isSigned)
            lessonsArr.put(obj)
        }
        editor.putString("lessons_json", lessonsArr.toString())

        // Circulars
        val circArr = JSONArray()
        for (c in _circulars.value) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("number", c.number)
            obj.put("title", c.title)
            obj.put("category", c.category)
            obj.put("date", c.date)
            obj.put("content", c.content)
            obj.put("requiresSignature", c.requiresSignature)
            obj.put("isSignedByParent", c.isSignedByParent)
            obj.put("isUrgent", c.isUrgent)
            circArr.put(obj)
        }
        editor.putString("circulars_json", circArr.toString())

        // Notifications
        val notifArr = JSONArray()
        for (notif in _notifications.value) {
            val obj = JSONObject()
            obj.put("id", notif.id)
            obj.put("title", notif.title)
            obj.put("message", notif.message)
            obj.put("timestamp", notif.timestamp)
            obj.put("category", notif.category.name)
            obj.put("targetRole", notif.targetRole.name)
            obj.put("isRead", notif.isRead)
            notifArr.put(obj)
        }
        editor.putString("notifications_json", notifArr.toString())

        // Active role
        editor.putString("active_role", _savedRole.value.name)
        editor.apply()
    }

    fun saveActiveRole(role: UserRole) {
        _savedRole.value = role
        prefs?.edit()?.putString("active_role", role.name)?.apply()
    }

    // ==========================================
    // REPOSITORY ACTIONS
    // ==========================================

    fun addGrade(
        studentId: String,
        subject: String,
        numericValue: Double,
        displayValue: String,
        type: GradeType,
        date: String,
        topic: String,
        teacher: String
    ): Grade {
        val student = _students.firstOrNull { it.id == studentId } ?: _students.first()
        val newGrade = Grade(
            id = UUID.randomUUID().toString(),
            studentId = studentId,
            studentName = student.fullName,
            subject = subject,
            numericValue = numericValue,
            displayValue = displayValue,
            type = type,
            date = date,
            topic = topic,
            teacher = teacher,
            timestamp = System.currentTimeMillis()
        )
        _grades.value = listOf(newGrade) + _grades.value

        // Dispatch simulated real-time push notification for the parent
        val notif = PushNotification(
            id = UUID.randomUUID().toString(),
            title = "Nuovo Voto: $subject",
            message = "Per ${student.firstName}: voto $displayValue (${type.label}) inserito da $teacher. Argomento: \"$topic\"",
            timestamp = System.currentTimeMillis(),
            category = NotificationCategory.VOTO,
            targetRole = UserRole.GENITORE,
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
        _headsupNotification.tryEmit(notif)

        saveAllToStorage()
        return newGrade
    }

    fun updateAttendance(studentId: String, newStatus: AttendanceStatus, timeDetail: String? = null) {
        val currentList = _attendance.value.toMutableList()
        val index = currentList.indexOfFirst { it.studentId == studentId }
        val student = _students.firstOrNull { it.id == studentId }

        if (index != -1 && student != null) {
            val updated = currentList[index].copy(
                status = newStatus,
                timeDetail = timeDetail,
                isJustified = newStatus == AttendanceStatus.PRESENTE
            )
            currentList[index] = updated
            _attendance.value = currentList

            if (newStatus != AttendanceStatus.PRESENTE) {
                val notif = PushNotification(
                    id = UUID.randomUUID().toString(),
                    title = "Registro Presenze: ${newStatus.label}",
                    message = "${student.fullName} risulta ${newStatus.label.uppercase()} in data odierna${if (timeDetail != null) " ($timeDetail)" else ""}.",
                    timestamp = System.currentTimeMillis(),
                    category = NotificationCategory.PRESENZA,
                    targetRole = UserRole.GENITORE,
                    isRead = false
                )
                _notifications.value = listOf(notif) + _notifications.value
                _headsupNotification.tryEmit(notif)
            }
            saveAllToStorage()
        }
    }

    fun addSchoolNote(
        studentId: String?,
        author: String,
        date: String,
        type: NoteType,
        text: String
    ): SchoolNote {
        val student = if (studentId != null) _students.firstOrNull { it.id == studentId } else null
        val targetName = student?.fullName ?: "Intera Classe 2ª B"

        val note = SchoolNote(
            id = UUID.randomUUID().toString(),
            studentId = studentId,
            studentName = targetName,
            author = author,
            date = date,
            type = type,
            text = text,
            isViewedByParent = false
        )
        _notes.value = listOf(note) + _notes.value

        val notif = PushNotification(
            id = UUID.randomUUID().toString(),
            title = "${type.label}: $targetName",
            message = "Nuova nota inserita da $author: \"$text\"",
            timestamp = System.currentTimeMillis(),
            category = NotificationCategory.NOTA,
            targetRole = UserRole.GENITORE,
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
        _headsupNotification.tryEmit(notif)

        saveAllToStorage()
        return note
    }

    fun signLessonLog(hour: Int, topic: String, homework: String) {
        val list = _lessonLogs.value.toMutableList()
        val index = list.indexOfFirst { it.hour == hour }
        if (index != -1) {
            list[index] = list[index].copy(
                topic = topic,
                homework = homework,
                isSigned = true
            )
            _lessonLogs.value = list
            saveAllToStorage()
        }
    }

    fun addCircular(
        number: String,
        title: String,
        category: String,
        date: String,
        content: String,
        requiresSignature: Boolean,
        isUrgent: Boolean
    ): SchoolCircular {
        val circ = SchoolCircular(
            id = UUID.randomUUID().toString(),
            number = number,
            title = title,
            category = category,
            date = date,
            content = content,
            requiresSignature = requiresSignature,
            isSignedByParent = false,
            isUrgent = isUrgent
        )
        _circulars.value = listOf(circ) + _circulars.value

        val notif = PushNotification(
            id = UUID.randomUUID().toString(),
            title = "Nuova Comunicazione: $number",
            message = "$title: pubblicata nella bacheca dell'Istituto.",
            timestamp = System.currentTimeMillis(),
            category = NotificationCategory.CIRCOLARE,
            targetRole = UserRole.GENITORE,
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
        _headsupNotification.tryEmit(notif)

        saveAllToStorage()
        return circ
    }

    fun signCircular(circularId: String) {
        val list = _circulars.value.toMutableList()
        val idx = list.indexOfFirst { it.id == circularId }
        if (idx != -1) {
            list[idx] = list[idx].copy(isSignedByParent = true)
            _circulars.value = list
            saveAllToStorage()
        }
    }

    fun justifyAbsence(attendanceId: String, reason: String) {
        val list = _attendance.value.toMutableList()
        val idx = list.indexOfFirst { it.id == attendanceId }
        if (idx != -1) {
            list[idx] = list[idx].copy(
                isJustified = true,
                justificationReason = reason
            )
            _attendance.value = list
            saveAllToStorage()
        }
    }

    fun markNoteAsViewed(noteId: String) {
        val list = _notes.value.toMutableList()
        val idx = list.indexOfFirst { it.id == noteId }
        if (idx != -1) {
            list[idx] = list[idx].copy(isViewedByParent = true)
            _notes.value = list
            saveAllToStorage()
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        val list = _notifications.value.toMutableList()
        val idx = list.indexOfFirst { it.id == notificationId }
        if (idx != -1) {
            list[idx] = list[idx].copy(isRead = true)
            _notifications.value = list
            saveAllToStorage()
        }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
        saveAllToStorage()
    }

    fun resetToEmpty() {
        _grades.value = emptyList()
        _notes.value = emptyList()
        _circulars.value = emptyList()
        _notifications.value = emptyList()
        _lessonLogs.value = defaultEmptyLessons()
        _attendance.value = defaultAttendance()
        saveAllToStorage()
    }

    fun loadSampleData() {
        _grades.value = listOf(
            Grade("g1", "s1", "Rossi Marco", "Italiano", 8.0, "8", GradeType.SCRITTO, "12 Ottobre", "Tema descrittivo e argomentativo", "Prof.ssa Bianchi"),
            Grade("g2", "s1", "Rossi Marco", "Matematica", 7.5, "7½", GradeType.SCRITTO, "10 Ottobre", "Verifica equazioni di 1° grado", "Prof. Verdi"),
            Grade("g3", "s1", "Rossi Marco", "Storia", 8.5, "8½", GradeType.ORALE, "07 Ottobre", "Interrogazione sull'Umanesimo", "Prof.ssa Bianchi"),
            Grade("g4", "s1", "Rossi Marco", "Inglese", 8.0, "8", GradeType.ORALE, "02 Ottobre", "Speaking: Past Continuous", "Prof. Smith")
        )

        _notes.value = listOf(
            SchoolNote(
                id = "n1",
                studentId = "s1",
                studentName = "Rossi Marco",
                author = "Prof.ssa Bianchi",
                date = "11 Ottobre",
                type = NoteType.GENERICA,
                text = "L'alunno ha partecipato con vivo interesse e prontezza al lavoro di gruppo in classe.",
                isViewedByParent = true
            )
        )

        _circulars.value = listOf(
            SchoolCircular(
                id = "c1",
                number = "Circ. n. 48",
                title = "Uscita Didattica al Planetario e Museo della Scienza",
                category = "Viaggi d'istruzione",
                date = "13 Ottobre 2026",
                content = "Si comunica che le classi seconde si recheranno in visita al Planetario martedì 27 ottobre. Partenza ore 08:30.",
                requiresSignature = true,
                isSignedByParent = false,
                isUrgent = true
            )
        )

        val logs = defaultEmptyLessons().toMutableList()
        logs[0] = logs[0].copy(
            topic = "I Promessi Sposi: commento al Capitolo VI",
            homework = "Leggere pag. 112-118 e rispondere alle domande.",
            isSigned = true
        )
        _lessonLogs.value = logs

        _notifications.value = listOf(
            PushNotification(
                id = "notif-sample-1",
                title = "Nuova Circolare n. 48",
                message = "Uscita Didattica al Planetario: è richiesta la firma del genitore.",
                timestamp = System.currentTimeMillis() - 3600000,
                category = NotificationCategory.CIRCOLARE,
                targetRole = UserRole.GENITORE,
                isRead = false
            )
        )
        saveAllToStorage()
    }

    fun simulateIncomingNotification(title: String, message: String, category: NotificationCategory) {
        val notif = PushNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            category = category,
            targetRole = UserRole.GENITORE,
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
        _headsupNotification.tryEmit(notif)
        saveAllToStorage()
    }
}
