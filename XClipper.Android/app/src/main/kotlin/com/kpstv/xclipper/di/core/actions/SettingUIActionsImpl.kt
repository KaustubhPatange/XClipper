package com.kpstv.xclipper.di.core.actions

import android.content.Context
import com.kpstv.xclipper.ui.actions.SettingUIActions
import com.kpstv.xclipper.ui.fragments.settings.GeneralPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SettingUIActionsImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingUIActions {
    override fun refreshGeneralSettingsUI() {
        GeneralPreference.refreshSettings(context)
    }
}