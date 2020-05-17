package com.kpstv.xclipper.ui.helpers

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.kpstv.license.Decrypt
import com.kpstv.license.Encrypt
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.clone
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.dialog_edit_layout.view.*

class MainEditHelper(
    private val context: Context,
    private val viewModel: MainViewModel
) {
    private lateinit var dialog: AlertDialog
    /**
     * Call this function to execute the edit event.
     */
    fun show(clip: Clip) =
        with(context) {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_layout, null)

            view.toolbar.navigationIcon = getDrawable(R.drawable.ic_close)
            view.toolbar.setNavigationOnClickListener {
                dialog.dismiss()
            }

            view.de_editText.setText(clip.data?.Decrypt())

            dialog = AlertDialog.Builder(this).apply {
                setView(view)
                setPositiveButton(getString(R.string.ok)) { _, _ ->
                    val text = view.de_editText.text.toString()

                    if (text.isNotBlank() && text != clip.data?.Decrypt()) {
                        viewModel.postUpdateToRepository(clip, clip.clone(text.Encrypt()))
                        Toast.makeText(this@with, getString(R.string.edit_success), Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }else
                        Toast.makeText(this@with, getString(R.string.error_empty_text), Toast.LENGTH_SHORT).show()
                }
            }.create()

            dialog.show()
        }
}