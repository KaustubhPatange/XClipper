package com.kpstv.xclipper.data.provider

import com.kpstv.license.DatabaseEncryption
import com.kpstv.license.DecryptPref
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.EMPTY_STRING
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.extensions.listeners.ResponseListener

class DBConnectionProviderImpl(
    private val preferenceProvider: PreferenceProvider
) : DBConnectionProvider {
    /**
     * This method will process the data coming from QR Image capture.
     */
    override fun processResult(data: String?, responseListener: ResponseListener<FBOptions>) {
        if (data == null) {
            responseListener.onError(Exception("Data is null"))
            return
        }
        App.UID_PATTERN_REGEX.toRegex().let {
            if (it.containsMatchIn(data)) {

                /** Here we will parse the data */

                val values = data.split(";")
                val firebaseConfigs = values[1].DecryptPref().split(";")
                val uid = values[0]
                val firebaseAppId = firebaseConfigs[0]
                val firebaseApiKey = firebaseConfigs[1]
                val firebaseEndpoint = firebaseConfigs[2]
                val firebasePassword = firebaseConfigs[3]

                responseListener.onComplete(
                    FBOptions.Builder().apply {
                        setUID(uid)
                        setApiKey(firebaseApiKey)
                        setAppId(firebaseAppId)
                        setEndPoint(firebaseEndpoint)
                        setPassword(firebasePassword)
                    }
                        .build()
                )
            } else
                responseListener.onError(Exception("Not a valid UID"))
        }
    }

    override fun isValidData(): Boolean {
        loadDataFromPreference()
        return !(App.FB_API_KEY.isEmpty() && App.FB_APP_ID.isEmpty() && App.FB_ENDPOINT.isEmpty())
    }

    override fun loadDataFromPreference() {
        App.FB_API_KEY = preferenceProvider.getEncryptString(
            App.FB_API_KEY_PREF,
            EMPTY_STRING
        ) ?: EMPTY_STRING
        App.FB_APP_ID = preferenceProvider.getEncryptString(
            App.FB_APP_ID_PREF,
            EMPTY_STRING
        ) ?: EMPTY_STRING
        App.FB_ENDPOINT = preferenceProvider.getEncryptString(
            App.FB_ENDPOINT_PREF,
            EMPTY_STRING
        ) ?: EMPTY_STRING
        App.UID = preferenceProvider.getStringKey(App.UID_PREF, EMPTY_STRING) ?: EMPTY_STRING

        /** This will initialize a password */
        val fbPassword = preferenceProvider.getEncryptString(
            App.FB_PASSWORD_PREF,
            EMPTY_STRING
        ) ?: EMPTY_STRING
        DatabaseEncryption.setPassword(fbPassword)
    }

    override fun saveOptionsToAll(options: FBOptions) {
        preferenceProvider.putEncryptString(
            App.FB_API_KEY_PREF, options.apiKey
        )
        preferenceProvider.putEncryptString(
            App.FB_ENDPOINT_PREF, options.endpoint
        )
        preferenceProvider.putEncryptString(
            App.FB_APP_ID_PREF, options.appId
        )
        preferenceProvider.putEncryptString(
            App.FB_PASSWORD_PREF, options.password
        )
        preferenceProvider.putStringKey(
            App.UID_PREF, options.uid
        )

        /** This will load the data to App.kt properties */
        loadDataFromPreference()
    }

    override fun optionsProvider(): FBOptions? {
        return if (isValidData()) {
            FBOptions.Builder().apply {
                setUID(App.UID)
                setApiKey(App.FB_API_KEY)
                setAppId(App.FB_APP_ID)
                setEndPoint(App.FB_ENDPOINT)
                setPassword(DatabaseEncryption.getPassword())
            }.build()
        } else
            null
    }

    override fun detachDataFromAll() {
        preferenceProvider.removeKey(App.FB_ENDPOINT_PREF)
        preferenceProvider.removeKey(App.FB_APP_ID_PREF)
        preferenceProvider.removeKey(App.FB_API_KEY_PREF)
        preferenceProvider.removeKey(App.FB_PASSWORD_PREF)
        preferenceProvider.removeKey(App.UID_PREF)

        loadDataFromPreference()
    }
}