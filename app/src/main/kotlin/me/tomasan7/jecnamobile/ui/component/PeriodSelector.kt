package me.tomasan7.jecnamobile.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tomasan7.jecnamobile.util.rememberMutableStateOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PeriodSelectorNullable(
    label: String,
    options: List<T>,
    selectedValue: T? = options[0],
    optionStringMap: @Composable (T?) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    onChange: (T) -> Unit
)
{
    var expanded by rememberMutableStateOf(false)

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            label = { Text(label) },
            value = optionStringMap(selectedValue),
            readOnly = true,
            onValueChange = {},
            shape = RoundedCornerShape(10.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionStringMap(option)) },
                    onClick = { expanded = false; onChange(option) }
                )
            }
        }
    }
}

@Composable
fun <T> PeriodSelector(
    label: String,
    options: List<T>,
    selectedValue: T = options[0],
    optionStringMap: @Composable (T) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    onChange: (T) -> Unit
) = PeriodSelectorNullable(
    label = label,
    options = options,
    selectedValue = selectedValue,
    optionStringMap = { optionStringMap(it!!) },
    modifier = modifier,
    onChange = onChange
)