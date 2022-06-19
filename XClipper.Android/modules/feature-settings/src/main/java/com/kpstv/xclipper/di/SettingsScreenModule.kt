package com.kpstv.xclipper.di

import com.kpstv.xclipper.data.model.SettingDefinition
import com.kpstv.xclipper.feature_settings.R
import com.kpstv.xclipper.ui.fragments.Upgrades
import com.kpstv.xclipper.ui.fragments.settings.*
import com.kpstv.xclipper.ui.fragments.settings.look_feel.LookFeelPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(FragmentComponent::class)
class SettingsScreenModule {
    @Provides
    @IntoMap
    @SettingKey(GeneralPreference::class)
    fun generalScreen() = SettingDefinition(
        clazz = GeneralPreference::class,
        titleRes = R.string.general,
        drawableRes = R.drawable.ic_general
    )

    @Provides
    @IntoMap
    @SettingKey(AccountPreference::class)
    fun accountScreen() = SettingDefinition(
        clazz = AccountPreference::class,
        titleRes = R.string.account,
        drawableRes = R.drawable.ic_synchronization
    )

    @Provides
    @IntoMap
    @SettingKey(LookFeelPreference::class)
    fun lookFeelScreen() = SettingDefinition(
        clazz = LookFeelPreference::class,
        titleRes = R.string.look_feel,
        drawableRes = R.drawable.ic_looks
    )

    @Provides
    @IntoMap
    @SettingKey(BackupPreference::class)
    fun backupScreen() = SettingDefinition(
        clazz = BackupPreference::class,
        titleRes = R.string.backup,
        drawableRes = R.drawable.ic_backup
    )

    @Provides
    @IntoMap
    @SettingKey(Upgrades::class)
    fun upgradeScreen() = SettingDefinition(
        clazz = Upgrades::class,
        titleRes = R.string.upgrade,
        drawableRes = R.drawable.ic_upgrade
    )

    @Provides
    @IntoMap
    @SettingKey(AboutPreference::class)
    fun aboutScreen() = SettingDefinition(
        clazz = AboutPreference::class,
        titleRes = R.string.about,
        drawableRes = R.drawable.ic_info
    )
}