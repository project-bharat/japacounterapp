package com.projectbharat.counterapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Translucent tinted surfaces; background remains visible while text retains contrast. */
@Composable
internal fun brandGlassColor(): Color = if (MaterialTheme.colorScheme.background.luminance() < .5f)
    Color(0xDB282442) else Color(0xDCFFF9ED)

@Composable
internal fun brandGlassBorder(): Color = if (MaterialTheme.colorScheme.background.luminance() < .5f)
    Color(0x667F90A8) else Color(0x669D9AB2)
