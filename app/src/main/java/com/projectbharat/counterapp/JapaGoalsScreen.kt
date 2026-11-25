package com.projectbharat.counterapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.projectbharat.counterapp.data.JapaPeriodGoal

@Composable
internal fun JapaGoalsScreen(language: UiLanguage, daily: Int, periods: List<JapaPeriodGoal>,
    onSave: (Int, Int?, Int?) -> Unit) {
    var day by remember(daily) { mutableStateOf(daily.toString()) }
    var month by remember(periods) { mutableStateOf(periods.firstOrNull { it.period == "MONTHLY" }?.target?.toString().orEmpty()) }
    var year by remember(periods) { mutableStateOf(periods.firstOrNull { it.period == "YEARLY" }?.target?.toString().orEmpty()) }
    fun optionalValid(s: String) = s.isBlank() || s.toIntOrNull()?.let { it in 1..999_999_999 } == true
    val valid = day.toIntOrNull()?.let { it in 1..999_999 } == true && optionalValid(month) && optionalValid(year)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(language.text("Japa goals", "जप लक्ष्य", "जप लक्ष्य"), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Text(language.text("Set a daily goal and optional monthly and yearly goals.", "रोजचे आणि ऐच्छिक मासिक व वार्षिक लक्ष्य ठरवा.", "दैनिक और वैकल्पिक मासिक व वार्षिक लक्ष्य तय करें।"), color = MaterialTheme.colorScheme.onBackground)
        SettingsGroup(language.text("Your targets", "तुमची लक्ष्ये", "आपके लक्ष्य")) {
            listOf(Triple(language.text("Daily", "दैनिक", "दैनिक"), day, { s: String -> day = s }),
                Triple(language.text("Monthly (optional)", "मासिक (ऐच्छिक)", "मासिक (वैकल्पिक)"), month, { s: String -> month = s }),
                Triple(language.text("Yearly (optional)", "वार्षिक (ऐच्छिक)", "वार्षिक (वैकल्पिक)"), year, { s: String -> year = s })).forEachIndexed { index, field ->
                OutlinedTextField(field.second, { value -> if (value.length <= (if (index == 0) 6 else 9) && value.all(Char::isDigit)) field.third(value) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text(field.first) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
        Button(onClick = { onSave(day.toInt(), month.toIntOrNull(), year.toIntOrNull()) }, enabled = valid, modifier = Modifier.fillMaxWidth()) {
            Text(language.text("Save goals", "लक्ष्ये जतन करा", "लक्ष्य सहेजें"))
        }
    }
}
