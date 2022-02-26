package com.kpstv.xclipper.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.navigation.*
import com.kpstv.xclipper.data.model.SettingDefinition
import com.kpstv.xclipper.extensions.FragClazz
import com.kpstv.xclipper.ui.navigation.AbstractNavigationOptions

class SettingNavViewModel : ViewModel() {
    private val _navigation = MutableLiveData<NavigationOptions>(null)
    val navigation: LiveData<NavigationOptions> = _navigation

    fun navigateTo(
        screenDefinition: SettingDefinition,
        args: BaseArgs? = null,
        transactionType: FragmentNavigator.TransactionType = FragmentNavigator.TransactionType.REPLACE,
        animation: NavAnimation = AnimationDefinition.SlideInRight,
        addToBackStack: Boolean = true
    ) {
        _navigation.value = NavigationOptions(
            clazz = screenDefinition.clazz,
            titleRes = screenDefinition.titleRes,
            navOptions = FragmentNavigator.NavOptions(
                args = args,
                animation = animation,
                transaction = transactionType,
                remember = addToBackStack
            )
        )
    }

    data class NavigationOptions(
        val clazz: FragClazz,
        @StringRes val titleRes: Int,
        val navOptions: FragmentNavigator.NavOptions
    ) : AbstractNavigationOptions()
}