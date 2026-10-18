package com.leetboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.PopupWindow
import android.widget.ListView
import android.widget.ArrayAdapter
import android.view.Gravity
import android.view.ViewGroup
import com.leetboard.keyboard.databinding.KeyboardViewBinding

class LeetInputMethodService : InputMethodService() {

    private lateinit var binding: KeyboardViewBinding
    private lateinit var keyboardView: KeyboardView
    private lateinit var leetConverter: LeetConverter

    private var currentMode: Mode = Mode.EN
    private var shiftOn: Boolean = false
    private val composingRaw = StringBuilder()

    enum class Mode { EN, HI, LEET }

    override fun onCreate() {
        super.onCreate()
        leetConverter = LeetConverter(this)
    }

    override fun onCreateInputView(): View {
        binding = KeyboardViewBinding.inflate(layoutInflater)
        keyboardView = binding.keyboardView
        keyboardView.setOnKeyboardListener(object : KeyboardView.OnKeyboardListener {
            override fun onKeyPress(key: String) {
                handleKeyPress(key)
            }

            override fun onModeSwitch() {
                currentMode = when (currentMode) {
                    Mode.EN -> Mode.HI
                    Mode.HI -> Mode.LEET
                    Mode.LEET -> Mode.EN
                }
                keyboardView.setMode(currentMode.name)
                clearComposing()
            }

            override fun onShiftToggle() {
                shiftOn = !shiftOn
                keyboardView.setShift(shiftOn)
            }

            override fun onKeyLongPress(key: String, alternates: List<String>) {
                showAlternatesDialog(key, alternates)
            }
        })

        keyboardView.setMode(currentMode.name)
        keyboardView.setShift(shiftOn)
        return binding.root
    }

    private fun showAlternatesDialog(key: String, alternates: List<String>) {
        if (alternates.isEmpty()) return
        val items = alternates.toTypedArray()

        // Create a simple ListView programmatically and show it in a PopupWindow.
        val listView = ListView(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        val popup = PopupWindow(
            listView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        listView.setOnItemClickListener { _, _, position, _ ->
            val selection = items[position]
            commitAlternate(selection)
            popup.dismiss()
        }

        // Show centered over the input view. This is safer than creating a free-floating AlertDialog from a service.
        try {
            popup.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        } catch (_: Exception) {
            // Fallback: commit first alternate if popup can't be shown
            commitAlternate(items[0])
        }
    }

    private fun commitAlternate(selection: String) {
        val ic = currentInputConnection ?: return
        when (currentMode) {
            Mode.EN -> ic.commitText(if (shiftOn) selection.uppercase() else selection, 1)
            Mode.HI -> ic.commitText(selection, 1)
            Mode.LEET -> {
                // If composing raw exists and selection is letter-like, append to composing.
                val isWord = selection.any { it.isLetterOrDigit() }
                if (isWord) {
                    composingRaw.append(selection)
                    val converted = leetConverter.applyCharRulesToWord(composingRaw.toString())
                    currentInputConnection.setComposingText(converted, 1)
                } else {
                    if (composingRaw.isNotEmpty()) {
                        val final = leetConverter.convertToLeet(composingRaw.toString())
                        ic.commitText(final + selection, 1)
                        composingRaw.clear()
                    } else {
                        ic.commitText(selection, 1)
                    }
                }
            }
        }
    }

    private fun clearComposing() {
        composingRaw.clear()
        currentInputConnection?.finishComposingText()
    }

    private fun handleKeyPress(key: String) {
        val ic = currentInputConnection ?: return

        when (key) {
            "backspace" -> {
                if (composingRaw.isNotEmpty()) {
                    composingRaw.deleteCharAt(composingRaw.length - 1)
                    val converted = leetConverter.applyCharRulesToWord(composingRaw.toString())
                    if (converted.isEmpty()) ic.finishComposingText() else ic.setComposingText(converted, 1)
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
            }

            "space" -> {
                if (currentMode == Mode.LEET && composingRaw.isNotEmpty()) {
                    val final = leetConverter.convertToLeet(composingRaw.toString())
                    ic.commitText(final + " ", 1)
                    composingRaw.clear()
                } else ic.commitText(" ", 1)
            }

            "enter" -> {
                if (currentMode == Mode.LEET && composingRaw.isNotEmpty()) {
                    val final = leetConverter.convertToLeet(composingRaw.toString())
                    ic.commitText(final, 1)
                    composingRaw.clear()
                }
                ic.commitText("\n", 1)
            }

            "123" -> {
                keyboardView.toggleSymbols()
                clearComposing()
            }

            else -> {
                when (currentMode) {
                    Mode.EN -> ic.commitText(if (shiftOn) key.uppercase() else key, 1)
                    Mode.HI -> ic.commitText(key, 1)
                    Mode.LEET -> {
                        val isWordChar = key.any { it.isLetterOrDigit() }
                        if (!isWordChar) {
                            if (composingRaw.isNotEmpty()) {
                                val final = leetConverter.convertToLeet(composingRaw.toString())
                                ic.commitText(final + key, 1)
                                composingRaw.clear()
                            } else ic.commitText(key, 1)
                        } else {
                            val appendChar = if (shiftOn) key.uppercase() else key
                            composingRaw.append(appendChar)
                            val composingConverted = leetConverter.applyCharRulesToWord(composingRaw.toString())
                            ic.setComposingText(composingConverted, 1)
                        }
                    }
                }
            }
        }
    }

    override fun onFinishInput() {
        super.onFinishInput()
        clearComposing()
    }
}
