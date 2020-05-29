package com.kpstv.xclipper.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.SpecialMenu
import com.kpstv.xclipper.ui.adapters.MenuAdapter
import com.kpstv.xclipper.ui.fragments.settings.GeneralPreference
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.recycler_view.view.*

class Settings : AppCompatActivity() {

    companion object {
        const val GENERAL_PREF = "com.kpstv.xclipper.general_pref"
        const val LOOK_FEEL_PREF = "com.kpstv.xclipper.look_feel_pref"
        const val BACKUP_PREF = "com.kpstv.xclipper.backup_pref"
    }

    private val settingsFragment = SettingsFragment()
    private val generalFragment = GeneralPreference()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        toolbar.title = getString(R.string.settings)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        settingsFragment.listener = {
            when(it) {
                GENERAL_PREF -> replaceFragment(generalFragment)
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
        }else
            super.onBackPressed()
    }

    /*class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.general_pref, rootKey)

            findPreference<Preference>(getString(R.string.set_service))?.setOnPreferenceClickListener {
                Toasty.info(requireContext(), "Hello").show()
                true
            }

            findPreference<Preference>(getString(R.string.set_look))?.setOnPreferenceClickListener {
                Toasty.info(requireContext(), "Hello1").show()
                true
            }

            findPreference<Preference>(getString(R.string.set_backup))?.setOnPreferenceClickListener {
                Toasty.info(requireContext(), "Hello2").show()
                true
            }
        }
    }*/
}