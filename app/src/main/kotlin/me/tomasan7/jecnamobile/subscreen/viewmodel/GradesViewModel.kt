package me.tomasan7.jecnamobile.subscreen.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.repository.GradesRepository
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.subscreen.state.GradesState
import javax.inject.Inject

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val gradesRepository: GradesRepository,
    @ApplicationContext
    val appContext: Context
) : ViewModel()
{
    var uiState by mutableStateOf(GradesState())
        private set

    private var loadGradesJob: Job? = null

    init
    {
        loadGrades()
    }

    fun selectSchoolYearHalf(schoolYearHalf: SchoolYearHalf)
    {
        uiState = uiState.copy(selectedSchoolYearHalf = schoolYearHalf)
        loadGrades()
    }

    fun selectSchoolYear(schoolYear: SchoolYear)
    {
        uiState = uiState.copy(selectedSchoolYear = schoolYear)
        loadGrades()
    }

    fun loadGrades()
    {
        uiState = uiState.copy(loading = true)

        loadGradesJob?.cancel()

        loadGradesJob = viewModelScope.launch {
            try
            {
                val grades = gradesRepository.queryGradesPage(uiState.selectedSchoolYear, uiState.selectedSchoolYearHalf)
                uiState = uiState.copy(loading = false, gradesPage = grades)
            }
            catch (e: ParseException)
            {
                e.printStackTrace()
                Toast.makeText(appContext, appContext.getString(R.string.unsupported_grades), Toast.LENGTH_LONG).show()
                uiState = uiState.copy(loading = false)
            }
            catch (e: CancellationException)
            {
                /* To not catch cancellation exception with the following catch block.  */
                throw e
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                Toast.makeText(appContext, appContext.getString(R.string.error_load), Toast.LENGTH_LONG).show()
                uiState = uiState.copy(loading = false)
            }
        }
    }
}