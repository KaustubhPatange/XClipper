package com.kpstv.xclipper.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.zxing.integration.android.IntentIntegrator
import com.kpstv.xclipper.App.ACTION_REPLACE_FRAG
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showConnectionDialog
import com.kpstv.xclipper.ui.fragments.Upgrade
import com.kpstv.xclipper.ui.fragments.settings.*
import com.kpstv.xclipper.ui.helpers.AuthenticationHelper
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class Settings : AppCompatActivity() {

    companion object {
        const val GENERAL_PREF = "com.kpstv.xclipper.general_pref"
        const val ACCOUNT_PREF = "com.kpstv.xclipper.sync_pref"
        const val LOOK_FEEL_PREF = "com.kpstv.xclipper.look_feel_pref"
        const val BACKUP_PREF = "com.kpstv.xclipper.backup_pref"
        const val UPGRADE_PREF = "com.kpstv.xclipper.upgrade_pref"
        const val ABOUT_PREF = "com.kpstv.xclipper.about_pref"
    }

    @Inject lateinit var dbConnectionProvider: DBConnectionProvider
    private val mainViewModel: MainViewModel by viewModels()

    private val settingsFragment = SettingsFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeUtils.setTheme(this)

        setContentView(R.layout.activity_settings)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        settingsFragment.listener = { pref, tag ->
            when (pref) {
                GENERAL_PREF -> replaceFragment(GeneralPreference(), tag)
                ACCOUNT_PREF -> replaceFragment(AccountPreference(), tag)
                LOOK_FEEL_PREF -> replaceFragment(LookFeelPreference(::onThemeChanged), tag)
                BACKUP_PREF -> replaceFragment(BackupPreference(), tag)
                UPGRADE_PREF -> replaceFragment(Upgrade(), tag)
                ABOUT_PREF -> replaceFragment(AboutPreference(), tag)
            }
        }
        replaceFragment(settingsFragment, getString(R.string.settings), false)

        if (intent.getBooleanExtra(ACTION_REPLACE_FRAG, false))
            replaceFragment(LookFeelPreference(::onThemeChanged), getString(R.string.look_feel))
    }

    private fun replaceFragment(fragment: Fragment, tag: String, addToBackStack: Boolean = true) {
        supportFragmentManager.commit {
            replace(R.id.settingsContainer, fragment, tag)
            if (addToBackStack) addToBackStack(null)
        }

        toolbar.title = tag
    }

    private fun onThemeChanged(value: Boolean) {
        val previousIntent = intent
        previousIntent.putExtra(ACTION_REPLACE_FRAG, true)
        finishAndRemoveTask()
        startActivity(previousIntent)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()

            toolbar.title = getString(R.string.settings)
        } else
            super.onBackPressed()
    }

    /**
     * This onActivityResult is handling connection details coming from QR capture
     * i.e from AccountPreference fragment
     *
     * TODO: You can try to use ActivityResultApi here, but it crashes for
     *       unknown reason.
     *
     * I tried to register for ActivityResult callback using contract but seems to
     * be not working.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /** Parsing the connection result. */

        val result =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result?.contents != null) {

            dbConnectionProvider.processResult(result.contents, ResponseListener(
                complete = { options ->

                    /** Check if auth is needed, if so make a auth2 call */

                    if (options.isAuthNeeded) {
                        AuthenticationHelper(this, options.authClientId!!).signIn(
                            options = options,
                            responseListener = ResponseListener(
                                complete = {
                                    /** Here we will make a connection request to the database.*/
                                    makeAConnectionRequest(options)
                                },
                                error = {
                                    Toasty.error(this, "Error: ${it.message}", Toasty.LENGTH_LONG).show()
                                }
                            ))
                    } else {
                        /** Here we will make a connection request to the database.*/
                        makeAConnectionRequest(options)
                    }
                },
                error = {
                    Toasty.error(this, it.message!!).show()
                }
            ))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun makeAConnectionRequest(options: FBOptions) {
        val dialog = showConnectionDialog(this)

        mainViewModel.updateDeviceConnection(options, ResponseListener(
            complete = {
                Toasty.info(this, getString(R.string.connect_success)).show()
                dialog.dismiss()
            },
            error = { e ->
                dialog.dismiss()
                Toasty.error(this, e.message!!).show()
            }
        ))
    }
}