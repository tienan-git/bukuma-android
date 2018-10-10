package jp.com.labit.bukuma.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.EditText

/**
 * Created by tani on 2017/06/26.
 */
class ImeInterceptEditText(context: Context,
                           attrs: AttributeSet? = null): EditText(context, attrs) {

  private var preImeListener: OnKeyListener? = null

  override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
    val consumed = preImeListener?.onKey(this, keyCode, event) ?: false
    return if (consumed) true else super.onKeyPreIme(keyCode, event)
  }

  fun setOnKeyPreImeListener(preImeListener: (v: View, keyCode: Int , event: KeyEvent) -> Boolean) {
    this.preImeListener = View.OnKeyListener(preImeListener)
  }
}