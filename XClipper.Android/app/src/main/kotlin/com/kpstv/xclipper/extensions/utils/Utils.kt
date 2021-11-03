package com.kpstv.xclipper.extensions.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.accessibility.AccessibilityManager
import androidx.annotation.AttrRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.AUTO_SYNC_PREF
import com.kpstv.xclipper.App.BIND_DELETE_PREF
import com.kpstv.xclipper.App.BIND_PREF
import com.kpstv.xclipper.BuildConfig
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.model.AppPkg
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.databinding.DialogConnectBinding
import com.kpstv.xclipper.databinding.DialogProgressViewBinding
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.ui.dialogs.FeatureDialog
import com.kpstv.xclipper.ui.helpers.AuthenticationHelper
import com.kpstv.xclipper.ui.helpers.FirebaseSyncHelper
import es.dmoral.toasty.Toasty
import java.io.InputStream
import java.text.DecimalFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

class Utils {
    companion object {
        fun isActivityRunning(ctx: Context, clazz: KClass<out Activity>): Boolean {
            val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return activityManager.getRunningTasks(Int.MAX_VALUE).any {
                    it.topActivity?.className == clazz.qualifiedName
                }
        }

        fun shareText(context: Activity, clip: Clip) {
            val intent = ShareCompat.IntentBuilder.from(context)
                .setChooserTitle(context.getString(R.string.share))
                .setType("text/plain")
                .setText(clip.data)
                .intent
            val shareIntent = Intent.createChooser(intent, null)
            context.startActivity(shareIntent)
        }

        /**
         * Always pass this@Activity as context.
         * Else it won't resolve theme
         */
        fun getDataFromAttr(
            context: Context,
            @AttrRes attr: Int,
            typedValue: TypedValue = TypedValue(),
            resolveRefs: Boolean = true
        ): Int {
            context.theme.resolveAttribute(attr, typedValue, resolveRefs)
            return typedValue.data
        }

        /** I am too lazy to write my own code.
         *
         *  Source: https://stackoverflow.com/a/31583695/10133501
         */
        fun getCountryDialCode(context: Context): String? {
            var contryDialCode: String? = null
            val telephonyMngr =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val countryId = telephonyMngr.simCountryIso.toUpperCase(Locale.ROOT)
            val arrContryCode: Array<String> =
                context.resources.getStringArray(R.array.DialingCountryCode)
            for (i in arrContryCode.indices) {
                val arrDial =
                    arrContryCode[i].split(",").toTypedArray()
                if (arrDial[1].trim { it <= ' ' } == countryId.trim()) {
                    contryDialCode = arrDial[0]
                    break
                }
            }
            return contryDialCode
        }

        fun isPackageInstalled(
            context: Context,
            packageName: String
        ): Boolean {
            return try {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }


        /**
         * Returns true if the current package name is not part of blacklist app list.
         */
        fun isPackageBlacklisted(pkg: CharSequence?) =
            App.blackListedApps?.contains(pkg) == true

        /**
         * This will check if accessibility service is enabled or not.
         *
         * @param service Provide the accessibility service class of which you want to
         * detect.
         */
        fun isAccessibilityServiceEnabled(
            context: Context,
            service: Class<out AccessibilityService?>
        ): Boolean {
            val am =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            for (enabledService in enabledServices) {
                val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
                if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == service.name
                ) return true
            }
            val accessibilityPrefs = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (accessibilityPrefs?.contains("${context.packageName}/${service.canonicalName}") == true) return true
            return false
        }

        /**
         * This will create and show dialog to user to enable accessibility service
         * to make clipboard capturing work even for the Android 10.
         */
        fun showAccessibilityDialog(context: Context, block: SimpleFunction): Unit = with(context) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.accessibility_service))
                .setMessage(context.getString(R.string.accessibility_capture))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    openAccessibility(this)
                    block.invoke()
                }
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel)) { _, _ -> block.invoke() }
                .show()
        }

        fun showDisableAccessibilityDialog(context: Context, block: SimpleFunction): Unit = with(context) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.accessibility_service_disable))
                .setMessage(getString(R.string.accessibility_disable_text))
                .setCancelable(false)
                .setPositiveButton(R.string.ok) { _, _ ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        ClipboardAccessibilityService.disableService(context)
                    } else openAccessibility(context)
                    block.invoke()
                }
                .setNegativeButton(R.string.cancel) { _, _ -> block.invoke() }
                .show()
        }

        fun showAccessibilityDialog(context: Context) {
            showAccessibilityDialog(context) { }
        }


        private const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
        private const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"

        fun openAccessibility(context: Context) = with(context) {
            val bundle = Bundle()
            val componentName = ComponentName(
                BuildConfig.APPLICATION_ID,
                ClipboardAccessibilityService::class.java.name
            ).flattenToString()
            bundle.putString(EXTRA_FRAGMENT_ARG_KEY, componentName)
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
                putExtra(EXTRA_FRAGMENT_ARG_KEY, componentName)
                putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
            }
            startActivity(intent)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun showOverlayDialog(context: Context): AlertDialog = with(context) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Suggestions [BETA]")
                .setMessage(getString(R.string.suggestion_capture))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    openSystemOverlay(this)
                }
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        fun isSystemOverlayEnabled(context: Context): Boolean = with(context) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(this)
            } else true
        }

        fun isAndroid10orUp() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        @RequiresApi(Build.VERSION_CODES.M)
        fun openSystemOverlay(context: Context) = with(context) {
            val myIntent =
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ).apply {
                    flags = FLAG_ACTIVITY_NEW_TASK
                }
            startActivity(myIntent)
        }

        /**
         * Call this function whenever you want to work with installed apps.
         */
        suspend fun retrievePackageList(context: Context): List<AppPkg> = with(context) {
            suspendCoroutine { r ->
                val result = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    .asSequence()
                    .filter {
                        it.flags != ApplicationInfo.FLAG_SYSTEM
                    }
                    .mapNotNull {
                        AppPkg(
                            it.loadLabel(packageManager),
                            it.packageName
                        )
                    }
                    .filter { it.label != null && !it.label.contains(".") && it.packageName != null }
                    .sortedBy { it.label.toString() }
                    .distinctBy { it.label.toString() }.toList()
                r.resume(result)
            }
        }

        /**
         * This will show connection dialog and from there we can initiate QR scanning.
         */
        fun showConnectDialog(activity: Activity): Unit = with(activity) {
            val binding = DialogConnectBinding.inflate(layoutInflater)

            val alert = AlertDialog.Builder(this)
                .setView(binding.root)
                .show()

            binding.btnScanConnect.setOnClickListener {
                IntentIntegrator(this)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    .setOrientationLocked(false)
                    .setBeepEnabled(false)
                    .setPrompt(getString(R.string.scan_code))
                    .setBarcodeImageEnabled(false)
                    .initiateScan()

                alert.dismiss()
            }
        }

        fun showConnectionDialog(context: Context): AlertDialog = with(context) {
            val binding = DialogProgressViewBinding.inflate(layoutInflater())

            val dialog = AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(binding.root)
                .show()

            binding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            return dialog
        }

        fun logoutFromDatabase(
            context: Context,
            preferenceProvider: PreferenceProvider,
            dbConnectionProvider: DBConnectionProvider
        ) {
            dbConnectionProvider.optionsProvider()?.apply {
                if (isAuthNeeded) {
                    FirebaseSyncHelper.get()?.let { app ->
                        Firebase.auth(app).signOut()
                    }
                    AuthenticationHelper.signOutGoogle(context, authClientId)
                }
            }
            dbConnectionProvider.detachDataFromAll()
            preferenceProvider.putBooleanKey(BIND_PREF, false)
            preferenceProvider.putBooleanKey(AUTO_SYNC_PREF, false)
            preferenceProvider.putBooleanKey(BIND_DELETE_PREF, false)
            App.bindToFirebase = false
            App.runAutoSync = false
            App.bindDelete = false
        }

        fun loginToDatabase(
            preferenceProvider: PreferenceProvider,
            dbConnectionProvider: DBConnectionProvider,
            options: FBOptions
        ) {
            dbConnectionProvider.saveOptionsToAll(options)
            preferenceProvider.putBooleanKey(BIND_PREF, true)
            preferenceProvider.putBooleanKey(AUTO_SYNC_PREF, true)
            App.bindToFirebase = true
            App.runAutoSync = true
        }

        fun isValidSQLite(stream: InputStream?): Boolean {
            val buffer = CharArray(16)
            stream?.bufferedReader()?.read(buffer, 0, 16)
            val str = String(buffer)
            return str == "SQLite format 3\u0000"
        }

        fun commonUrlLaunch(context: Context, url: String): Unit = with(context) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                flags = FLAG_ACTIVITY_NEW_TASK
            }
            try {
                startActivity(intent)
            }catch (e: Exception) {
                Toasty.error(this, R.string.err_action).show()
            }
        }

        fun dpToPixel(context: Context, value: Float): Float = with(context) {
            return value * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun vibrateDevice(context: Context) {
            val m = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                m.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                m.vibrate(50)
            }
        }

        fun showSearchFeatureDialog(context: Context, preferenceProvider: PreferenceProvider): Boolean {
            if (preferenceProvider.getBooleanKey(App.SHOW_SEARCH_FEATURE, true)) {
                FeatureDialog(context)
                    .setResourceId(R.drawable.feature_suggestion_search)
                    .setTitle(R.string.search_title)
                    .setSubtitle(R.string.search_subtitle)
                    .show()
                preferenceProvider.putBooleanKey(App.SHOW_SEARCH_FEATURE, false)
                return true
            }
            return false
        }

        fun showDisclosureDialog(context: Context, @StringRes message: Int, onAccept: () -> Unit, onDeny: () -> Unit = {}) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.disclosure)
                .setMessage(message)
                .setPositiveButton(R.string.accept) { _, _ -> onAccept()}
                .setNegativeButton(R.string.deny) { _, _ ->
                    Toasty.error(context, context.getString(R.string.disclosure_deny)).show()
                    onDeny()
                }
                .setCancelable(false)
                .show()
        }

        fun getSizePretty(size: Long?, addPrefix: Boolean = true): String? {
            val df = DecimalFormat("0.00")
            val sizeKb = 1024.0f
            val sizeMb = sizeKb * sizeKb
            val sizeGb = sizeMb * sizeKb
            val sizeTerra = sizeGb * sizeKb
            return if (size != null) {
                when {
                    size < sizeMb -> df.format(size / sizeKb)
                        .toString() + if (addPrefix) " KB" else ""
                    size < sizeGb -> df.format(
                        size / sizeMb
                    ).toString() + " MB"
                    size < sizeTerra -> df.format(size / sizeGb)
                        .toString() + if (addPrefix) " GB" else ""
                    else -> ""
                }
            } else "0" + if (addPrefix) " B" else ""
        }

    }
}