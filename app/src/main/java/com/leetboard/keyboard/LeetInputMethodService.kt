package com.leetboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import com.leetboard.keyboard.databinding.KeyboardViewBinding

class LeetInputMethodService : InputMethodService() {

    private lateinit var binding: KeyboardViewBinding
    private lateinit var keyboardView: KeyboardView
    private lateinit var leetConverter: LeetConverter

    private var currentMode: Mode = Mode.EN
    private var shiftOn: Boolean = false
    private val composingRaw = StringBuilder()

    enum class Mode {
        EN, HI, LEET
    }

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
        })

        keyboardView.setMode(currentMode.name)
        keyboardView.setShift(shiftOn)
        return binding.root
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
                    if (converted.isEmpty()) {
                        ic.finishComposingText()
                    } else {
                        ic.setComposingText(converted, 1)
                    }
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
            }

            "space" -> {
                if (currentMode == Mode.LEET && composingRaw.isNotEmpty()) {
                    val final = leetConverter.convertToLeet(composingRaw.toString())
                    ic.commitText(final + " ", 1)
                    composingRaw.clear()
                } else {
                    ic.commitText(" ", 1)
                }
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

            "shift" -> {
                shiftOn = !shiftOn
                keyboardView.setShift(shiftOn)
            }

            else -> {
                when (currentMode) {
                    Mode.EN -> {
                        val out = if (shiftOn) key.uppercase() else key
                        ic.commitText(out, 1)
                    }
                    Mode.HI -> {
                        ic.commitText(key, 1)
                    }
                    Mode.LEET -> {
                        if (key.isBlank()) {
                            ic.commitText(key, 1)
                            return
                        }

                        val isWordChar = key.any { it.isLetterOrDigit() }
                        if (!isWordChar) {
                            if (composingRaw.isNotEmpty()) {
                                val final = leetConverter.convertToLeet(composingRaw.toString())
                                ic.commitText(final + key, 1)
                                composingRaw.clear()
                            } else {
                                ic.commitText(key, 1)
                            }
                        } else {
                            val applied = if (shiftOn) key.uppercase() else key
                            composingRaw.append(applied)
                            val converted = leetConverter.applyCharRulesToWord(composingRaw.toString())
                            ic.setComposingText(converted, 1)
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
