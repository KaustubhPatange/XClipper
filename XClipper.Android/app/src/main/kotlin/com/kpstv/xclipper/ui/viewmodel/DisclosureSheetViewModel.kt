package com.kpstv.xclipper.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import com.kpstv.xclipper.extensions.utils.asString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class DisclosureSheetViewModel @Inject constructor() : ViewModel() {
    val privacyCheckedMutableState = MutableStateFlow(false)
    val agreementCheckedMutableState = MutableStateFlow(false)

    val acceptState: Flow<Boolean> =
        privacyCheckedMutableState.combine(agreementCheckedMutableState) { a, b -> a && b}

    internal fun fetchPolicy() : Flow<DisclosureState> = flow {
        emit(DisclosureState.Loading)
        val body = RetrofitUtils.fetch(POLICY_URL).getOrNull()?.asString()
            ?: run { emit(DisclosureState.EmptyPolicy); return@flow }
        val date = UPDATED_DATE_PATTERN.toRegex().find(body)?.groupValues?.get(1)
        if (date != null) {
            emit(DisclosureState.UpdatePolicy(body, date))
        } else {
            emit(DisclosureState.UpdatePolicy(body))
        }
    }

    private companion object {
        private const val UPDATED_DATE_PATTERN = "Updated:\\s?([\\d]{2}/[\\d]{2}/[\\d]{2,4})"

        private const val POLICY_URL = "https://raw.githubusercontent.com/KaustubhPatange/XClipper/master/XClipper.Android/POLICY.md"
    }
}

internal sealed class DisclosureState {
    object Loading : DisclosureState()
    data class UpdatePolicy(val data: String, val lastUpdated: String) : DisclosureState() {
        constructor(data: String) : this(data = data, lastUpdated = "unknown")
    }
    object EmptyPolicy : DisclosureState()
}