package com.kpstv.xclipper.ui.helpers.special

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
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

internal class CustomCaptureManager(
    private val activity: Activity,
    private val barcodeView: DecoratedBarcodeView
) : CaptureManager(activity, barcodeView) {
    private val handler = Handler(Looper.getMainLooper())

    private val callback = BarcodeCallback call@{ result ->
        barcodeView.pause()
        if (result == null) return@call
        handler.post { showConfirmationDialog(result) }
    }

    override fun decode() {
        barcodeView.decodeContinuous(callback)
    }

    private fun showConfirmationDialog(rawResult: BarcodeResult) {
        MaterialAlertDialogBuilder(activity)
            .setMessage(rawResult.text)
            .setNegativeButton(R.string.cancel) { _, _ ->
                barcodeView.resume()
            }
            .setPositiveButton(R.string.csp_add_to_data) { _, _ ->
                returnResult(rawResult)
            }
            .setCancelable(false)
            .show()
    }
}

internal class CustomCaptureActivity : AppCompatActivity() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private val capture: CustomCaptureManager by lazy { CustomCaptureManager(this, barcodeView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barcodeView = initializeContent()
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()
    }

    fun initializeContent(): DecoratedBarcodeView {
        setContentView(R.layout.zxing_capture)
        return findViewById<View>(R.id.zxing_barcode_scanner) as DecoratedBarcodeView
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}

object QRCodeLimitExceedException : Exception() {
    val messageRes: Int = R.string.csp_error_qr_code_limit
}