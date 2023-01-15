package com.fakedevelopers.presentation.ui.productRegistration

import android.text.Editable
import android.text.InputFilter
import android.text.Selection
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.EditText
import com.fakedevelopers.domain.secret.Constants.Companion.dec

class PriceTextWatcher(
    private val editText: EditText,
    private val checkCondition: (() -> Unit)? = null
) : TextWatcher {

    private var strAmount = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // 안해!
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (!TextUtils.isEmpty(s.toString()) && s.toString() != strAmount) {
            strAmount = makeComma(s.toString())
            editText.setText(strAmount)
            Selection.setSelection(editText.text, strAmount.length)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        checkCondition?.invoke()
    }

    companion object {
        const val MAX_PRICE_LENGTH = 17
        const val MAX_TICK_LENGTH = 12
        const val MAX_CONTENT_LENGTH = 1000
        const val MAX_EXPIRATION_TIME = 72
        const val MAX_EXPIRATION_LENGTH = 3
        const val IS_NOT_NUMBER = "[^\\d]"

        fun addEditTextFilter(editText: EditText, length: Int, checkCondition: (() -> Unit)? = null) {
            val priceFilter = InputFilter { source, _, _, _, _, _ ->
                source.replace("[^\\d,]".toRegex(), "")
            }
            editText.filters = arrayOf(priceFilter, InputFilter.LengthFilter(length))
            editText.addTextChangedListener(PriceTextWatcher(editText) { checkCondition?.invoke() })
        }
        fun makeComma(price: String) =
            price.replace(",", "").toLongOrNull()?.let { dec.format(it) } ?: ""
    }
}
