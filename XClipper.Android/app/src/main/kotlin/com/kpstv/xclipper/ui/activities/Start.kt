package com.kpstv.xclipper.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.navigation.*
import com.kpstv.onboarding.OnBoardingFragment
import com.kpstv.xclipper.PinLockHelper
import com.kpstv.xclipper.extensions.FragClazz
import com.kpstv.xclipper.extensions.applyEdgeToEdgeMode
import com.kpstv.xclipper.ui.fragments.Home
import com.kpstv.xclipper.ui.fragments.Settings
import com.kpstv.xclipper.ui.fragments.sheets.DisclosureSheet
import com.kpstv.xclipper.ui.helpers.ActivityIntentHelper
import com.kpstv.xclipper.ui.helpers.AppSettings
import com.kpstv.xclipper.ui.helpers.FirebaseSyncHelper
import com.kpstv.xclipper.ui.helpers.fragments.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Start : AppCompatActivity(), FragmentNavigator.Transmitter {
    private val navViewModel by viewModels<NavViewModel>()
    private lateinit var navigator: FragmentNavigator

    @Inject
    lateinit var appSettings: AppSettings

    val updateHelper by lazy { UpdateHelper(this) }
    private val intentHelper by lazy { ActivityIntentHelper(this) }

    override fun getNavigator(): FragmentNavigator = navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        PinLockHelper.checkPinLock(this)
        applyEdgeToEdgeMode()
        super.onCreate(savedInstanceState)

        val startScreen = if (appSettings.isOnBoardingScreensShowed()) Screen.HOME.clazz else Screen.ONBOARDING.clazz
        navigator = FragmentNavigator.with(this, savedInstanceState)
            .initialize(findViewById(android.R.id.content), Destination.of(startScreen))
        navigator.autoChildElevation()

        navViewModel.navigation.observe(this) { options ->
            navigator.navigateTo(options.clazz, options.navOptions)
        }

        registerHelpers()

        intentHelper.handle(intent)

        FirebaseSyncHelper.migrate(this)
    }

    private fun registerHelpers() {
        updateHelper.register()
        SyncDialogHelper(this).register()
        ReviewHelper(this).register()
        ImproveDetectionHelper(this).register()
        DisclosureHelper(this, navViewModel).register()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intentHelper.handle(intent)
    }

    override fun onBackPressed() {
        if (navigator.canFinish())
            super.onBackPressed()
    }

    enum class Screen(val clazz: FragClazz) {
        /* Main screens */
        ONBOARDING(OnBoardingFragment::class),
        HOME(Home::class),
        SETTING(Settings::class),

        /* Sheets */
        DISCLOSURE(DisclosureSheet::class);
    }
}

class NavViewModel : ViewModel() {
    internal val navigation = MutableLiveData<NavigationOptions>()
    fun navigateTo(
        screen: Start.Screen,
        args: BaseArgs? = null,
        remember: Boolean = false,
        transactionType: FragmentNavigator.TransactionType = FragmentNavigator.TransactionType.REPLACE,
        animation: NavAnimation = AnimationDefinition.None,
        historyOptions: HistoryOptions = HistoryOptions.None
    ) {
        navigation.value = NavigationOptions(
            clazz = screen.clazz,
            navOptions = FragmentNavigator.NavOptions(
                args = args,
                animation = animation,
                transaction = transactionType,
                remember = remember,
                historyOptions = historyOptions
            )
        )
    }

    data class NavigationOptions(
        val clazz: FragClazz,
        val navOptions: FragmentNavigator.NavOptions
    )
}