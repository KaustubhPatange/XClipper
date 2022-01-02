package com.kpstv.xclipper.data.provider

import android.content.Context
import com.kpstv.license.Encryption
import com.kpstv.license.Encryption.DecryptPref
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DBConnectionProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
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
        UID_PATTERN_REGEX.toRegex().let {
            if (it.containsMatchIn(data)) {

                /** Here we will parse the data */

                val values = data.split(";")
                val firebaseConfigs = values[1].DecryptPref().split(";")
                val uid = values[0]
                val firebaseAppId = firebaseConfigs[0]
                val firebaseApiKey = firebaseConfigs[1]
                val firebaseEndpoint = firebaseConfigs[2]
                val firebasePassword = firebaseConfigs[3]
                val isAuthNeeded = firebaseConfigs[4].toBoolean()
                val firebaseAuthClientId = if (isAuthNeeded) firebaseConfigs[5] else null
                val hasBinded = firebaseConfigs[6].toBoolean()

                if (!hasBinded) {
                    responseListener.onError(Exception(context.getString(R.string.db_provider_enable_bind_setting)))
                    return
                }

                responseListener.onComplete(
                    FBOptions.Builder().apply {
                        setUID(uid)
                        setApiKey(firebaseApiKey)
                        setAppId(firebaseAppId)
                        setEndPoint(firebaseEndpoint)
                        setPassword(firebasePassword)
                        setIsAuthNeeded(isAuthNeeded)
                        setAuthClientId(firebaseAuthClientId)
                    }
                        .build()
                )
            } else
                responseListener.onError(Exception("Not a valid UID"))
        }
    }

    override fun isValidData(): Boolean {
        loadDataFromPreference()
        return !(FB_API_KEY.isEmpty() && FB_APP_ID.isEmpty() && FB_ENDPOINT.isEmpty())
    }

    override fun loadDataFromPreference() {
        FB_API_KEY = preferenceProvider.getEncryptString(
            FB_API_KEY_PREF,
            EMPTY_STRING
        ) ?: EMPTY_STRING
        FB_APP_ID = preferenceProvider.getEncryptString(
            FB_APP_ID_PREF,
            EMPTY_STRING
        ) ?: EMPTY_STRING
        FB_ENDPOINT = preferenceProvider.getEncryptString(
            FB_ENDPOINT_PREF,
            EMPTY_STRING
        ) ?: EMPTY_STRING
        AUTH_NEEDED = preferenceProvider.getBooleanKey(
            AUTH_NEEDED_PREF,
            false
        )
        UID = preferenceProvider.getStringKey(UID_PREF, EMPTY_STRING) ?: EMPTY_STRING

        /** This will initialize a password */
        val fbPassword = preferenceProvider.getEncryptString(
            FB_PASSWORD_PREF,
            EMPTY_STRING
        ) ?: EMPTY_STRING
        Encryption.setPassword(fbPassword)
    }

    override fun saveOptionsToAll(options: FBOptions) {
        preferenceProvider.putEncryptString(
            FB_API_KEY_PREF, options.apiKey
        )
        preferenceProvider.putEncryptString(
            FB_ENDPOINT_PREF, options.endpoint
        )
        preferenceProvider.putEncryptString(
            FB_APP_ID_PREF, options.appId
        )
        preferenceProvider.putEncryptString(
            FB_PASSWORD_PREF, options.password
        )
        preferenceProvider.putStringKey(
            UID_PREF, options.uid
        )
        preferenceProvider.putBooleanKey(
            AUTH_NEEDED_PREF, options.isAuthNeeded
        )

        /** This will update the credentials */
        loadDataFromPreference()
    }

    override fun optionsProvider(): FBOptions? {
        return if (isValidData()) {
            FBOptions.Builder().apply {
                setUID(UID)
                setApiKey(FB_API_KEY)
                setAppId(FB_APP_ID)
                setEndPoint(FB_ENDPOINT)
                setPassword(Encryption.getPassword())
                setIsAuthNeeded(AUTH_NEEDED)
            }.build()
        } else
            null
    }

    override fun detachDataFromAll() {
        preferenceProvider.removeKey(FB_ENDPOINT_PREF)
        preferenceProvider.removeKey(FB_APP_ID_PREF)
        preferenceProvider.removeKey(FB_API_KEY_PREF)
        preferenceProvider.removeKey(FB_PASSWORD_PREF)
        preferenceProvider.removeKey(UID_PREF)
        preferenceProvider.removeKey(AUTH_NEEDED_PREF)

        loadDataFromPreference()
    }

    private companion object {
        private const val EMPTY_STRING = ""
        private const val UID_PATTERN_REGEX = "([\\w\\d]+)-([\\w\\d]+)-([\\w\\d]+)-([\\w\\d]+)"

        private const val UID_PREF = "uid_key"
        private const val FB_API_KEY_PREF = "apiKey_pref"
        private const val FB_APP_ID_PREF = "appId_pref"
        private const val FB_PASSWORD_PREF = "password_pref"
        private const val FB_ENDPOINT_PREF = "endpoint_pref"
        private const val AUTH_NEEDED_PREF = "authNeed_pref"

        @Volatile private var FB_ENDPOINT: String = ""
        @Volatile private var FB_API_KEY: String = ""
        @Volatile private var FB_APP_ID: String = ""
        @Volatile private var AUTH_NEEDED: Boolean = false
        @Volatile private var UID: String = ""
    }
}