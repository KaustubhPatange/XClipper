package com.kpstv.xclipper.data.helper

import com.kpstv.xclipper.extensions.enumerations.FirebaseState

interface FirebaseProviderHelper {
    fun observeDatabaseChangeEvents()
    fun observeDatabaseInitialization()
    fun removeDatabaseInitializationObservation()
    fun retrieveFirebaseStatus() : FirebaseState
}