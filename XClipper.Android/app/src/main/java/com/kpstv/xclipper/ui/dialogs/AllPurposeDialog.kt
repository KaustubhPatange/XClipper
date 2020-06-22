package com.kpstv.xclipper.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.kpstv.xclipper.App.BLANK_STRING
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.collapse
import com.kpstv.xclipper.extensions.show
import kotlinx.android.synthetic.main.dialog_allpurpose.view.*
import kotlinx.android.synthetic.main.dialog_allpurpose.view.toolbar

class AllPurposeDialog : DialogFragment() {
    private lateinit var alertDialog: AlertDialog

    /** Default values... */
    private var isProgressDialog = false
    private var message = ""

    private var isPositiveButton = true
    private var positiveButtonText = "OK"
    private var positiveOnClick: SimpleFunction = { }

    private var isNegativeButton = false
    private var negativeButtonText = "Cancel"
    private var negativeOnClick: SimpleFunction = { }

    private var isNeutralButton = false
    private var neutralButtonText = "Cancel"
    private var neutralOnClick: SimpleFunction = { }

    private lateinit var mainView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        mainView = LayoutInflater.from(context).inflate(R.layout.dialog_allpurpose, null)

        with(mainView) {
            toolbar.navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_close)
            toolbar.setNavigationOnClickListener { alertDialog.dismiss() }
        }

        alertDialog = AlertDialog.Builder(context).apply {
            setView(mainView)
            setPositiveButton(BLANK_STRING) { _, _ ->
                positiveOnClick.invoke()
            }
            setNegativeButton(BLANK_STRING) { _, _ ->
                negativeOnClick.invoke()
            }
            setNeutralButton(BLANK_STRING) { _, _ ->
                neutralOnClick.invoke()
            }
        }.create()

        setDialog()

        return alertDialog
    }

    fun setMessage(text: String): AllPurposeDialog {
        message = text
        return this
    }

    fun setIsProgressDialog(boolean: Boolean): AllPurposeDialog {
        isProgressDialog = boolean
        return this
    }

    fun setPositiveButton(buttonText: String, block: SimpleFunction): AllPurposeDialog {
        isPositiveButton = true
        positiveButtonText = buttonText
        positiveOnClick = block
        return this
    }

    fun setNegativeButton(buttonText: String, block: SimpleFunction): AllPurposeDialog {
        isNegativeButton = true
        negativeButtonText = buttonText
        negativeOnClick = block
        return this
    }

    fun setNeutralButton(buttonText: String, block: SimpleFunction): AllPurposeDialog {
        isNeutralButton = true
        neutralButtonText = buttonText
        neutralOnClick = block
        return this
    }

    fun setToolbarMenu(@MenuRes id: Int): AllPurposeDialog {
        mainView.toolbar.inflateMenu(id)
        return this
    }

    fun setToolbarMenuItemListener(listener: (item: MenuItem)-> Boolean): AllPurposeDialog {
        mainView.toolbar.setOnMenuItemClickListener(listener)
        return this
    }

    fun clearToolbarMenu() = mainView.toolbar.menu.clear()

    fun update() = setDialog()

    fun setShowPositiveButton(boolean: Boolean): AllPurposeDialog {
        isPositiveButton = boolean
        return this
    }

    fun setShowNegativeButton(boolean: Boolean): AllPurposeDialog {
        isNegativeButton = boolean
        return this
    }

    fun setShowNeutralButton(boolean: Boolean): AllPurposeDialog {
        isNeutralButton = boolean
        return this
    }

    private fun setDialog() = with(mainView) {

        if (isProgressDialog) {
            all_progress.show()
            all_textView.collapse()
        } else {
            all_progress.collapse()
            all_textView.show()
        }

        all_textView.text = message

        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        if (isPositiveButton) {
            positiveButton?.apply {
                text = positiveButtonText
                visibility = View.VISIBLE
            }
        }else {
            positiveButton?.apply {
                visibility = View.GONE
            }
        }

        val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        if (isNegativeButton) {
            negativeButton?.apply {
                text = negativeButtonText
                visibility = View.VISIBLE
            }
        }else
        {
            negativeButton?.apply {
                visibility = View.GONE
            }
        }

        val neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        if (isNeutralButton) {
            neutralButton?.apply {
                text = neutralButtonText
                visibility = View.VISIBLE
            }
        }else {
            neutralButton?.apply {
                visibility = View.GONE
            }
        }

        Unit
    }
}