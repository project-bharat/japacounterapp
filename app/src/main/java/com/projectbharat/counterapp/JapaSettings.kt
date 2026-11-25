package com.projectbharat.counterapp

import android.content.Intent
import android.net.Uri
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    onBackup: () -> Unit, onHelp: () -> Unit, themeName: String, onThemes: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppScrollingHeader()
        SettingsGroup(if (uiLanguage.isEnglish) "Practice preferences" else uiLanguage.indic("जप प्राधान्ये", "जप की प्राथमिकताएँ"), Icons.Default.SelfImprovement) {
            PreferenceSwitch(if (uiLanguage.isEnglish) "Tap vibration" else uiLanguage.indic("स्पर्श कंपन", "टैप पर कंपन"), tap, onTap, Icons.Default.Vibration)
            PreferenceSwitch(if (uiLanguage.isEnglish) "Mala completion vibration" else uiLanguage.indic("माळ पूर्ण झाल्यावर कंपन", "माला पूरी होने पर कंपन"), mala, onMala, Icons.Default.NotificationsActive)
            PreferenceSwitch(if (uiLanguage.isEnglish) "Tap sound" else uiLanguage.indic("स्पर्श ध्वनी", "टैप की ध्वनि"), sound, onSound, Icons.Default.VolumeUp)
        }
        SettingsGroup(uiLanguage.text("Appearance", "देखावा", "दिखावट"), Icons.Default.Palette) {
            SettingsLink(uiLanguage.text("Themes", "थीम", "थीम") + " · " + themeName, onThemes, Icons.Default.Wallpaper)
        }
        SettingsGroup(uiLanguage.text("Language", "भाषा", "भाषा"), Icons.Default.Translate) {
            SettingsDropdown(if (uiLanguage.isEnglish) "App language" else uiLanguage.indic("अॅपची भाषा", "ऐप की भाषा"),
                UiLanguage.entries.map { it.label }, uiLanguage.ordinal) { onLanguage(UiLanguage.entries[it]) }
            SettingsDropdown(if (uiLanguage.isEnglish) "Mantra display script" else uiLanguage.indic("मंत्राची लिपी", "मंत्र की लिपि"),
                if (uiLanguage.isEnglish) listOf("Devanagari", "English script")
                else listOf("देवनागरी", uiLanguage.indic("इंग्रजी लिपी", "अंग्रेज़ी लिपि")),
                if (latinMantras) 1 else 0) { onMantraScript(it == 1) }
        }
        SettingsGroup(if (uiLanguage.isEnglish) "Reminders and data" else uiLanguage.indic("स्मरणपत्रे व डेटा", "अनुस्मारक और डेटा"), Icons.Default.CloudQueue) {
            SettingsLink(if (uiLanguage.isEnglish) "Reminders" else uiLanguage.indic("स्मरणपत्रे", "अनुस्मारक"), onReminders, Icons.Default.Notifications)
            AiVoiceDetectorStatus(uiLanguage, onModels)
            SettingsLink(if (uiLanguage.isEnglish) "Backup & sync" else uiLanguage.indic("बॅकअप व सिंक", "बैकअप और सिंक"), onBackup, Icons.Default.Backup)
        }
        SettingsGroup(if (uiLanguage.isEnglish) "Support" else uiLanguage.indic("मदत", "सहायता"), Icons.Default.FavoriteBorder) {
            SettingsLink(if (uiLanguage.isEnglish) "Help & About" else uiLanguage.indic("मदत व अॅपची माहिती", "सहायता और ऐप की जानकारी"), onHelp, Icons.Default.Info)
        }
        DeveloperCard(uiLanguage)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SettingsDropdown(title: String, options: List<String>, index: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x663D3963), contentColor = Color(0xFFD6EBEE)),
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
internal fun SettingsGroup(title: String, icon: ImageVector? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = brandGlassColor(), contentColor = MaterialTheme.colorScheme.onSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, brandGlassBorder())) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (icon != null) Box(Modifier.size(34.dp).background(Color(0x554F7191), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(19.dp), tint = Color(0xFFD3EAED))
                }
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}

@Composable
internal fun SettingsLink(title: String, onClick: () -> Unit, icon: ImageVector? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp).background(Color(0x443D3963), RoundedCornerShape(50))
        .heightIn(min = 52.dp).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (icon != null) Icon(icon, null, Modifier.size(20.dp), tint = Color(0xFFB7D1D9))
        Text(title, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun PreferenceSwitch(title: String, checked: Boolean, onChange: (Boolean) -> Unit, icon: ImageVector? = null) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (icon != null) Icon(icon, null, Modifier.size(20.dp), tint = Color(0xFFB7D1D9))
        Text(title, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
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
        DeveloperCard(uiLanguage)
    }
}

@Composable
internal fun AiVoiceDetectorStatus(language: UiLanguage, onRetry: () -> Unit) {
    val context = LocalContext.current
    var installed by remember { mutableStateOf(false) }
    var work by remember { mutableStateOf<WorkInfo?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            val state = withContext(Dispatchers.IO) {
                VoskOfflineModelInstaller.isInstalled(context, "hi") to
                    runCatching { WorkManager.getInstance(context).getWorkInfosByTag("vosk_hi").get().lastOrNull() }.getOrNull()
            }
            installed = state.first
            work = state.second
            delay(2000)
        }
    }
    val info = work
    val progress = info?.progress?.getInt("progress", 0) ?: 0
    val downloading = info?.state == WorkInfo.State.RUNNING || info?.state == WorkInfo.State.ENQUEUED
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.Mic, null, Modifier.size(20.dp), tint = Color(0xFFB7D1D9))
            Text(language.text("AI Voice Detector", "एआय आवाज ओळख", "एआई आवाज़ पहचान"), color = MaterialTheme.colorScheme.onSurface)
        }
        val label = when {
            installed -> language.text("Ready", "तयार", "तैयार")
            downloading && progress > 0 -> language.text("Downloading · $progress%", "डाउनलोड होत आहे · $progress%", "डाउनलोड हो रहा है · $progress%")
            downloading -> language.text("Preparing download…", "डाउनलोड तयार होत आहे…", "डाउनलोड तैयार हो रहा है…")
            else -> language.text("Model not ready", "मॉडेल तयार नाही", "मॉडल तैयार नहीं है")
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (downloading && progress > 0) LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
        if (!installed && !downloading) TextButton(onClick = onRetry) { Text(language.text("Download", "डाउनलोड करा", "डाउनलोड करें")) }
    }
}

@Composable
internal fun DeveloperCard(language: UiLanguage) {
    val context = LocalContext.current
    fun open(uri: String) { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri))) } }
    val contrast = MaterialTheme.typography.bodySmall.copy(shadow = Shadow(Color(0xFF17132D), Offset(1f, 2f), 7f))
    Column(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(language.text("Designed & developed by", "संकल्पना व विकास", "परिकल्पना और विकास"), color = Color(0xFFFEF5E6), style = contrast)
        Text("Bharat Rasve", color = Color(0xFFFEF5E6), style = MaterialTheme.typography.titleMedium.copy(shadow = contrast.shadow), fontWeight = FontWeight.Bold)
        Text("+91 72188 38122", color = Color(0xFFFEF5E6), style = contrast)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(Triple(R.drawable.ic_social_whatsapp, "WhatsApp", "https://wa.me/917218838122"),
                Triple(R.drawable.ic_social_linkedin, "LinkedIn", "https://www.linkedin.com/in/bharatrasve"),
                Triple(R.drawable.ic_social_instagram, "Instagram", "https://www.instagram.com/bharat_rasve_?r=nametag")).forEach { (icon, label, url) ->
                IconButton(onClick = { open(url) }, modifier = Modifier.size(48.dp).background(Color(0xAA282442), CircleShape)) {
                    Icon(painterResource(icon), contentDescription = label, modifier = Modifier.size(22.dp), tint = Color(0xFFFEF5E6))
                }
            }
        }
    }
}

@Composable
internal fun DrivePreviewCard(uiLanguage: UiLanguage, onUpgrade: () -> Unit) {
    SettingsGroup(uiLanguage.text("Google Drive Cloud Sync · Pro", "Google Drive क्लाउड सिंक · Pro", "Google Drive क्लाउड सिंक · Pro")) {
        Image(painterResource(R.drawable.ic_drive_brand), "Google Drive", Modifier.size(28.dp))
        Text(uiLanguage.text("Private backup and restore are planned for Pro.", "खाजगी बॅकअप व पुनर्संचयित Pro साठी नियोजित आहे.", "निजी बैकअप और बहाली Pro के लिए नियोजित हैं."), color = MaterialTheme.colorScheme.onSurface)
        FilledTonalButton(onClick = onUpgrade) { Text(uiLanguage.text("Explore Pro", "Pro पहा", "Pro देखें")) }
    }
}

@Composable
internal fun JapaProScreen(language: UiLanguage) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Bhajame Pro", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Text(language.text("Plan preview · purchases are not available yet", "योजनांचा पूर्वावलोकन · खरेदी अद्याप उपलब्ध नाही", "योजनाओं का पूर्वावलोकन · खरीदारी अभी उपलब्ध नहीं है"), color = MaterialTheme.colorScheme.onBackground)
        listOf(language.text("Monthly", "मासिक", "मासिक"), language.text("Yearly", "वार्षिक", "वार्षिक"), language.text("Lifetime", "आजीवन", "आजीवन")).forEach { name ->
            SettingsGroup(name) { Text(language.text("Pricing and availability to be announced", "किंमत व उपलब्धता नंतर जाहीर होईल", "कीमत और उपलब्धता बाद में बताई जाएगी"), color = MaterialTheme.colorScheme.onSurface) }
        }
        Text(language.text("Google Drive backup and Play Billing are not connected in this preview.", "या पूर्वावलोकनात Google Drive बॅकअप व Play Billing जोडलेले नाहीत.", "इस पूर्वावलोकन में Google Drive बैकअप और Play Billing जुड़े नहीं हैं।"), color = MaterialTheme.colorScheme.onBackground)
    }
}
