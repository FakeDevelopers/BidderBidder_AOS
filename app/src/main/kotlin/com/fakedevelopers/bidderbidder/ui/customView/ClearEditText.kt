package com.fakedevelopers.bidderbidder.ui.customView

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class ClearEditText : AppCompatEditText, TextWatcher, View.OnFocusChangeListener {

    private val clearDrawable by lazy {
        ContextCompat.getDrawable(context, androidx.appcompat.R.drawable.abc_ic_clear_material)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        clearDrawable?.let {
            DrawableCompat.setTintList(it, hintTextColors)
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
        }
        setClearIconVisible(false)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // 안써!
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (isFocused) {
            setClearIconVisible(!text.isNullOrEmpty())
        }
    }

    override fun afterTextChanged(s: Editable?) {
        // 안써!
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        performClick()
        clearDrawable?.let {
            // x 버튼이 있는 영역을 클릭
            if (it.isVisible && event.x > width - paddingRight - it.intrinsicWidth) {
                if (event.action == ACTION_UP) {
                    error = null
                    setText("")
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        // 안써!
        // onTouchEvent가 override 해달래서 해줌
        return super.performClick()
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus) {
            setClearIconVisible(!text.isNullOrEmpty())
        } else {
            setClearIconVisible(false)
        }

        onFocusChangeListener.onFocusChange(v, hasFocus)
    }

    private fun setClearIconVisible(visible: Boolean) {
        clearDrawable?.let {
            it.setVisible(visible, false)
            setCompoundDrawables(null, null, if (visible) clearDrawable else null, null)
        }
    }
}
