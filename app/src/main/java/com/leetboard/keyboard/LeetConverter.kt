package com.leetboard.keyboard

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LeetConverter(private val context: Context) {

    companion object {
        val DEFAULT_CHAR_RULES: Map<Char, String> = mapOf(
            'a' to "4",
            'e' to "3",
            'i' to "1",
            'o' to "0",
            's' to "5",
            't' to "7",
            'b' to "8",
            'g' to "6",
            'z' to "2",
            'l' to "1"
        )

        val DEFAULT_WORD_RULES: Map<String, String> = mapOf(
            "you" to "Y0U",
            "your" to "Y0UR",
            "love" to "L0V3",
            "friend" to "FR13ND",
            "hacker" to "H4CK3R",
            "gaming" to "G4M1NG",
            "gamer" to "G4M3R",
            "hello" to "H3LL0",
            "tere" to "T3R3",
            "meri" to "M3R1",
            "jaan" to "J44N",
            "pyar" to "PY4R",
            "bhai" to "8H41",
            "bro" to "8R0",
            "world" to "W0RLD",
            "code" to "C0D3",
            "hack" to "H4CK",
            "elite" to "3L1T3"
        )
    }

    private val prefsKeyChars = "leet_char_rules"
    private val prefsKeyWords = "leet_word_rules"
    private val prefsName = "leet_prefs"

    private val gson = Gson()

    private val charRules: Map<Char, String>
    private val wordRules: Map<String, String>

    init {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val charJson = prefs.getString(prefsKeyChars, null)
        val wordJson = prefs.getString(prefsKeyWords, null)

        val loadedCharRules: Map<String, String> = if (!charJson.isNullOrBlank()) {
            try {
                gson.fromJson(charJson, object : TypeToken<Map<String, String>>() {}.type)
            } catch (_: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }

        val loadedWordRules: Map<String, String> = if (!wordJson.isNullOrBlank()) {
            try {
                gson.fromJson(wordJson, object : TypeToken<Map<String, String>>() {}.type)
            } catch (_: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }

        val mergedChar = LinkedHashMap<Char, String>()
        mergedChar.putAll(DEFAULT_CHAR_RULES)
        for ((k, v) in loadedCharRules) {
            if (k.isNotEmpty()) {
                val c = k[0].lowercaseChar()
                mergedChar[c] = v
            }
        }

        val mergedWords = LinkedHashMap<String, String>()
        mergedWords.putAll(DEFAULT_WORD_RULES)
        for ((k, v) in loadedWordRules) {
            mergedWords[k.lowercase()] = v
        }

        charRules = mergedChar.toMap()
        wordRules = mergedWords.toMap()
    }

    fun convertToLeet(text: String): String {
        if (text.isEmpty()) return text

        val tokenRegex = Regex("\\s+|[^\\s]+")
        val out = StringBuilder()

        for (match in tokenRegex.findAll(text)) {
            val token = match.value
            if (token.isBlank()) {
                out.append(token)
                continue
            }

            val leading = token.takeWhile { !it.isLetterOrDigit() }
            val trailing = token.reversed().takeWhile { !it.isLetterOrDigit() }.reversed().joinToString("")
            val coreStart = leading.length
            val coreEnd = token.length - trailing.length
            if (coreStart >= coreEnd) {
                out.append(token)
                continue
            }

            val core = token.substring(coreStart, coreEnd)
            val coreLower = core.lowercase()

            val replaced = if (wordRules.containsKey(coreLower)) {
                wordRules[coreLower] ?: core
            } else {
                applyCharRulesToWord(core)
            }

            out.append(leading)
            out.append(replaced)
            out.append(trailing)
        }

        return out.toString()
    }

    fun applyCharRulesToWord(word: String): String {
        if (word.isEmpty()) return word
        val sb = StringBuilder()
        for (ch in word) {
            val mapped = charRules[ch.lowercaseChar()]
            if (mapped != null) {
                if (ch.isUpperCase()) sb.append(mapped.uppercase()) else sb.append(mapped)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun getMergedCharRules(): Map<Char, String> = charRules
    fun getMergedWordRules(): Map<String, String> = wordRules
}
