package com.kpstv.xclipper.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kpstv.xclipper.data.api.GoogleDictionaryApi
import com.kpstv.xclipper.data.api.TinyUrlApi
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.data.repository.TagRepository
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper

/**
 * This factory will return only one instance of viewModel
 */
class MainViewModelFactory(
    private val application: Application,
    private val mainRepository: MainRepository,
    private val tagRepository: TagRepository,
    private val firebaseProvider: FirebaseProvider,
    private val dictionaryApiHelper: DictionaryApiHelper,
    private val tinyUrlApiHelper: TinyUrlApiHelper
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val key = "MainViewModel"
            return if (hashMapViewModel.containsKey(key)) {
                getViewModel(key) as T
            } else {
                addViewModel(
                    key,
                    MainViewModel(
                        application,
                        mainRepository,
                        tagRepository,
                        firebaseProvider,
                        dictionaryApiHelper,
                        tinyUrlApiHelper
                    )
                )
                getViewModel(key) as T
            }
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }


    companion object {
        val hashMapViewModel = HashMap<String, ViewModel>()
        fun addViewModel(key: String, viewModel: ViewModel) {
            hashMapViewModel.put(key, viewModel)
        }

        fun getViewModel(key: String): ViewModel? {
            return hashMapViewModel[key]
        }
    }
}