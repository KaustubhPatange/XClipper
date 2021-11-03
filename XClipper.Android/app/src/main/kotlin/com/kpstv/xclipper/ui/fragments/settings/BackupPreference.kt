package com.kpstv.xclipper.ui.fragments.settings

import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.getFormattedDate
import com.kpstv.xclipper.ui.viewmodels.BackupViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import java.util.*

@AndroidEntryPoint
class BackupPreference : PreferenceFragmentCompat() {

    companion object {
        private const val EXPORT_RESULT_CODE = 110
        private const val IMPORT_RESULT_CODE = 111

        private const val EXPORT_PREF = "export_pref"
        private const val IMPORT_PREF = "import_pref"
    }

    private val backupViewModel: BackupViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.backup_pref, rootKey)

        /** Export file */
        findPreference<Preference>(EXPORT_PREF)?.setOnPreferenceClickListener {
            val intent = Intent(ACTION_CREATE_DOCUMENT).apply {
                type = "application/json"
                flags = FLAG_GRANT_WRITE_URI_PERMISSION
                putExtra(
                    EXTRA_TITLE,
                    "xclipper-${Calendar.getInstance().time.getFormattedDate()}.json"
                )
            }
            startActivityForResult(intent, EXPORT_RESULT_CODE)
            true
        }

        /** Import file */
        findPreference<Preference>(IMPORT_PREF)?.setOnPreferenceClickListener {
            val intent = Intent(ACTION_OPEN_DOCUMENT).apply {
                addCategory(CATEGORY_OPENABLE)
                type = "application/json"
            }
            startActivityForResult(intent, IMPORT_RESULT_CODE)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            EXPORT_RESULT_CODE -> {
                data?.data?.let { uri ->
                    with(requireContext()) {
                        backupViewModel.createBackup(uri, onError = {
                            Toasty.error(this, getString(R.string.err_database_export)).show()
                        },
                        onComplete = {
                            Toasty.info(this, getString(R.string.database_export)).show()
                        })
                    }
                }
            }
            IMPORT_RESULT_CODE -> {
                data?.data?.let save@{ uri ->
                    with(requireActivity()) {
                        backupViewModel.restoreBackup(uri, onError = {
                            Toasty.error(this, getString(R.string.err_database)).show()
                        },
                        onComplete = {
                            Toasty.info(this, getString(R.string.database_import)).show()
                        })
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}