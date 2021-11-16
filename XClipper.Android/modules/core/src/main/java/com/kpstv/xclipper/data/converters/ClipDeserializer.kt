package com.kpstv.xclipper.data.converters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.kpstv.xclipper.data.model.Clip
import java.lang.reflect.Type

class ClipDeserializer: JsonDeserializer<Clip> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Clip? {
        return Clip.parse(json!!)
    }
}