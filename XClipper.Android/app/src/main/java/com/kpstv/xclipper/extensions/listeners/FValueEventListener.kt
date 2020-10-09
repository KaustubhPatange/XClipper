package com.kpstv.xclipper.extensions.listeners

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kpstv.xclipper.extensions.FirebaseFunction

/**
 * An alternate callback for Firebase event listener.
 */
class FValueEventListener(
    private val onDataChange: FirebaseFunction,
    private val onError: (DatabaseError) -> Unit
) :
    ValueEventListener {
    override fun onDataChange(data: DataSnapshot) = onDataChange.invoke(data)
    override fun onCancelled(error: DatabaseError) = onError.invoke(error)
}