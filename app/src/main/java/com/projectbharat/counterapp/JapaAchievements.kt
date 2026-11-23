package com.projectbharat.counterapp

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
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        milestones.forEach { (target, current, streak) ->
            val earned = current >= target
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(
                    if (earned) listOf(Color(0xFFFFE3B8), Color(0xFFFFF8EC))
                    else listOf(Color(0xFFF1E9DE), Color(0xFFFFFCF7)))).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(Icons.Default.EmojiEvents, null, tint = if (earned) Color(0xFFAD5B12) else Color(0xFF72685D))
                    Column {
                        Text(if (streak) language.text("$target day practice streak", "$target दिवस सलग जप", "$target दिन लगातार जप")
                            else language.text("$target lifetime chants", "$target एकूण जप", "$target कुल जप"),
                            color = Color(0xFF302941), fontWeight = FontWeight.SemiBold)
                        Text(if (earned) language.text("Badge earned", "पदक मिळाले", "बैज मिला")
                            else "$current / $target", color = Color(0xFF514655))
                        LinearProgressIndicator(progress = { (current.toFloat() / target).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = Color(0xFFA54E0A))
                    }
                }
            }
        }
    }
}
