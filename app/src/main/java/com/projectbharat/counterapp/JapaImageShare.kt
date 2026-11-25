package com.projectbharat.counterapp

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class JapaImageCard(
    val title: String,
    val mantra: String,
    val chants: Int,
    val detail: String,
    val uiLanguage: UiLanguage,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
internal fun JapaImagePreview(card: JapaImageCard) {
    val context = LocalContext.current
    val image = remember(context, card) { renderJapaImage(context, card).asImageBitmap() }
    Image(image, card.title, Modifier.fillMaxWidth().aspectRatio(4f / 5f))
}

internal suspend fun shareJapaImage(context: Context, card: JapaImageCard) {
    val uri = withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.cacheDir, "japa-share").apply { mkdirs() }
            directory.listFiles()?.filter { it.lastModified() < System.currentTimeMillis() - 86_400_000 }
                ?.forEach { it.delete() }
            val file = File.createTempFile("bhajame-japa-", ".png", directory)
            val bitmap = renderJapaImage(context, card)
            try {
                file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            } finally {
                bitmap.recycle()
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }.getOrNull()
    }
    if (uri == null) {
        Toast.makeText(context, if (card.uiLanguage.isEnglish) "Could not create image" else card.uiLanguage.indic("चित्र तयार झाले नाही", "चित्र नहीं बन सका"), Toast.LENGTH_SHORT).show()
        return
    }
    val send = Intent(Intent.ACTION_SEND).setType("image/png")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        .apply { clipData = ClipData.newUri(context.contentResolver, "Bhajame Japa", uri) }
    context.startActivity(Intent.createChooser(send, card.title))
}

private fun renderJapaImage(context: Context, card: JapaImageCard): Bitmap {
    val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    val background = BitmapFactory.decodeResource(context.resources, R.drawable.practice_sunrise)
    if (background != null) {
        canvas.drawBitmap(background, null, RectF(0f, 0f, 1080f, 1350f), paint)
        background.recycle()
    } else {
        canvas.drawColor(0xFFF5E8D5.toInt())
    }
    paint.color = 0xFFFCF9F4.toInt()
    canvas.drawRoundRect(RectF(64f, 250f, 1016f, 1218f), 48f, 48f, paint)
    val logo = BitmapFactory.decodeResource(context.resources, R.drawable.bilan_header_logo_light)
    if (logo != null) {
        val width = 258f
        val height = width * logo.height / logo.width
        canvas.drawBitmap(logo, null, RectF(411f, 88f, 669f, 88f + height), paint)
        logo.recycle()
    }
    fun text(value: String, top: Float, size: Float, bold: Boolean = false, maxLines: Int = 2) {
        val ink = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF423C6D.toInt()
            textSize = size
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
            textLocale = card.uiLanguage.locale
        }
        val layout = StaticLayout.Builder.obtain(value, 0, value.length, ink, 820)
            .setAlignment(Layout.Alignment.ALIGN_CENTER).setMaxLines(maxLines)
            .setEllipsize(android.text.TextUtils.TruncateAt.END).build()
        canvas.save()
        canvas.translate(130f, top)
        layout.draw(canvas)
        canvas.restore()
    }
    text(card.title, 310f, 54f, true)
    text(card.mantra, 455f, 42f, maxLines = 3)
    text(card.chants.toString(), 655f, 134f, true, 1)
    text(if (card.uiLanguage.isEnglish) "Chants  ·  ${card.chants / 108} malas" else card.uiLanguage.indic("जप  ·  ${card.chants / 108} माळा", "जप  ·  ${card.chants / 108} माला"), 825f, 45f)
    text(card.detail, 990f, 40f)
    val utc = DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm 'UTC'", card.uiLanguage.locale)
        .withZone(ZoneOffset.UTC).format(Instant.ofEpochMilli(card.timestamp))
    text(utc, 1110f, 32f)
    text("Designed & developed by Bharat Rasve", 1165f, 25f)
    return bitmap
}
