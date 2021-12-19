package com.kpstv.xclipper.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kpstv.xclipper.BuildConfig
import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.ActivityCrashBinding
import com.kpstv.xclipper.extensions.await
import com.kpstv.xclipper.extensions.viewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.PrintWriter
import java.io.StringWriter

@Suppress("DEPRECATION")
class Crash : AppCompatActivity() {
    companion object {
        private const val CRASH_TRACE = "crash_trace"
        fun launch(context: Context?, ex: Throwable) {
            val sw = StringWriter()
            ex.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()

            val intent = Intent(context, Crash::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(CRASH_TRACE, "${ex.message}\n\n${stackTrace}")
            }
            context?.startActivity(intent)
        }
    }

    private val binding by viewBinding(ActivityCrashBinding::inflate)

    private val job = SupervisorJob()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = 0
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.tvTop.setOnApplyWindowInsetsListener { view, insets ->
            view.updateLayoutParams<ConstraintLayout.LayoutParams> {
                updateMargins(top = insets.systemWindowInsetTop + topMargin)
            }
            insets
        }

        val stackTrace = intent?.getStringExtra(CRASH_TRACE) ?: return

        binding.btnShowTrace.setOnClickListener { v ->
            val scrollView = NestedScrollView(v.context).apply {
                addView(
                    TextView(v.context).apply {
                        val p = 15.dp().toInt()
                        setPadding(p,p,p,p)
                        text = stackTrace
                    }
                )
            }
            AlertDialog.Builder(this).apply {
                setView(scrollView)
                setPositiveButton("OK", null)
            }.show()
        }

        binding.btnClose.setOnClickListener { finish() }

        CoroutineScope(job + Dispatchers.IO).launch {
            // No dependency injection because this activity outlives original process.
            if (!BuildConfig.DEBUG)
                sendReport(stackTrace)
        }
    }

    private fun updateMessage(message: String) {
        lifecycleScope.launch {
            binding.secondaryText.text = message
        }
    }

    private suspend fun sendReport(stackTrace: String) {
        updateMessage(getString(R.string.crash_text_sending))

        val deviceDetails = """
                -----------------------------------------------------------
                Model: ${Build.MODEL}
                Product: ${Build.PRODUCT}
                Brand: ${Build.BRAND}
                Manufacture: ${Build.MANUFACTURER}
                Id: ${Build.ID}
                Release: ${Build.VERSION.RELEASE}
                App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
                -----------------------------------------------------------
                """.trimIndent()
        val body = "$deviceDetails\n\n$stackTrace"

        Log.e("CrashActivity", "Body: $body")
        val url = "${BuildConfig.SERVER_URI}/report?sender=API%20${Build.VERSION.SDK_INT}&category=1"
        val request = Request.Builder().url(url).post(body.toRequestBody("text/plain".toMediaType())).build()
        val response = OkHttpClient().newCall(request).await()
        response.onSuccess { updateMessage(getString(R.string.crash_text_sent)) }
        response.onFailure { updateMessage(getString(R.string.crash_text_sent_second_try)) }
        if (FirebaseCrashlytics.getInstance().checkForUnsentReports().await() == true) {
            FirebaseCrashlytics.getInstance().sendUnsentReports()
        }
        updateMessage(getString(R.string.crash_text_sent))
    }

    private fun Int.dp() = this * resources.displayMetrics.density

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}