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
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.fragments.Home
import com.kpstv.xclipper.ui.fragments.Settings
import com.kpstv.xclipper.ui.fragments.welcome.*
import com.kpstv.xclipper.ui.helpers.*
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Start : AppCompatActivity(), FragmentNavigator.Transmitter {
    private val binding by viewBinding(ActivityStartBinding::inflate)
    private val navViewModel by viewModels<NavViewModel>()
    private val mainViewModel by viewModels<MainViewModel>()
    private lateinit var navigator: FragmentNavigator

    @Inject
    lateinit var dbConnectionProvider: DBConnectionProvider
    @Inject
    lateinit var preferenceProvider: PreferenceProvider
    @Inject
    lateinit var retrofitUtils: RetrofitUtils

    val updateHelper by lazy { UpdateHelper(this, retrofitUtils) }
    private val intentHelper by lazy { ActivityIntentHelper(this) }

    override fun getNavigator(): FragmentNavigator = navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        applyEdgeToEdgeMode()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val startScreen = if (preferenceProvider.getBooleanKey(App.TUTORIAL_PREF, false)) Screen.HOME.clazz else Screen.GREET.clazz
        navigator = FragmentNavigator.with(this, savedInstanceState)
            .initialize(binding.root, Destination.of(startScreen))
        navigator.autoChildElevation()

        navViewModel.navigation.observe(this) { options ->
            navigator.navigateTo(options.clazz, options.navOptions)
        }

        registerHelpers()

        intentHelper.handle(intent)
    }

    private fun registerHelpers() {
        updateHelper.register()
        SyncDialogHelper(this, preferenceProvider, dbConnectionProvider).register()
        ReviewHelper(this, preferenceProvider).register()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intentHelper.handle(intent)
    }

    override fun onStart() {
        super.onStart()
        Notifications.sendUpdateAvailableNotification(this)
// TODO: Update available notification does not work, try testing it.
    }

    override fun onBackPressed() {
        if (navigator.canFinish())
            super.onBackPressed()
    }

    // Needed for scanning QRs
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        ConnectionHelper(this, mainViewModel, dbConnectionProvider)
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