package me.tomasan7.jecnamobile.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.tomasan7.jecnamobile.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineDataIndicator(
    underlyingIcon: ImageVector,
    lastUpdateTimestamp: Instant?,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
)
{
    if (lastUpdateTimestamp == null) return

    val tooltipState = remember { PlainTooltipState() }
    val scope = rememberCoroutineScope()
    val text = run {
        val localDateTime = LocalDateTime.ofInstant(lastUpdateTimestamp, ZoneId.systemDefault())
        val localDate = localDateTime.toLocalDate()

        if (localDate == LocalDate.now())
        {
            val time = localDateTime.toLocalTime()
            val timeStr = time.format(OFFLINE_MESSAGE_TIME_FORMATTER)
            stringResource(R.string.showing_offline_data_time_description, timeStr)
        }
        else
        {
            val dateStr = localDateTime.format(OFFLINE_MESSAGE_DATE_FORMATTER)
            stringResource(R.string.showing_offline_data_date_description, dateStr)
        }
    }

    PlainTooltipBox(
        tooltipState = tooltipState,
        tooltip = { Text(text) }
    ) {
        AnimatedVisibility(
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            visible = visible
        ) {
            Box(
                modifier = modifier
                    .width(35.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .clickable {
                        scope.launch {
                            if (tooltipState.isVisible) tooltipState.dismiss() else tooltipState.show()
                        }
                    }
            )
            {
                Icon(
                    modifier = Modifier.drawWithContent {
                        clipRect(right = size.width - size.width / 3.5f) {
                            this@drawWithContent.drawContent()
                        }
                    },
                    imageVector = underlyingIcon,
                    contentDescription = null
                )
                Icon(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    imageVector = Icons.Filled.PriorityHigh,
                    contentDescription = null
                )
            }
        }
    }
}

private val OFFLINE_MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
private val OFFLINE_MESSAGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("d. M.")