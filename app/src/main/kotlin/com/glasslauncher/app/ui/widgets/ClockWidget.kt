package com.glasslauncher.app.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glasslauncher.app.data.model.ClockTimeFormat
import com.glasslauncher.app.data.model.ClockWidgetSettings
import com.glasslauncher.app.glass.GlassSurface
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

/** Live clock, e.g. "VENERDÌ / 15:24 / 28 Agosto". Ticks every second, real system time. */
@Composable
fun ClockWidget(settings: ClockWidgetSettings, modifier: Modifier = Modifier) {
    if (!settings.enabled) return

    val now by produceState(initialValue = LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(1000L)
        }
    }

    val locale = Locale.ITALIAN
    val dayName = now.dayOfWeek.getDisplayName(JTextStyle.FULL, locale).uppercase(locale)
    val hour24 = "%02d:%02d".format(now.hour, now.minute)
    val hour12 = run {
        val h = if (now.hour % 12 == 0) 12 else now.hour % 12
        val suffix = if (now.hour < 12) "AM" else "PM"
        "%02d:%02d %s".format(h, now.minute, suffix)
    }
    val timeText = if (settings.timeFormat == ClockTimeFormat.H24) hour24 else hour12
    val monthName = now.month.getDisplayName(JTextStyle.FULL, locale).replaceFirstChar { it.uppercase(locale) }
    val dateText = "${now.dayOfMonth} $monthName"

    val alignment = when (settings.alignment) {
        0 -> Alignment.Start
        2 -> Alignment.End
        else -> Alignment.CenterHorizontally
    }
    val textAlign = when (settings.alignment) {
        0 -> TextAlign.Start
        2 -> TextAlign.End
        else -> TextAlign.Center
    }

    GlassSurface(
        modifier = modifier.fillMaxWidth(settings.widthFraction),
        transparencyOverride = settings.transparency,
        blurRadiusDpOverride = settings.blurRadiusDp,
        cornerRadiusDp = 32f,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalAlignment = alignment,
        ) {
            if (settings.showDay) {
                BasicText(
                    text = dayName,
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = settings.dayDateSizeSp.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        textAlign = textAlign,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            BasicText(
                text = timeText,
                style = TextStyle(
                    color = Color.White,
                    fontSize = settings.timeSizeSp.sp,
                    fontWeight = FontWeight(settings.fontWeight.coerceIn(100, 900)),
                    textAlign = textAlign,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            if (settings.showDate) {
                BasicText(
                    text = dateText,
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = settings.dayDateSizeSp.sp,
                        textAlign = textAlign,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
