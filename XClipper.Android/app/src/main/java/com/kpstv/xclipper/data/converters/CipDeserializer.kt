package com.kpstv.xclipper.data.converters

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.kpstv.xclipper.App.gson
import com.kpstv.xclipper.data.model.Clip
import java.lang.reflect.Type
import kotlin.math.log

class CipDeserializer: JsonDeserializer<Clip> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Clip {
        return Clip.from(json!!)
    }
}