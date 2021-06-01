package com.kpstv.xclipper.ui.dialogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.DialogFeatureBinding
import com.kpstv.xclipper.extensions.hasThemeColorAttribute
import com.kpstv.xclipper.extensions.layoutInflater
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeatureDialog(ctx: Context) {
    val context = if (!ctx.hasThemeColorAttribute(R.attr.colorTextPrimary))
        ContextThemeWrapper(ctx, R.style.AppTheme_Dark)
    else ctx
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    private var alertDialog: AlertDialog? = null
    private val binding: DialogFeatureBinding = DialogFeatureBinding.inflate(context.layoutInflater())

    private val dialogData = DialogData()

    init {
        binding.buttonOk.setOnClickListener { alertDialog?.dismiss() }
    }

    fun setResourceId(@DrawableRes id: Int): FeatureDialog {
        dialogData.drawable = id
        binding.imageView.setImageResource(id)
        return this
    }

    fun setTitle(@StringRes id: Int): FeatureDialog {
        binding.title.setText(id)
        return this
    }

    fun setTitle(title: CharSequence): FeatureDialog {
        binding.title.text = title
        return this
    }

    fun setSubtitle(@StringRes id: Int): FeatureDialog {
        binding.subtitle.setText(id)
        return this
    }

    fun setSubtitle(subtitle: CharSequence): FeatureDialog {
        binding.subtitle.text = subtitle
        return this
    }

    fun setOnClickListener(block: (AlertDialog) -> Unit) {
        binding.buttonOk.setOnClickListener {
            block.invoke(alertDialog!!)
        }
    }

    fun show() {
        if (context !is Activity) {
            val data = toDialogData()
            context.startActivity(Intent(context, DialogActivity::class.java).apply {
                putExtra(DialogActivity.DIALOG_DATA, data)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } else {
            builder.setView(binding.root)
            builder.setCancelable(false)
            alertDialog = builder.create()
            alertDialog?.show()
        }
    }

    private fun toDialogData(): DialogData = dialogData.apply {
        title = binding.title.text
        subtitle = binding.subtitle.text
    }

    companion object {
        private fun create(context: Context, dialogData: DialogData): FeatureDialog {
            return FeatureDialog(context)
                .setTitle(dialogData.title)
                .setSubtitle(dialogData.subtitle)
                .setResourceId(dialogData.drawable)
        }
    }

    @Parcelize
    private class DialogData(var title: CharSequence = "", var subtitle: CharSequence = "", @DrawableRes var drawable: Int = -1): Parcelable

    class DialogActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val data: DialogData = intent?.getParcelableExtra(DIALOG_DATA) as DialogData
            val dialog = create(this, data).apply {
                setOnClickListener { alert ->
                    alert.setOnDismissListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            finish()
                        }
                    }
                    alert.dismiss()
                }
            }
            dialog.show()
        }

        companion object {
            internal const val DIALOG_DATA = "dialog_data"
        }
    }
}