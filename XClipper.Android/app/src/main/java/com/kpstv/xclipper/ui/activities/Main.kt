package com.kpstv.xclipper.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.kpstv.xclipper.App.DARK_THEME
import com.kpstv.xclipper.App.TUTORIAL_PREF
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.ToolbarState
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.ui.helpers.ReviewHelper
import com.kpstv.xclipper.ui.helpers.SyncDialogHelper
import com.kpstv.xclipper.ui.helpers.UpdateHelper
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

@AndroidEntryPoint
class Main : AppCompatActivity() {

    private val TAG = javaClass.simpleName

    @Inject lateinit var preferenceProvider: PreferenceProvider
    @Inject lateinit var dbConnectionProvider: DBConnectionProvider

    private val mainViewModel: MainViewModel by viewModels()

    private var isDarkTheme = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isDarkTheme = DARK_THEME

        ThemeUtils.setTheme(this)

        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)

        val navOptions =
            NavOptions.Builder().setPopUpTo(R.id.fragment_greet, true).build()

        if (preferenceProvider.getBooleanKey(TUTORIAL_PREF, false)) {
            navController.navigate(R.id.fragment_home, null, navOptions)
        }

        UpdateHelper(this).register()
        SyncDialogHelper(
            activity = this,
            preferenceProvider = preferenceProvider,
            dbConnectionProvider = dbConnectionProvider
        ).register()
        ReviewHelper(
            activity = this,
            preferenceProvider = preferenceProvider,
            onNeedToShowReview = { helper: ReviewHelper ->
                helper.showReviewDialog { helper.requestForReview() }
            }
        ).register()
    }

    /**
     * Check if current theme has changed...
     */
    override fun onResume() {
        super.onResume()
        if (DARK_THEME != isDarkTheme) {
            val previousIntent = intent
            finish()
            startActivity(previousIntent)
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