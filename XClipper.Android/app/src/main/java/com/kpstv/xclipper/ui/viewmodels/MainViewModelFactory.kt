package com.kpstv.xclipper.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.data.repository.TagRepository

class MainViewModelFactory(
    private val application: Application,
    private val mainRepository: MainRepository,
    private val tagRepository: TagRepository,
    private val firebaseProvider: FirebaseProvider
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(application, mainRepository, tagRepository, firebaseProvider) as T
    }
}