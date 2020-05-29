package com.kpstv.xclipper.ui.helpers

import com.kpstv.xclipper.App.DICTIONARY_LANGUAGE
import com.kpstv.xclipper.data.api.GoogleDictionaryApi
import com.kpstv.xclipper.data.model.Definition
import com.kpstv.xclipper.data.repository.DefineRepository
import com.kpstv.xclipper.extensions.ioThread
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.mainThread

class DictionaryApiHelper(
    private val googleDictionaryApi: GoogleDictionaryApi,
    private val defineRepository: DefineRepository
) {
    private val TAG = javaClass.simpleName
    fun define(word: String, responseListener: ResponseListener<Definition>) {
        ioThread {
            try {
                val data = defineRepository.getData(word)
                if (data == null) {
                    val definition =
                        googleDictionaryApi.defineAsync(DICTIONARY_LANGUAGE, word)?.await()
                    if (definition?.define != null) {
                        /** Save data to database */
                        defineRepository.insert(definition)
                        mainThread { responseListener.onComplete(definition) }
                    } else
                        mainThread { responseListener.onError(Exception("Response is null for $word")) }
                } else {
                    mainThread { responseListener.onComplete(data) }
                }
            } catch (e: Exception) {
                mainThread { responseListener.onError(e) }
            }
        }
    }
}

