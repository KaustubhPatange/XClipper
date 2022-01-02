package com.kpstv.xclipper.di

import androidx.fragment.app.Fragment
import dagger.MapKey
import kotlin.reflect.KClass

@MapKey
annotation class SettingKey(val value: KClass<out Fragment>)