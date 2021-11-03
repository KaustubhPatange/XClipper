package com.kpstv.xclipper.data.converters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.kpstv.xclipper.data.model.Definition
import java.lang.reflect.Type

class DefinitionDeserializer : JsonDeserializer<Definition> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Definition {
        val string = json.toString()

        val dictionaryRegex = DICTIONARY_DEFINITION_PATTERN_REGEX.toRegex()
        val wordPattern = DICTIONARY_WORD_PATTERN_REGEX.toRegex()
        if (dictionaryRegex.containsMatchIn(string)) {
            val word = wordPattern.find(string)?.value?.split("\"")?.get(3)
            val define = dictionaryRegex.find(string)?.value?.split("\"")?.get(3)
            return Definition.from(word, define)
        }
        return Definition.returnNull()
    }

    private companion object {
        private const val DICTIONARY_WORD_PATTERN_REGEX = "\"word\":[\\s]?\".*?\""
        private const val DICTIONARY_DEFINITION_PATTERN_REGEX = "\"definition\":[\\s]?\".*?\""
    }
}