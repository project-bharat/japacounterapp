package com.projectbharat.counterapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Stable IDs are persisted independently of display language and list order. */
internal enum class BhajameTheme(val id: String, val image: Int, val english: String, val marathi: String, val hindi: String) {
    CLASSIC("classic", R.drawable.practice_sunrise, "Original Sunrise", "मूळ सूर्योदय", "मूल सूर्योदय"),
    DAWN("dawn", R.drawable.theme_dawn, "Brahmamuhurta Dawn", "ब्राह्ममुहूर्त", "ब्रह्ममुहूर्त"),
    GHAT("ghat", R.drawable.theme_ghat, "Sacred Ghat & Diya", "पावन घाट व दिवा", "पावन घाट और दीप"),
    BODHI("bodhi", R.drawable.theme_bodhi, "Bodhi & Banyan Shade", "बोधी व वटवृक्षाची छाया", "बोधि और वटवृक्ष की छाया"),
    LOTUS("lotus", R.drawable.theme_lotus, "Lotus Pond", "कमळ तलाव", "कमल सरोवर"),
    KAILASH("kailash", R.drawable.theme_kailash, "Kailash Horizon", "कैलास क्षितिज", "कैलाश क्षितिज"),
    TULSI("tulsi", R.drawable.theme_tulsi, "Sacred Tulsi Courtyard", "तुळशी वृंदावन", "तुलसी वृंदावन"),
    WAVES("waves", R.drawable.theme_waves, "Dhyana Mandala Waves", "ध्यान लहरी", "ध्यान लहरें"),
    NARMADA("narmada", R.drawable.theme_narmada, "Narmada Riverbank", "नर्मदा तीर", "नर्मदा तट");

    fun title(language: UiLanguage) = language.text(english, marathi, hindi)
    companion object { fun fromId(id: String?) = entries.firstOrNull { it.id == id } ?: CLASSIC }
}

@Composable
internal fun BhajameThemePicker(applied: BhajameTheme, language: UiLanguage, onApply: (BhajameTheme) -> Unit) {
    var pendingId by rememberSaveable(applied.id) { mutableStateOf(applied.id) }
    val pending = BhajameTheme.fromId(pendingId)
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(language.text("Choose a theme", "थीम निवडा", "थीम चुनें"),
            color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(language.text("Preview a background, then apply it across the app.", "पार्श्वभूमी पाहून अॅपसाठी लागू करा.", "पृष्ठभूमि देखकर उसे ऐप में लागू करें।"),
            color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
        LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 145.dp), modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(BhajameTheme.entries, key = { it.id }) { theme ->
                val selected = theme == pending
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .border(if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else brandGlassBorder(), RoundedCornerShape(18.dp))
                    .clickable(role = Role.RadioButton) { pendingId = theme.id }
                    .semantics { this.selected = selected }) {
                    Box(Modifier.fillMaxWidth().aspectRatio(9f / 16f).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))) {
                        Image(painterResource(theme.image), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        if (selected) Icon(Icons.Default.CheckCircle, null,
                            Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp), tint = Color(0xFFFEF5D8))
                    }
                    Surface(color = brandGlassColor(), contentColor = MaterialTheme.colorScheme.onSurface) {
                        Column(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Text(theme.title(language), style = MaterialTheme.typography.labelLarge, maxLines = 2)
                            if (theme == applied) Text(language.text("Current", "सध्याची", "वर्तमान"),
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        Button(onClick = { onApply(pending) }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Text(language.text("Apply theme", "थीम लागू करा", "थीम लागू करें"))
        }
    }
}
