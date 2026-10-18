package com.leetboard.keyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SettingsActivity : AppCompatActivity() {

    private val prefsName = "leet_prefs"
    private val prefsKeyChars = "leet_char_rules"
    private val prefsKeyWords = "leet_word_rules"
    private val gson = Gson()

    private lateinit var charList: LinearLayout
    private lateinit var wordList: LinearLayout
    private lateinit var charFrom: EditText
    private lateinit var charTo: EditText
    private lateinit var charAdd: Button
    private lateinit var wordFrom: EditText
    private lateinit var wordTo: EditText
    private lateinit var wordAdd: Button
    private lateinit var previewInput: EditText
    private lateinit var previewOutput: TextView
    private lateinit var openInputSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        charList = findViewById(R.id.char_rules_list)
        wordList = findViewById(R.id.word_rules_list)
        charFrom = findViewById(R.id.char_from)
        charTo = findViewById(R.id.char_to)
        charAdd = findViewById(R.id.char_add)
        wordFrom = findViewById(R.id.word_from)
        wordTo = findViewById(R.id.word_to)
        wordAdd = findViewById(R.id.word_add)
        previewInput = findViewById(R.id.preview_input)
        previewOutput = findViewById(R.id.preview_output)
        openInputSettings = findViewById(R.id.open_input_settings)

        charFrom.filters = arrayOf(InputFilter.LengthFilter(1))

        charAdd.setOnClickListener {
            val from = charFrom.text.toString().trim()
            val to = charTo.text.toString().trim()
            if (from.isNotEmpty() && to.isNotEmpty()) {
                addCharRule(from, to)
                charFrom.setText("")
                charTo.setText("")
                loadRules()
            }
        }

        wordAdd.setOnClickListener {
            val from = wordFrom.text.toString().trim()
            val to = wordTo.text.toString().trim()
            if (from.isNotEmpty() && to.isNotEmpty()) {
                addWordRule(from.lowercase(), to)
                wordFrom.setText("")
                wordTo.setText("")
                loadRules()
            }
        }

        previewInput.addTextChangedListener(SimpleTextWatcher { text ->
            previewOutput.text = LeetConverter(this).convertToLeet(text ?: "")
        })

        openInputSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        loadRules()
        previewOutput.text = LeetConverter(this).convertToLeet(previewInput.text.toString())
    }

    private fun loadRules() {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)

        val charMap: MutableMap<String, String> = try {
            val json = prefs.getString(prefsKeyChars, null)
            if (json.isNullOrBlank()) mutableMapOf() else gson.fromJson(json, object : TypeToken<MutableMap<String, String>>() {}.type)
        } catch (_: Exception) {
            mutableMapOf()
        }

        val wordMap: MutableMap<String, String> = try {
            val json = prefs.getString(prefsKeyWords, null)
            if (json.isNullOrBlank()) mutableMapOf() else gson.fromJson(json, object : TypeToken<MutableMap<String, String>>() {}.type)
        } catch (_: Exception) {
            mutableMapOf()
        }

        charList.removeAllViews()
        for ((key, value) in charMap) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            val text = TextView(this).apply {
                text = "$key → $value"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val removeBtn = Button(this).apply {
                text = "Remove"
                setOnClickListener {
                    charMap.remove(key)
                    saveMap(prefsKeyChars, charMap)
                    loadRules()
                }
            }

            row.addView(text)
            row.addView(removeBtn)
            charList.addView(row)
        }

        wordList.removeAllViews()
        for ((key, value) in wordMap) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            val text = TextView(this).apply {
                text = "$key → $value"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val removeBtn = Button(this).apply {
                text = "Remove"
                setOnClickListener {
                    wordMap.remove(key)
                    saveMap(prefsKeyWords, wordMap)
                    loadRules()
                }
            }

            row.addView(text)
            row.addView(removeBtn)
            wordList.addView(row)
        }
    }

    private fun addCharRule(from: String, to: String) {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val json = prefs.getString(prefsKeyChars, null)
        val map: MutableMap<String, String> = try {
            if (json.isNullOrBlank()) mutableMapOf() else gson.fromJson(json, object : TypeToken<MutableMap<String, String>>() {}.type)
        } catch (_: Exception) {
            mutableMapOf()
        }
        map[from.lowercase()] = to
        saveMap(prefsKeyChars, map)
    }

    private fun addWordRule(from: String, to: String) {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val json = prefs.getString(prefsKeyWords, null)
        val map: MutableMap<String, String> = try {
            if (json.isNullOrBlank()) mutableMapOf() else gson.fromJson(json, object : TypeToken<MutableMap<String, String>>() {}.type)
        } catch (_: Exception) {
            mutableMapOf()
        }
        map[from.lowercase()] = to
        saveMap(prefsKeyWords, map)
    }

    private fun saveMap(key: String, map: Map<String, String>) {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        prefs.edit().putString(key, gson.toJson(map)).apply()
    }
}
