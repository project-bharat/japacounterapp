package com.projectbharat.counterapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.Manifest
import android.content.pm.PackageManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.projectbharat.counterapp.data.CounterDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.zip.ZipInputStream

const val TRACKING_STATE_CHANGED = "com.projectbharat.counterapp.TRACKING_STATE_CHANGED"
const val EXTRA_TRACKING_KIND = "tracking_kind"

private object TrackingNotifications {
    const val CHANNEL_ID = "active_tracking"
    const val CHANT_ID = 7102
    fun show(context: Context, id: Int, title: String, text: String, stopIntent: PendingIntent?): Notification {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Active tracking", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Visible while automatic tracking is active"
                setShowBadge(false)
            }
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(title).setContentText(text).setOngoing(true)
            .setOnlyAlertOnce(true).setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(CounterQuickActions.activityPending(context, 7110, quickStart = false))
            .apply {
                if (stopIntent != null) {
                    val ui = UiLanguage.load(context)
                    addAction(android.R.drawable.ic_media_pause,
                        if (ui.isEnglish) "Stop" else ui.indic("थांबवा", "रोकें"), stopIntent)
                }
            }
            .build()
    }
}

/** Reuses the loaded Vosk acoustic model between Japa sessions in this app process. */
private object VoskModelCache {
    private var cachedPath: String? = null
    private var cachedModel: Model? = null

    @Synchronized
    fun isCached(directory: File): Boolean = cachedPath == directory.absolutePath && cachedModel != null

    @Synchronized
    fun get(directory: File): Model {
        val path = directory.absolutePath
        if (cachedPath == path && cachedModel != null) return cachedModel!!
        cachedModel?.close()
        cachedModel = null
        cachedPath = null
        return Model(path).also {
            cachedModel = it
            cachedPath = path
        }
    }
}

/** Keep the unrestricted recognizer warm across pause/resume toggles. */
private object VoskRecognizerCache {
    private var cachedKey: String? = null
    private var cachedModelPath: String? = null
    private var cachedRecognizer: Recognizer? = null

    @Synchronized
    fun isCached(key: String): Boolean = cachedKey == key && cachedRecognizer != null

    @Synchronized
    fun clearForModelChange(path: String) {
        if (cachedModelPath != null && cachedModelPath != path) {
            cachedRecognizer?.close()
            cachedRecognizer = null
            cachedKey = null
            cachedModelPath = null
        }
    }

    @Synchronized
    fun get(model: Model, modelPath: String, key: String): Recognizer {
        if (cachedKey == key && cachedRecognizer != null) {
            cachedRecognizer!!.reset()
            return cachedRecognizer!!
        }
        cachedRecognizer?.close()
        val next = Recognizer(model, 16000.0f)
        cachedRecognizer = next
        cachedKey = key
        cachedModelPath = modelPath
        return next
    }
}

class ChantRecognitionService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var recording = false
    @Volatile private var stopRequested = false
    private var audioRecord: AudioRecord? = null
    private var recognizer: Recognizer? = null
    private var model: Model? = null
    private var worker: Thread? = null
    private val recognitionLock = Any()
    private var categoryId = -1L
    private var mantra = "Ram"
    private val language = "hi"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_PAUSE_CHANT) { pauseRecognition(); return START_NOT_STICKY }
        val previousWorker = worker
        if (previousWorker?.isAlive == true) {
            stopRequested = true
            recording = false
            getSharedPreferences("tracking_state", MODE_PRIVATE).edit().putBoolean("chant_audio_active", false).apply()
            try { audioRecord?.stop() } catch (_: Exception) { }
            previousWorker.interrupt()
        }
        categoryId = intent?.getLongExtra("category_id", -1L) ?: -1L
        mantra = intent?.getStringExtra(EXTRA_MANTRA)?.trim().orEmpty().ifBlank { "Ram" }
        if (categoryId <= 0 || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            stopSelf(); return START_NOT_STICKY
        }
        val ui = UiLanguage.load(this)
        val notification = TrackingNotifications.show(this, TrackingNotifications.CHANT_ID,
            ui.text("Voice japa", "आवाजाने जप", "आवाज़ से जप"),
            ui.text("Preparing offline model", "ऑफलाइन मॉडेल तयार करत आहे", "ऑफ़लाइन मॉडल तैयार हो रहा है"),
            stopAction())
        try {
            if (Build.VERSION.SDK_INT >= 30) ServiceCompat.startForeground(this, TrackingNotifications.CHANT_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
            else ServiceCompat.startForeground(this, TrackingNotifications.CHANT_ID, notification, 0)
        } catch (error: SecurityException) {
            getSharedPreferences("tracking_state", MODE_PRIVATE).edit().putBoolean("chant_enabled", false).putBoolean("chant_audio_active", false).apply()
            sendState(false, if (language == "en") "Microphone permission required" else "मायक्रोफोन परवानगी आवश्यक")
            stopSelf()
            return START_NOT_STICKY
        } catch (error: Exception) {
            getSharedPreferences("tracking_state", MODE_PRIVATE).edit().putBoolean("chant_enabled", false).apply()
            sendState(false, error.message ?: "Could not start microphone service")
            stopSelf()
            return START_NOT_STICKY
        }
        getSharedPreferences("tracking_state", MODE_PRIVATE).edit().putBoolean("chant_enabled", true).putBoolean("chant_audio_active", false).apply()
        QuickActionsNotification.hide(this)
        scope.launch {
            val log = CounterDatabase.get(this@ChantRecognitionService).dao()
                .findMantraLog(categoryId, LocalDate.now().toString(), "MANTRA:" + mantra)
            updateJapaNotification(log?.count ?: 0, ui.text("Preparing offline model", "ऑफलाइन मॉडेल तयार करत आहे", "ऑफ़लाइन मॉडल तैयार हो रहा है"))
        }
        sendState(false, if (language == "en") "Preparing recognizer…" else "ओळख यंत्रणा तयार होत आहे…")
        val voiceStartedAt = System.currentTimeMillis()
        val voiceCategoryId = categoryId
        val voiceMantra = mantra
        val recognitionPhrase = displayMantra(this, voiceCategoryId, voiceMantra, latin = false)
        worker = Thread {
            try {
                if (previousWorker?.isAlive == true) previousWorker.join()
                synchronized(recognitionLock) {
                    if (!Thread.currentThread().isInterrupted) {
                        stopRequested = false
                        runRecognition(voiceCategoryId, voiceMantra, recognitionPhrase, language, voiceStartedAt)
                    }
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }.also { it.name = "CounterApp-Vosk"; it.start() }
        return START_NOT_STICKY
    }

    private fun runRecognition(voiceCategoryId: Long, mantra: String, phrase: String, language: String, voiceStartedAt: Long) {
        var inputRoute: ChantAudioRoute? = null
        val routePrefs = getSharedPreferences("tracking_state", MODE_PRIVATE)
        try {
            val modelName = if (language == "hi") "vosk-model-small-hi-0.22" else "vosk-model-small-en-in-0.4"
            val modelUrl = if (language == "hi")
                "https://alphacephei.com/vosk/models/vosk-model-small-hi-0.22.zip"
            else "https://alphacephei.com/vosk/models/vosk-model-small-en-in-0.4.zip"
            val modelDir = File(filesDir, modelName)
            if (!File(modelDir, "am/final.mdl").exists()) downloadAndExtractModel(modelUrl, modelName, modelDir)
            if (stopRequested || Thread.currentThread().isInterrupted) return
            val recognizerKey = modelDir.absolutePath + "|" + language + "|" + phrase.trim()
            val warmRecognizer = VoskRecognizerCache.isCached(recognizerKey)
            sendState(true, if (language == "en") {
                if (warmRecognizer) "Reconnecting microphone…" else if (VoskModelCache.isCached(modelDir)) "Preparing speech recognizer…" else "Loading offline speech model…"
            } else {
                if (warmRecognizer) "माइक पुन्हा जोडत आहे…" else if (VoskModelCache.isCached(modelDir)) "ओळख यंत्रणा तयार होत आहे…" else "ऑफलाइन आवाज मॉडेल लोड होत आहे…"
            })
            VoskRecognizerCache.clearForModelChange(modelDir.absolutePath)
            model = VoskModelCache.get(modelDir)
            // A mantra-only grammar can force unrelated speech into the selected phrase.
            // Decode with the model's full vocabulary, then match complete phrases.
            recognizer = VoskRecognizerCache.get(model!!, modelDir.absolutePath, recognizerKey)
            val audioManager = requireNotNull(getSystemService(AudioManager::class.java))
            if (audioManager.mode == AudioManager.MODE_IN_CALL) {
                throw IllegalStateException("Microphone busy — another app or call is using it")
            }
            val route = ChantAudioRoute(audioManager)
            inputRoute = route
            val source = route.prepare()
            if (stopRequested || Thread.currentThread().isInterrupted) return
            val inputRate = route.sampleRate
            val minBuffer = AudioRecord.getMinBufferSize(inputRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            if (minBuffer <= 0) throw IllegalStateException("Audio input is not supported")
            val bufferSize = maxOf(minBuffer, inputRate / 4)
            val record = AudioRecord(source, inputRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
            audioRecord = record
            if (record.state != AudioRecord.STATE_INITIALIZED) throw IllegalStateException("Microphone unavailable")
            route.preferHeadset(record)
            if (stopRequested || Thread.currentThread().isInterrupted) { record.release(); audioRecord = null; return }
            try {
                record.startRecording()
            } catch (error: Exception) {
                record.release()
                audioRecord = null
                throw IllegalStateException("Microphone busy — another app or call is using it", error)
            }
            if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                record.release()
                audioRecord = null
                throw IllegalStateException("Microphone unavailable")
            }
            recording = true
            getSharedPreferences("tracking_state", MODE_PRIVATE).edit().putBoolean("chant_audio_active", true).apply()
            sendState(true, if (language == "en") "Offline model ready · listening offline" else "ऑफलाइन मॉडेल तयार · ऐकत आहे")
            val buffer = ByteArray(bufferSize)
            val scoResampler = if (inputRate == 8000) ScoPcmResampler(bufferSize) else null
            var lastPartial = ""
            var lastRouteCheck = 0L
            var lastRouteStatus: String? = null
            while (recording && !Thread.currentThread().isInterrupted) {
                val read = record.read(buffer, 0, buffer.size)
                if (read < 0) throw IllegalStateException("Microphone read failed: " + read)
                if (read > 0) {
                    val now = android.os.SystemClock.elapsedRealtime()
                    if (now - lastRouteCheck >= 500L) {
                        lastRouteCheck = now
                        val routed = record.routedDevice
                        val routeStatus = when {
                            routed == null -> null
                            routed.isBluetoothVoice() -> "bluetooth"
                            route.headsetRequested -> "bluetooth_unavailable"
                            else -> "device"
                        }
                        if (routeStatus != lastRouteStatus) {
                            lastRouteStatus = routeStatus
                            routePrefs.edit().putString("chant_input_route", routeStatus).apply()
                        }
                    }
                    val activeRecognizer = recognizer ?: continue
                    val pcm = scoResampler?.convert(buffer, read) ?: buffer
                    val samples = if (scoResampler == null) read else (read / 2) * 4
                    if (samples == 0) continue
                    if (activeRecognizer.acceptWaveForm(pcm, samples)) {
                        val transcript = JSONObject(activeRecognizer.result ?: "{}").optString("text").trim()
                        val matches = MantraMatcher.count(transcript, phrase)
                        sendRecognitionUpdate(transcript, matches, voiceCategoryId, mantra, phrase, voiceStartedAt)
                        lastPartial = ""
                    } else {
                        val partial = JSONObject(activeRecognizer.partialResult ?: "{}").optString("partial").trim()
                        if (partial != lastPartial) {
                            lastPartial = partial
                            // Partial hypotheses may be revised; show them without saving counts.
                            sendState(true, "Listening offline", partial, false)
                        }
                    }
                }
            }
        } catch (error: Exception) {
            Log.e("CounterAppVosk", "Offline chant recognition stopped", error)
            if (!stopRequested) {
                getSharedPreferences("tracking_state", MODE_PRIVATE).edit().putBoolean("chant_enabled", false).putBoolean("chant_audio_active", false).apply()
                val detail = error.message.orEmpty()
                val friendly = when {
                    detail.contains("busy", true) || detail.contains("startRecording", true) ->
                        if (language == "en") "Microphone busy — another app or call is using it" else "मायक्रोफोन व्यस्त — दुसरे अॅप किंवा कॉल वापरत आहे"
                    detail.contains("Microphone unavailable", true) || detail.contains("audio input", true) ->
                        if (language == "en") "Microphone unavailable" else "मायक्रोफोन उपलब्ध नाही"
                    detail.contains("permission", true) || error is SecurityException ->
                        if (language == "en") "Microphone permission required" else "मायक्रोफोन परवानगी आवश्यक"
                    else -> detail.ifBlank { if (language == "en") "Voice recognition stopped" else "आवाज ओळख थांबली" }
                }
                sendState(false, friendly)
                if (Build.VERSION.SDK_INT >= 24) stopForeground(STOP_FOREGROUND_REMOVE) else stopForeground(true)
                stopSelf()
            }
        } finally {
            recording = false
            routePrefs.edit().putBoolean("chant_audio_active", false).remove("chant_input_route").apply()
            try {
                try { audioRecord?.stop() } catch (_: Exception) { }
                audioRecord?.release()
            } finally {
                audioRecord = null
                runCatching { inputRoute?.close() }.onFailure { Log.w("CounterAppVosk", "Could not release audio route", it) }
            }
            recognizer = null
            // Both model and grammar recognizer remain cached for a fast next start.
        }
    }

    private fun sendRecognitionUpdate(
        transcript: String, matches: Int, voiceCategoryId: Long, voiceMantra: String, phrase: String, voiceStartedAt: Long
    ) {
        val matched = matches > 0 || MantraMatcher.count(transcript, phrase) > 0
        val countedAt = System.currentTimeMillis()
        val date = LocalDate.now().toString()
        sendState(true, if (matched) "Mantra matched" else "Listening offline", transcript, matched)
        scope.launch {
            val dao = CounterDatabase.get(this@ChantRecognitionService).dao()
            if (matches > 0) {
                if (!dao.incrementMantraDaily(voiceCategoryId, date, voiceMantra, matches, countedAt, voiceStartedAt)) {
                    return@launch
                }
                CounterHomeWidget.refresh(applicationContext)
                if (!recording || stopRequested || voiceCategoryId != categoryId || voiceMantra != mantra) return@launch
                val log = dao.findMantraLog(voiceCategoryId, date, "MANTRA:" + voiceMantra)
                updateJapaNotification(log?.count ?: 0, transcript.ifBlank { UiLanguage.load(this@ChantRecognitionService).text("Mantra matched", "मंत्र जुळला", "मंत्र पहचाना गया") })
                sendBroadcast(Intent(TRACKING_STATE_CHANGED).setPackage(packageName)
                    .putExtra(EXTRA_TRACKING_KIND, "chant").putExtra("delta", matches)
                    .putExtra("transcript", transcript).putExtra("matched", true))
            } else {
                if (!recording || stopRequested || voiceCategoryId != categoryId || voiceMantra != mantra) return@launch
                val log = dao.findMantraLog(voiceCategoryId, date, "MANTRA:" + voiceMantra)
                updateJapaNotification(log?.count ?: 0, transcript.ifBlank { UiLanguage.load(this@ChantRecognitionService).text("Listening offline", "ऑफलाइन ऐकत आहे", "ऑफ़लाइन सुन रहा है") })
            }
        }
    }

    private fun stopAction(): PendingIntent = PendingIntent.getService(
        this, 7102, Intent(this, ChantRecognitionService::class.java).setAction(ACTION_PAUSE_CHANT),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun updateJapaNotification(total: Int, liveText: String) {
        if (!getSharedPreferences("tracking_state", MODE_PRIVATE).getBoolean("chant_enabled", false)) return
        val title = UiLanguage.load(this).text("Japa", "जप", "जप")
        val notification = TrackingNotifications.show(this, TrackingNotifications.CHANT_ID,
            "$title · $total", liveText.take(120), stopAction())
        getSystemService(NotificationManager::class.java).notify(TrackingNotifications.CHANT_ID, notification)
    }

    private fun downloadAndExtractModel(modelUrl: String, modelName: String, target: File) {
        val selectedLanguage = if (modelName.contains("-hi-")) "hi" else "en"
        VoskOfflineModelInstaller.install(this, selectedLanguage) { status ->
            sendState(true, status)
        }
    }

    private fun pauseRecognition() {
        // Keep the started service and process-level acoustic model cache warm.
        // Stop only the microphone/recognizer so the next toggle can resume quickly.
        stopRequested = true
        recording = false
        try { audioRecord?.stop() } catch (_: Exception) { }
        worker?.interrupt()
        getSharedPreferences("tracking_state", MODE_PRIVATE).edit().putBoolean("chant_enabled", false).putBoolean("chant_audio_active", false).apply()
        getSystemService(NotificationManager::class.java).cancel(TrackingNotifications.CHANT_ID)
        if (Build.VERSION.SDK_INT >= 24) stopForeground(STOP_FOREGROUND_REMOVE) else stopForeground(true)
        sendState(false, "Stopped")
    }

    private fun stopRecognition() {
        pauseRecognition()
    }
    private fun sendState(active: Boolean, message: String, transcript: String? = null, matched: Boolean? = null) {
        val intent = Intent(TRACKING_STATE_CHANGED).setPackage(packageName)
            .putExtra(EXTRA_TRACKING_KIND, "chant").putExtra("active", active).putExtra("message", message)
        transcript?.let { intent.putExtra("transcript", it) }
        matched?.let { intent.putExtra("matched", it) }
        sendBroadcast(intent)
    }
    override fun onDestroy() {
        stopRequested = true
        recording = false
        try { audioRecord?.stop() } catch (_: Exception) { }
        worker?.interrupt()
        getSharedPreferences("tracking_state", MODE_PRIVATE).edit().putBoolean("chant_enabled", false).apply()
        scope.cancel()
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_MANTRA = "mantra"
        private const val ACTION_PAUSE_CHANT = "com.projectbharat.counterapp.action.PAUSE_CHANT"
        fun start(context: Context, categoryId: Long, mantra: String) {
            val intent = Intent(context, ChantRecognitionService::class.java)
                .putExtra("category_id", categoryId)
                .putExtra(EXTRA_MANTRA, mantra)
            ContextCompat.startForegroundService(context, intent)
        }
        fun stop(context: Context) { context.startService(Intent(context, ChantRecognitionService::class.java).setAction(ACTION_PAUSE_CHANT)) }
    }
}

