package com.kpstv.xclipper.data.localized

import java.lang.Exception

class FBOptions {
    lateinit var uid: String
    lateinit var appId: String
    lateinit var apiKey: String
    lateinit var endpoint: String

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

        fun build(): FBOptions {
            with(options) {
                if (::uid.isInitialized && ::appId.isInitialized && ::apiKey.isInitialized && ::endpoint.isInitialized) {
                    return options
                }else throw FBException("All properties not initialized")
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

