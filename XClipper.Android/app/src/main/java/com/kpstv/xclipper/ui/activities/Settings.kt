package com.kpstv.xclipper.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import com.kpstv.xclipper.App.ACTION_REPLACE_FRAG
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.SpecialMenu
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showConnectionDialog
import com.kpstv.xclipper.ui.adapters.MenuAdapter
import com.kpstv.xclipper.ui.fragments.settings.AccountPreference
import com.kpstv.xclipper.ui.fragments.settings.BackupPreference
import com.kpstv.xclipper.ui.fragments.settings.GeneralPreference
import com.kpstv.xclipper.ui.fragments.settings.LookFeelPreference
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.recycler_view.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class Settings : AppCompatActivity(), KodeinAware {

    companion object {
        const val GENERAL_PREF = "com.kpstv.xclipper.general_pref"
        const val ACCOUNT_PREF = "com.kpstv.xclipper.sync_pref"
        const val LOOK_FEEL_PREF = "com.kpstv.xclipper.look_feel_pref"
        const val BACKUP_PREF = "com.kpstv.xclipper.backup_pref"
    }

    override val kodein by kodein()
    private val viewModelFactory by instance<MainViewModelFactory>()
    private val dbConnectionProvider by instance<DBConnectionProvider>()
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private val settingsFragment = SettingsFragment()
    private val generalFragment = GeneralPreference()
    private val accountFragment = AccountPreference()
    private val backupFragment = BackupPreference()
    private val lookFeelFragment = LookFeelPreference(::onThemeChanged)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeUtils.setTheme(this)

        setContentView(R.layout.activity_settings)

        toolbar.title = getString(R.string.settings)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        settingsFragment.listener = {
            when (it) {
                GENERAL_PREF -> replaceFragment(generalFragment)
                ACCOUNT_PREF -> replaceFragment(accountFragment)
                LOOK_FEEL_PREF -> replaceFragment(lookFeelFragment)
                BACKUP_PREF -> replaceFragment(backupFragment)
            }
        }
        replaceFragment(settingsFragment, false)

        if (intent.getBooleanExtra(ACTION_REPLACE_FRAG, false))
            replaceFragment(lookFeelFragment)
    }

    class SettingsFragment : Fragment() {
        lateinit var listener: (String) -> Unit
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            with(
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.recycler_view, container, false) as View
            ) {
                val list = ArrayList<SpecialMenu>()

                /** General Setting */
                list.add(
                    SpecialMenu(
                        title = getString(R.string.service),
                        image = R.drawable.ic_service
                    ) { listener.invoke(GENERAL_PREF) })


                /** Sync Setting */
                list.add(
                    SpecialMenu(
                        title = context.getString(R.string.account),
                        image = R.drawable.ic_account
                    ) { listener.invoke(ACCOUNT_PREF) })

                /** Look & Feel Setting */
                list.add(
                    SpecialMenu(
                        title = getString(R.string.look_feel),
                        image = R.drawable.ic_looks
                    ) { listener.invoke(LOOK_FEEL_PREF) })

                /** Backup Setting */
                list.add(
                    SpecialMenu(
                        title = getString(R.string.backup),
                        image = R.drawable.ic_backup
                    ) { listener.invoke(BACKUP_PREF) })

                recycler_view.layoutManager = LinearLayoutManager(requireContext())
                recycler_view.adapter = MenuAdapter(list, R.layout.item_settings)
                recycler_view.setHasFixedSize(true)
                return this
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transition = supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, fragment)
        if (addToBackStack)
            transition.addToBackStack(null)
        transition.commit()
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
        } else
            super.onBackPressed()
    }

    /**
     * This onActivityResult is handling connection details coming from QR capture
     * i.e from AccountPreference fragment
     *
     * TODO: Violation of Open-Close principle
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
                complete = {

                    /** Here we will make a connection request to the database.*/

                    val dialog = showConnectionDialog(this)

                    mainViewModel.updateDeviceConnection(it, ResponseListener(
                        complete = {
                            Toasty.info(this, getString(R.string.connect_success)).show()
                            dialog.dismiss()
                        },
                        error = { e ->
                            dialog.dismiss()
                            Toasty.error(this, e.message!!).show()
                        }
                    ))
                },
                error = {
                    Toasty.error(this, it.message!!).show()
                }
            ))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}