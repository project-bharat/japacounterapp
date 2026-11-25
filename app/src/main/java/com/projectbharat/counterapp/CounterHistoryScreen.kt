package com.projectbharat.counterapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectbharat.counterapp.data.CounterLog
import com.projectbharat.counterapp.data.DayTotal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CounterHistoryScreen(
    uiLanguage: UiLanguage,
    ink: Color,
    date: LocalDate,
    logs: List<CounterLog>,
    days: List<DayTotal>,
    mantraLabel: (String) -> String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onUpdate: (CounterLog) -> Unit,
    onDelete: (CounterLog) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var editing by remember { mutableStateOf<CounterLog?>(null) }
    var deleting by remember { mutableStateOf<CounterLog?>(null) }
    val total = logs.sumOf { it.count }
    Column(Modifier.fillMaxSize()) {
        if (onBack != null) {
            TextButton(onClick = onBack) { Text(if (uiLanguage.isEnglish) "Back to Japa report" else uiLanguage.indic("जप अहवालाकडे परत", "जप रिपोर्ट पर वापस")) }
        }
        Text(if (uiLanguage.isEnglish) "HISTORY & ANALYTICS" else uiLanguage.indic("इतिहास व विश्लेषण", "इतिहास और विश्लेषण"), color = ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp), colors = CardDefaults.cardColors(containerColor = brandGlassColor())) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(if (uiLanguage.isEnglish) "7-day total" else uiLanguage.indic("७ दिवसांची एकूण मोजणी", "७ दिनों की कुल गिनती"), color = ink.copy(alpha = .6f), style = MaterialTheme.typography.labelSmall)
                        Text(days.sumOf { it.total }.toString(), color = ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (uiLanguage.isEnglish) "Daily average" else uiLanguage.indic("दररोज सरासरी", "दैनिक औसत"), color = ink.copy(alpha = .6f), style = MaterialTheme.typography.labelSmall)
                        Text("%.1f".format(days.sumOf { it.total }.toDouble() / 7.0), color = ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
                WeeklyChart(days, Modifier.fillMaxWidth().height(92.dp).padding(top = 8.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onPrevious) { Text("‹ " + if (uiLanguage.isEnglish) "Previous" else uiLanguage.indic("मागील", "पिछला")) }
            Text(date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", uiLanguage.locale)), color = ink, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onNext, enabled = date.isBefore(LocalDate.now())) { Text((if (uiLanguage.isEnglish) "Next" else uiLanguage.indic("पुढील", "अगला")) + " ›") }
        }
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = brandGlassColor()) {
            Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(if (uiLanguage.isEnglish) "Recorded entries" else uiLanguage.indic("नोंदी", "प्रविष्टियाँ"), color = ink.copy(alpha = .6f), style = MaterialTheme.typography.labelSmall)
                    Text(logs.size.toString(), color = ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (uiLanguage.isEnglish) "Total count" else uiLanguage.indic("एकूण मोजणी", "कुल गिनती"), color = ink.copy(alpha = .6f), style = MaterialTheme.typography.labelSmall)
                    Text(total.toString(), color = ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        if (logs.isEmpty()) {
            Text(if (uiLanguage.isEnglish) "No entries recorded on this date." else uiLanguage.indic("या दिवशी कोणतीही नोंद नाही.", "इस दिन कोई प्रविष्टि नहीं है।"), color = ink.copy(alpha = .6f), modifier = Modifier.padding(12.dp))
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                items(logs, key = { it.id }) { log ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = brandGlassColor())) {
                        Row(Modifier.fillMaxWidth().padding(start = 13.dp, end = 5.dp, top = 9.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f).clickable { editing = log }.padding(vertical = 3.dp)) {
                                val time = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("hh:mm:ss a"))
                                val mantraEntry = log.note?.takeIf { it.startsWith("MANTRA:") }
                                Text(if (mantraEntry != null) mantraEntry.removePrefix("MANTRA:").let(mantraLabel) + " · " + log.count else time + "  ·  +" + log.count, color = ink, fontWeight = FontWeight.SemiBold)
                                val details = listOfNotNull(log.strength?.takeIf { it.isNotBlank() }, log.note?.takeIf { it.isNotBlank() && !it.startsWith("MANTRA:") }).joinToString(" · ")
                                if (details.isNotBlank()) Text(details, color = ink.copy(alpha = .62f), style = MaterialTheme.typography.bodySmall)
                                if (log.sessionId != null) Text(if (uiLanguage.isEnglish) "Session record" else uiLanguage.indic("सत्राची नोंद", "सत्र की प्रविष्टि"), color = ink.copy(alpha = .48f), style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { editing = log }) { Icon(Icons.Default.Edit, contentDescription = if (uiLanguage.isEnglish) "Edit" else uiLanguage.indic("बदला", "बदलें"), tint = ink.copy(alpha = .7f)) }
                            IconButton(onClick = { deleting = log }) { Icon(Icons.Default.Delete, contentDescription = if (uiLanguage.isEnglish) "Delete" else uiLanguage.indic("हटवा", "हटाएँ"), tint = Color(0xFFB34E55)) }
                        }
                    }
                }
            }
        }
    }
    editing?.let { original ->
        var strength by remember(original.id) { mutableStateOf(original.strength.orEmpty()) }
        var note by remember(original.id) { mutableStateOf(original.note.orEmpty()) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (uiLanguage.isEnglish) "Edit entry" else uiLanguage.indic("नोंद बदला", "प्रविष्टि संपादित करें")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(strength, { strength = it }, label = { Text(if (uiLanguage.isEnglish) "Strength / tag" else uiLanguage.indic("ताकद / खूण", "तीव्रता / चिह्न")) }, singleLine = true)
                    OutlinedTextField(note, { note = it }, label = { Text(if (uiLanguage.isEnglish) "Note" else uiLanguage.indic("टीप", "टिप्पणी")) }, minLines = 2)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdate(original.copy(strength = strength.trim().ifBlank { null }, note = note.trim().ifBlank { null }))
                    editing = null
                }) { Text(if (uiLanguage.isEnglish) "Save" else uiLanguage.indic("जतन करा", "सहेजें")) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(if (uiLanguage.isEnglish) "Cancel" else uiLanguage.indic("रद्द", "रद्द करें")) } }
        )
    }
    deleting?.let { item ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(if (uiLanguage.isEnglish) "Delete this entry?" else uiLanguage.indic("ही नोंद हटवायची?", "यह प्रविष्टि हटाएँ?")) },
            text = { Text(if (uiLanguage.isEnglish) "This cannot be undone." else uiLanguage.indic("ही कृती मागे घेता येणार नाही.", "इस कार्रवाई को वापस नहीं लिया जा सकता।")) },
            confirmButton = { TextButton(onClick = { onDelete(item); deleting = null }) { Text(if (uiLanguage.isEnglish) "Delete" else uiLanguage.indic("हटवा", "हटाएँ")) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(if (uiLanguage.isEnglish) "Cancel" else uiLanguage.indic("रद्द", "रद्द करें")) } }
        )
    }
}
