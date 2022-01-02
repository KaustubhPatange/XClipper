package com.kpstv.xclipper.di

import com.kpstv.xclipper.data.helper.ClipRepositoryHelper
import com.kpstv.xclipper.data.helper.ClipRepositoryHelperImpl
import com.kpstv.xclipper.data.helper.FirebaseProviderHelper
import com.kpstv.xclipper.data.helper.FirebaseProviderHelperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class HelperModules {
    @[Binds Singleton]
    abstract fun clipRepositoryHelper(clipRepositoryHelperImpl: ClipRepositoryHelperImpl): ClipRepositoryHelper

    @[Binds Singleton]
    abstract fun firebaseProviderHelper(firebaseProviderHelperImpl: FirebaseProviderHelperImpl): FirebaseProviderHelper
}