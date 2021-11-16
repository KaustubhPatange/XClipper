package com.kpstv.onboarding.internals

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.navigation.*
import com.kpstv.xclipper.extensions.FragClazz

internal class OnBoardingNavViewModel : ViewModel() {
    internal val navigation = MutableLiveData<NavigationOptions>()
    fun navigateTo(
        screen: OnBoardingRoutes,
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