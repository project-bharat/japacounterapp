package com.projectbharat.counterapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.AutoAwesome
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Switch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.content.pm.PackageManager
import android.Manifest
import androidx.compose.foundation.Canvas
import android.content.IntentFilter
import android.os.Build
import java.text.NumberFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.projectbharat.counterapp.data.DayTotal
import com.projectbharat.counterapp.data.CounterLog
import com.projectbharat.counterapp.data.JapaGoal
import com.projectbharat.counterapp.data.japaGoalOn
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import java.time.LocalDate

private fun registerTrackingReceiver(context: Context, receiver: BroadcastReceiver) {
    val filter = IntentFilter(TRACKING_STATE_CHANGED)
    if (Build.VERSION.SDK_INT >= 33) {
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        context.registerReceiver(receiver, filter)
    }
}

@Composable
private fun ProgressGradientCard(
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    content: @Composable () -> Unit
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    val colors = if (dark) listOf(Color(0xD64B456D), Color(0xD1282442))
        else listOf(Color(0xDEFFF9ED), Color(0xCBEADCC1))
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .09f))
    ) {
        Box(Modifier.background(Brush.linearGradient(colors)).padding(15.dp)) { content() }
    }
}

@Composable
private fun StepMetricCard(label: String, value: String, note: String, ink: Color, modifier: Modifier = Modifier) {
    ProgressGradientCard(modifier) {
        Column(Modifier.fillMaxWidth().heightIn(min = 86.dp)) {
            Text(label, color = ink.copy(alpha = .76f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(value, color = ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(note, color = ink.copy(alpha = .75f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

private data class ChantPreset(val title: String, val devanagari: String, val latin: String, val deity: String = "Other")

private val chantPresets = listOf(
    ChantPreset("Ram", "राम", "Ram", "Ram"),
    ChantPreset("Sri Rama Ram Jai Jai Ram", "श्रीराम राम जय जय राम श्रीराम जय राम जय जय राम", "Sri Rama Ram Jai Jai Ram Sri Rama Jai Ram Jai Jai Ram", "Ram"),
    ChantPreset("Jai Jai Pandurang Hari", "जय जय पांडुरंग हरी", "Jai Jai Pandurang Hari", "Vishnu"),
    ChantPreset("Jai Shri Ram", "जय श्रीराम", "Jai Shri Ram", "Ram"),
    ChantPreset("Om Namah Shivaya", "ॐ नमः शिवाय", "Om Namah Shivaya", "Shiva"),
    ChantPreset("Maha Mrityunjaya Mantra", "ॐ त्र्यम्बकं यजामहे सुगन्धिं पुष्टिवर्धनम्। उर्वारुकमिव बन्धनान्मृत्योर्मुक्षीय मामृतात्॥", "Om Tryambakam Yajamahe Sugandhim Pushtivardhanam Urvarukamiva Bandhanan Mrityor Mukshiya Maamritat", "Shiva"),
    ChantPreset("Om Namo Bhagavate Vasudevaya", "ॐ नमो भगवते वासुदेवाय", "Om Namo Bhagavate Vasudevaya", "Krishna"),
    ChantPreset("Hare Krishna Mahamantra", "हरे कृष्ण हरे कृष्ण, कृष्ण कृष्ण हरे हरे। हरे राम हरे राम, राम राम हरे हरे॥", "Hare Krishna Hare Krishna Krishna Krishna Hare Hare Hare Rama Hare Rama Rama Rama Hare Hare", "Krishna"),
    ChantPreset("Om Gam Ganapataye Namaha", "ॐ गं गणपतये नमः", "Om Gam Ganapataye Namaha", "Ganesh"),
    ChantPreset("Gayatri Mantra", "ॐ भूर्भुवः स्वः तत्सवितुर्वरेण्यं भर्गो देवस्य धीमहि धियो यो नः प्रचोदयात्॥", "Om Bhur Bhuvah Svah Tat Savitur Varenyam Bhargo Devasya Dhimahi Dhiyo Yo Nah Prachodayat", "Devi"),
    ChantPreset("Shri Ram Jai Ram", "श्री राम जय राम जय जय राम", "Shri Ram Jai Ram Jai Jai Ram", "Ram"),
    ChantPreset("Om Hanumate Namah", "ॐ हनुमते नमः", "Om Hanumate Namah", "Hanuman"),
    ChantPreset("Guru Mantra", "गुरुर्ब्रह्मा गुरुर्विष्णुः गुरुर्देवो महेश्वरः। गुरुः साक्षात् परब्रह्म तस्मै श्रीगुरवे नमः॥", "Gurur Brahma Gurur Vishnuh Gurur Devo Maheshwarah Guruh Sakshat Parabrahma Tasmai Shri Gurave Namah", "Guru"),
    ChantPreset("Om (Pranava)", "ॐ", "Om", "Other"),
    ChantPreset("Ram Krishna Hari", "राम कृष्ण हरी ॥", "Ram Krishna Hari", "Vishnu"),
    ChantPreset("Om Namo Narayanaya", "ॐ नमो नारायणाय ॥", "Om Namo Narayanaya", "Vishnu"),
    ChantPreset("Digambara Digambara", "दिगंबरा दिगंबरा श्रीपाद वल्लभ दिगंबरा ॥", "Digambara Digambara Shripada Vallabha Digambara", "Dattatreya"),
    ChantPreset("Om Aim Saraswatyai Namah", "ॐ ऐं सरस्वत्यै नमः ॥", "Om Aim Saraswatyai Namah", "Devi"),
    ChantPreset("Om Mahalakshmyai Namah", "ॐ महालक्ष्म्यै नमः ॥", "Om Mahalakshmyai Namah", "Devi"),
    ChantPreset("Vakratunda Mahakaya", "वक्रतुण्ड महाकाय सूर्यकोटि समप्रभ । निर्विघ्नं कुरु मे देव सर्वकार्येषु सर्वदा ॥", "Vakratunda Mahakaya Suryakoti Samaprabha Nirvighnam Kuru Me Deva Sarvakaryeshu Sarvada", "Ganesh"),
    ChantPreset("Pandurang Hari", "पांडुरंग हरी", "Pandurang Hari", "Vishnu"),
    ChantPreset("Pandurang", "पांडुरंग", "Pandurang", "Vishnu")
)

internal fun displayMantra(context: Context, categoryId: Long, target: String, latin: Boolean): String {
    val custom = context.getSharedPreferences("chant_settings", Context.MODE_PRIVATE)
        .getStringSet("custom_mantras_$categoryId", emptySet()).orEmpty()
    val preset = chantPresets.firstOrNull { it.devanagari == target || it.latin.equals(target, ignoreCase = true) }
    val pair = custom.asSequence().map { it.split('\u001F', limit = 2) }
        .firstOrNull { it.size == 2 && (it[0] == target || it[1].equals(target, ignoreCase = true)) }
    return if (latin) pair?.get(1) ?: preset?.latin ?: target
        else pair?.get(0) ?: preset?.devanagari ?: target
}

@Composable
fun AutoChantCounterScreen(
    categoryId: Long,
    count: Int,
    totalCount: Int,
    uiLanguage: UiLanguage,
    ink: Color,
    dailyGoal: Int,
    dayTotal: Int,
    sessionCount: Int,
    latinMantras: Boolean,
    mantraLibrary: Boolean,
    onLibraryChange: (Boolean) -> Unit,
    onEditGoal: () -> Unit,
    onFinish: (() -> Unit)?,
    onMantraChange: (String) -> Unit,
    onManualCount: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("chant_settings", Context.MODE_PRIVATE) }
    val legacyLatinKey = remember(categoryId) {
        val initial = prefs.getString("target_$categoryId", null)
        initial != null && initial.none { it in '\u0900'..'\u097F' }
    }
    var target by remember(categoryId) { mutableStateOf(prefs.getString("target_$categoryId", "राम") ?: "राम") }
    var automatic by remember { mutableStateOf(context.getSharedPreferences("tracking_state", Context.MODE_PRIVATE).getBoolean("chant_enabled", false)) }
    var animationEvent by remember { mutableIntStateOf(0) }
    var animationFromVoice by remember { mutableStateOf(false) }
    val flyingProgress = remember { Animatable(1f) }
    LaunchedEffect(animationEvent) {
        if (animationEvent > 0) {
            flyingProgress.snapTo(0f)
            flyingProgress.animateTo(1f, tween(650))
        }
    }
    var running by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var pendingMantra by remember(categoryId) { mutableStateOf<ChantPreset?>(null) }
    var selectedDeity by remember(categoryId) { mutableStateOf("All") }
    var query by rememberSaveable(categoryId) { mutableStateOf("") }
    var collection by rememberSaveable(categoryId) { mutableStateOf(0) }
    var favourites by remember(categoryId) {
        mutableStateOf(prefs.getStringSet("favourites_$categoryId", emptySet()).orEmpty().toSet())
    }
    var recent by remember(categoryId) {
        mutableStateOf(prefs.getString("recent_$categoryId", "").orEmpty().split('\u001F').filter(String::isNotBlank))
    }
    fun rememberMantra(mantra: String) {
        recent = (listOf(mantra) + recent.filterNot { it == mantra }).take(8)
        prefs.edit().putString("recent_$categoryId", recent.joinToString("\u001F")).apply()
    }
    var addMantraDialog by remember { mutableStateOf(false) }
    var newMantraMr by remember { mutableStateOf("") }
    var newMantraEn by remember { mutableStateOf("") }
    var customMantras by remember(categoryId) {
        mutableStateOf(prefs.getStringSet("custom_mantras_$categoryId", emptySet()).orEmpty().map { saved ->
            val parts = saved.split("\u001F", limit = 2)
            if (parts.size == 2) ChantPreset(parts[1], parts[0], parts[1])
            else ChantPreset(saved, saved, saved)
        })
    }

    val stopRecognition = {
        ChantRecognitionService.stop(context)
        running = false
        automatic = false
        recognizedText = ""
        status = if (uiLanguage.isEnglish) "Voice paused" else uiLanguage.indic("आवाज मोजणी थांबवली", "आवाज़ से गिनती रुकी है")
    }
    val startService = {
        if (target.isNotBlank()) {
            prefs.edit().putString("target_$categoryId", target.trim()).apply()
            try {
                ChantRecognitionService.start(context, categoryId, target.trim())
                automatic = true
                running = context.getSharedPreferences("tracking_state", Context.MODE_PRIVATE).getBoolean("chant_audio_active", false)
                status = if (uiLanguage.isEnglish) "Preparing offline model…" else uiLanguage.indic("ऑफलाइन मॉडेल तयार होत आहे…", "ऑफ़लाइन मॉडल तैयार हो रहा है…")
            } catch (error: Exception) {
                running = false
                automatic = false
                status = when {
                    error is SecurityException -> if (uiLanguage.isEnglish) "Microphone permission required" else uiLanguage.indic("मायक्रोफोन परवानगी आवश्यक", "माइक्रोफ़ोन की अनुमति चाहिए")
                    error.message?.contains("busy", true) == true -> if (uiLanguage.isEnglish) "Microphone busy — another app or call is using it" else uiLanguage.indic("मायक्रोफोन व्यस्त — दुसरे अॅप किंवा कॉल वापरत आहे", "माइक्रोफ़ोन व्यस्त है, कोई दूसरा ऐप या कॉल इसका इस्तेमाल कर रहा है")
                    else -> error.message ?: if (uiLanguage.isEnglish) "Could not start" else uiLanguage.indic("सुरू झाले नाही", "शुरू नहीं हो सका")
                }
            }
        }
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startService() else { automatic = false; status = if (uiLanguage.isEnglish) "Notification permission denied" else uiLanguage.indic("सूचना परवानगी नाकारली", "सूचनाओं की अनुमति नहीं मिली") }
    }
    val microphonePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            automatic = false
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            else startService()
        } else { automatic = false; running = false; status = if (uiLanguage.isEnglish) "Microphone permission denied" else uiLanguage.indic("मायक्रोफोन परवानगी नाकारली", "माइक्रोफ़ोन की अनुमति नहीं मिली") }
    }
    DisposableEffect(context, categoryId, uiLanguage) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.getStringExtra(EXTRA_TRACKING_KIND) != "chant") return
                if (intent.hasExtra("active")) running = intent.getBooleanExtra("active", false)
                intent.getStringExtra("message")?.let {
                    status = when {
                        it == "Stopped" -> {
                            automatic = false
                            if (uiLanguage.isEnglish) "Voice paused" else uiLanguage.indic("आवाज मोजणी थांबवली", "आवाज़ से गिनती रुकी है")
                        }
                        it.contains("Microphone busy", true) || it.contains("मायक्रोफोन व्यस्त") ->
                            if (uiLanguage.isEnglish) "Microphone busy. You can still count by tapping." else uiLanguage.indic("मायक्रोफोन व्यस्त आहे. स्पर्श करून जप मोजता येतील.", "माइक्रोफ़ोन व्यस्त है। टैप करके जप गिन सकते हैं।")
                        it.contains("Microphone unavailable", true) || it.contains("मायक्रोफोन उपलब्ध नाही") ->
                            if (uiLanguage.isEnglish) "Microphone unavailable. You can still count by tapping." else uiLanguage.indic("मायक्रोफोन उपलब्ध नाही. स्पर्श करून जप मोजता येतील.", "माइक्रोफ़ोन उपलब्ध नहीं है। टैप करके जप गिन सकते हैं।")
                        else -> it
                    }
                    if (it.contains("Microphone busy", true) || it.contains("Microphone unavailable", true) ||
                        it.contains("microphone permission", true) || it.contains("मायक्रोफोन व्यस्त") ||
                        it.contains("मायक्रोफोन उपलब्ध नाही") || it.contains("परवानगी आवश्यक")) {
                        automatic = false
                    }
                }
                intent.getStringExtra("transcript")?.let { recognizedText = it.trim().takeLast(160) }
                if (intent.getIntExtra("delta", 0) > 0) {
                    animationFromVoice = true
                    animationEvent++
                }
            }
        }
        registerTrackingReceiver(context, receiver)
        onDispose { context.unregisterReceiver(receiver) }
    }
    LaunchedEffect(categoryId, uiLanguage, automatic) {
        var lastRetryAt = 0L
        while (true) {
            val active = context.getSharedPreferences("tracking_state", Context.MODE_PRIVATE).getBoolean("chant_audio_active", false)
            running = active
            val recoverable = status.contains("reconnecting", true) || status.contains("stopped", true) || status.contains("error", true)
            if (automatic && !active && recoverable && System.currentTimeMillis() - lastRetryAt >= 15000L) {
                lastRetryAt = System.currentTimeMillis()
                status = if (uiLanguage.isEnglish) "Recognition reconnecting…" else uiLanguage.indic("आवाज ओळख पुन्हा जोडत आहे…", "आवाज़ की पहचान फिर से शुरू हो रही है…")
                startService()
            }
            delay(1500)
        }
    }

    if (!mantraLibrary) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 4.dp, end = 4.dp, top = 20.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.bilan_header_logo_light),
                    "Bhajame", Modifier.width(128.dp).height(40.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.weight(1f))
                val magicCountLabel = uiLanguage.text("Magic Count", "मॅजिक काउंट", "मैजिक काउंट")
                Row(Modifier.background(Color(0x663D3963), RoundedCornerShape(50)).border(1.dp, Color(0x6694B5C2), RoundedCornerShape(50)).padding(start = 10.dp, end = 4.dp), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(magicCountLabel, style = MaterialTheme.typography.labelLarge, color = Color(0xFFFEF5E6))
                    Switch(
                        checked = automatic,
                        thumbContent = { Icon(Icons.Default.AutoAwesome, null, Modifier.size(15.dp), tint = Color(0xFF423C6D)) },
                        modifier = Modifier.semantics { contentDescription = magicCountLabel },
                        onCheckedChange = { enabled ->
                            if (!enabled) stopRecognition()
                            else {
                                automatic = false
                                recognizedText = ""
                                status = uiLanguage.text("Starting Magic Count…", "मॅजिक काउंट सुरू होत आहे…", "मैजिक काउंट शुरू हो रहा है…")
                                running = false
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                                    microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                                else if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                else startService()
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(22.dp))
            val malaProgress = if (totalCount > 0 && totalCount % 108 == 0) 108 else totalCount % 108
            val malaFraction = malaProgress / 108f
            val beadTint by animateColorAsState(if (malaProgress == 108) Color(0xFFEADCC1) else Color(0xFF94B5C2), label = "beadCountTint")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Surface(Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = Color(0xFF282442).copy(alpha = .52f)) {
                    Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF94B5C2).copy(alpha = .22f), Color(0xFF423C6D).copy(alpha = .08f)))).padding(vertical = 10.dp, horizontal = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(totalCount.toString(), color = Color(0xFFCDE5E9), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        Text(if (uiLanguage.isEnglish) "LIFETIME" else uiLanguage.indic("आजवरचे जप", "अब तक के जप"), color = Color(0xFFCDE5E9), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                Surface(Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = Color(0xFF282442).copy(alpha = .52f)) {
                    Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF94B5C2).copy(alpha = .22f), Color(0xFF423C6D).copy(alpha = .08f)))).padding(vertical = 10.dp, horizontal = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$malaProgress/108", color = Color(0xFFCDE5E9), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        Text(if (uiLanguage.isEnglish) "MALA PROGRESS" else uiLanguage.indic("माळ प्रगती", "माला की प्रगति"), color = Color(0xFFCDE5E9), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                Surface(Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = Color(0xFF282442).copy(alpha = .52f)) {
                    Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF94B5C2).copy(alpha = .22f), Color(0xFF423C6D).copy(alpha = .08f)))).padding(vertical = 10.dp, horizontal = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text((totalCount / 108).toString(), color = Color(0xFFCDE5E9), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        Text(if (uiLanguage.isEnglish) "MALAS" else uiLanguage.indic("पूर्ण माळा", "पूरी मालाएँ"), color = Color(0xFFCDE5E9), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
            Text(displayMantra(context, categoryId, target, latinMantras), color = Color(0xFFFEF5E6), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp))
            val manualInteraction = remember { MutableInteractionSource() }
            val manualPressed by manualInteraction.collectIsPressedAsState()
            val manualScale by animateFloatAsState(if (manualPressed) 0.94f else 1f, label = "japaTapScale")
            val circleAccent by animateColorAsState(if (count > 0 && count % 108 == 0) Color(0xFFEADCC1) else Color(0xFF94B5C2), label = "japaCircleAccent")
            val circleSize = minOf(280, (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp - 72).coerceAtLeast(180)).dp
            Box(Modifier.padding(top = 6.dp).size(circleSize), contentAlignment = Alignment.Center) {
                val pulse = if (animationEvent > 0) (1f - flyingProgress.value) else 0f
                if (animationEvent > 0 && flyingProgress.value < 1f) Canvas(Modifier.fillMaxSize().graphicsLayer {
                    scaleX = 1f + flyingProgress.value * .30f; scaleY = scaleX
                }) { drawCircle(Color(0xFFB7D1D9).copy(alpha = (1f - flyingProgress.value) * .55f), style = Stroke(width = 2.dp.toPx())) }
                Box(Modifier.fillMaxSize().graphicsLayer(scaleX = 1f + pulse * .18f, scaleY = 1f + pulse * .18f)
                    .background(Brush.radialGradient(listOf(Color(0xFFB9D9E2).copy(alpha = .5f * pulse), Color.Transparent)), CircleShape))
                Canvas(Modifier.size(circleSize)) {
                    val width = 14.dp.toPx()
                    val inset = width / 2
                    val area = Size(size.width - width, size.height - width)
                    drawArc(Color(0xFFE8E1D7), -90f, 360f, false, topLeft = Offset(inset, inset), size = area,
                        style = Stroke(width = width))
                    if (malaFraction > 0f) drawArc(beadTint, -90f, 360f * malaFraction, false,
                        topLeft = Offset(inset, inset), size = area, style = Stroke(width = width))
                }
                Box(
                    Modifier.size(circleSize - 25.dp)
                        .graphicsLayer(scaleX = manualScale, scaleY = manualScale)
                        .shadow(12.dp, CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFFEF5D8), Color(0xFFEADCC1))), CircleShape)
                        .border(3.dp, circleAccent, CircleShape)
                        .clickable(interactionSource = manualInteraction, indication = null, onClick = {
                            onManualCount(target)
                            animationFromVoice = false
                            animationEvent++
                        }),
                    contentAlignment = Alignment.Center
                ) {
                val mandalaTransition = rememberInfiniteTransition(label = "mandalaRotation")
                val mandalaAngle by mandalaTransition.animateFloat(0f, 360f,
                    androidx.compose.animation.core.infiniteRepeatable(tween(90000, easing = androidx.compose.animation.core.LinearEasing)), label = "mandalaAngle")
                Canvas(Modifier.fillMaxSize().graphicsLayer { rotationZ = mandalaAngle }) {
                    val petalWidth = size.width * .25f
                    val petalHeight = size.height * .43f
                    val petalFill = Brush.linearGradient(listOf(
                        Color(0xFFDDBACD).copy(alpha = .23f),
                        Color(0xFFADC9D0).copy(alpha = .13f),
                        Color(0xFFEADCC1).copy(alpha = .04f)))
                    repeat(12) { petal ->
                        rotate(petal * 30f) {
                            drawOval(brush = petalFill,
                                topLeft = Offset((size.width - petalWidth) / 2f, size.height * .035f),
                                size = Size(petalWidth, petalHeight))
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SelfImprovement, null, tint = Color(0xFF7855A6), modifier = Modifier.size(42.dp))
                    Text((if (sessionCount > 0) sessionCount.toString() else "+1"), color = Color(0xFF38234F), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold)
                    Text(if (uiLanguage.isEnglish) "TAP TO COUNT" else uiLanguage.indic("स्पर्श करून मोजा", "गिनने के लिए टैप करें"), color = Color(0xFF38234F), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
                if (animationEvent > 0 && flyingProgress.value < 1f) {
                    val progress = flyingProgress.value
                    val travel = with(androidx.compose.ui.platform.LocalDensity.current) { (circleSize / 2 + 45.dp).toPx() }
                    Text(displayMantra(context, categoryId, target, latinMantras),
                        modifier = Modifier.graphicsLayer {
                            translationY = (if (animationFromVoice) travel else -travel) * (1f - progress)
                            translationX = travel * .30f * kotlin.math.sin(progress * Math.PI).toFloat() * (if (animationEvent % 2 == 0) 1 else -1)
                            scaleX = 1f + .25f * kotlin.math.sin(progress * Math.PI).toFloat() - progress * .35f
                            scaleY = scaleX
                            alpha = (if (progress < .15f) progress / .15f else (1f - progress) / .85f).coerceIn(0f, 1f)
                        }.clearAndSetSemantics { },
                        maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                        color = androidx.compose.ui.graphics.lerp(if (animationEvent % 2 == 0) Color(0xFFD5E9F0) else Color(0xFFF3D9E4), Color(0xFF302B54), progress),
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
            if (!automatic && status.isNotBlank() &&
                !status.contains("paused", true) && !status.contains("थांबवली") && !status.contains("रुकी")) {
                Text(status, color = Color(0xFFFEF5E6),
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())
            }
            if (automatic) {
                val microphonePulse = rememberInfiniteTransition(label = "microphonePulse")
                val microphoneScale by microphonePulse.animateFloat(
                    initialValue = 1f, targetValue = 1.18f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(tween(800), RepeatMode.Reverse),
                    label = "listeningScale")
                Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, null, tint = Color(0xFFC4FFCF),
                            modifier = Modifier.size(18.dp).graphicsLayer {
                                scaleX = if (running) microphoneScale else 1f
                                scaleY = if (running) microphoneScale else 1f
                            })
                        Text(if (running) uiLanguage.text("Listening", "ऐकत आहे", "सुन रहा है")
                            else uiLanguage.text("Preparing voice model…", "आवाज मॉडेल तयार होत आहे…", "आवाज़ मॉडल तैयार हो रहा है…"),
                            color = Color(0xFFC4FFCF), style = MaterialTheme.typography.labelMedium.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(Color(0xFF211E39), Offset(1f, 2f), 5f)))
                    }
                    if (recognizedText.isNotBlank()) {
                        val spokenMantra = displayMantra(context, categoryId, target, latin = false)
                        val displayed = if (latinMantras && spokenMantra.isNotBlank())
                            recognizedText.replace(spokenMantra, displayMantra(context, categoryId, target, latin = true))
                            else recognizedText
                        Text(displayed, modifier = Modifier.fillMaxWidth(), color = Color(0xFFD3EDFF),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(Color(0xFF211E39), Offset(1f, 2f), 5f)), textAlign = TextAlign.Center,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Spacer(Modifier.height((androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * .30f).dp))
            Column(Modifier.padding(top = 30.dp, bottom = 32.dp).widthIn(max = 290.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val glassButton = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xAA302B54), contentColor = Color(0xFFFEF5E6))
                val glassBorder = BorderStroke(1.dp, Color(0xAAFEF5E6))
                OutlinedButton(onClick = { pendingMantra = null; onLibraryChange(true) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
                    colors = glassButton, border = glassBorder) {
                    Icon(Icons.Default.Search, null)
                    Spacer(Modifier.width(8.dp))
                    Text(uiLanguage.text("Choose mantra", "मंत्र निवडा", "मंत्र चुनें"))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEditGoal, modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(8.dp),
                        colors = glassButton, border = glassBorder) {
                        Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(uiLanguage.text("Edit goal", "लक्ष्य बदला", "लक्ष्य बदलें"))
                    }
                    OutlinedButton(onClick = { onFinish?.invoke() }, enabled = onFinish != null,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xAA302B54),
                            contentColor = Color(0xFFFEF5E6), disabledContainerColor = Color(0x66302B54),
                            disabledContentColor = Color(0xFFBBB3C6)), border = glassBorder) {
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(uiLanguage.text("Finish", "सत्र पूर्ण करा", "पूरा करें"))
                    }
                }
            }
        }
    }
    if (mantraLibrary) {
        val groups = listOf("All", "Ram", "Krishna", "Shiva", "Ganesh", "Hanuman", "Devi", "Vishnu", "Dattatreya", "Guru", "Other")
        val groupsMr = listOf(uiLanguage.indic("सर्व", "सभी"), "राम", "कृष्ण", "शिव", "गणेश", "हनुमान", "देवी", "विष्णू", "दत्तात्रेय", "गुरु", uiLanguage.indic("इतर", "अन्य"))
        val visible = (chantPresets + customMantras).distinctBy { it.devanagari }.filter { item ->
            (selectedDeity == "All" || item.deity == selectedDeity) &&
                listOf(item.title, item.devanagari, item.latin).any { it.contains(query.trim(), ignoreCase = true) } &&
                when (collection) {
                    1 -> item.devanagari in recent
                    2 -> item.devanagari in favourites
                    else -> true
                }
        }.let { items -> if (collection == 1) items.sortedBy { recent.indexOf(it.devanagari) } else items }
        val selection = pendingMantra
        Column(Modifier.fillMaxSize().imePadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(query, { query = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                label = { Text(uiLanguage.text("Search mantras", "मंत्र शोधा", "मंत्र खोजें")) })
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val labels = listOf(uiLanguage.text("All", "सर्व", "सभी"), uiLanguage.text("Recent", "अलीकडील", "हाल के"),
                    uiLanguage.text("Favourites", "आवडते", "पसंदीदा"))
                labels.forEachIndexed { index, label ->
                    FilterChip(collection == index, { collection = index }, { Text(label) })
                }
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                groups.forEachIndexed { index, group ->
                    FilterChip(selectedDeity == group, { selectedDeity = group },
                        { Text(if (uiLanguage.isEnglish) group else groupsMr[index]) })
                }
            }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (visible.isEmpty()) item {
                    Text(uiLanguage.text("No matching mantras", "जुळणारे मंत्र नाहीत", "कोई मिलता-जुलता मंत्र नहीं है"))
                }
                items(visible, key = { it.devanagari }) { item ->
                    val selected = item == selection || (selection == null &&
                        (target == item.devanagari || target == item.latin))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .94f) else brandGlassColor(),
                        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f).clickable { pendingMantra = item }.padding(12.dp)) {
                                Text(if (latinMantras) item.latin else item.devanagari,
                                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium)
                            }
                            if (selected) Icon(Icons.Default.Check, uiLanguage.text("Selected", "निवडलेला", "चुना गया"),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            val favourite = item.devanagari in favourites
                            IconButton(onClick = {
                                favourites = if (favourite) favourites - item.devanagari else favourites + item.devanagari
                                prefs.edit().putStringSet("favourites_$categoryId", favourites).apply()
                            }) {
                                Icon(if (favourite) Icons.Default.Star else Icons.Default.StarBorder,
                                    if (favourite) uiLanguage.text("Remove favourite", "आवडीतून काढा", "पसंदीदा से हटाएँ")
                                    else uiLanguage.text("Add favourite", "आवडीत जोडा", "पसंदीदा में जोड़ें"),
                                    tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
            if (selection != null) {
                Text(if (latinMantras) selection.latin else selection.devanagari, style = MaterialTheme.typography.labelLarge,
                    maxLines = 2, color = MaterialTheme.colorScheme.onBackground)
            }
            Button(onClick = {
                if (selection != null) {
                    val chosen = if (legacyLatinKey) selection.latin else selection.devanagari
                    if (chosen != target) {
                        target = chosen
                        prefs.edit().putString("target_$categoryId", target).apply()
                        onMantraChange(target)
                        if (automatic) startService()
                    }
                    rememberMantra(selection.devanagari)
                }
                onLibraryChange(false)
            }, modifier = Modifier.widthIn(max = 260.dp).fillMaxWidth().align(Alignment.CenterHorizontally)) {
                Text(uiLanguage.text("Start Japa", "जप सुरू करा", "जप शुरू करें"))
            }
            TextButton(onClick = { newMantraMr = ""; newMantraEn = ""; addMantraDialog = true },
                modifier = Modifier.widthIn(max = 260.dp).fillMaxWidth().align(Alignment.CenterHorizontally)) {
                Text(uiLanguage.text("Add mantra", "मंत्र जोडा", "मंत्र जोड़ें"))
            }
        }
    }
    if (addMantraDialog) {
        AlertDialog(
            onDismissRequest = { addMantraDialog = false },
            title = { Text(if (uiLanguage.isEnglish) "Add mantra" else uiLanguage.indic("नवीन मंत्र", "नया मंत्र")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = newMantraMr, onValueChange = { newMantraMr = it },
                        label = { Text(uiLanguage.text("Mantra (Devanagari)", "मंत्र (देवनागरी)", "मंत्र (देवनागरी)")) }, singleLine = false)
                    OutlinedTextField(value = newMantraEn, onValueChange = { newMantraEn = it },
                        label = { Text(uiLanguage.text("Mantra (English script)", "मंत्र (इंग्रजी लिपी)", "मंत्र (अंग्रेज़ी लिपि)")) }, singleLine = false)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val devanagari = newMantraMr.trim()
                    val latin = newMantraEn.trim()
                    if (devanagari.isNotBlank() && latin.isNotBlank()) {
                        val added = ChantPreset(latin, devanagari, latin)
                        customMantras = (customMantras + added).distinctBy { it.devanagari }
                        val saved = customMantras.map { it.devanagari + "\u001F" + it.latin }.toSet()
                        prefs.edit().putStringSet("custom_mantras_$categoryId", saved).apply()
                        pendingMantra = added
                    }
                    addMantraDialog = false
                }, enabled = newMantraMr.isNotBlank() && newMantraEn.isNotBlank()) { Text(if (uiLanguage.isEnglish) "Add" else uiLanguage.indic("जोडा", "जोड़ें")) }
            },
            dismissButton = { TextButton(onClick = { addMantraDialog = false }) { Text(if (uiLanguage.isEnglish) "Cancel" else uiLanguage.indic("रद्द", "रद्द करें")) } }
        )
    }
}


@Composable
fun JapaReportScreen(
    logs: List<CounterLog>,
    goals: List<JapaGoal>,
    periodGoals: List<com.projectbharat.counterapp.data.JapaPeriodGoal>,
    today: LocalDate,
    uiLanguage: UiLanguage,
    ink: Color,
    mantraLabel: (String) -> String,
    onDaySelected: (LocalDate) -> Unit,
    onAchievements: () -> Unit,
    onEditGoal: () -> Unit
) {
    var monthValue by rememberSaveable { mutableStateOf(today.withDayOfMonth(1).toEpochDay()) }
    val month = LocalDate.ofEpochDay(monthValue)
    var period by rememberSaveable { mutableStateOf("WEEKLY") }
    var anchorValue by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    val anchor = LocalDate.ofEpochDay(anchorValue)
    var chartMode by rememberSaveable { mutableStateOf("BAR") }
    val totalsByDate = remember(logs) { logs.groupBy { it.date }.mapValues { (_, records) -> records.sumOf { it.count } } }
    val activeDates = remember(totalsByDate) { totalsByDate.filterValues { it > 0 }.keys.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet() }
    val total = logs.sumOf { it.count }
    val todayCount = totalsByDate[today.toString()] ?: 0
    val average = if (activeDates.isNotEmpty()) total / activeDates.size else 0
    val best = totalsByDate.maxByOrNull { it.value }
    val currentStreak = run {
        var cursor = if (today in activeDates) today else today.minusDays(1)
        var streak = 0
        while (cursor in activeDates) { streak++; cursor = cursor.minusDays(1) }
        streak
    }
    val goalStreak = run {
        fun reached(date: LocalDate) = (totalsByDate[date.toString()] ?: 0) >= japaGoalOn(goals, date)
        var cursor = if (reached(today)) today else today.minusDays(1)
        var streak = 0
        while (reached(cursor)) { streak++; cursor = cursor.minusDays(1) }
        streak
    }
    val longestStreak = run {
        var run = 0
        var bestRun = 0
        var previous: LocalDate? = null
        activeDates.sorted().forEach { date ->
            run = if (previous?.plusDays(1) == date) run + 1 else 1
            bestRun = maxOf(bestRun, run)
            previous = date
        }
        bestRun
    }
    val periodStart = if (period == "MONTHLY") anchor.withDayOfMonth(1) else anchor.minusDays((anchor.dayOfWeek.value - 1).toLong())
    val periodEnd = if (period == "MONTHLY") periodStart.plusMonths(1).minusDays(1) else periodStart.plusDays(6)
    val chartDays = when(period) {
        "MONTHLY" -> (1..periodStart.lengthOfMonth()).map { d -> val date=periodStart.withDayOfMonth(d); DayTotal(date.toString(), totalsByDate[date.toString()] ?: 0) }
        else -> (0..6).map { i -> val date=periodStart.plusDays(i.toLong()); DayTotal(date.toString(), totalsByDate[date.toString()] ?: 0) }
    }
    val periodSum = chartDays.sumOf { it.total }
    val monthDays = month.lengthOfMonth()
    val offset = (month.dayOfWeek.value + 6) % 7
    val monthCells: List<Int?> = (0 until 42).map { cell -> (cell - offset + 1).takeIf { it in 1..monthDays } }
    val mantraBreakdown = logs.groupBy { it.note.orEmpty().removePrefix("MANTRA:") }.mapValues { (_, rows) -> rows.sumOf { it.count } }.entries.sortedByDescending { it.value }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val context = LocalContext.current
        val target = japaGoalOn(goals, today)
        val remaining = (target - todayCount).coerceAtLeast(0)
        val recentWeek = (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong()).toString()
            DayTotal(date, totalsByDate[date] ?: 0)
        }
        var shareKind by rememberSaveable { mutableIntStateOf(0) }
        val goalReached = todayCount >= target
        ProgressGradientCard(Modifier.fillMaxWidth(), highlight = goalReached) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(uiLanguage.text("Today’s goal · All mantras", "आजचे लक्ष्य · सर्व मंत्र", "आज का लक्ष्य · सभी मंत्र"),
                    style = MaterialTheme.typography.titleMedium, color = ink)
                Text("$todayCount / $target", style = MaterialTheme.typography.headlineLarge, color = ink, fontWeight = FontWeight.Bold)
                Text(if (goalReached) uiLanguage.text("✓ Goal reached", "✓ लक्ष्य पूर्ण", "✓ लक्ष्य पूरा")
                    else uiLanguage.text("$remaining remaining", "$remaining बाकी", "$remaining बाकी"), color = ink)
                LinearProgressIndicator(progress = { (todayCount.toFloat() / target).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                Row {
                    TextButton(onClick = onEditGoal) { Text(uiLanguage.text("Edit goal", "लक्ष्य बदला", "लक्ष्य बदलें")) }
                    TextButton(onClick = { shareKind = 1 }) { Text(uiLanguage.text("Share", "शेअर करा", "साझा करें")) }
                }
            }
        }
        periodGoals.forEach { goal ->
            val prefix = if (goal.period == "MONTHLY") today.toString().take(7) else today.year.toString()
            val periodTotal = totalsByDate.filterKeys { it.startsWith(prefix) }.values.sum()
            ProgressGradientCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (goal.period == "MONTHLY") uiLanguage.text("Monthly goal", "मासिक लक्ष्य", "मासिक लक्ष्य") else uiLanguage.text("Yearly goal", "वार्षिक लक्ष्य", "वार्षिक लक्ष्य"), color = ink)
                    Text("$periodTotal / ${goal.target}", color = ink, style = MaterialTheme.typography.headlineSmall)
                    LinearProgressIndicator(progress = { (periodTotal.toFloat() / goal.target).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        val milestone = if (total < 108) 108 else if (total < 1080) 1080 else ((total.toLong() / 1080) + 1) * 1080
        val monthlyTotal = totalsByDate.filterKeys { it.startsWith(today.toString().take(7)) }.values.sum()
        ProgressGradientCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(uiLanguage.text("Personal milestone", "वैयक्तिक टप्पा", "व्यक्तिगत उपलब्धि"),
                    style = MaterialTheme.typography.titleMedium, color = ink)
                Text("$total / $milestone", style = MaterialTheme.typography.headlineMedium,
                    color = ink, fontWeight = FontWeight.Bold)
                Text(uiLanguage.text("Lifetime chants", "आजवरचे जप", "अब तक के जप"), color = ink)
                OutlinedButton(onClick = onAchievements) {
                    Text(uiLanguage.text("View achievements", "पदके पहा", "उपलब्धियाँ देखें"))
                }
                FilledTonalButton(onClick = { shareKind = 2 }) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(6.dp))
                    Text(uiLanguage.text("Monthly recap", "मासिक सारांश", "मासिक सारांश"))
                }
            }
        }
        if (shareKind != 0) {
            val title = if (shareKind == 1) { if (uiLanguage.isEnglish) "Daily Japa progress" else uiLanguage.indic("आजची जप प्रगती", "आज की जप प्रगति") }
                else if (uiLanguage.isEnglish) "Monthly Japa recap" else uiLanguage.indic("जपाचा मासिक सारांश", "जप का मासिक सारांश")
            val scopeText = if (uiLanguage.isEnglish) "All mantras" else uiLanguage.indic("सर्व मंत्र", "सभी मंत्र")
            val shareTimestamp = remember(shareKind) { System.currentTimeMillis() }
            val card = JapaImageCard(title, scopeText, if (shareKind == 1) todayCount else monthlyTotal,
                if (shareKind == 1) "$todayCount / $target" else today.toString().take(7), uiLanguage, shareTimestamp)
            val shareScope = androidx.compose.runtime.rememberCoroutineScope()
            AlertDialog(onDismissRequest = { shareKind = 0 }, title = { Text(title) },
                text = { JapaImagePreview(card) },
                confirmButton = { Button(onClick = { shareScope.launch { shareJapaImage(context, card) } }) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (uiLanguage.isEnglish) "Share image" else uiLanguage.indic("चित्र शेअर करा", "चित्र साझा करें"))
                } },
                dismissButton = { TextButton(onClick = { shareKind = 0 }) { Text(if (uiLanguage.isEnglish) "Close" else uiLanguage.indic("बंद करा", "बंद करें")) } })
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = brandGlassColor())) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (uiLanguage.isEnglish) "PROGRESS ANALYTICS" else uiLanguage.indic("जप प्रगती विश्लेषण", "जप प्रगति का विश्लेषण"), color = ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    SegmentedButtonRowCompat(chartMode, listOf("BAR","LINE"), { chartMode=it }, uiLanguage)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("WEEKLY","MONTHLY").forEach { p ->
                        FilterChip(selected=period==p,onClick={period=p;anchorValue=today.toEpochDay()},label={Text(if(uiLanguage.isEnglish)p else if(p=="WEEKLY") "साप्ताहिक" else "मासिक")})
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) {
                    IconButton(onClick={anchorValue=(if (period == "MONTHLY") anchor.minusMonths(1) else anchor.minusWeeks(1)).toEpochDay()}){Text("‹")}
                    Text(periodStart.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM", uiLanguage.locale))+" – "+periodEnd.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", uiLanguage.locale)),color=ink,fontWeight=FontWeight.Medium)
                    IconButton(onClick={anchorValue=(if (period == "MONTHLY") anchor.plusMonths(1) else anchor.plusWeeks(1)).coerceAtMost(today).toEpochDay()}){Text("›")}
                }
                var selectedDay by rememberSaveable(period, anchorValue) { mutableStateOf<String?>(null) }
                JapaActivityChart(chartDays, chartMode, Modifier.fillMaxWidth().height(190.dp)
                    .pointerInput(chartDays) {
                        detectTapGestures { point ->
                            val index = (point.x / size.width * chartDays.size).toInt().coerceIn(0, chartDays.lastIndex)
                            selectedDay = chartDays[index].date
                        }
                    }, ink, uiLanguage)
                selectedDay?.let { date ->
                    val value = totalsByDate[date] ?: 0
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("$date · $value", Modifier.weight(1f), color = ink)
                        TextButton(onClick = { runCatching { LocalDate.parse(date) }.getOrNull()?.let(onDaySelected) },
                            enabled = runCatching { !LocalDate.parse(date).isAfter(today) }.getOrDefault(false)) {
                            Text(uiLanguage.text("View day", "दिवस पहा", "दिन देखें"))
                        }
                    }
                }
                Surface(color=brandGlassColor(),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()) {
                    Text((if(uiLanguage.isEnglish)"Total: " else uiLanguage.indic("एकूण: ", "कुल: "))+NumberFormat.getIntegerInstance(uiLanguage.locale).format(periodSum)+(if(uiLanguage.isEnglish)" Japa" else " जप"),modifier=Modifier.padding(12.dp),color=ink,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
                }
            }
        }
        Text(today.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", uiLanguage.locale)), color = ink.copy(alpha = .6f), style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StepMetricCard(if (uiLanguage.isEnglish) "TOTAL JAPA" else uiLanguage.indic("एकूण जप", "कुल जप"), NumberFormat.getIntegerInstance(uiLanguage.locale).format(total), if (uiLanguage.isEnglish) "${total / 108} malas" else uiLanguage.indic("${total / 108} माळा", "${total / 108} माला"), ink, Modifier.weight(1f))
            StepMetricCard(if (uiLanguage.isEnglish) "TODAY" else "आज", NumberFormat.getIntegerInstance(uiLanguage.locale).format(todayCount), if (uiLanguage.isEnglish) "Japa count" else uiLanguage.indic("आजचा जप", "आज के जप"), ink, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StepMetricCard(if (uiLanguage.isEnglish) "PRACTICE STREAK" else uiLanguage.indic("जपाची सध्याची सलगता", "लगातार जप के दिन"), "$currentStreak", if (uiLanguage.isEnglish) "days" else uiLanguage.indic("दिवस", "दिन"), ink, Modifier.weight(1f))
            StepMetricCard(if (uiLanguage.isEnglish) "LONGEST STREAK" else uiLanguage.indic("सर्वाधिक सलगता", "सबसे लंबी निरंतरता"), "$longestStreak", if (uiLanguage.isEnglish) "days" else uiLanguage.indic("दिवस", "दिन"), ink, Modifier.weight(1f))
        }
        var moreStats by rememberSaveable { mutableStateOf(false) }
        TextButton(onClick = { moreStats = !moreStats }) {
            Text(if (moreStats) uiLanguage.text("Less stats", "कमी आकडे", "कम आँकड़े")
                else uiLanguage.text("More stats", "अधिक आकडे", "और आँकड़े"))
        }
        if (moreStats) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StepMetricCard(if (uiLanguage.isEnglish) "ACTIVE DAYS" else uiLanguage.indic("सक्रिय दिवस", "सक्रिय दिन"), "${activeDates.size}", if (uiLanguage.isEnglish) "All time" else uiLanguage.indic("एकूण", "कुल"), ink, Modifier.weight(1f))
            StepMetricCard(if (uiLanguage.isEnglish) "AVERAGE / DAY" else uiLanguage.indic("दररोज सरासरी", "दैनिक औसत"), NumberFormat.getIntegerInstance(uiLanguage.locale).format(average), if (uiLanguage.isEnglish) "Per active day" else uiLanguage.indic("सक्रिय दिवस", "सक्रिय दिन"), ink, Modifier.weight(1f))
        }
        if (moreStats) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StepMetricCard(if (uiLanguage.isEnglish) "BEST DAY" else uiLanguage.indic("सर्वोत्तम दिवस", "सबसे अच्छा दिन"), NumberFormat.getIntegerInstance(uiLanguage.locale).format(best?.value ?: 0), best?.key?.let { runCatching { LocalDate.parse(it).format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", uiLanguage.locale)) }.getOrDefault(it) } ?: "—", ink, Modifier.weight(1f))
            StepMetricCard(if (uiLanguage.isEnglish) "TOTAL MALAS" else uiLanguage.indic("पूर्ण माळा", "पूरी मालाएँ"), NumberFormat.getIntegerInstance(uiLanguage.locale).format(total / 108), if (uiLanguage.isEnglish) "108 japa each" else uiLanguage.indic("प्रत्येक माळ १०८", "प्रति माला १०८"), ink, Modifier.weight(1f))
        }
        Text(
            if (uiLanguage.isEnglish) "Goal streak · $goalStreak days" else uiLanguage.indic("लक्ष्याची सलगता · $goalStreak दिवस", "लगातार लक्ष्य पूरे · $goalStreak दिन"),
            color = ink, style = MaterialTheme.typography.titleSmall
        )
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = brandGlassColor())) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (uiLanguage.isEnglish) month.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", uiLanguage.locale)) else month.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", uiLanguage.locale)), color = ink, fontWeight = FontWeight.Bold)
                    Row {
                        TextButton(onClick = { monthValue = month.minusMonths(1).toEpochDay() }) { Text("‹") }
                        TextButton(onClick = { monthValue = month.plusMonths(1).coerceAtMost(today.withDayOfMonth(1)).toEpochDay() }) { Text("›") }
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    (if (uiLanguage.isEnglish) listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun") else listOf("सोम",uiLanguage.indic("मंगळ", "मंगल"),"बुध","गुरु","शुक्र","शनि","रवि")).forEach { label ->
                        Text(label, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = ink.copy(alpha=.6f))
                    }
                }
                monthCells.chunked(7).forEach { weekRow ->
                    Row(Modifier.fillMaxWidth()) {
                        weekRow.forEach { day ->
                            Box(Modifier.weight(1f).height(38.dp).padding(2.dp), contentAlignment = Alignment.Center) {
                                if (day != null) {
                                    val date = month.withDayOfMonth(day)
                                    val value = totalsByDate[date.toString()] ?: 0
                                    val target = japaGoalOn(goals, date)
                                    val reached = value >= target
                                    val label = if (uiLanguage.isEnglish) "$date, $value of $target chants"
                                        else uiLanguage.indic("$date, $target पैकी $value जप", "$date, $target में से $value जप")
                                    Surface(
                                        modifier = Modifier.fillMaxSize().semantics { contentDescription = label }
                                            .clickable(enabled = !date.isAfter(today)) { onDaySelected(date) },
                                        shape = CircleShape,
                                        color = when {
                                            reached -> Color(0xFFDDEEE4)
                                            value > 0 -> Color(0xFFE7D8F6)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f)
                                        }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(if (reached) "$day ✓" else if (value > 0) "$day •" else day.toString(),
                                                modifier = Modifier.clearAndSetSemantics { },
                                                color = if (value > 0) Color(0xFF20262B) else ink,
                                                style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Text(if (uiLanguage.isEnglish) "✓ Goal reached · • Partial practice" else uiLanguage.indic("✓ लक्ष्य पूर्ण · • काही जप झाले", "✓ लक्ष्य पूरा · • कुछ जप हुए"), color = ink.copy(alpha=.55f), style = MaterialTheme.typography.labelSmall)
            }
        }
        Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(containerColor=brandGlassColor())) {
            Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(if(uiLanguage.isEnglish)"YOUR JAPA NOTEBOOK" else uiLanguage.indic("तुमची जप नोंदवही", "आपकी जप डायरी"),color=ink,fontWeight=FontWeight.SemiBold)
                Text(if(uiLanguage.isEnglish)"Last 5 days of japa" else uiLanguage.indic("मागील ५ दिवसांचा जप", "पिछले ५ दिनों के जप"),color=ink.copy(alpha=.55f),style=MaterialTheme.typography.bodySmall)
                (0..4).map{today.minusDays(it.toLong())}.forEach { date ->
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text(date.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")),modifier=Modifier.weight(1f),color=ink)
                        Text(NumberFormat.getIntegerInstance(uiLanguage.locale).format(totalsByDate[date.toString()]?:0),color=ink,fontWeight=FontWeight.Bold)
                    }
                    if(date!=today.minusDays(4)) Divider(color=ink.copy(alpha=.08f))
                }
            }
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = brandGlassColor())) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (uiLanguage.isEnglish) "JAPA BY MANTRA" else uiLanguage.indic("मंत्रानुसार जप", "मंत्र के अनुसार जप"), color = ink, fontWeight = FontWeight.SemiBold)
                if (mantraBreakdown.isEmpty()) Text(if (uiLanguage.isEnglish) "No japa records yet" else uiLanguage.indic("अजून जप नोंद नाही", "अभी कोई जप रिकॉर्ड नहीं है"), color = ink.copy(alpha=.6f))
                mantraBreakdown.forEach { (mantra, value) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(mantraLabel(mantra), color = ink, maxLines = 2)
                            Text(uiLanguage.text("${value / 108} malas", "${value / 108} माळा", "${value / 108} माला"), color = ink.copy(alpha=.55f), style = MaterialTheme.typography.labelSmall)
                        }
                        Text(NumberFormat.getIntegerInstance(uiLanguage.locale).format(value), color = ink, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(progress = { if (total > 0) value.toFloat() / total else 0f }, modifier = Modifier.fillMaxWidth().height(5.dp), color = Color(0xFF8B69B5), trackColor = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}



@Composable
private fun SegmentedButtonRowCompat(selected:String, options:List<String>, onSelect:(String)->Unit, uiLanguage:UiLanguage) {
    Row(horizontalArrangement=Arrangement.spacedBy(3.dp)) { options.forEach { o -> FilterChip(selected==o,onClick={onSelect(o)},label={Text(if(uiLanguage.isEnglish)o else if(o=="BAR")"स्तंभ" else uiLanguage.indic("रेषा", "रेखा"),style=MaterialTheme.typography.labelSmall)}) } }
}
@Composable
private fun JapaActivityChart(days:List<DayTotal>, mode:String, modifier:Modifier, ink:Color, uiLanguage: UiLanguage) {
    Canvas(modifier) {
        val max=days.maxOfOrNull{it.total}?.coerceAtLeast(1)?.toFloat()?:1f
        val bottom=size.height-24.dp.toPx()
        val top=12.dp.toPx()
        val slot=size.width/days.size.coerceAtLeast(1)
        if(mode=="BAR") days.forEachIndexed { i,d ->
            val h=(bottom-top)*d.total/max
            drawRoundRect(brush=Brush.verticalGradient(listOf(Color(0xFF94B5C2),Color(0xFF423C6D)),startY=bottom-h,endY=bottom),topLeft=Offset(i*slot+slot*.2f,bottom-h),size=Size(slot*.6f,h),cornerRadius=androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()))
        } else {
            val points=days.mapIndexed { i,d -> Offset(i*slot+slot/2,bottom-(bottom-top)*d.total/max) }
            points.zipWithNext().forEach { (a,b)->drawLine(ink,a,b,strokeWidth=3.dp.toPx()) }
            points.forEach { drawCircle(ink,4.dp.toPx(),it) }
        }
        days.forEachIndexed { i,d -> drawContext.canvas.nativeCanvas.drawText(if(days.size<=7) runCatching{LocalDate.parse(d.date).format(java.time.format.DateTimeFormatter.ofPattern("EEE", uiLanguage.locale))}.getOrDefault("") else runCatching{LocalDate.parse(d.date).dayOfMonth.toString()}.getOrDefault(""),i*slot+slot/2,bottom+17.dp.toPx(),android.graphics.Paint().apply{color=ink.toArgb();textSize=10.dp.toPx();textAlign=android.graphics.Paint.Align.CENTER}) }
    }
}
