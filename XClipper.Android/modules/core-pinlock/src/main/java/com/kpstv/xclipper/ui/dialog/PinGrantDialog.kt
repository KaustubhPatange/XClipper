package com.kpstv.xclipper.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kpstv.xclipper.PinLockHelper
import com.kpstv.xclipper.core_pinlock.R
import com.kpstv.xclipper.core_pinlock.databinding.ActivityDialogPinGrantBinding
import com.kpstv.xclipper.di.CommonReusableEntryPoints
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.utils.ToastyUtils
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.helpers.AppThemeHelper

class PinGrantDialog(private val context: Context, private val key: String) {

    private val preferenceProvider = CommonReusableEntryPoints.get(context.applicationContext).preferenceProvider()

    private var listener: SimpleFunction? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    // register to listen success
    fun register(fragment: Fragment, onGranted: SimpleFunction = {}) {
        listener = onGranted
        activityResultLauncher = fragment.requireActivity().activityResultRegistry.register(
            key, ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                unregister()
                listener?.invoke()
            }
        }
    }

    fun unregister() {
        activityResultLauncher?.unregister()
    }

    fun shouldShow(): Boolean {
        val type = GrantType.values()[preferenceProvider.getIntKey("$SAVE_TYPE$key", 1)]
        val timeMillis = preferenceProvider.getLongKey("$GRANT_ACCESS_MILLIS$key", -1L)

        if (type == GrantType.Forever) return false
        if (timeMillis == -1L) return true

        val currentTime = System.currentTimeMillis()
        val differenceMinutes = (currentTime - timeMillis) / (1000 * 60)

        return when(type) {
            GrantType.Min15 -> differenceMinutes > 15
            GrantType.Hour1 -> differenceMinutes > 60
            GrantType.Hour6 -> differenceMinutes > 60 * 6
            GrantType.Hour12 -> differenceMinutes > 60 * 12
            GrantType.Hour24 -> differenceMinutes > 60 * 24
            GrantType.Locked -> false
            else -> false
        }
    }

    fun launch() {
        val launcher = activityResultLauncher
        val intent = PinGrantDialogActivity.create(context, key).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (launcher != null) {
            launcher.launch(intent)
        } else {
            context.startActivity(intent)
        }
    }

    enum class GrantType(@StringRes val resId: Int) {
        Min15(R.string.grant_15m),
        Hour1(R.string.grant_1h),
        Hour6(R.string.grant_6h),
        Hour12(R.string.grant_12h),
        Hour24(R.string.grant_24h),
        Locked(R.string.grant_locked),
        Forever(R.string.grant_forever),
    }

    companion object {
        internal const val SAVE_TYPE = "pin_grant:save_type:"
        internal const val GRANT_ACCESS_MILLIS = "pin_grant:access_millis:"
    }

    class PinGrantDialogActivity : AppCompatActivity() {

        private val binding by viewBinding(ActivityDialogPinGrantBinding::inflate)

        private val preferenceProvider by lazy { CommonReusableEntryPoints.get(applicationContext).preferenceProvider() }

        private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

        private var shouldRemember = true

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            AppThemeHelper.applyDialogTheme(this)
            setContentView(binding.root)

            val key = intent.getStringExtra(KEY)!!

            activityResultLauncher = activityResultRegistry.register("pin-grant-activity", ActivityResultContracts.StartActivityForResult()) { result ->
                if (PinLockHelper.Result.isSuccess(result)) {
                    activityResultLauncher.unregister()
                    val position = binding.spinner.selectedItemPosition
                    val currentTimeMillis = System.currentTimeMillis()

                    if (shouldRemember) {
                        preferenceProvider.putIntKey("$SAVE_TYPE$key", position)
                    }
                    preferenceProvider.putLongKey("$GRANT_ACCESS_MILLIS$key", currentTimeMillis)

                    setResult(Activity.RESULT_OK)

                    ToastyUtils.showInfo(this, getString(R.string.pin_lock_access_success))
                }
                finish()
            }

            binding.spinner.adapter = CustomSpinnerAdapter(this)
            binding.spinner.setSelection(preferenceProvider.getIntKey("$SAVE_TYPE$key", 1))

            binding.cbRemember.setOnCheckedChangeListener { _, isChecked ->
                shouldRemember = isChecked
            }

            binding.btnDeny.setOnClickListener {
                finish()
            }

            binding.btnGrant.setOnClickListener {
                PinLockHelper.checkPinLock(this, activityResultLauncher)
            }
        }

        class CustomSpinnerAdapter(context: Context)
            : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, GrantType.values().map { context.getString(it.resId) }) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                view.findViewById<TextView>(android.R.id.text1).apply {
                    text = getItem(position)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                }
                return view
            }
        }

        companion object {
            private const val KEY = "pin_lock:key"
            fun create(context: Context, key: String) : Intent = Intent(context, PinGrantDialogActivity::class.java).apply {
                putExtra(KEY, key)
            }
        }
    }
}