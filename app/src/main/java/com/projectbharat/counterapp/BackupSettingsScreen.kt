package com.projectbharat.counterapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BackupSettingsScreen(
    uiLanguage: UiLanguage,
    ink: Color,
    message: String?,
    busy: Boolean,
    onUpgrade: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onExportCsv: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (uiLanguage.isEnglish) "Backup & sync" else uiLanguage.indic("बॅकअप व सिंक", "बैकअप और सिंक"), color = ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(if (uiLanguage.isEnglish) "Your data stays on this device until you export it." else uiLanguage.indic("निर्यात करेपर्यंत तुमचा डेटा याच उपकरणावर राहतो.", "निर्यात करने तक आपका डेटा इसी डिवाइस पर रहता है।"), color = ink.copy(alpha = .65f), style = MaterialTheme.typography.bodySmall)
        Card(colors = CardDefaults.cardColors(containerColor = brandGlassColor())) {
            Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = ink)
                    Text(if (uiLanguage.isEnglish) "Device backup" else uiLanguage.indic("उपकरणाचा बॅकअप", "डिवाइस बैकअप"), color = ink, fontWeight = FontWeight.SemiBold)
                }
                Text(if (uiLanguage.isEnglish) "JSON includes counters, records and session records. Restore replaces current data." else uiLanguage.indic("JSON मध्ये मोजण्या, नोंदी आणि सत्रांच्या नोंदी असतात. पुनर्संचयित केल्यास सध्याचा डेटा बदलतो.", "JSON में काउंटर, प्रविष्टियाँ और सत्र होते हैं। बहाल करने से वर्तमान डेटा बदल जाता है।"), color = ink.copy(alpha = .65f), style = MaterialTheme.typography.bodySmall)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(onClick = onExportBackup, enabled = !busy) {
                        Icon(Icons.Default.FileDownload, null); Spacer(Modifier.width(6.dp)); Text(if (uiLanguage.isEnglish) "Export backup file" else uiLanguage.indic("बॅकअप फाइल निर्यात", "बैकअप फ़ाइल निर्यात करें"))
                    }
                    OutlinedButton(onClick = onImportBackup, enabled = !busy) {
                        Icon(Icons.Default.FileUpload, null); Spacer(Modifier.width(6.dp)); Text(if (uiLanguage.isEnglish) "Restore from file" else uiLanguage.indic("फाइलमधून पुनर्संचयित", "फ़ाइल से बहाल करें"))
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = brandGlassColor())) {
            Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(if (uiLanguage.isEnglish) "Spreadsheet export" else "स्प्रेडशीट निर्यात", color = ink, fontWeight = FontWeight.SemiBold)
                Text(if (uiLanguage.isEnglish) "Export counter entries as CSV." else uiLanguage.indic("मोजणीच्या नोंदी CSV स्वरूपात निर्यात करा.", "गिनती के रिकॉर्ड CSV में निर्यात करें।"), color = ink.copy(alpha = .65f), style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = onExportCsv, enabled = !busy) {
                    Icon(Icons.Default.FileDownload, null); Spacer(Modifier.width(6.dp)); Text(if (uiLanguage.isEnglish) "Export CSV" else "CSV निर्यात")
                }
            }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        DrivePreviewCard(uiLanguage, onUpgrade)
        if (!message.isNullOrBlank()) {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
                Text(message, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(12.dp))
            }
        }
    }
}
