package com.kpstv.xclipper.data.converters

import com.google.gson.reflect.TypeToken
import com.kpstv.xclipper.data.model.Definition
import com.kpstv.xclipper.extensions.GsonListConverter

object DefinitionListConverter : GsonListConverter<Definition>(typeToken = object : TypeToken<List<Definition>>() {})