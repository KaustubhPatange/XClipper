package com.kpstv.xclipper.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import com.kpstv.xclipper.App.UID_PATTERN_REGEX
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.SpecialMenu
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.ui.adapters.MenuAdapter
import com.kpstv.xclipper.ui.fragments.settings.AccountPreference
import com.kpstv.xclipper.ui.fragments.settings.GeneralPreference
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
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private val settingsFragment = SettingsFragment()
    private val generalFragment = GeneralPreference()
    private val accountFragment = AccountPreference()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        toolbar.title = getString(R.string.settings)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        settingsFragment.listener = {
            when (it) {
                GENERAL_PREF -> replaceFragment(generalFragment)
                ACCOUNT_PREF -> replaceFragment(accountFragment)
            }
        }

        replaceFragment(settingsFragment, false)
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

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else
            super.onBackPressed()
    }

    private val TAG = javaClass.simpleName
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /** Parsing the connection result. */

        val result =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result?.contents != null) {
            UID_PATTERN_REGEX.toRegex().let {
                if (it.containsMatchIn(result.contents)) {
                    /** This will connect this device with repository. */

                   val dialog = AlertDialog.Builder(this)
                        .setView(LayoutInflater.from(this).inflate(R.layout.animation_view, null))
                        .show()

                    mainViewModel.updateDeviceConnection(result.contents, ResponseListener(
                        complete = {
                            Toasty.info(this, getString(R.string.connect_success)).show()
                            dialog.dismiss()
                        },
                        error = { e ->
                            Toasty.error(this, e.message!!).show()
                        }
                    ))
                } else
                    Toasty.error(this, getString(R.string.err_uid)).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}