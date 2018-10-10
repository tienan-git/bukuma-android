package jp.com.labit.bukuma.ui.custom

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.View
import android.widget.*
import jp.com.labit.bukuma.R

/**
 * Created by zoonooz on 9/20/2016 AD.
 * Form view for input text
 */
class FormView : FrameLayout {

  private val STATE_SUPER = "state_super"
  private val STATE_TEXT = "state_text"
  private val STATE_POSITION = "state_position"

  // cannot join declaration as lint says
  var titleView: TextView
  var inputView: EditText
  var spinnerView: Spinner

  var inputType = 0
  var entries: Array<String>? = null

  constructor(context: Context) : super(context)
  constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
    applyAttrs(attributeSet)
  }

  constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(context, attributeSet, defStyle) {
    applyAttrs(attributeSet)
  }

  init {
    inflate(context, R.layout.view_form, this)
    titleView = findViewById(R.id.title_textview) as TextView
    inputView = findViewById(R.id.input) as EditText
    spinnerView = findViewById(R.id.spinner) as Spinner

  }

  override fun onSaveInstanceState(): Parcelable {
    val bundle = Bundle()
    bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState())
    bundle.putString(STATE_TEXT, inputView.text.toString())
    bundle.putInt(STATE_POSITION, spinnerView.selectedItemPosition)
    return bundle
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state is Bundle) {
      super.onRestoreInstanceState(state.getParcelable(STATE_SUPER))
      inputView.post { inputView.setText(state.getString(STATE_TEXT)) }
      spinnerView.post { spinnerView.setSelection(state.getInt(STATE_POSITION)) }
    } else super.onRestoreInstanceState(state)
  }

  private fun applyAttrs(attributeSet: AttributeSet) {
    val a = context.theme.obtainStyledAttributes(attributeSet, R.styleable.FormView, 0, 0)

    try {
      titleView.text = a.getString(R.styleable.FormView_title)

      val entriesId = a.getResourceId(R.styleable.FormView_entries, 0)
      if (entriesId != 0) {
        // spinner mode
        inputView.visibility = View.GONE
        spinnerView.visibility = View.VISIBLE
        entries = resources.getStringArray(entriesId)
        val adapter = ArrayAdapter(context, R.layout.formview_spinner_item, entries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerView.adapter = adapter
      } else {
        // normal edit text mode
        a.getString(R.styleable.FormView_hint)?.let { inputView.hint = it }
        a.getString(R.styleable.FormView_text)?.let { inputView.setText(it) }

        // digits
        a.getString(R.styleable.FormView_digits)?.let {
          inputView.keyListener = DigitsKeyListener.getInstance(it)
        }

        // length
        a.getInt(R.styleable.FormView_limit, 0).let {
          if (it != 0) inputView.filters = arrayOf(InputFilter.LengthFilter(it))
        }

        inputType = a.getInteger(R.styleable.FormView_input, 0)
        when (inputType) {
          1 -> inputView.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
          2 -> {
            inputView.transformationMethod = PasswordTransformationMethod.getInstance()
            inputView.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
          }
          3 -> {
            inputView.keyListener = DigitsKeyListener.getInstance("1234567890")
            inputView.setRawInputType(InputType.TYPE_CLASS_PHONE)
          }
        }
      }

      // editable
      if (!a.getBoolean(R.styleable.FormView_editable, true)) {
        inputView.isEnabled = false
        spinnerView.isEnabled = false
      }
    } finally {
      a.recycle()
    }
  }

  var text: String
    get() {
      if (entries != null) return entries!![spinnerView.selectedItemPosition]
      return inputView.text.toString()
    }
    set(value) {
      if (entries != null) {
        spinnerView.setSelection(entries!!.indexOf(value))
      } else {
        inputView.setText(value)
      }
    }

  var error: String? = null
    set(value) {
      inputView.error = value
    }
}
