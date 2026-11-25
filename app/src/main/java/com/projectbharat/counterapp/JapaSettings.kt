package com.projectbharat.counterapp

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
internal fun JapaSettingsScreen(
    uiLanguage: UiLanguage, tap: Boolean, mala: Boolean, sound: Boolean,
    latinMantras: Boolean, onLanguage: (UiLanguage) -> Unit, onMantraScript: (Boolean) -> Unit, onTap: (Boolean) -> Unit, onMala: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit, onReminders: () -> Unit, onModels: () -> Unit,
    onBackup: () -> Unit, onHelp: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsGroup(if (uiLanguage.isEnglish) "Practice preferences" else uiLanguage.indic("जप प्राधान्ये", "जप की प्राथमिकताएँ")) {
            PreferenceSwitch(if (uiLanguage.isEnglish) "Tap vibration" else uiLanguage.indic("स्पर्श कंपन", "टैप पर कंपन"), tap, onTap)
            PreferenceSwitch(if (uiLanguage.isEnglish) "Mala completion vibration" else uiLanguage.indic("माळ पूर्ण झाल्यावर कंपन", "माला पूरी होने पर कंपन"), mala, onMala)
            PreferenceSwitch(if (uiLanguage.isEnglish) "Tap sound" else uiLanguage.indic("स्पर्श ध्वनी", "टैप की ध्वनि"), sound, onSound)
        }
        SettingsGroup(uiLanguage.text("Language", "भाषा", "भाषा")) {
            SettingsDropdown(if (uiLanguage.isEnglish) "App language" else uiLanguage.indic("अॅपची भाषा", "ऐप की भाषा"),
                UiLanguage.entries.map { it.label }, uiLanguage.ordinal) { onLanguage(UiLanguage.entries[it]) }
            SettingsDropdown(if (uiLanguage.isEnglish) "Mantra display script" else uiLanguage.indic("मंत्राची लिपी", "मंत्र की लिपि"),
                if (uiLanguage.isEnglish) listOf("Devanagari", "English script")
                else listOf("देवनागरी", uiLanguage.indic("इंग्रजी लिपी", "अंग्रेज़ी लिपि")),
                if (latinMantras) 1 else 0) { onMantraScript(it == 1) }
        }
        SettingsGroup(if (uiLanguage.isEnglish) "Reminders and data" else uiLanguage.indic("स्मरणपत्रे व डेटा", "अनुस्मारक और डेटा")) {
            SettingsLink(if (uiLanguage.isEnglish) "Reminders" else uiLanguage.indic("स्मरणपत्रे", "अनुस्मारक"), onReminders)
            SettingsLink(if (uiLanguage.isEnglish) "Offline voice models" else uiLanguage.indic("ऑफलाइन आवाज मॉडेल", "ऑफ़लाइन आवाज़ मॉडल"), onModels)
            SettingsLink(if (uiLanguage.isEnglish) "Backup & sync" else uiLanguage.indic("बॅकअप व सिंक", "बैकअप और सिंक"), onBackup)
        }
        SettingsGroup(if (uiLanguage.isEnglish) "Support" else uiLanguage.indic("मदत", "सहायता")) {
            SettingsLink(if (uiLanguage.isEnglish) "Help & About" else uiLanguage.indic("मदत व अॅपची माहिती", "सहायता और ऐप की जानकारी"), onHelp)
        }
    }
}

@Composable
private fun SettingsDropdown(title: String, options: List<String>, index: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelMedium)
                Text(options[index])
            }
            Icon(Icons.Default.ArrowDropDown, null)
        }
        DropdownMenu(expanded, { expanded = false }) {
            options.forEachIndexed { option, label ->
                DropdownMenuItem(text = { Text(label) }, onClick = {
                    expanded = false
                    onSelect(option)
                }, trailingIcon = { if (option == index) Icon(Icons.Default.Check, null) })
            }
        }
    }
}

@Composable
internal fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = brandGlassColor(), contentColor = MaterialTheme.colorScheme.onSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, brandGlassBorder())) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 6.dp))
            content()
        }
    }
}

@Composable
internal fun SettingsLink(title: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onClick).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Text("›", color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
internal fun PreferenceSwitch(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Switch(checked, onChange)
    }
}

@Composable
internal fun JapaHelpScreen(uiLanguage: UiLanguage) {
    val context = LocalContext.current
    val version = remember(context) {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsGroup(uiLanguage.text("Magic Count", "मॅजिक काउंट", "मैजिक काउंट")) {
            Text(if (uiLanguage.isEnglish) "Allow microphone access and install an offline model. You can always count by tapping."
                else uiLanguage.indic("मायक्रोफोनची परवानगी द्या व ऑफलाइन मॉडेल डाउनलोड करा. स्पर्श करून जप मोजता येतात.", "माइक्रोफ़ोन की अनुमति दें और ऑफ़लाइन मॉडल डाउनलोड करें। टैप करके भी जप गिन सकते हैं।"))
        }
        SettingsGroup(if (uiLanguage.isEnglish) "Data and privacy" else uiLanguage.indic("डेटा व गोपनीयता", "डेटा और गोपनीयता")) {
            Text(if (uiLanguage.isEnglish) "Voice is processed on the device. Export a backup before changing phones. Restoring a file replaces local records."
                else uiLanguage.indic("आवाजाची प्रक्रिया उपकरणावर होते. फोन बदलण्यापूर्वी बॅकअप घ्या. फाइल पुनर्संचयित केल्यावर स्थानिक नोंदी बदलतात.", "आवाज़ की प्रक्रिया डिवाइस पर होती है। फ़ोन बदलने से पहले बैकअप लें। फ़ाइल बहाल करने से स्थानीय रिकॉर्ड बदल जाते हैं।"))
        }
        Text(uiLanguage.text("Bhajame - Daily Japa & Bhakti", "भजमे - दैनिक जप आणि भक्ती", "भजमे - दैनिक जप और भक्ति") + " · $version", color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
internal fun DrivePreviewCard(uiLanguage: UiLanguage) {
    val context = LocalContext.current
    val debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    var preview by remember { mutableStateOf(false) }
    var state by remember { mutableIntStateOf(0) }
    var autoSync by remember { mutableStateOf(false) }
    SettingsGroup(if (uiLanguage.isEnglish) "Google Drive Cloud Sync · Pro" else "Google Drive क्लाउड सिंक · Pro") {
        Image(painterResource(R.drawable.ic_drive_brand), "Google Drive", Modifier.size(28.dp))
        Text(if (uiLanguage.isEnglish) "Private backup and restore on your Google Drive." else uiLanguage.indic("तुमच्या Google Drive वर खाजगी बॅकअप व पुनर्संचयित.", "अपने Google Drive में निजी बैकअप लें और बहाल करें।"))
        if (debuggable) {
            FilledTonalButton(onClick = { preview = true }) {
                Icon(Icons.Default.Lock, null)
                Spacer(Modifier.width(6.dp))
                Text(if (uiLanguage.isEnglish) "Upgrade to Pro" else uiLanguage.indic("Pro घ्या", "Pro लें"))
            }
        }
    }
    if (debuggable && preview) {
        val labels = if (uiLanguage.isEnglish) listOf("Upgrade", "Link account", "Connected", "Syncing", "Error", "Conflict")
            else listOf("अपग्रेड", uiLanguage.indic("खाते जोडा", "खाता जोड़ें"), uiLanguage.indic("जोडले आहे", "कनेक्ट है"), uiLanguage.indic("सिंक सुरू", "सिंक जारी है"), uiLanguage.indic("त्रुटी", "त्रुटि"), uiLanguage.indic("विरोध", "बैकअप टकराव"))
        Dialog(onDismissRequest = { preview = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                Column(Modifier.heightIn(max = 620.dp).verticalScroll(rememberScrollState()).padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Image(painterResource(R.drawable.ic_drive_brand), null, Modifier.size(32.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Japa Plus", style = MaterialTheme.typography.headlineSmall)
                            Text(if (uiLanguage.isEnglish) "Debug preview" else uiLanguage.indic("चाचणी पूर्वदृश्य", "परीक्षण पूर्वावलोकन"), style = MaterialTheme.typography.labelMedium)
                        }
                        FilledTonalIconButton(onClick = { preview = false }) {
                            Icon(Icons.Default.Close, if (uiLanguage.isEnglish) "Close" else uiLanguage.indic("बंद करा", "बंद करें"))
                        }
                    }
                    var menu by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { menu = true }) { Text(labels[state]) }
                        DropdownMenu(menu, { menu = false }) {
                            labels.forEachIndexed { index, label ->
                                DropdownMenuItem(text = { Text(label) }, onClick = { state = index; menu = false })
                            }
                        }
                    }
                    when (state) {
                        0 -> {
                            Text(if (uiLanguage.isEnglish) "Automatic private backups, Sync Now, and cloud restore." else uiLanguage.indic("स्वयंचलित खाजगी बॅकअप, सिंक व क्लाउड पुनर्संचयित.", "स्वचालित निजी बैकअप, सिंक और क्लाउड से बहाली।"))
                            Button(onClick = { state = 1 }) { Text(if (uiLanguage.isEnglish) "Preview account setup" else uiLanguage.indic("खाते सेटअप पहा", "खाता सेटअप देखें")) }
                        }
                        1 -> OutlinedButton(onClick = { state = 2 }) {
                            Image(painterResource(R.drawable.ic_google_brand), null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (uiLanguage.isEnglish) "Connect Google Account" else uiLanguage.indic("Google खाते जोडा", "Google खाता जोड़ें"))
                        }
                        else -> {
                            Text("preview@example.com")
                            TextButton(onClick = { state = 1; autoSync = false }) { Text(if (uiLanguage.isEnglish) "Disconnect" else uiLanguage.indic("खाते वेगळे करा", "खाता डिस्कनेक्ट करें")) }
                            PreferenceSwitch(if (uiLanguage.isEnglish) "Auto-Sync" else uiLanguage.indic("स्वयंचलित सिंक", "स्वचालित सिंक"), autoSync) { autoSync = it }
                            Text(if (uiLanguage.isEnglish) "Last synced · 21 Nov 2026, 10:30" else uiLanguage.indic("शेवटचा सिंक · २१ नोव्हेंबर २०२६, १०:३०", "अंतिम सिंक · २१ नवंबर २०२६, १०:३०"))
                            Button(onClick = { state = 3 }, enabled = state != 3) { Icon(Icons.Default.Sync, null); Spacer(Modifier.width(6.dp)); Text(if (uiLanguage.isEnglish) "Sync Now" else uiLanguage.indic("आता सिंक करा", "अभी सिंक करें")) }
                            OutlinedButton(onClick = { state = 4 }, enabled = state != 3) { Icon(Icons.Default.CloudDownload, null); Spacer(Modifier.width(6.dp)); Text(if (uiLanguage.isEnglish) "Restore from Cloud" else uiLanguage.indic("क्लाउडवरून पुनर्संचयित", "क्लाउड से बहाल करें")) }
                            if (state == 3) {
                                LinearProgressIndicator(Modifier.fillMaxWidth())
                                TextButton(onClick = { state = 2 }) { Text(if (uiLanguage.isEnglish) "Preview success" else uiLanguage.indic("यशस्वी स्थिती पहा", "सफल स्थिति देखें")) }
                            }
                            if (state == 4) Text(if (uiLanguage.isEnglish) "Cloud access needs authorization." else uiLanguage.indic("क्लाउड प्रवेशासाठी परवानगी आवश्यक आहे.", "क्लाउड एक्सेस के लिए अनुमति चाहिए।"))
                            if (state == 5) Text(if (uiLanguage.isEnglish) "Local and Drive backups both changed. Review both before continuing." else uiLanguage.indic("स्थानिक व Drive बॅकअप बदलले आहेत. पुढे जाण्यापूर्वी दोन्ही तपासा.", "स्थानीय और Drive बैकअप दोनों बदले हैं। आगे बढ़ने से पहले दोनों जाँचें।"))
                        }
                    }
                }
            }
        }
    }
}
