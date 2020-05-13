package com.kpstv.xclipper

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import com.kpstv.xclipper.App.DeviceID
import com.kpstv.xclipper.data.db.MainDatabase
import com.kpstv.xclipper.data.provider.ClipProvider
import com.kpstv.xclipper.data.provider.ClipProviderImpl
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.FirebaseProviderImpl
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.data.repository.MainRepositoryImpl
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

@SuppressLint("HardwareIds")
@ExperimentalStdlibApi
class XClipperApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@XClipperApplication))

        bind() from singleton { MainDatabase(instance()) }
        bind() from singleton { instance<MainDatabase>().clipMainDao() }
        bind<FirebaseProvider>() with singleton { FirebaseProviderImpl() }
        bind<ClipProvider>() with singleton { ClipProviderImpl() }
        bind<MainRepository>() with singleton { MainRepositoryImpl(instance(), instance(), instance()) }
        bind() from provider { MainViewModelFactory(instance(), instance(), instance()) }
    }

    override fun onCreate() {
        super.onCreate()

        init()
    }

    private fun init() {

        // Set device ID at startup
        DeviceID = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }
}