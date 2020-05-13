package com.kpstv.xclipper.extensions

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kpstv.license.Encrypt
import com.kpstv.xclipper.data.converters.DateConverter
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipEntry
import java.util.*
import kotlin.collections.ArrayList


class FValueEventListener(val onDataChange: (DataSnapshot) -> Unit, val onError: (DatabaseError) -> Unit) :
    ValueEventListener {
    override fun onDataChange(data: DataSnapshot) = onDataChange.invoke(data)
    override fun onCancelled(error: DatabaseError) = onError.invoke(error)
}

fun List<Clip>.cloneToEntries() : List<ClipEntry> {
    val list = ArrayList<ClipEntry>()
    this.forEach {
        list.add(ClipEntry(it.data, DateConverter.fromDateToString(it.time)))
    }
    return list
}
