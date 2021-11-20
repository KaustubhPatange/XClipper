package com.kpstv.xclipper.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.listeners.ResponseResult
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import com.kpstv.xclipper.extensions.utils.asString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class UpgradeViewModel @Inject constructor() : ViewModel() {

    suspend fun fetchLatestPrice(context: Context): Flow<ResponseResult<String>> = flow {
        val result = RetrofitUtils.fetch(
            context.getString(R.string.app_website)
        )

        try {
            result.fold(
                onSuccess = { response ->
                    if (!response.isSuccessful) throw Exception()
                    val body = response.asString()
                    val amount = PREMIUM_PRICE_REGEX.toRegex().find(body!!)?.groups?.get(1)?.value!!

                    emit(ResponseResult.complete(amount))
                },
                onFailure = { throw Exception("Oops") }
            )
        }catch (e: Exception) {
            emit(ResponseResult.error<String>(Exception(context.getString(R.string.premium_latest))))
        }
    }

    private companion object {
        private const val PREMIUM_PRICE_REGEX = "id=\\\"premium-card-amount\\\">(.*?)<"
    }
}