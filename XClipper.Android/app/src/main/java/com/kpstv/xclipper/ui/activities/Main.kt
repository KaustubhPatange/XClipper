package com.kpstv.xclipper.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.kpstv.xclipper.App.DARK_THEME
import com.kpstv.xclipper.App.TUTORIAL_PREF
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.ToolbarState
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.fragment_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class Main : AppCompatActivity(), KodeinAware {

    private val TAG = javaClass.simpleName

    override val kodein by kodein()
    private val viewModelFactory by instance<MainViewModelFactory>()
    private val preferenceProvider by instance<PreferenceProvider>()

    var isDarkTheme = true

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isDarkTheme = DARK_THEME

        ThemeUtils.setTheme(this)

        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)

        val navOptions =
            NavOptions.Builder().setPopUpTo(R.id.fragment_greet, true).build()

        if (preferenceProvider.getBooleanKey(TUTORIAL_PREF, false)) {
            navController.navigate(R.id.fragment_home, null, navOptions)
        }
    }


    /**
     * Check if current theme has changed...
     */
    override fun onResume() {
        super.onResume()
        if (DARK_THEME != isDarkTheme) {
            val previousIntent = intent
            finish()
            startActivity(previousIntent);
        }
    }

    override fun onBackPressed() {
        when {
            mainViewModel.stateManager.isMultiSelectionStateActive() -> mainViewModel.stateManager.setToolbarState(
                ToolbarState.NormalViewState
            )
            searchView?.onBackPressed() == true -> return
            else -> super.onBackPressed()
        }
    }
}