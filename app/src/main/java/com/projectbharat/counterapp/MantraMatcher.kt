package com.projectbharat.counterapp

import java.text.Normalizer
import java.util.Locale

/** Counts complete, non-overlapping mantra token sequences in finalized speech text. */
internal object MantraMatcher {
    private val word = Regex("[\\p{L}\\p{M}\\p{N}]+")

    private fun words(value: String): List<String> {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFC)
            .lowercase(Locale.ROOT)
            // These are the same spoken syllable, commonly transcribed either way.
            .replace("ॐ", "ओम")
        return word.findAll(normalized).map { it.value }.toList()
    }

    fun count(transcript: String, mantra: String): Int {
        val target = words(mantra)
        val spoken = words(transcript)
        if (target.isEmpty() || spoken.size < target.size) return 0
        var position = 0
        var matches = 0
        while (position <= spoken.size - target.size) {
            if (target.indices.all { spoken[position + it] == target[it] }) {
                matches++
                position += target.size
            } else position++
        }
        return matches
    }
}
