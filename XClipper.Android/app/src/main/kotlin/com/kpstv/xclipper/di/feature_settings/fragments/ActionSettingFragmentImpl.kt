package com.kpstv.xclipper.di.feature_settings.fragments

import com.kpstv.xclipper.di.fragments.ActionSettingFragment
import com.kpstv.xclipper.extensions.FragClazz
import com.kpstv.xclipper.ui.fragments.SpecialActionFragment
import javax.inject.Inject
import javax.inject.Singleton

class ActionSettingFragmentImpl @Inject constructor() : ActionSettingFragment {
    override fun getClass(): FragClazz = SpecialActionFragment::class
}