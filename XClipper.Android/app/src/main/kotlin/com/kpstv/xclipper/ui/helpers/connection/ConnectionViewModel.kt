package com.kpstv.xclipper.ui.helpers.connection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.listeners.ResponseResult
import com.kpstv.xclipper.extensions.utils.DeviceUtils
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.helpers.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val dbConnectionProvider: DBConnectionProvider,
    private val firebaseProvider: FirebaseProvider
) : ViewModel() {
    fun updateDeviceConnection(context: Context, options: FBOptions, responseListener: ResponseListener<Unit>) {
        viewModelScope.launch {
            dbConnectionProvider.saveOptionsToAll(options)
            val result = firebaseProvider.addDevice(DeviceUtils.getDeviceId(context))
            when (result) {
                is ResponseResult.Complete -> {
                    Utils.loginToDatabase(
                        appSettings = appSettings,
                        dbConnectionProvider = dbConnectionProvider,
                        options = options
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
            val result = firebaseProvider.removeDevice(DeviceUtils.getDeviceId(context))
            when (result) {
                is ResponseResult.Complete -> {
                    Utils.logoutFromDatabase(
                        context = context,
                        appSettings = appSettings,
                        dbConnectionProvider = dbConnectionProvider
                    )
                    responseListener.onComplete(Unit)
                }
                is ResponseResult.Error -> {
                    Utils.logoutFromDatabase(
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