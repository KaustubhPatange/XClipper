package com.kpstv.xclipper.ui.helpers

import com.kpstv.xclipper.data.api.GoogleDictionaryApi
import com.kpstv.xclipper.data.localized.dao.DefineDao
import com.kpstv.xclipper.data.model.Definition
import com.kpstv.xclipper.extensions.launchInIO
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.launchInMain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictionaryApiHelper @Inject constructor(
    private val googleDictionaryApi: GoogleDictionaryApi,
    private val defineRepository: DefineDao
) {
    private val TAG = javaClass.simpleName
    fun define(word: String, langCode: String, responseListener: ResponseListener<Definition>) {
        launchInIO {
            try {
                val data = defineRepository.getWord(word)
                if (data == null) {
                    val definition =
                        googleDictionaryApi.defineAsync(langCode, word)?.await()
                    if (definition?.define != null) {
                        /** Save data to database */
                        defineRepository.insert(definition)
                        launchInMain { responseListener.onComplete(definition) }
                    } else
                        launchInMain { responseListener.onError(Exception("Response is null for $word")) }
                } else {
                    launchInMain { responseListener.onComplete(data) }
                }
            } catch (e: Exception) {
                launchInMain { responseListener.onError(e) }
            }
        }
    }
}

