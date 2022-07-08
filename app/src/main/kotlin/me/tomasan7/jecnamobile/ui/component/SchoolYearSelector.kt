package me.tomasan7.jecnamobile.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnamobile.R
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolYearSelector(
    selectedSchoolYear: SchoolYear,
    modifier: Modifier = Modifier,
    onChange: (SchoolYear) -> Unit
)
{
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            label = { Text(stringResource(R.string.school_year)) },
            value = selectedSchoolYear.toString(),
            readOnly = true,
            onValueChange = {},
            shape = RoundedCornerShape(10.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        /* SchoolYearRange of four past SchoolYears including the current one. */
        val past4SchoolYears = remember {
            val currentSchoolYear = SchoolYear(LocalDate.now())
            ((currentSchoolYear - 3)..currentSchoolYear)
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            past4SchoolYears.forEach { schoolYear ->
                DropdownMenuItem(
                    text = { Text(schoolYear.toString()) },
                    onClick = { expanded = false; onChange(schoolYear) }
                )
            }
        }
    }
}