package com.kpstv.xclipper.ui.helpers.special

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.ColorInt
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.*
import com.kpstv.xclipper.specials.R

object QRActionHelper {
    // QR code handles 4296 character but we will round it off to 4000
    fun canGenerateQR(text: String): Boolean = text.length < 4000

    fun createQR(
        data: String,
        width: Int = Resources.getSystem().displayMetrics.widthPixels / 2,
        @ColorInt frontColor: Int = Color.BLACK,
        @ColorInt backColor: Int = Color.WHITE
    ): Result<Bitmap> {
        if (!canGenerateQR(data)) return Result.failure(QRCodeLimitExceedException)
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, width)

        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (bitMatrix[x, y]) frontColor else backColor
            }
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return Result.success(bitmap)
    }

    class QRCodeReader(private val activity: ComponentActivity) {
        private val scanOptions = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setOrientationLocked(false)
            .setBeepEnabled(false)
            .setCaptureActivity(CustomCaptureActivity::class.java)
            .setPrompt(activity.getString(R.string.csp_scan_qr_code))
            .setBarcodeImageEnabled(false)

        private lateinit var activityResultLauncher: ActivityResultLauncher<ScanOptions>

        fun init(lifecycleOwner: LifecycleOwner, listener: (text: String) -> Unit) {
            activityResultLauncher = activity.activityResultRegistry.register("qr_capture_1", ScanContract()) { result ->
                if (result.contents != null) {
                    listener.invoke(result.contents)
                }
            }

            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    activityResultLauncher.unregister()
                }
            })
        }

        fun start() {
            activityResultLauncher.launch(scanOptions)
        }
    }
}

@SuppressLint("DiscouragedPrivateApi")
internal class CustomCaptureActivity : CaptureActivity() {
    private var isDataConfirmed: Boolean = false

    override fun finish() {
        if (isDataConfirmed) {
            super.finish()
        }
        // just save from reflection
        val code = resultCode.getInt(this)
        if (code != RESULT_OK) {
            super.finish()
        }
        if (code == RESULT_OK && !isDataConfirmed) {
            (resultIntent.get(this) as? Intent)?.let { intent ->
                val result = ScanIntentResult.parseActivityResult(code, intent)
                showConfirmationDialog(result.contents)
                return
            }
        }
    }

    private fun showConfirmationDialog(data: String) {
        MaterialAlertDialogBuilder(this)
            .setMessage(data)
            .setNegativeButton(R.string.cancel) { _, _ ->
                resultCode.setInt(this, RESULT_CANCELED)
                isDataConfirmed = false
                finish()
            }
            .setPositiveButton(R.string.csp_add_to_data) { _, _ ->
                isDataConfirmed = true
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private val resultCode get() = Activity::class.java.getDeclaredField("mResultCode").apply {
        isAccessible = true
    }
    private val resultIntent get() = Activity::class.java.getDeclaredField("mResultData").apply {
        isAccessible = true
    }
}

object QRCodeLimitExceedException : Exception() {
    val messageRes: Int = R.string.csp_error_qr_code_limit
}