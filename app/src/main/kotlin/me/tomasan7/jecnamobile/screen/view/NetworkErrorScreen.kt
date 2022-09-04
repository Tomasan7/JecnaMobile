package me.tomasan7.jecnamobile.screen.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import me.tomasan7.jecnamobile.R

@RequiresApi(Build.VERSION_CODES.S)
@RootNavGraph
@Destination
@Composable
fun NetworkErrorScreen()
{
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 30.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.SentimentVeryDissatisfied,
                contentDescription = "Sad smiley face",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(150.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.error_no_internet_connection),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}