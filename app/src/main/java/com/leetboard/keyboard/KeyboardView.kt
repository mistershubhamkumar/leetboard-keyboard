package com.leetboard.keyboard

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val density = resources.displayMetrics.density
    private var listener: OnKeyboardListener? = null
    private var modeName: String = "EN"
    private var shiftOn: Boolean = false
    private var symbolsOn: Boolean = false
    private var matraOn: Boolean = false
    private val rowContainer: LinearLayout

    init {
        orientation = VERTICAL
        val root = LayoutInflater.from(context).inflate(R.layout.keyboard_view_root, this, true)
        rowContainer = root.findViewById(R.id.row_container)
        render()
    }

    fun setOnKeyboardListener(l: OnKeyboardListener) {
        listener = l
    }

    fun setMode(mode: String) {
        modeName = mode
        symbolsOn = false
        matraOn = false
        render()
    }

    fun setShift(on: Boolean) {
        shiftOn = on
        render()
    }

    fun toggleSymbols() {
        symbolsOn = !symbolsOn
        render()
    }

    private fun dp(value: Number): Int = (value.toFloat() * density).toInt()

    private fun clearRows() {
        rowContainer.removeAllViews()
    }

    private fun render() {
        clearRows()

        val functionRow = createRow()
        val modeBtn = makeKeyButton(
            when (modeName) {
                "EN" -> "EN"
                "HI" -> "हिं"
                else -> "1337"
            },
            isMode = true
        )
        modeBtn.setOnClickListener { listener?.onModeSwitch() }
        functionRow.addView(modeBtn)

        val shiftBtn = makeKeyButton("Shift")
        shiftBtn.setOnClickListener { listener?.onShiftToggle() }
        functionRow.addView(shiftBtn)

        val symbolBtn = makeKeyButton("123")
        symbolBtn.setOnClickListener { listener?.onKeyPress("123") }
        functionRow.addView(symbolBtn)

        val backspaceBtn = makeKeyButton("⌫")
        backspaceBtn.setOnClickListener { listener?.onKeyPress("backspace") }
        functionRow.addView(backspaceBtn)

        rowContainer.addView(functionRow)

        when (modeName) {
            "EN" -> renderEnglish()
            "HI" -> renderHindi()
            "LEET" -> renderLeet()
            else -> renderEnglish()
        }

        val bottomRow = createRow()
        val spaceBtn = makeKeyButton("space", weight = 1.5f)
        spaceBtn.setOnClickListener { listener?.onKeyPress("space") }
        bottomRow.addView(spaceBtn)

        val enterBtn = makeKeyButton("Enter")
        enterBtn.setOnClickListener { listener?.onKeyPress("enter") }
        bottomRow.addView(enterBtn)

        rowContainer.addView(bottomRow)
    }

    private fun renderEnglish() {
        if (symbolsOn) {
            val rows = listOf(
                listOf("1","2","3","4","5","6","7","8","9","0"),
                listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/"),
                listOf("*", "\"", "'", ":", ";", "!", "?", "%", "^", "~")
            )
            for (r in rows) addKeyRow(r)
            return
        }

        addKeyRow(listOf("q","w","e","r","t","y","u","i","o","p"))
        addKeyRow(listOf("a","s","d","f","g","h","j","k","l"))
        addKeyRow(listOf("z","x","c","v","b","n","m"))
    }

    private fun renderLeet() {
        if (symbolsOn) {
            val rows = listOf(
                listOf("1","2","3","4","5","6","7","8","9","0"),
                listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/"),
                listOf("*", "\"", "'", ":", ";", "!", "?", "%", "^", "~")
            )
            for (r in rows) addKeyRow(r)
            return
        }

        addKeyRow(listOf("q","w","e","r","t","y","u","i","o","p"))
        addKeyRow(listOf("a","s","d","f","g","h","j","k","l"))
        addKeyRow(listOf("z","x","c","v","b","n","m"))
    }

    private fun renderHindi() {
        addKeyRow(listOf("अ","आ","इ","ई","उ","ऊ","ए","ऐ","ओ","औ"))
        addKeyRow(listOf("क","ख","ग","घ","च","छ","ज","झ","ञ"))
        addKeyRow(listOf("ट","ठ","ड","ढ","ण","त","थ","द","ध","न"))
        addKeyRow(listOf("प","फ","ब","भ","म","य","र","ल","व"))
        addKeyRow(listOf("श","ष","स","ह","क्ष","त्र","ज्ञ","श्र"))

        val matraRow = createRow()
        val matraBtn = makeKeyButton("मात्रा")
        matraBtn.setOnClickListener {
            matraOn = !matraOn
            render()
        }
        matraRow.addView(matraBtn)

        val numbersBtn = makeKeyButton("०१२३४५६७८९")
        numbersBtn.setOnClickListener {
            symbolsOn = !symbolsOn
            render()
        }
        matraRow.addView(numbersBtn)
        rowContainer.addView(matraRow)

        if (matraOn) {
            addKeyRow(listOf("ा","ि","ी","ु","ू","े","ै","ो","ौ","ं","ः","्"))
        }
    }

    private fun addKeyRow(keys: List<String>) {
        val row = createRow()
        val scroll = HorizontalScrollView(context).apply { isHorizontalScrollBarEnabled = false }
        val inner = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
        }

        for (key in keys) {
            val label = if (shiftOn) key.uppercase() else key
            val btn = makeKeyButton(label)
            btn.setOnClickListener { listener?.onKeyPress(key) }

            btn.setOnLongClickListener {
                val alternates = getAlternatesForKey(key)
                if (alternates.isNotEmpty()) {
                    listener?.onKeyLongPress(key, alternates)
                    true
                } else false
            }

            inner.addView(btn)
        }

        scroll.addView(inner)
        row.addView(scroll)
        rowContainer.addView(row)
    }

    private fun getAlternatesForKey(key: String): List<String> {
        // Provide helpful long-press alternatives for English letters and Hindi consonants
        val lower = key.lowercase()

        val hindiConsonants = setOf(
            "क","ख","ग","घ","च","छ","ज","झ","ट","ठ","ड","ढ","ण",
            "त","थ","द","ध","न","प","फ","ब","भ","म","य","र","ल","व",
            "श","ष","स","ह","क्ष","त्र","ज्ञ","श्र"
        )

        if (key in hindiConsonants) {
            val matras = listOf("ा","ि","ी","ु","ू","े","ै","ो","ौ","ं","ः","्")
            return matras.map { m -> key + m }
        }

        return when (lower) {
            "a" -> listOf("á","à","ä","â","ã","å")
            "e" -> listOf("é","è","ë","ê")
            "i" -> listOf("í","ì","ï","î")
            "o" -> listOf("ó","ò","ö","ô","õ")
            "u" -> listOf("ú","ù","ü","û")
            "s" -> listOf("ß")
            "c" -> listOf("ç")
            else -> emptyList()
        }
    }

    private fun createRow(): LinearLayout {
        val row = LinearLayout(context)
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(6)
        row.layoutParams = lp
        row.orientation = HORIZONTAL
        row.gravity = Gravity.CENTER
        return row
    }

    private fun makeKeyButton(text: String, weight: Float = 1f, isMode: Boolean = false): MaterialButton {
        val btn = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)
        val lp = LayoutParams(0, dp(48), weight)
        lp.marginStart = dp(4)
        lp.marginEnd = dp(4)
        btn.layoutParams = lp
        btn.text = text
        btn.textSize = 16f
        btn.typeface = Typeface.DEFAULT_BOLD
        btn.setTextColor(ContextCompat.getColor(context, R.color.key_text))
        btn.setBackgroundResource(if (isMode) R.drawable.mode_key_background else R.drawable.key_background)
        btn.isAllCaps = false
        return btn
    }

    interface OnKeyboardListener {
        fun onKeyPress(key: String)
        fun onModeSwitch()
        fun onShiftToggle()
        fun onKeyLongPress(key: String, alternates: List<String>)
    }
}
