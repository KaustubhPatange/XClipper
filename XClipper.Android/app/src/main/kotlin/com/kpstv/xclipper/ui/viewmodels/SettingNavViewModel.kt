package com.kpstv.xclipper.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.NavAnimation
import com.kpstv.navigation.Navigator
import com.kpstv.xclipper.ui.fragments.Settings

class SettingNavViewModel : ViewModel() {
    private val _navigation = MutableLiveData<Navigator.NavOptions>(null)
    val navigation: LiveData<Navigator.NavOptions> = _navigation

    fun navigateTo(
        screen: Settings.Screen,
        args: BaseArgs? = null,
        transactionType: Navigator.TransactionType = Navigator.TransactionType.REPLACE,
        animation: NavAnimation = AnimationDefinition.SlideInRight(),
        addToBackStack: Boolean = true
    ) {
        _navigation.value = Navigator.NavOptions(
            clazz = screen.clazz,
            args = args,
            animation = animation,
            type = transactionType,
            addToBackStack = addToBackStack
        )
    }
}