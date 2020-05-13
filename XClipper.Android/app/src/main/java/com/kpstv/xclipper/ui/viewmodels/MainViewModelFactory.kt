package com.kpstv.xclipper.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository

class MainViewModelFactory(
    private val application: Application,
    private val repository: MainRepository,
    private val firebaseProvider: FirebaseProvider
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(application, repository, firebaseProvider) as T
    }
}