package com.projectbharat.counterapp.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object CounterBackupManager {
    private const val FORMAT_VERSION = 1

    suspend fun exportJson(context: Context, uri: Uri) {
        val database = CounterDatabase.get(context)
        val root = database.withTransaction {
            val dao = database.dao()
            val categories = dao.allCategories()
            val logs = dao.allLogs()
            val sessions = dao.allBabySessions()
            val detections = dao.allBabyDetections()
            val walkingSessions = dao.allWalkingSessions()
            val root = JSONObject().put("formatVersion", FORMAT_VERSION)
                .put("app", "Daily Counter")
                .put("exportedAt", System.currentTimeMillis())
            root.put("categories", JSONArray().apply {
                categories.forEach { c ->
                    put(JSONObject().put("id", c.id).put("titleMr", c.titleMr).put("titleEn", c.titleEn)
                        .put("kind", c.kind).put("goal", c.goal).put("enabled", c.enabled))
                }
            })
            root.put("logs", JSONArray().apply {
                logs.forEach { l ->
                    put(JSONObject().put("id", l.id).put("date", l.date).put("timestamp", l.timestamp)
                        .put("categoryId", l.categoryId).put("count", l.count)
                        .putNullable("sessionDurationMs", l.sessionDurationMs).putNullable("strength", l.strength)
                        .putNullable("note", l.note).putNullable("sessionId", l.sessionId))
                }
            })
            root.put("babyDetections", JSONArray().apply {
                detections.forEach { d -> put(JSONObject().put("id", d.id).put("sessionId", d.sessionId).put("timestamp", d.timestamp).put("magnitude", d.magnitude.toDouble()).put("status", d.status)) }
            })
            root.put("walkingSessions", JSONArray().apply {
                walkingSessions.forEach { s -> put(JSONObject().put("id", s.id).put("categoryId", s.categoryId).put("date", s.date)
                    .put("startedAt", s.startedAt).putNullable("endedAt", s.endedAt).put("steps", s.steps)
                    .put("distanceMeters", s.distanceMeters).put("gpsReliable", s.gpsReliable)) }
            })
            root.put("babySessions", JSONArray().apply {
                sessions.forEach { s ->
                    put(JSONObject().put("id", s.id).put("categoryId", s.categoryId).put("date", s.date)
                        .put("startedAt", s.startedAt).putNullable("endedAt", s.endedAt)
                        .put("dayPeriod", s.dayPeriod).put("movementCount", s.movementCount)
                        .putNullable("timeToTenMs", s.timeToTenMs).putNullable("strength", s.strength)
                        .putNullable("note", s.note).put("trackingMode", s.trackingMode).put("firstHourCount", s.firstHourCount))
                }
            })
            root.put("japaSessions", JSONArray().apply {
                dao.allJapaSessions().forEach { session ->
                    put(JSONObject().put("id", session.id).put("categoryId", session.categoryId)
                        .put("mantra", session.mantra).put("startedAt", session.startedAt)
                        .putNullable("endedAt", session.endedAt).put("count", session.count))
                }
            })
            root.put("japaGoals", JSONArray().apply {
                dao.allJapaGoals().forEach { goal ->
                    put(JSONObject().put("categoryId", goal.categoryId)
                        .put("effectiveDate", goal.effectiveDate).put("target", goal.target))
                }
            })
            root.put("japaPeriodGoals", JSONArray().apply {
                dao.allPeriodGoals().forEach { goal ->
                    put(JSONObject().put("categoryId", goal.categoryId).put("period", goal.period).put("target", goal.target))
                }
            })
            root
        }
        context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { it.write(root.toString(2)) }
            ?: error("Unable to create backup file")
    }

    suspend fun restoreJson(context: Context, uri: Uri) {
        val text = context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
        } ?: error("Unable to read backup file")
        val root = JSONObject(text)
        require(root.optInt("formatVersion", -1) == FORMAT_VERSION) { "Unsupported backup format" }
        val categoryArray = root.getJSONArray("categories")
        val logArray = root.getJSONArray("logs")
        val sessionArray = root.getJSONArray("babySessions")
        val detectionArray = root.optJSONArray("babyDetections") ?: JSONArray()
        val walkingArray = root.optJSONArray("walkingSessions") ?: JSONArray()
        require(categoryArray.length() > 0) { "Backup contains no categories" }

        val categories = (0 until categoryArray.length()).map { i ->
            val o = categoryArray.getJSONObject(i)
            CounterCategory(o.getLong("id"), o.getString("titleMr"), o.getString("titleEn"),
                o.optString("kind", "GENERAL"), o.optInt("goal", 0), o.optBoolean("enabled", true))
        }
        val logs = (0 until logArray.length()).map { i ->
            val o = logArray.getJSONObject(i)
            CounterLog(o.getLong("id"), o.getString("date"), o.getLong("timestamp"),
                o.getLong("categoryId"), o.optInt("count", 1), o.longOrNull("sessionDurationMs"),
                o.stringOrNull("strength"), o.stringOrNull("note"), o.longOrNull("sessionId"))
        }
        val sessions = (0 until sessionArray.length()).map { i ->
            val o = sessionArray.getJSONObject(i)
            BabySession(o.getLong("id"), o.getLong("categoryId"), o.getString("date"),
                o.getLong("startedAt"), o.longOrNull("endedAt"), o.getString("dayPeriod"),
                o.optInt("movementCount", 0), o.longOrNull("timeToTenMs"),
                o.stringOrNull("strength"), o.stringOrNull("note"), o.optString("trackingMode", "TWO_HOURS"), o.optInt("firstHourCount", 0))
        }

        val detections = (0 until detectionArray.length()).map { i ->
            val o = detectionArray.getJSONObject(i)
            BabyDetection(o.getLong("id"), o.getLong("sessionId"), o.getLong("timestamp"), o.getDouble("magnitude").toFloat(), o.optString("status", "PENDING"))
        }
        val walkingSessions = (0 until walkingArray.length()).map { i ->
            val o = walkingArray.getJSONObject(i)
            WalkingSession(o.getLong("id"), o.getLong("categoryId"), o.getString("date"), o.getLong("startedAt"),
                o.longOrNull("endedAt"), o.optInt("steps", 0), o.optDouble("distanceMeters", 0.0), o.optBoolean("gpsReliable", false))
        }
        require(detections.all { d -> sessions.any { it.id == d.sessionId } && d.status in setOf("PENDING", "CONFIRMED", "DISMISSED") }) { "Invalid baby detection records" }
        require(walkingSessions.all { it.steps >= 0 && it.distanceMeters >= 0.0 && categories.any { c -> c.id == it.categoryId } }) { "Invalid walking session records" }
        BackupSnapshotValidator.validate(categories, logs, sessions)
        val mantraIds = categories.filter { it.kind == "MANTRA" }.map { it.id }.toSet()
        val japaSessionArray = root.optJSONArray("japaSessions") ?: JSONArray()
        val japaSessions = (0 until japaSessionArray.length()).map { index ->
            val row = japaSessionArray.getJSONObject(index)
            JapaSession(row.getLong("id"), row.getLong("categoryId"), row.getString("mantra"),
                row.getLong("startedAt"), row.longOrNull("endedAt"), row.getInt("count"))
        }
        require(japaSessions.all { it.id > 0 && it.categoryId in mantraIds && it.mantra.isNotBlank() &&
            it.startedAt > 0 && (it.endedAt == null || it.endedAt >= it.startedAt) && it.count > 0 })
        require(japaSessions.distinctBy { it.id }.size == japaSessions.size)
        val active = japaSessions.filter { it.endedAt == null }
        require(active.distinctBy { it.categoryId to it.mantra }.size == active.size)
        val japaGoalArray = root.optJSONArray("japaGoals") ?: JSONArray()
        val japaGoals = (0 until japaGoalArray.length()).map { index ->
            val row = japaGoalArray.getJSONObject(index)
            val date = java.time.LocalDate.parse(row.getString("effectiveDate"))
            JapaGoal(row.getLong("categoryId"), date.toString(), row.getInt("target"))
        }
        require(japaGoals.all { it.categoryId in mantraIds && it.target in 1..999_999 })
        require(japaGoals.distinctBy { it.categoryId to it.effectiveDate }.size == japaGoals.size)
        val periodArray = root.optJSONArray("japaPeriodGoals") ?: JSONArray()
        val periodGoals = (0 until periodArray.length()).map { index ->
            val row = periodArray.getJSONObject(index)
            JapaPeriodGoal(row.getLong("categoryId"), row.getString("period"), row.getInt("target"))
        }
        require(periodGoals.all { it.categoryId in mantraIds && it.period in setOf("MONTHLY", "YEARLY") && it.target in 1..999_999_999 })
        require(periodGoals.distinctBy { it.categoryId to it.period }.size == periodGoals.size)
        CounterDatabase.get(context).dao().restoreSnapshot(categories, logs, sessions, detections, walkingSessions,
            japaSessions, japaGoals, periodGoals)
    }

    suspend fun exportCsv(context: Context, uri: Uri) {
        val dao = CounterDatabase.get(context).dao()
        val categories = dao.allCategories().associateBy { it.id }
        val logs = dao.allLogs().sortedBy { it.timestamp }
        val output = context.contentResolver.openOutputStream(uri, "wt") ?: error("Unable to create CSV file")
        output.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write('\uFEFF'.code)
            writer.write("date,timestamp,category_id,category_marathi,category_english,count,strength,note,session_id")
            writer.newLine()
            logs.forEach { log ->
                val category = categories[log.categoryId]
                val values = listOf(log.date, log.timestamp.toString(), log.categoryId.toString(),
                    category?.titleMr.orEmpty(), category?.titleEn.orEmpty(), log.count.toString(),
                    log.strength.orEmpty(), log.note.orEmpty(), log.sessionId?.toString().orEmpty())
                writer.write(values.joinToString(",") { csv(it) })
                writer.newLine()
            }
        }
    }

    private fun csv(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""
    private fun JSONObject.putNullable(key: String, value: Any?): JSONObject = put(key, value ?: JSONObject.NULL)
    private fun JSONObject.longOrNull(key: String): Long? = if (isNull(key) || !has(key)) null else getLong(key)
    private fun JSONObject.stringOrNull(key: String): String? = if (isNull(key) || !has(key)) null else getString(key)
}
