package com.projectbharat.counterapp

import org.junit.Assert.assertEquals
import org.junit.Test

class MantraMatcherTest {
    @Test fun singleWordRequiresWholeWord() {
        assertEquals(0, MantraMatcher.count("आज मौसम अच्छा है", "राम"))
        assertEquals(0, MantraMatcher.count("रामचंद्र विराम", "राम"))
        assertEquals(2, MantraMatcher.count("राम राम", "राम"))
    }

    @Test fun wholeMantraMustMatchInOrder() {
        val mantra = "दिगंबरा दिगंबरा श्रीपाद वल्लभ दिगंबरा"
        assertEquals(0, MantraMatcher.count("श्रीपाद वल्लभ", mantra))
        assertEquals(0, MantraMatcher.count("दिगंबरा श्रीपाद वल्लभ दिगंबरा", mantra))
        assertEquals(1, MantraMatcher.count(mantra, mantra))
        assertEquals(2, MantraMatcher.count("$mantra $mantra", mantra))
    }

    @Test fun honorsDevanagariVowelMarksAndPunctuation() {
        assertEquals(0, MantraMatcher.count("रम", "राम"))
        assertEquals(1, MantraMatcher.count("ओम नमः शिवाय", "ॐ नमः शिवाय॥"))
        assertEquals(0, MantraMatcher.count("ओम नम शिवाय", "ॐ नमः शिवाय"))
        assertEquals(1, MantraMatcher.count("RAM, ram", "Ram Ram"))
    }
}
