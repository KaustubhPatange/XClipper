package com.kpstv.xclipper.di

import androidx.fragment.app.Fragment
import com.kpstv.xclipper.data.model.SettingDefinition
import com.kpstv.xclipper.di.fragments.ActionSettingFragment
import com.kpstv.xclipper.extensions.FragClazz
import com.kpstv.xclipper.feature_settings.R
import com.kpstv.xclipper.ui.fragments.Upgrades
import com.kpstv.xclipper.ui.fragments.settings.GeneralPreference
import com.kpstv.xclipper.ui.fragments.settings.look_feel.LookFeelPreference
import com.kpstv.xclipper.ui.fragments.settings.SettingsFragment
import javax.inject.Inject

class SettingScreenHandler @Inject constructor(
    private val settingDefinitions: Map<Class<out Fragment>, SettingDefinition>,
    private val actionSettingFragment: ActionSettingFragment
) {
    fun getAll(): List<SettingDefinition> = settingDefinitions
        .map { it.value }
        .toMutableList()
        .apply {
            add(3, screenAction())
        }
    fun get(clazz: FragClazz) = findScreen(clazz)

    fun screenGeneral(): SettingDefinition = findScreen(GeneralPreference::class)

    fun screenLookFeel(): SettingDefinition = findScreen(LookFeelPreference::class)

    fun screenUpgrade(): SettingDefinition = findScreen(Upgrades::class)
    fun argsScreenUpgrade(provideArgs: Upgrades.Args.() -> Unit) = Upgrades.Args().apply(provideArgs)
    fun isUpgradeScreen(definition: SettingDefinition): Boolean = definition.clazz == Upgrades::class

    private fun screenAction(): SettingDefinition = SettingDefinition(
        clazz = actionSettingFragment.getClass(),
        titleRes = R.string.actions,
        drawableRes = R.drawable.ic_extension
    )

    private fun screenSettingList(): SettingDefinition = SettingDefinition(
        clazz = SettingsFragment::class,
        titleRes = R.string.settings,
        drawableRes = -1
    )

    private fun findScreen(clazz: FragClazz): SettingDefinition {
        if (clazz == SettingsFragment::class) return screenSettingList()
        return getAll().find { it.clazz == clazz }!!
    }
}