package com.leetboard.keyboard

import android.text.Editable
import android.text.TextWatcher

class SimpleTextWatcher(private val callback: (String?) -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        callback(s?.toString())
    }
    override fun afterTextChanged(s: Editable?) = Unit
}
