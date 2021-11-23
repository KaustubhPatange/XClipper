@file:Suppress("MemberVisibilityCanBePrivate")

package com.kpstv.xclipper.ui.dialogs

import android.content.Context
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.kpstv.xclipper.databinding.DialogLottieBinding
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.ui.utils.LaunchUtils

class CustomLottieDialog(private val context: Context) {
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    private var alertDialog: AlertDialog? = null
    private val binding: DialogLottieBinding = DialogLottieBinding.inflate(context.layoutInflater())

    init {
        binding.btnDetails.hide()
        binding.btnNeutral.hide()
        binding.btnClose.hide()
        binding.message.hide()
        builder.setView(binding.root)
    }

    fun show() {
        alertDialog = builder.create()
        alertDialog?.show()
    }

    fun setLottieRes(@RawRes raw: Int): CustomLottieDialog {
        binding.lottieView.setAnimation(raw)
        return this
    }

    fun setLottieResCredits(url: String) : CustomLottieDialog {
        binding.lottieView.setOnLongClickListener {
            LaunchUtils.commonUrlLaunch(binding.root.context, url)
            true
        }
        return this
    }

    fun setSpeed(value: Float): CustomLottieDialog {
        binding.lottieView.speed = value
        return this
    }

    fun setLoop(boolean: Boolean): CustomLottieDialog {
        if (!boolean)
            binding.lottieView.repeatCount = 0
        return this
    }

    fun setTitle(@StringRes titleId: Int): CustomLottieDialog {
        return setTitle(context.getString(titleId))
    }

    fun setTitle(title: CharSequence?): CustomLottieDialog {
        binding.title.text = title
        return this
    }

    fun setMessage(@StringRes messageId: Int): CustomLottieDialog {
        return setMessage(context.getString(messageId))
    }

    fun setMessage(message: CharSequence?): CustomLottieDialog {
        binding.message.show()
        binding.message.text = message
        return this
    }

    fun setPositiveButton(
        textId: Int,
        listener: SimpleFunction
    ): CustomLottieDialog {
        return setPositiveButton(context.getString(textId), listener)
    }

    fun setPositiveButton(
        text: CharSequence?,
        listener: SimpleFunction
    ): CustomLottieDialog {
        binding.btnDetails.show()
        binding.btnDetails.text = text
        binding.btnDetails.setOnClickListener { listener.invoke(); alertDialog?.dismiss() }
        return this
    }

    fun setNegativeButton(
        textId: Int,
        listener: SimpleFunction? = null
    ): CustomLottieDialog {
        return setNegativeButton(context.getString(textId), listener)
    }

    fun setNegativeButton(
        text: CharSequence?,
        listener: SimpleFunction? = null
    ): CustomLottieDialog {
        binding.btnClose.show()
        binding.btnClose.text = text
        binding.btnClose.setOnClickListener { listener?.invoke(); alertDialog?.dismiss() }
        return this
    }

    fun setNeutralButton(
        textId: Int,
        listener: SimpleFunction? = null
    ): CustomLottieDialog {
        return setNeutralButton(context.getString(textId), listener)
    }

    fun setNeutralButton(
        text: CharSequence?,
        listener: SimpleFunction? = null
    ): CustomLottieDialog {
        binding.btnNeutral.show()
        binding.btnNeutral.text = text
        binding.btnNeutral.setOnClickListener { listener?.invoke(); alertDialog?.dismiss() }
        return this
    }
}