package com.kpstv.xclipper.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.navigation.*
import com.kpstv.xclipper.App
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.databinding.ActivityStartBinding
import com.kpstv.xclipper.extensions.FragClazz
import com.kpstv.xclipper.extensions.applyEdgeToEdgeMode
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.fragments.Home
import com.kpstv.xclipper.ui.fragments.Settings
import com.kpstv.xclipper.ui.fragments.welcome.*
import com.kpstv.xclipper.ui.helpers.ConnectionHelper
import com.kpstv.xclipper.ui.helpers.ReviewHelper
import com.kpstv.xclipper.ui.helpers.SyncDialogHelper
import com.kpstv.xclipper.ui.helpers.UpdateHelper
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Start : AppCompatActivity(), NavigatorTransmitter {
    private val binding by viewBinding(ActivityStartBinding::inflate)
    private val navViewModel by viewModels<NavViewModel>()
    private val mainViewModel by viewModels<MainViewModel>()
    private lateinit var navigator: Navigator
    @Inject lateinit var dbConnectionProvider: DBConnectionProvider
    @Inject lateinit var preferenceProvider: PreferenceProvider

    override fun getNavigator(): Navigator = navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        applyEdgeToEdgeMode()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        navigator = Navigator(supportFragmentManager, binding.root)
        navigator.autoChildElevation()

        navViewModel.navigation.observe(this) { options ->
            navigator.navigateTo(options)
        }

        if (savedInstanceState == null) {
            if (preferenceProvider.getBooleanKey(App.TUTORIAL_PREF, false)) {
                navViewModel.navigateTo(Screen.HOME)
            } else {
                navViewModel.navigateTo(Screen.GREET)
            }
        }

        registerHelpers()
    }

    private fun registerHelpers() {
        UpdateHelper(this).register()
        SyncDialogHelper(this, preferenceProvider, dbConnectionProvider).register()
        ReviewHelper(this, preferenceProvider).register()
    }

    override fun onBackPressed() {
        if (navigator.canFinish())
            super.onBackPressed()
    }

    // Needed for scanning QRs
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        ConnectionHelper(this, mainViewModel,dbConnectionProvider)
            .parse(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    enum class Screen(val clazz: FragClazz) {
        /* Introduction screens */
        GREET(Greeting::class),
        ANDROID10(Android10::class),
        TURN_ON_SERVICE(TurnOnService::class),
        ENABLE_SUGGESTIONS(EnableSuggestion::class),
        STANDARD_COPY(StandardCopy::class),
        QUICK_SETTING_TITLE(QuickSettingTitle::class),
        WINDOWS_APP(WindowApp::class),

        /* Main screens */
        HOME(Home::class),
        SETTING(Settings::class);
    }
}

class NavViewModel : ViewModel() {
    internal val navigation = MutableLiveData<Navigator.NavOptions>()
    fun navigateTo(
        screen: Start.Screen,
        args: BaseArgs? = null,
        addToBackStack: Boolean = false,
        transactionType: Navigator.TransactionType = Navigator.TransactionType.REPLACE,
        animation: NavAnimation = AnimationDefinition.None,
        popUpTo: Boolean = false,
    ) {
        navigation.value = Navigator.NavOptions(
            clazz = screen.clazz,
            args = args,
            animation = animation,
            type = transactionType,
            addToBackStack = addToBackStack,
            popUpToThis = popUpTo
        )
    }
}