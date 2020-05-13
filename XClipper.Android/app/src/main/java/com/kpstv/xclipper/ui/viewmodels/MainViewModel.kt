package com.kpstv.xclipper.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.data.model.User
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository

class MainViewModel(
    application: Application,
    private val repository: MainRepository,
    private val firebaseProvider: FirebaseProvider
) : AndroidViewModel(application) {

    private val TAG = javaClass.name
    val clipLiveData = MutableLiveData<User>()

    init {
       /* firebaseProvider.observeDataChange(
            changed = {
               // Log.e(TAG, it?.clips?.size.toString())
                clipLiveData.postValue(it)
            },
            error = {
                // TODO: Do something when error
            },
            deviceValidated = {
               *//* if (!it)
                    Log.e(TAG, "Failed to validate")
                else Log.e(TAG, "Validated")*//*
            }
        )*/
    }



}