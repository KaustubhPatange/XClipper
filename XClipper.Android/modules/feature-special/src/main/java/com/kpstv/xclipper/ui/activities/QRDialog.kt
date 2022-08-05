package com.kpstv.xclipper.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.feature_special.R
import com.kpstv.xclipper.feature_special.databinding.DialogQrCodeBinding
import com.kpstv.xclipper.ui.helpers.AppThemeHelper
import com.kpstv.xclipper.ui.helpers.special.QRActionHelper
import com.kpstv.xclipper.ui.helpers.special.QRCodeLimitExceedException
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.flow

class QRDialog : AppCompatActivity() {

    private val binding by viewBinding(DialogQrCodeBinding::inflate)
    private val viewModel by viewModels<QRDialogViewModel>()

    private val dialog: AlertDialog by lazy {
        val dialog = AlertDialog.Builder(this).setView(binding.root).create()
        dialog.setOnDismissListener {
            finish()
        }
        return@lazy dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = intent.getStringExtra(ARG_TEXT) ?: run { finish(); return; }

        AppThemeHelper.applyDialogTheme(this)

        val foregroundColor = Color.WHITE
        val backgroundColor = if (AppThemeHelper.isDarkVariant()) {
            getColorAttr(R.attr.colorForeground)
        } else {
            Color.BLACK
        }

        viewModel.get(text, frontColor = foregroundColor, backColor = backgroundColor).observe(this) { state ->
            if (state !is QRDialogViewModel.UiState.Loading) {
                binding.progressBar.hide()
            }
            when (state) {
                is QRDialogViewModel.UiState.Loading -> {}
                is QRDialogViewModel.UiState.Success -> {
                    binding.ivQr.show()
                    binding.ivQr.setImageBitmap(state.bitmap)
                }
                is QRDialogViewModel.UiState.Error -> {
                    val message = if (state.exception is QRCodeLimitExceedException) {
                        getString(state.exception.messageRes)
                    } else {
                        "Unknown error"
                    }
                    Toasty.error(this, message).show()
                    dialog.dismiss()
                }
            }
        }

        binding.btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    companion object {
        private const val ARG_TEXT = "com.kpstv.xclipper:arg_text"
        fun launch(context: Context, text: String) = with(context) {
            val intent = Intent(this, QRDialog::class.java).apply {
                putExtra(ARG_TEXT, text)
            }
            startActivity(intent)
        }
    }
}

class QRDialogViewModel : ViewModel() {
    fun get(text: String, @ColorInt frontColor: Int, @ColorInt backColor: Int) : LiveData<UiState> = flow {
        emit(UiState.Loading)
        QRActionHelper.createQR(data = text, frontColor = frontColor, backColor = backColor)
            .fold(
                onSuccess = { emit(UiState.Success(it)) },
                onFailure = { emit(UiState.Error(it)) }
            )
    }.asLiveData(viewModelScope.coroutineContext)

    sealed class UiState {
        object Loading : UiState()
        data class Success(val bitmap: Bitmap) : UiState()
        data class Error(val exception: Throwable) : UiState()
    }
}