package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AttendanceStatus
import com.example.data.model.GradeType
import com.example.data.model.NoteType
import com.example.data.model.UserRole
import com.example.data.repository.RegisterRepository
import com.example.ui.viewmodel.RegisterViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RegisterTest {

    private lateinit var app: Application
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        // Clear shared prefs for test isolation
        app.getSharedPreferences("registro_scuola_data_v1", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        viewModel = RegisterViewModel(app)
    }

    @Test
    fun testInitiallyEverythingEmpty() {
        // As requested: all'inizio deve essere tutto vuoto
        assertTrue(viewModel.grades.value.isEmpty())
        assertTrue(viewModel.notes.value.isEmpty())
        assertTrue(viewModel.circulars.value.isEmpty())
        assertTrue(viewModel.notifications.value.isEmpty())
        assertEquals(10, viewModel.students.size)
    }

    @Test
    fun testAddGradeAndPersistOnReopen() = runTest {
        viewModel.addGrade(
            studentId = "s1",
            subject = "Italiano",
            numericValue = 8.5,
            displayValue = "8½",
            type = GradeType.ORALE,
            date = "14 Ottobre",
            topic = "Interrogazione di grammatica"
        )

        assertEquals(1, viewModel.grades.value.size)
        assertEquals("8½", viewModel.grades.value.first().displayValue)

        // Simulate closing and reopening the app: new ViewModel on same context
        val reopenedViewModel = RegisterViewModel(app)
        assertEquals(1, reopenedViewModel.grades.value.size)
        assertEquals("8½", reopenedViewModel.grades.value.first().displayValue)
        assertEquals("Italiano", reopenedViewModel.grades.value.first().subject)
    }

    @Test
    fun testAttendancePersistenceOnReopen() = runTest {
        viewModel.updateAttendance("s1", AttendanceStatus.ASSENTE)
        assertEquals(AttendanceStatus.ASSENTE, viewModel.attendance.value.first { it.studentId == "s1" }.status)

        // Reopen app
        val reopened = RegisterViewModel(app)
        assertEquals(AttendanceStatus.ASSENTE, reopened.attendance.value.first { it.studentId == "s1" }.status)
    }

    @Test
    fun testNotesPersistenceOnReopen() = runTest {
        viewModel.addNote(
            studentId = "s1",
            type = NoteType.DISCIPLINARE,
            text = "Comportamento scorretto."
        )

        assertEquals(1, viewModel.notes.value.size)
        // Reopen
        val reopened = RegisterViewModel(app)
        assertEquals(1, reopened.notes.value.size)
        assertEquals("Comportamento scorretto.", reopened.notes.value.first().text)
    }

    @Test
    fun testCircularsPersistenceOnReopen() = runTest {
        viewModel.addCircular(
            number = "Circ. n. 1",
            title = "Visita guidata",
            category = "Uscite",
            date = "14 Ottobre",
            content = "Dettagli della visita guidata...",
            requiresSignature = true,
            isUrgent = false
        )

        assertEquals(1, viewModel.circulars.value.size)
        // Reopen
        val reopened = RegisterViewModel(app)
        assertEquals(1, reopened.circulars.value.size)
        assertEquals("Circ. n. 1", reopened.circulars.value.first().number)
    }

    @Test
    fun testRolePersistenceOnReopen() {
        viewModel.setRole(UserRole.GENITORE)
        assertEquals(UserRole.GENITORE, viewModel.uiState.value.currentRole)

        // Reopen app
        val reopened = RegisterViewModel(app)
        assertEquals(UserRole.GENITORE, reopened.uiState.value.currentRole)
    }
}
