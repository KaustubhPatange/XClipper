package com.kpstv.xclipper.ui.fragments.settings

import android.Manifest
import android.content.Intent
import android.content.Intent.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kpstv.xclipper.App.DATABASE_MIME_TYPE
import com.kpstv.xclipper.App.DATABASE_NAME
import com.kpstv.xclipper.App.EXPORT_PREF
import com.kpstv.xclipper.App.IMPORT_PREF
import com.kpstv.xclipper.App.PERMISSION_REQUEST_CODE
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.getFormattedDate
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isValidSQLite
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import java.util.*

@AndroidEntryPoint
class BackupPreference : PreferenceFragmentCompat() {

    companion object {
        private const val EXPORT_RESULT_CODE = 110
        private const val IMPORT_RESULT_CODE = 111
    }

    private val mainViewModel: MainViewModel by viewModels()

    private var internalBlock: SimpleFunction? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.backup_pref, rootKey)

        /** Export file */
        findPreference<Preference>(EXPORT_PREF)?.setOnPreferenceClickListener {
            checkAndAskForPermission {
                val intent = Intent(ACTION_CREATE_DOCUMENT).apply {
                    type = DATABASE_MIME_TYPE
                    flags = FLAG_GRANT_WRITE_URI_PERMISSION
                    putExtra(
                        EXTRA_TITLE,
                        "xclipper-${Calendar.getInstance().time.getFormattedDate()}.db"
                    )
                }
                startActivityForResult(intent, EXPORT_RESULT_CODE)
            }
            true
        }

        /** Import file */
        findPreference<Preference>(IMPORT_PREF)?.setOnPreferenceClickListener {
            checkAndAskForPermission {
                val intent = Intent(ACTION_OPEN_DOCUMENT).apply {
                    addCategory(CATEGORY_OPENABLE)
                    type = "*/*" // application/octet-stream
                }
                startActivityForResult(intent, IMPORT_RESULT_CODE)
            }
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            EXPORT_RESULT_CODE -> {
                data?.data?.let { uri ->
                    with(requireContext()) {
                        val databasePath = getDatabasePath(DATABASE_NAME)
                        if (databasePath?.exists() == false) {
                            Toasty.error(this, getString(R.string.err_database_export)).show()
                            return
                        }

                        /** Using scope storage to write to a file */
                        contentResolver.openOutputStream(uri)?.apply {
                            write(databasePath.readBytes())
                            close()
                        }
                        Toasty.info(this, getString(R.string.database_export)).show()
                    }
                }
            }
            IMPORT_RESULT_CODE -> {
                data?.data?.let save@{ uri ->
                    with(requireActivity()) {
                        val databasePath = getDatabasePath(DATABASE_NAME)

                        /** Using scope storage to write to a file */
                        val openInputStream = contentResolver.openInputStream(uri)

                        contentResolver.openInputStream(uri)?.let {
                            if (!isValidSQLite(it)) {
                                Toasty.error(this, getString(R.string.err_database)).show()
                                return@save
                            }
                            it.close()
                        }

                        if (openInputStream == null) {
                            Toasty.error(this, getString(R.string.err_import_database)).show()
                            return
                        }
                        contentResolver.openOutputStream(databasePath.toUri())?.apply {
                            write(openInputStream.readBytes())
                            close()
                        }
                        Toasty.info(this, getString(R.string.database_import)).show()

                        mainViewModel.refreshRepository()

                        openInputStream.close()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkAndAskForPermission(block: SimpleFunction) = with(requireContext()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED
        ) {
            internalBlock = block
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else
            block.invoke()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty())
            internalBlock?.invoke()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}