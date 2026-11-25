package com.projectbharat.counterapp

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectbharat.counterapp.data.CounterLog
import java.time.LocalDate

@Composable
internal fun JapaAchievements(logs: List<CounterLog>, language: UiLanguage) {
    val totals = logs.groupBy { it.date }.mapValues { it.value.sumOf { log -> log.count } }
    val count = totals.values.sum()
    var best = 0
    var run = 0
    var previous: LocalDate? = null
    totals.filterValues { it > 0 }.keys.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        .sorted().forEach { day ->
            run = if (previous?.plusDays(1) == day) run + 1 else 1
            best = maxOf(best, run)
            previous = day
        }
    val milestones = listOf(108, 1080, 10008, 50000, 100000).map { Triple(it, count, false) } +
        listOf(7, 30, 108).map { Triple(it, best, true) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(language.text("Your japa journey", "तुमची जप साधना", "आपकी जप यात्रा"),
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        milestones.forEach { (target, current, streak) ->
            val earned = current >= target
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = brandGlassColor()),
                border = androidx.compose.foundation.BorderStroke(1.dp, brandGlassBorder())) {
                Row(Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    val badge = if (!earned) R.drawable.badge_locked else if (streak) when (target) {
                        7 -> R.drawable.streak_7_days
                        30 -> R.drawable.streak_30_days
                        else -> R.drawable.streak_108_days
                    } else when (target) {
                        108 -> R.drawable.chants_108
                        1080 -> R.drawable.chants_1080
                        10008 -> R.drawable.chants_10008
                        50000 -> R.drawable.chants_50000
                        else -> R.drawable.chants_100000
                    }
                    Image(painterResource(badge), null, Modifier.size(48.dp))
                    Column {
                        Text(if (streak) language.text("$target day practice streak", "$target दिवस सलग जप", "$target दिन लगातार जप")
                            else language.text("$target lifetime chants", "$target एकूण जप", "$target कुल जप"),
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Text(if (earned) language.text("Badge earned", "पदक मिळाले", "बैज मिला")
                            else "$current / $target", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LinearProgressIndicator(progress = { (current.toFloat() / target).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
