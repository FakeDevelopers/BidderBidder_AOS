package com.fakedevelopers.bidderbidder.ui.product_registration

import android.text.Editable
import android.text.InputFilter
import android.text.Selection
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.EditText
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.dec

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

    private fun makeComma(price: String) =
        price.replace(",", "").toLongOrNull()?.let { dec.format(it) } ?: ""

    companion object {
        fun addEditTextFilter(editText: EditText, length: Int, checkCondition: (() -> Unit)? = null) {
            val priceFilter = InputFilter { source, _, _, _, _, _ ->
                source.replace("[^(0-9|,)]".toRegex(), "")
            }
            editText.filters = arrayOf(priceFilter, InputFilter.LengthFilter(length))
            editText.addTextChangedListener(PriceTextWatcher(editText) { checkCondition?.invoke() })
        }
    }
}
