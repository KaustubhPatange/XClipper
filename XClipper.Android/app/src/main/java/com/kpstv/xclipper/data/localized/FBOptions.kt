package com.kpstv.xclipper.data.localized

class FBOptions {
    lateinit var uid: String
        private set
    lateinit var appId: String
        private set
    lateinit var apiKey: String
        private set
    lateinit var endpoint: String
        private set
    lateinit var password: String
        private set
    var authClientId: String? = null
        private set
    var isAuthNeeded: Boolean = false
        private set

    class Builder {
        private val options = FBOptions()
        fun setUID(value: String): Builder {
            options.uid = value
            return this
        }

        fun setAppId(value: String): Builder {
            options.appId = value
            return this
        }

        fun setApiKey(value: String): Builder {
            options.apiKey = value
            return this
        }

        fun setEndPoint(value: String): Builder {
            options.endpoint = value
            return this
        }

        fun setPassword(value: String): Builder {
            options.password = value
            return this
        }

        fun setAuthClientId(value: String?): Builder {
            options.authClientId = value
            return this
        }

        fun setIsAuthNeeded(value: Boolean): Builder {
            options.isAuthNeeded = value
            return this
        }

        fun build(): FBOptions {
            with(options) {
                if (::uid.isInitialized && ::appId.isInitialized && ::apiKey.isInitialized && ::endpoint.isInitialized) {
                    return options
                } else throw FBException("All properties not initialized")
            }
        }
    }

    class FBException(
        private val msg: String
    ) : Exception() {

        override val message: String?
            get() = msg
    }
}

