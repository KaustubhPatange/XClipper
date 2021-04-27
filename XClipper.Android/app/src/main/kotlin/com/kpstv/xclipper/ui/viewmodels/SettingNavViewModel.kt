package com.kpstv.xclipper.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.NavAnimation
import com.kpstv.navigation.Navigator
import com.kpstv.xclipper.extensions.FragClazz
import com.kpstv.xclipper.ui.fragments.Settings

class SettingNavViewModel : ViewModel() {
    private val _navigation = MutableLiveData<NavigationOptions>(null)
    val navigation: LiveData<NavigationOptions> = _navigation

    fun navigateTo(
        screen: Settings.Screen,
        args: BaseArgs? = null,
        transactionType: Navigator.TransactionType = Navigator.TransactionType.REPLACE,
        animation: NavAnimation = AnimationDefinition.SlideInRight,
        addToBackStack: Boolean = true
    ) {
        _navigation.value = NavigationOptions(
            clazz = screen.clazz,
            navOptions = Navigator.NavOptions(
                args = args,
                animation = animation,
                transaction = transactionType,
                remember = addToBackStack
            )
        )
    }

    data class NavigationOptions(
        val clazz: FragClazz,
        val navOptions: Navigator.NavOptions
    )
}