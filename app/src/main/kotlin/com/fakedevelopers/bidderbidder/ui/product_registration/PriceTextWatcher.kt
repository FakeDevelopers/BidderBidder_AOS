package com.fakedevelopers.bidderbidder.ui.product_registration

import android.text.Editable
import android.text.Selection
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.EditText
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.dec

class PriceTextWatcher(
    private val editText: EditText,
    private val checkCondition: () -> Unit
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
        checkCondition()
    }

    private fun makeComma(price: String): String {
        price.replace(",", "").toLongOrNull()?.let {
            return dec.format(it)
        }
        return ""
    }
}
