package me.tomasan7.jecnamobile.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tomasan7.jecnamobile.ui.theme.label_dark
import me.tomasan7.jecnamobile.ui.theme.label_light

@Composable
fun DialogRow(
    label: String,
    value: String
)
{
    Surface(
        tonalElevation = 10.dp,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(top = 5.dp, start = 15.dp, end = 15.dp, bottom = 15.dp)) {
            Text(
                text = label,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                color = if (isSystemInDarkTheme()) label_dark else label_light,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = value,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}