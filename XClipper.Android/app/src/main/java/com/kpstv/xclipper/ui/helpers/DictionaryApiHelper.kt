package com.kpstv.xclipper.ui.helpers

import com.kpstv.xclipper.data.api.GoogleDictionaryApi
import com.kpstv.xclipper.data.model.Definition
import com.kpstv.xclipper.extensions.Coroutines
import org.kodein.di.android.kodein

class DictionaryApiHelper(
    private val googleDictionaryApi: GoogleDictionaryApi
) {
   fun define(word: String, block: (Definition) -> Unit) {
       Coroutines.main {
           val definition = googleDictionaryApi.defineAsync(word)?.await()
           if (definition?.define != null)
               block.invoke(definition)
       }
   }
}

