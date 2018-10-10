package jp.com.labit.bukuma.ui.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.util.Rfc822Tokenizer
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import jp.com.labit.bukuma.ui.custom.chips.RecipientEditTextView

/**
 * Created by tani on 2017/07/05.
 */
class TagEditText(context: Context,
                  attrs: AttributeSet? = null): RecipientEditTextView(context, attrs) {

  private var preImeListener: OnKeyListener? = null

  init {
    setTokenizer(Rfc822Tokenizer())
    addTextChangedListener(object: TextWatcher {
      override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) return
        var i = s.length - 1
        if (s[i].isWhitespace()) {
          if (i == 0 || s[i - 1] == ' ') {
            s.delete(i, i + 1)
          } else if (s[i - 1] != ',' && s[i - 1] != ';') {
            s.replace(i, i + 1, ";")
          }
        } else if (s[i] == ',' || s[i] == ';') {
          s.delete(i, i + 1)
        }
      }

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

      }
    })
  }

  override fun onEditorAction(view: TextView?, action: Int, keyEvent: KeyEvent?): Boolean {
    if (super.onEditorAction(view, action, keyEvent)) {
      return true
    }
    return true
  }

  override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
    val consumed = preImeListener?.onKey(this, keyCode, event) ?: false
    return if (consumed) true else super.onKeyPreIme(keyCode, event)
  }

  fun setOnKeyPreImeListener(preImeListener: (v: View, keyCode: Int, event: KeyEvent) -> Boolean) {
    this.preImeListener = View.OnKeyListener(preImeListener)
  }
}