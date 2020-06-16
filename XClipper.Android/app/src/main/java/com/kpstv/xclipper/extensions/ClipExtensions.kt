package com.kpstv.xclipper.extensions

import com.kpstv.license.Decrypt
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipEntry
import com.kpstv.xclipper.data.model.ClipTag
import java.util.*
import kotlin.collections.ArrayList

fun Clip.clone(data: String?): Clip {
    return copy(data = data)
}

fun Clip.clone(data: String, tags: Map<String, String>?): Clip {
    return copy(data = data, tags = tags)
}

fun Clip.clone(id: Int) : Clip {
    return copy(id = id)
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

/**
 * An extension function which will decrypt the clip data.
 *
 * It provides a new copy of existing clip model.
 */
fun Clip.decrypt(): Clip {
    return copy(data = data?.Decrypt())
}

/** Converts name to lowercase name */
fun ClipTag.small(): String {
    return name.toLowerCase(Locale.ROOT)
}