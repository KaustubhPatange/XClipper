package com.kpstv.xclipper.ui.viewmodels

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpgradeModel @ViewModelInject constructor(
    @ApplicationContext private val context: Context,
    private val retrofitUtils: RetrofitUtils
) : ViewModel() {

    suspend fun fetchLatestPrice(): Flow<Result<String>> = flow {
        val response = retrofitUtils.fetch(
            context.getString(R.string.app_website)
        )

        try {
            if (!response.isSuccessful) throw Exception()
            val body = response.body?.string()

            response.close()

            val amount = App.PREMIUM_PRICE_REGEX.toRegex().find(body!!)?.groups?.get(1)?.value!!

            emit(Result.success(amount))
        }catch (e: Exception) {
            emit(Result.failure<String>(Exception(context.getString(R.string.premium_latest))))
        }
    }
}