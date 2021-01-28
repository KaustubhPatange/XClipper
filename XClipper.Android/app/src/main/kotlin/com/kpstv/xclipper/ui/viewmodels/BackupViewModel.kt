package com.kpstv.xclipper.ui.viewmodels

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.xclipper.data.provider.BackupProvider
import com.kpstv.xclipper.extensions.SimpleFunction
import kotlinx.coroutines.launch

class BackupViewModel @ViewModelInject constructor(
    private val backupProvider: BackupProvider
) : ViewModel() {

    fun createBackup(toUri: Uri, onComplete: SimpleFunction = {}, onError: SimpleFunction = {}) {
        viewModelScope.launch {
            if (backupProvider.backup(toUri))
                onComplete.invoke()
            else
                onError.invoke()
        }
    }

    fun restoreBackup(fromUri: Uri, onComplete: SimpleFunction = {}, onError: SimpleFunction = {}) {
        viewModelScope.launch {
            if (backupProvider.restore(fromUri))
                onComplete.invoke()
            else
                onError.invoke()
        }
    }
}