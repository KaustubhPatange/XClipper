package com.kpstv.xclipper.extensions

import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipEntry

fun Clip.clone(data: String?): Clip {
    return copy(data = data)
}

fun Clip.clone(data: String, tags: Map<String, String>?): Clip {
    return copy(data = data, tags = tags)
}

/**
 * A process of converting Clip Model to ClipEntry Model
 */
fun List<Clip>.cloneToEntries(): List<ClipEntry> {
    val list = ArrayList<ClipEntry>()
    this.forEach {
        list.add(ClipEntry.from(it))
    }
    return list
}

/**
 * An extension function which will auto-generate "timeString" property
 * in all of the clip items.
 */
fun List<Clip>.cloneForAdapter(): List<Clip> {
    this.forEach {
        Clip.autoFill(it)
    }
    return this
}