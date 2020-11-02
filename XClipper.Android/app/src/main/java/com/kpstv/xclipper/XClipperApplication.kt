package com.kpstv.xclipper

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import androidx.preference.PreferenceManager
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.App.AUTO_SYNC_PREF
import com.kpstv.xclipper.App.BIND_PREF
import com.kpstv.xclipper.App.bindToFirebase
import com.kpstv.xclipper.App.DARK_PREF
import com.kpstv.xclipper.App.DARK_THEME
import com.kpstv.xclipper.App.DICTIONARY_LANGUAGE
import com.kpstv.xclipper.App.DeviceID
import com.kpstv.xclipper.App.LANG_PREF
import com.kpstv.xclipper.App.SUGGESTION_PREF
import com.kpstv.xclipper.App.SWIPE_DELETE_PREF
import com.kpstv.xclipper.App.UID
import com.kpstv.xclipper.App.blackListedApps
import com.kpstv.xclipper.App.runAutoSync
import com.kpstv.xclipper.App.showSuggestion
import com.kpstv.xclipper.App.swipeToDelete
import com.kpstv.xclipper.data.api.GoogleDictionaryApi
import com.kpstv.xclipper.data.api.TinyUrlApi
import com.kpstv.xclipper.data.db.MainDatabase
import com.kpstv.xclipper.data.provider.*
import com.kpstv.xclipper.data.repository.*
import com.kpstv.xclipper.extensions.utils.FirebaseUtils
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import com.kpstv.xclipper.extensions.utils.interceptors.NetworkConnectionInterceptor
import com.kpstv.xclipper.service.worker.AccessibilityWorker
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.NotificationHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

@Suppress("unused")
@SuppressLint("HardwareIds")
@ExperimentalStdlibApi
class XClipperApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@XClipperApplication))

        bind() from singleton { MainDatabase(instance()) }
        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { RetrofitUtils(instance()) }
        bind() from singleton { GoogleDictionaryApi(instance()) }
        bind() from singleton { TinyUrlApi(instance()) }
        bind() from singleton { NotificationHelper(instance()) }
        bind() from singleton { DictionaryApiHelper(instance(), instance()) }
        bind() from singleton { TinyUrlApiHelper(instance(), instance()) }
        bind() from singleton { instance<MainDatabase>().clipMainDao() }
        bind() from singleton { instance<MainDatabase>().clipTagDao() }
        bind() from singleton { instance<MainDatabase>().clipDefineDao() }
        bind() from singleton { instance<MainDatabase>().clipUrlDao() }
        bind<DBConnectionProvider>() with singleton {
            DBConnectionProviderImpl(
                instance()
            )
        }
        bind<PreferenceProvider>() with singleton { PreferenceProviderImpl(instance()) }
        bind<FirebaseProvider>() with singleton { FirebaseProviderImpl(instance(), instance()) }
        bind<ClipboardProvider>() with singleton { ClipboardProviderImpl(instance(), instance()) }
        bind<TagRepository>() with singleton { TagRepositoryImpl(instance()) }
        bind<DefineRepository>() with singleton { DefineRepositoryImpl(instance()) }
        bind<UrlRepository>() with singleton { UrlRepositoryImpl(instance()) }
        bind<MainRepository>() with singleton {
            MainRepositoryImpl(
                instance(),
                instance(),
                instance()
            )
        }
        bind() from singleton {
            FirebaseUtils(
                instance(),
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
        bind() from provider {
            MainViewModelFactory(
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
    }
    private val notificationHelper by instance<NotificationHelper>()
    private val preferenceProvider by instance<PreferenceProvider>()
    private val firebaseProvider by instance<FirebaseProvider>()
    private val dbConnectionProvider by instance<DBConnectionProvider>()

    override fun onCreate() {
        super.onCreate()
        init()

        notificationHelper.createChannel()
    }

    private val TAG = javaClass.simpleName

    private fun init() {

        /** Setup HVLog */
        HVLog.Config.init(this)
        HVLog.Config.compactClassName = true

        /** Set device ID at startup */
        DeviceID = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        /** Load settings here */
        DICTIONARY_LANGUAGE = preferenceProvider.getStringKey(LANG_PREF, "en")!!

        /** This will load firebase config setting */
        dbConnectionProvider.loadDataFromPreference()

        DARK_THEME = preferenceProvider.getBooleanKey(DARK_PREF, true)
        showSuggestion = preferenceProvider.getBooleanKey(SUGGESTION_PREF, false)
        runAutoSync = preferenceProvider.getBooleanKey(AUTO_SYNC_PREF, false)
        swipeToDelete = preferenceProvider.getBooleanKey(SWIPE_DELETE_PREF, true)
        blackListedApps = preferenceProvider.getStringSet(App.BLACKLIST_PREF, mutableSetOf())
        bindToFirebase = if (UID.isBlank()) false
        else
            preferenceProvider.getBooleanKey(BIND_PREF, false)

        /** Initialize firebase data */
        firebaseProvider.initialize(dbConnectionProvider.optionsProvider())

        /** Initialize accessibility worker */
        AccessibilityWorker.schedule(this)
    }
}