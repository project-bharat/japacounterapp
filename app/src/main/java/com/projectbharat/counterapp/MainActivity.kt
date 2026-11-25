package com.projectbharat.counterapp

import android.content.Intent
import android.app.TimePickerDialog
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.foundation.interaction.MutableInteractionSource
import android.os.Bundle
import android.media.ToneGenerator
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projectbharat.counterapp.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
private fun BhajameLaunchSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF423C6D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash_logo),
                contentDescription = null,
                modifier = Modifier.size(164.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Bhajame - Daily Japa & Bhakti",
                color = Color(0xFFFEF5D8),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    var launchCounterKind by mutableStateOf<String?>(null)
        private set
    var launchCounterQuickStart by mutableStateOf(false)
        private set
    fun consumeLaunchCounterKind() {
        launchCounterKind = null
        launchCounterQuickStart = false
    }

    private fun readCounterIntent(intent: Intent) {
        if (intent.action != ACTION_OPEN_COUNTER ||
            intent.getStringExtra(CounterQuickActions.EXTRA_COUNTER_KIND) != "MANTRA") return
        launchCounterQuickStart = intent.getBooleanExtra("quick_start", false)
        launchCounterKind = "MANTRA"
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readCounterIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        readCounterIntent(intent)
        // Use the Android SplashScreen API defaults. Android removes the splash
        // automatically as soon as the first app frame is ready.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        SpeechModelDownloadScheduler.enqueueFirstInstall(this)
        DailyReminderScheduler.rescheduleAll(this)
        SeedData.initialize(this)
        val repo = CounterRepository(CounterDatabase.get(this).dao())
        setContent {
            var showBrandSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(900)
                showBrandSplash = false
            }
            if (showBrandSplash) {
                BhajameLaunchSplash()
            } else {
                CounterApp(repo)
            }
        }
    }

    companion object {
        const val ACTION_OPEN_COUNTER = "com.projectbharat.counterapp.action.OPEN_COUNTER"
    }
}

class CounterViewModel(private val repo: CounterRepository) : ViewModel() {
    val categories = repo.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val archivedCategories = repo.archivedCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun count(id: Long) = repo.today(id)
    fun logsForDay(id: Long, date: LocalDate) = repo.logsForDay(id, date)
    fun mantraLogsForDay(id: Long, date: LocalDate) = repo.mantraLogsForDay(id, date)
    fun allMantraLogs(id: Long) = repo.allMantraLogs(id)
    fun mantraToday(id: Long, mantra: String) = repo.mantraToday(id, mantra)
    fun activeJapaSession(id: Long, mantra: String) = repo.activeJapaSession(id, mantra)
    fun japaGoals(id: Long) = repo.japaGoals(id)
    fun japaDayTotal(id: Long, date: LocalDate) = repo.japaDayTotal(id, date)
    suspend fun saveJapaGoal(id: Long, target: Int) = repo.saveJapaGoal(id, target)
    suspend fun finishJapaSession(session: JapaSession) =
        repo.finishJapaSession(session.categoryId, session.mantra, session.id)
    fun mantraAllTime(id: Long, mantra: String) = repo.mantraAllTime(id, mantra)
    fun mantraWeek(id: Long, mantra: String) = repo.mantraWeek(id, mantra)
    suspend fun addMantra(id: Long, mantra: String) = repo.addMantraCount(id, mantra)
    suspend fun updateLog(log: CounterLog) = repo.updateLog(log)
    suspend fun deleteLog(id: Long) = repo.deleteLog(id)
    fun week(id: Long) = repo.week(id)
    suspend fun addCategory(mr: String, en: String) = repo.addCategory(mr, en)
    suspend fun updateCategory(category: CounterCategory) = repo.updateCategory(category)
    suspend fun archiveCategory(id: Long) = repo.archiveCategory(id)
    suspend fun restoreCategory(id: Long) = repo.restoreCategory(id)
    suspend fun add(id: Long, strength: String?, duration: Long? = null) = repo.add(id, strength, duration)
    suspend fun addCount(id: Long, amount: Int) = repo.addCount(id, amount)
    suspend fun undo(id: Long) = repo.undo(id)
    suspend fun reset(id: Long) = repo.reset(id)
    class Factory(private val repo: CounterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CounterViewModel(repo) as T
    }
}

private val Navy = Color(0xFF102E55)
private val Gold = Color(0xFFE7A33C)
private val Cream = Color(0xFFF5F0E6)
@Composable
fun CounterApp(repo: CounterRepository) {
    val vm: CounterViewModel = viewModel(factory = CounterViewModel.Factory(repo))
    val categories by vm.categories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedId by rememberSaveable { mutableLongStateOf(1L) }
    val appPreferences = remember { context.getSharedPreferences("app_preferences", android.content.Context.MODE_PRIVATE) }
    var uiLanguage by remember { mutableStateOf(UiLanguage.load(context)) }
    var latinMantras by remember { mutableStateOf(appPreferences.getBoolean("mantra_latin_script", false)) }
    var monthlyHistory by remember { mutableStateOf(false) }

    val reportState = rememberSaveableStateHolder()
    var remindersScreen by remember { mutableStateOf(false) }
    var backupScreen by remember { mutableStateOf(false) }
    var voiceModelsDialog by remember { mutableStateOf(false) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var backupBusy by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var tab by rememberSaveable { mutableStateOf(0) }
    var help by rememberSaveable { mutableStateOf(false) }
    var achievements by rememberSaveable { mutableStateOf(false) }
    fun navigate(destination: Int) {
        tab = destination
        monthlyHistory = false
        remindersScreen = false
        backupScreen = false
        help = false
        achievements = false
    }
    var resetDialog by remember { mutableStateOf(false) }
    var historyDate by remember { mutableStateOf(LocalDate.now()) }
    var today by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            today = LocalDate.now()
            delay(15_000)
        }
    }
    var editGoalDialog by remember { mutableStateOf(false) }
    var finishedSession by remember { mutableStateOf<JapaSession?>(null) }
    var finishingSession by remember { mutableStateOf(false) }
    var mantraLibrary by rememberSaveable { mutableStateOf(false) }
    val child = monthlyHistory || remindersScreen || backupScreen || help || achievements
    fun goBack() {
        when {
            mantraLibrary -> mantraLibrary = false
            child -> navigate(tab)
            else -> navigate(0)
        }
    }
    BackHandler(mantraLibrary || child || tab != 0) { goBack() }
    val preferences = remember { context.getSharedPreferences("practice_feedback", android.content.Context.MODE_PRIVATE) }
    var tapFeedback by remember { mutableStateOf(preferences.getBoolean("tap", true)) }
    var malaFeedback by remember { mutableStateOf(preferences.getBoolean("mala", false)) }
    var soundFeedback by remember { mutableStateOf(preferences.getBoolean("sound", false)) }
    val scope = rememberCoroutineScope()
    val activity = context as? MainActivity
    val shortcutKind = activity?.launchCounterKind
    val shortcutQuickStart = activity?.launchCounterQuickStart == true
    var mantraTarget by remember { mutableStateOf(context.getSharedPreferences("chant_settings", android.content.Context.MODE_PRIVATE).getString("target_1", "राम") ?: "राम") }
    val jsonExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            backupBusy = true
            backupMessage = null
            runCatching { withContext(Dispatchers.IO) { CounterBackupManager.exportJson(context, uri) } }
                .onSuccess { backupMessage = if (uiLanguage.isEnglish) "Backup saved successfully." else uiLanguage.indic("बॅकअप यशस्वीपणे जतन झाला.", "बैकअप सहेजा गया।") }
                .onFailure { backupMessage = (if (uiLanguage.isEnglish) "Backup failed: " else uiLanguage.indic("बॅकअप अयशस्वी: ", "बैकअप विफल: ")) + (it.message ?: "Unknown error") }
            backupBusy = false
        }
    }
    val csvExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch {
            backupBusy = true
            backupMessage = null
            runCatching { withContext(Dispatchers.IO) { CounterBackupManager.exportCsv(context, uri) } }
                .onSuccess { backupMessage = if (uiLanguage.isEnglish) "CSV exported successfully." else uiLanguage.indic("CSV निर्यात यशस्वी.", "CSV निर्यात सफल रहा।") }
                .onFailure { backupMessage = (if (uiLanguage.isEnglish) "CSV export failed: " else uiLanguage.indic("CSV निर्यात अयशस्वी: ", "CSV निर्यात विफल: ")) + (it.message ?: "Unknown error") }
            backupBusy = false
        }
    }
    val jsonImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pendingRestoreUri = uri
    }
    val haptic = LocalHapticFeedback.current
    val clickTone = remember { ToneGenerator(android.media.AudioManager.STREAM_SYSTEM, 35) }
    DisposableEffect(clickTone) { onDispose { clickTone.release() } }
    LaunchedEffect(categories, uiLanguage) {
        CounterQuickActions.publishShortcuts(context, categories)
        CounterHomeWidget.refresh(context)
    }
    LaunchedEffect(categories, shortcutKind, shortcutQuickStart) {
        if (shortcutKind == null) return@LaunchedEffect
        val category = categories.firstOrNull { it.kind == "MANTRA" } ?: return@LaunchedEffect
        selectedId = category.id
        navigate(0)
        mantraLibrary = false
        if (shortcutQuickStart &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            val prefs = context.getSharedPreferences("chant_settings", android.content.Context.MODE_PRIVATE)
            val target = prefs.getString("target_${category.id}", "राम") ?: "राम"
            mantraTarget = target
            ChantRecognitionService.start(context, category.id, target)
        }
        activity?.consumeLaunchCounterKind()
    }
    val selected = categories.firstOrNull { it.kind == "MANTRA" && it.id == selectedId }
        ?: categories.firstOrNull { it.kind == "MANTRA" }
    LaunchedEffect(categories) {
        selected?.let { if (selectedId != it.id) selectedId = it.id }
    }

    LaunchedEffect(selected?.id, selected?.kind) {
        if (selected?.kind == "MANTRA") {
            mantraTarget = context.getSharedPreferences("chant_settings", android.content.Context.MODE_PRIVATE)
                .getString("target_${selected.id}", "राम") ?: "राम"
        }
    }
    val countFlow = remember(selected?.id, selected?.kind, mantraTarget, today) {
        selected?.let { if (it.kind == "MANTRA") vm.mantraToday(it.id, mantraTarget) else vm.count(it.id) } ?: flowOf(0)
    }
    val mantraTotalFlow = remember(selected?.id, selected?.kind, mantraTarget) {
        selected?.takeIf { it.kind == "MANTRA" }?.let { vm.mantraAllTime(it.id, mantraTarget) } ?: flowOf(0)
    }
    val weekFlow = remember(selected?.id, selected?.kind, mantraTarget) {
        selected?.let { if (it.kind == "MANTRA") vm.mantraWeek(it.id, mantraTarget) else vm.week(it.id) } ?: flowOf(emptyList())
    }
    val historyLogsFlow = remember(selected?.id, selected?.kind, historyDate) {
        selected?.let { if (it.kind == "MANTRA") vm.mantraLogsForDay(it.id, historyDate) else vm.logsForDay(it.id, historyDate) } ?: flowOf(emptyList())
    }
    val allMantraLogsFlow = remember(selected?.id, selected?.kind) {
        selected?.takeIf { it.kind == "MANTRA" }?.let { vm.allMantraLogs(it.id) } ?: flowOf(emptyList())
    }
    val japaCategoryId = selected?.takeIf { it.kind == "MANTRA" }?.id
    val goalsFlow = remember(japaCategoryId) {
        japaCategoryId?.let(vm::japaGoals) ?: flowOf(emptyList())
    }
    val goalHistory by goalsFlow.collectAsStateWithLifecycle(emptyList())
    val dailyGoal = japaGoalOn(goalHistory, today)
    val dayTotalFlow = remember(japaCategoryId, today) {
        japaCategoryId?.let { vm.japaDayTotal(it, today) } ?: flowOf(0)
    }
    val japaDayTotal by dayTotalFlow.collectAsStateWithLifecycle(0)
    val activeJapaFlow = remember(japaCategoryId, mantraTarget) {
        japaCategoryId?.let { vm.activeJapaSession(it, mantraTarget) } ?: flowOf(null)
    }
    val storedSession by activeJapaFlow.collectAsStateWithLifecycle(null)
    val activeJapa = storedSession?.takeIf { it.categoryId == japaCategoryId && it.mantra == mantraTarget.trim() }
    LaunchedEffect(activeJapa?.id) {
        if (activeJapa != null) QuickActionsNotification.hide(context)
        else QuickActionsNotification.show(context)
    }
    val count by countFlow.collectAsStateWithLifecycle(0)
    val mantraTotal by mantraTotalFlow.collectAsStateWithLifecycle(0)
    var previousMalaCount by remember(selected?.id, mantraTarget) { mutableStateOf<Int?>(null) }
    LaunchedEffect(mantraTotal, malaFeedback, selected?.id, mantraTarget) {
        val completed = mantraTotal / 108
        if (malaFeedback && previousMalaCount?.let { completed > it } == true) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        previousMalaCount = completed
    }
    LaunchedEffect(count, selected?.id) { CounterHomeWidget.refresh(context) }
    val days by weekFlow.collectAsStateWithLifecycle(emptyList())
    val historyLogs by historyLogsFlow.collectAsStateWithLifecycle(emptyList())
    val allMantraLogs by allMantraLogsFlow.collectAsStateWithLifecycle(emptyList())
    val dark = isSystemInDarkTheme()
    val page = if (dark) Color(0xFF211E39) else Color(0xFFFEF5D8)
    val ink = if (dark) Color(0xFFFEF5E6) else Color(0xFF302B54)
    val scheme = if (dark) darkColorScheme(primary = Color(0xFFB7D1D9), onPrimary = Color(0xFF242044), secondary = Color(0xFFB7D1D9), background = page, onBackground = ink, surface = Color(0xFF302C4B), onSurface = ink, surfaceVariant = Color(0xFF47415F), onSurfaceVariant = Color(0xFFEEE5DE), secondaryContainer = Color(0xFF514C6B), onSecondaryContainer = Color(0xFFFFF6E5))
                 else lightColorScheme(primary = Color(0xFF423C6D), onPrimary = Color.White, secondary = Color(0xFF647E8A), background = page, onBackground = ink, surface = Color(0xFFFFFBF1), onSurface = ink, surfaceVariant = Color(0xFFEADCC1), onSurfaceVariant = Color(0xFF423C6D), secondaryContainer = Color(0xFFE7DFD4), onSecondaryContainer = Color(0xFF302B54))

    MaterialTheme(colorScheme = scheme) {
        if (voiceModelsDialog) VoiceModelsDialog(uiLanguage = uiLanguage, onDismiss = { voiceModelsDialog = false })
        Box(Modifier.fillMaxSize().background(page)) {
        val practiceBackground = tab == 0 && !child && !mantraLibrary
        Image(painterResource(if (practiceBackground) {
            if (dark) R.drawable.practice_sunrise_dark else R.drawable.practice_sunrise
        } else if (dark) R.drawable.brand_background_dark else R.drawable.brand_background_light),
            null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        if (practiceBackground) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
                Color(0x440F1029), Color.Transparent, Color(0x66100E29)))))
        }
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (!mantraLibrary && !child) {
                    val shape = RoundedCornerShape(28.dp)
                    val footerColor = if (practiceBackground || dark) Color(0xB82D294B) else Color(0xCFFEF9ED)
                    val footerInk = if (practiceBackground || dark) Color(0xFFFFF6E5) else ink
                    val accent = if (practiceBackground || dark) Color(0xFFB7D1D9) else MaterialTheme.colorScheme.primary
                    Box(Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().shadow(6.dp, shape).clip(shape)
                            .background(footerColor).border(1.dp, Color.White.copy(alpha = .22f), shape).padding(top = 10.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            listOf(1, 0, 2).forEach { index ->
                                val label = when (index) {
                                    1 -> uiLanguage.text("Progress", "प्रगती", "प्रगति")
                                    2 -> uiLanguage.text("Settings", "सेटिंग्ज", "सेटिंग्स")
                                    else -> uiLanguage.text("Practice", "जप", "जप")
                                }
                                val icon = when (index) {
                                    1 -> R.drawable.brand_icon_progress
                                    2 -> R.drawable.brand_icon_settings
                                    else -> R.drawable.brand_icon_practice
                                }
                                Column(Modifier.weight(1f).clickable { navigate(index) }.padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(painterResource(icon), null, Modifier.size(26.dp), tint = if (tab == index) accent else footerInk)
                                    Text(label, color = if (tab == index) accent else footerInk,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                if (tab != 0 || child || mantraLibrary) Row(
                    Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (child || mantraLibrary) IconButton(onClick = ::goBack) {
                        Icon(Icons.Default.ArrowBack, uiLanguage.text("Back", "मागे", "वापस"), tint = ink)
                    }
                    Image(painterResource(if (dark) R.drawable.bilan_header_logo_light else R.drawable.bilan_header_logo_dark), "Bhajame",
                        Modifier.width(96.dp).height(36.dp), contentScale = ContentScale.Fit)
                }
                Box(Modifier.weight(1f)) {
                if (achievements) {
                    JapaAchievements(allMantraLogs, uiLanguage)
                } else if (help) {
                    JapaHelpScreen(uiLanguage)
                } else if (backupScreen) {
                    BackupSettingsScreen(uiLanguage = uiLanguage, ink = ink, message = backupMessage, busy = backupBusy,
                        onExportBackup = { jsonExportLauncher.launch("AppBackup_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".json") },
                        onImportBackup = { jsonImportLauncher.launch(arrayOf("application/json", "text/*")) },
                        onExportCsv = { csvExportLauncher.launch("daily-counter-history.csv") })
                } else if (remindersScreen) {
                    ReminderSettingsScreen(uiLanguage = uiLanguage, ink = ink)
                } else if (monthlyHistory && selected != null) {
                    CounterHistoryScreen(
                        uiLanguage = uiLanguage, ink = ink, date = historyDate, logs = historyLogs, days = days,
                        mantraLabel = { displayMantra(context, selected.id, it, latinMantras) },
                        onPrevious = { historyDate = historyDate.minusDays(1) },
                        onNext = { if (historyDate.isBefore(LocalDate.now())) historyDate = historyDate.plusDays(1) },
                        onBack = null,
                        onUpdate = { log -> scope.launch { vm.updateLog(log) } },
                        onDelete = { log -> scope.launch { vm.deleteLog(log.id) } }
                    )
                } else if (tab == 1 && selected?.kind == "MANTRA") {
                    reportState.SaveableStateProvider("japa-report") {
                        JapaReportScreen(
                            logs = allMantraLogs, goals = goalHistory, today = today, uiLanguage = uiLanguage, ink = ink,
                            mantraLabel = { displayMantra(context, selected.id, it, latinMantras) },
                            onDaySelected = { date -> historyDate = date; monthlyHistory = true },
                            onAchievements = { achievements = true },
                            onEditGoal = { editGoalDialog = true }
                        )
                    }
                } else if (tab == 1) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatTile(if (uiLanguage.isEnglish) "TOTAL" else uiLanguage.indic("एकूण", "कुल"), days.sumOf { it.total }.toString(), ink, Modifier.weight(1f))
                            StatTile(if (uiLanguage.isEnglish) "DAILY AVG" else uiLanguage.indic("दररोज सरासरी", "दैनिक औसत"), "%.1f".format(days.sumOf { it.total }.toDouble() / 7.0), ink, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(14.dp))
                        WeeklyChart(days, Modifier.fillMaxWidth().height(210.dp))
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            val title = if (uiLanguage.isEnglish) "Weekly progress" else uiLanguage.indic("साप्ताहिक प्रगती", "साप्ताहिक प्रगति")
                            val category = selected?.let { if (uiLanguage.isEnglish) it.titleEn else it.titleMr }.orEmpty()
                            val card = JapaImageCard(title, category, days.sumOf { it.total },
                                if (uiLanguage.isEnglish) "Last 7 days" else uiLanguage.indic("मागील ७ दिवस", "पिछले ७ दिन"), uiLanguage)
                            scope.launch { shareJapaImage(context, card) }
                        }) {
                            Icon(Icons.Default.Share, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (uiLanguage.isEnglish) "Share image" else uiLanguage.indic("चित्र शेअर करा", "चित्र साझा करें"))
                        }
                    }
                } else if (tab == 2) {
                    JapaSettingsScreen(
                        uiLanguage, tapFeedback, malaFeedback, soundFeedback,
                        latinMantras = latinMantras,
                        onLanguage = {
                            uiLanguage = it
                            appPreferences.edit().putString("ui_language", it.tag).apply()
                            if (activeJapa == null) QuickActionsNotification.show(context)
                            CounterHomeWidget.refresh(context)
                        },
                        onMantraScript = {
                            latinMantras = it
                            appPreferences.edit().putBoolean("mantra_latin_script", it).apply()
                        },
                        onTap = { tapFeedback = it; preferences.edit().putBoolean("tap", it).apply() },
                        onMala = { malaFeedback = it; preferences.edit().putBoolean("mala", it).apply() },
                        onSound = { soundFeedback = it; preferences.edit().putBoolean("sound", it).apply() },
                        onReminders = { remindersScreen = true },
                        onModels = { voiceModelsDialog = true },
                        onBackup = { backupScreen = true },
                        onHelp = { help = true }
                    )
                } else if (selected?.kind == "MANTRA") {
                    AutoChantCounterScreen(
                        categoryId = selected.id, count = count, totalCount = mantraTotal, uiLanguage = uiLanguage, ink = ink,
                        dailyGoal = dailyGoal,
                        dayTotal = japaDayTotal,
                        latinMantras = latinMantras,
                        mantraLibrary = mantraLibrary,
                        onLibraryChange = { mantraLibrary = it },
                        sessionCount = activeJapa?.count ?: 0,
                        onEditGoal = { editGoalDialog = true },
                        onFinish = activeJapa?.takeUnless { finishingSession }?.let { session ->
                            {
                                finishingSession = true
                                scope.launch {
                                    try {
                                        ChantRecognitionService.stop(context)
                                        finishedSession = vm.finishJapaSession(session)
                                    } finally {
                                        finishingSession = false
                                    }
                                }
                                Unit
                            }
                        },
                        onMantraChange = { mantra -> mantraTarget = mantra },
                        onManualCount = { mantra ->
                            if (tapFeedback) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (soundFeedback) clickTone.startTone(ToneGenerator.TONE_PROP_BEEP, 65)
                            scope.launch { vm.addMantra(selected.id, mantra) }
                        }
                    )
                } else if (selected != null) {
                    CounterTapScreen(
                        uiLanguage = uiLanguage, count = count, goal = selected.goal, kind = selected.kind,
                        categoryTitle = if (uiLanguage.isEnglish) selected.titleEn else selected.titleMr,
                        ink = ink, onTap = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); clickTone.startTone(ToneGenerator.TONE_PROP_BEEP, 65); scope.launch { vm.add(selected.id, null) } },
                        onUndo = { scope.launch { vm.undo(selected.id) } }, onReset = { resetDialog = true }
                    )
                }
                }
                if (tab == 1 && selected?.kind != "MANTRA" && !child) {
                    TextButton(onClick = { historyDate = today; monthlyHistory = true }) {
                        Text(if (uiLanguage.isEnglish) "Open history" else uiLanguage.indic("इतिहास उघडा", "इतिहास खोलें"))
                    }
                }
            }
        }
    }
    }
    if (editGoalDialog && japaCategoryId != null) {
        var input by remember(japaCategoryId) { mutableStateOf(dailyGoal.toString()) }
        val target = input.toIntOrNull()?.takeIf { it in 1..999_999 }
        AlertDialog(
            onDismissRequest = { editGoalDialog = false },
            icon = { Icon(Icons.Default.Edit, null) },
            title = { Text(if (uiLanguage.isEnglish) "Daily Japa goal" else uiLanguage.indic("दररोजचे जप लक्ष्य", "दैनिक जप लक्ष्य")) },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) input = it },
                    label = { Text(if (uiLanguage.isEnglish) "Chants per day" else uiLanguage.indic("दररोजचे जप", "प्रतिदिन जप")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    target?.let { scope.launch { vm.saveJapaGoal(japaCategoryId, it) } }
                    editGoalDialog = false
                }, enabled = target != null) { Text(if (uiLanguage.isEnglish) "Save" else uiLanguage.indic("जतन करा", "सहेजें")) }
            },
            dismissButton = {
                TextButton(onClick = { editGoalDialog = false }) { Text(if (uiLanguage.isEnglish) "Cancel" else uiLanguage.indic("रद्द", "रद्द करें")) }
            }
        )
    }
    finishedSession?.let { session ->
        val title = uiLanguage.text("Japa session saved", "जप सत्र जतन झाले", "जप सत्र सहेजा गया")
        val mantra = displayMantra(context, session.categoryId, session.mantra, latinMantras)
        val progress = uiLanguage.text("Today · $japaDayTotal/$dailyGoal", "आज · $japaDayTotal/$dailyGoal", "आज · $japaDayTotal/$dailyGoal")
        AlertDialog(
            onDismissRequest = { finishedSession = null },
            title = { Text(title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(mantra, style = MaterialTheme.typography.titleMedium)
                    Text(uiLanguage.text("This session · ${session.count} chants", "या सत्रात · ${session.count} जप", "इस सत्र में · ${session.count} जप"))
                    Text(progress)
                    Text(uiLanguage.text("${session.count / 108} malas", "${session.count / 108} माळा", "${session.count / 108} माला"))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val card = JapaImageCard(title, mantra, session.count, progress, uiLanguage, session.endedAt ?: session.startedAt)
                    scope.launch { shareJapaImage(context, card) }
                }) { Text(uiLanguage.text("Share image", "चित्र शेअर करा", "चित्र साझा करें")) }
            },
            confirmButton = {
                TextButton(onClick = { finishedSession = null }) { Text(uiLanguage.text("Done", "पूर्ण", "हो गया")) }
            }
        )
    }
    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text(if (uiLanguage.isEnglish) "Replace app data?" else uiLanguage.indic("सध्याचा डेटा बदलायचा?", "वर्तमान डेटा बदलें?")) },
            text = { Text(if (uiLanguage.isEnglish) "Restoring replaces all counters, history and session records on this device. Export a backup first if you need to keep the current data." else uiLanguage.indic("बॅकअप पुनर्संचयित केल्याने या उपकरणावरील सर्व मोजण्या, इतिहास आणि सत्रांच्या नोंदी बदलली जातील. सध्याचा डेटा हवा असल्यास आधी बॅकअप घ्या.", "बैकअप बहाल करने से इस डिवाइस के सभी काउंटर, इतिहास और सत्र बदल जाएँगे। वर्तमान डेटा रखने के लिए पहले बैकअप लें।")) },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestoreUri = null
                    scope.launch {
                        backupBusy = true
                        backupMessage = null
                        runCatching { withContext(Dispatchers.IO) { CounterBackupManager.restoreJson(context, uri) } }
                            .onSuccess { backupMessage = if (uiLanguage.isEnglish) "Backup restored successfully." else uiLanguage.indic("बॅकअप यशस्वीपणे पुनर्संचयित झाला.", "बैकअप बहाल हो गया।") }
                            .onFailure { backupMessage = (if (uiLanguage.isEnglish) "Restore failed: " else uiLanguage.indic("पुनर्संचयित करणे अयशस्वी: ", "बहाली विफल: ")) + (it.message ?: "Unknown error") }
                        backupBusy = false
                    }
                }) { Text(if (uiLanguage.isEnglish) "Replace data" else uiLanguage.indic("डेटा बदला", "डेटा बदलें")) }
            },
            dismissButton = { TextButton(onClick = { pendingRestoreUri = null }) { Text(if (uiLanguage.isEnglish) "Cancel" else uiLanguage.indic("रद्द", "रद्द करें")) } }
        )
    }
    if (resetDialog) AlertDialog(onDismissRequest = { resetDialog = false }, title = { Text(if (uiLanguage.isEnglish) "Reset today's count?" else uiLanguage.indic("आजची मोजणी रीसेट करायची?", "आज की गिनती रीसेट करें?")) },
        confirmButton = { TextButton(onClick = { scope.launch { selected?.let { vm.reset(it.id) } }; resetDialog = false }) { Text(if (uiLanguage.isEnglish) "Reset" else "रीसेट") } },
        dismissButton = { TextButton(onClick = { resetDialog = false }) { Text(if (uiLanguage.isEnglish) "Cancel" else uiLanguage.indic("रद्द", "रद्द करें")) } })
}

@Composable
private fun CounterTapScreen(uiLanguage:UiLanguage,count:Int,goal:Int,kind:String,categoryTitle:String,ink:Color,onTap:()->Unit,onUndo:()->Unit,onReset:()->Unit) {
    val mantra = kind == "MANTRA"
    val tapScale = remember { Animatable(1f) }
    val tapScope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally) {
        Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                Box(Modifier.size(58.dp).background(if(mantra) Color(0xFFEDE4FA) else Color(0xFFDDF1E8),CircleShape),contentAlignment=Alignment.Center) {
                    Icon(Icons.Default.SelfImprovement,null,tint=Color(0xFF7855A6),modifier=Modifier.size(30.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(if(uiLanguage.isEnglish) "TODAY" else "आज",color=ink.copy(alpha=.55f),style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Bold)
                Text(count.toString(),color=ink,style=MaterialTheme.typography.displayLarge,fontWeight=FontWeight.ExtraBold)
                Text(if(mantra) { if(uiLanguage.isEnglish) "CHANTS" else "जप" } else categoryTitle,color=ink.copy(alpha=.55f),style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)
                if(mantra) {
                    Text((count/108).toString()+" "+(if(uiLanguage.isEnglish) "mala" else uiLanguage.indic("माळ", "माला"))+" · "+(count%108)+"/108",color=Gold,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=9.dp))
                    Row(Modifier.padding(top=8.dp),horizontalArrangement=Arrangement.Center) { repeat(12) { index -> Box(Modifier.padding(3.dp).size(11.dp).background(if(index<count%108*12/108) Gold else Color(0xFFE8E1D7),CircleShape)) } }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        val tapInteraction = remember { MutableInteractionSource() }
        Box(Modifier.size(214.dp).graphicsLayer(scaleX = tapScale.value, scaleY = tapScale.value).background(Brush.linearGradient(if(mantra) listOf(Color(0xFFE9DDF8),Color(0xFFD8C4F0)) else listOf(Color(0xFFDDF1E8),Color(0xFFBDE2D0))),CircleShape).clickable(interactionSource = tapInteraction, indication = null) { tapScope.launch { tapScale.animateTo(.90f, tween(75)); tapScale.animateTo(1.08f, tween(110)); tapScale.animateTo(1f, tween(180)) }; onTap() },contentAlignment=Alignment.Center) {
            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                Icon(Icons.Default.SelfImprovement,null,tint=ink,modifier=Modifier.size(36.dp))
                Text("+1",color=ink,style=MaterialTheme.typography.displayMedium,fontWeight=FontWeight.ExtraBold)
                Text(if(uiLanguage.isEnglish) "TAP TO ADD" else uiLanguage.indic("मोजणी वाढवा", "गिनती बढ़ाएँ"),color=ink,style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick=onUndo,modifier=Modifier.weight(1f),shape=RoundedCornerShape(15.dp)) { Icon(Icons.Default.Undo,null); Spacer(Modifier.width(5.dp)); Text(if(uiLanguage.isEnglish) "Undo" else uiLanguage.indic("मागे", "वापस")) }
            OutlinedButton(onClick=onReset,modifier=Modifier.weight(1f),shape=RoundedCornerShape(15.dp)) { Icon(Icons.Default.Refresh,null); Spacer(Modifier.width(5.dp)); Text(if(uiLanguage.isEnglish) "Reset" else "रीसेट") }
        }
        Spacer(Modifier.weight(.8f))
    }
}


@Composable
private fun ReminderSettingsScreen(uiLanguage: UiLanguage, ink: Color) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val settings = remember { mutableStateListOf<DailyReminder>().apply { addAll(DailyReminderScheduler.load(context)) } }
    Column(Modifier.fillMaxSize()) {
        Text(if (uiLanguage.isEnglish) "DAILY REMINDERS" else uiLanguage.indic("दैनिक स्मरणपत्रे", "दैनिक अनुस्मारक"), color = ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(if (uiLanguage.isEnglish) "Choose times that fit your routine." else uiLanguage.indic("तुमच्या दिनक्रमानुसार वेळ निवडा.", "अपनी दिनचर्या के अनुसार समय चुनें।"), color = ink.copy(alpha = .65f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 14.dp)) {
            items(settings, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(if (uiLanguage.isEnglish) item.titleEn else uiLanguage.indic(item.titleMr, "जप का अनुस्मारक"), color = ink, fontWeight = FontWeight.SemiBold)
                            Text("%02d:%02d".format(item.hour, item.minute), color = Color(0xFF39865B), style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.clickable {
                                    TimePickerDialog(context, { _, hour, minute ->
                                        val index = settings.indexOfFirst { it.id == item.id }
                                        if (index >= 0) {
                                            settings[index] = settings[index].copy(hour = hour, minute = minute)
                                            DailyReminderScheduler.saveAndSchedule(context, settings.toList())
                                        }
                                    }, item.hour, item.minute, false).show()
                                })
                            Text(if (uiLanguage.isEnglish) "Tap time to change" else uiLanguage.indic("वेळ बदलण्यासाठी टॅप करा", "समय बदलने के लिए टैप करें"), color = ink.copy(alpha = .45f), style = MaterialTheme.typography.labelSmall)
                        }
                        Switch(checked = item.enabled, onCheckedChange = { enabled ->
                            val index = settings.indexOfFirst { it.id == item.id }
                            if (index >= 0) {
                                settings[index] = settings[index].copy(enabled = enabled)
                                DailyReminderScheduler.saveAndSchedule(context, settings.toList())
                                if (enabled && Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    activity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 4101) }
                                }
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, ink: Color, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp)) {
            Text(label, color = ink.copy(alpha = .55f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(value, color = ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun WeeklyChart(days: List<DayTotal>, modifier: Modifier = Modifier) {
    val chartInk = MaterialTheme.colorScheme.onSurface
    val maxValue=(days.maxOfOrNull{it.total} ?: 0).coerceAtLeast(1)
    Row(modifier, horizontalArrangement=Arrangement.spacedBy(6.dp), verticalAlignment=Alignment.Bottom) {
        days.forEach { day ->
            Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Bottom) {
                Text(day.total.toString(),color=chartInk,style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Box(Modifier.fillMaxWidth(.72f).weight(1f,false).height((145f*day.total/maxValue).coerceAtLeast(if(day.total>0) 8f else 3f).dp).clip(RoundedCornerShape(topStart=8.dp,topEnd=8.dp)).background(if(day.total==maxValue) Gold else Color(0xFF9BB6D3)))
                Spacer(Modifier.height(7.dp))
                Text(day.date.takeLast(2),color=chartInk.copy(alpha=.8f),style=MaterialTheme.typography.labelSmall)
            }
        }
    }
}
