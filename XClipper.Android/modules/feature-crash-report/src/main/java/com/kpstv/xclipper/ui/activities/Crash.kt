package com.kpstv.xclipper.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.feature_crash_report.R
import com.kpstv.xclipper.feature_crash_report.databinding.ActivityCrashBinding
import com.kpstv.xclipper.ui.helpers.CrashReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.PrintWriter
import java.io.StringWriter

@Suppress("DEPRECATION")
class Crash : AppCompatActivity() {
    companion object {

        private const val CRASH_TRACE = "crash_trace"
        fun launch(context: Context?, ex: Throwable) {
            val intent = Intent(context, Crash::class.java).apply {
                putExtra(CRASH_TRACE, ex)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

        val throwable = intent?.getSerializableExtra(CRASH_TRACE) as? Throwable ?: Throwable("Error: Could not capture")

        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

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

        lifecycleScope.launchWhenStarted {
            CoroutineScope(job + Dispatchers.IO).launch {
                sendReport(throwable)
            }
        }
    }

    private fun updateMessage(message: String) {
        lifecycleScope.launch {
            binding.secondaryText.text = message
        }
    }

    private suspend fun sendReport(throwable: Throwable) {
        updateMessage(getString(R.string.crash_text_sending))

        // Manually pass crash event to sentry.io

        CrashReport.sendFatalException(this, throwable)

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