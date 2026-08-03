package com.project.topakuli.util

import kotlin.math.min

object FuzzySearchHelper {

    /**
     * Menghitung Levenshtein Distance antara dua string.
     * Semakin kecil nilainya, semakin mirip string tersebut.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) {
            for (j in 0..s2.length) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    else -> {
                        val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                        dp[i][j] = min(
                            min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + cost
                        )
                    }
                }
            }
        }
        return dp[s1.length][s2.length]
    }

    /**
     * Mengecek apakah string target mengandung atau mirip dengan query.
     * Menggunakan threshold untuk menentukan tingkat kemiripan.
     */
    fun isSimilar(query: String, target: String, threshold: Double = 0.45): Boolean {
        val q = query.lowercase().trim()
        val t = target.lowercase().trim()

        // 1. Cek mengandung kata kunci secara langsung (Sub-string match)
        if (t.contains(q) || q.contains(t)) return true

        val queryWords = q.split(" ").filter { it.length >= 2 }
        val targetWords = t.split(" ", ",", "\n", ".", "(", ")", "-").filter { it.length >= 2 }

        for (qw in queryWords) {
            // 2. Cek Prefix Match (misal: "flur" match "flu")
            if (targetWords.any { it.startsWith(qw) || qw.startsWith(it) }) return true

            for (tw in targetWords) {
                // 3. Levenshtein Distance (Fuzzy Match)
                val distance = levenshteinDistance(qw, tw)
                val maxLength = maxOf(qw.length, tw.length)
                val similarity = 1.0 - (distance.toDouble() / maxLength)
                
                if (similarity >= (1.0 - threshold)) return true

                // 4. Vowel-blind comparison (untuk typo huruf vokal/konsonan seperti "batuk" -> "bitok")
                if (qw.length >= 3 && tw.length >= 3) {
                    val qwNoVowels = qw.replace(Regex("[aeiou]"), "")
                    val twNoVowels = tw.replace(Regex("[aeiou]"), "")
                    
                    if (qwNoVowels == twNoVowels) return true
                    
                    // Allow 1 char difference even without vowels for longer words
                    if (qwNoVowels.length >= 3 && levenshteinDistance(qwNoVowels, twNoVowels) <= 1) return true
                }
            }
        }

        return false
    }
}