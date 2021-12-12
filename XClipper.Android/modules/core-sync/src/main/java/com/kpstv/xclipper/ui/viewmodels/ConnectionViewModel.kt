package com.kpstv.xclipper.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.listeners.ResponseResult
import com.kpstv.xclipper.extensions.utils.SystemUtils
import com.kpstv.xclipper.ui.helpers.AppSettings
import com.kpstv.xclipper.ui.helpers.ConnectionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ConnectionViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val dbConnectionProvider: DBConnectionProvider,
    private val firebaseProvider: FirebaseProvider
) : ViewModel() {
    fun updateDeviceConnection(context: Context, options: FBOptions, responseListener: ResponseListener<Unit>) {
        viewModelScope.launch {
            dbConnectionProvider.saveOptionsToAll(options)
            val result = firebaseProvider.addDevice(SystemUtils.getDeviceId(context))
            when (result) {
                is ResponseResult.Complete -> {
                    ConnectionHelper.loginToDatabase(
                        options = options,
                        appSettings = appSettings,
                        dbConnectionProvider = dbConnectionProvider
                    )
                    responseListener.onComplete(Unit)
                }
                is ResponseResult.Error -> {
                    dbConnectionProvider.detachDataFromAll()
                    responseListener.onError(result.error)
                }
            }
        }
    }

    fun removeDeviceConnection(context: Context, responseListener: ResponseListener<Unit>) {
        viewModelScope.launch {
            val result = firebaseProvider.removeDevice(SystemUtils.getDeviceId(context))
            when (result) {
                is ResponseResult.Complete -> {
                    ConnectionHelper.logoutFromDatabase(
                        context = context,
                        appSettings = appSettings,
                        dbConnectionProvider = dbConnectionProvider
                    )
                    responseListener.onComplete(Unit)
                }
                is ResponseResult.Error -> {
                    ConnectionHelper.logoutFromDatabase(
                        context = context,
                        appSettings = appSettings,
                        dbConnectionProvider = dbConnectionProvider
                    )
                    responseListener.onError(result.error)
                }
            }
        }
    }
}