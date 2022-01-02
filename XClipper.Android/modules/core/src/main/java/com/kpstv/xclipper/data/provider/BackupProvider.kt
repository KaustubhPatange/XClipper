package com.kpstv.xclipper.data.provider

import android.net.Uri

interface BackupProvider {
    suspend fun backup(toUri: Uri): Boolean
    suspend fun restore(fromUri: Uri): Boolean
}