package com.kpstv.xclipper

import android.provider.Settings
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kpstv.xclipper.data.converters.CipDeserializer
import com.kpstv.xclipper.data.model.Clip

object App {

    val gson: Gson = GsonBuilder()
        .serializeNulls()
        .registerTypeAdapter(Clip::class.java, CipDeserializer())
        .create()

    var CLIP_DATA: String? = null
    var MIN_ITEM_STORAGE = 5
    var MAX_ITEM_STORAGE = 20

    var UNDO_DELETE_SPAN: Long = 2500


    // TODO: Do all your jack jacks
    const val STANDARD_DATE_FORMAT = "yyyyMMddHHmmss"

    // TODO: Make a way to set and get this UID
    var UID: String = "1PAF8EB-4KR35L-1ICT12V-H7M3FM"
    lateinit var DeviceID: String

    var BindToFirebase = true
    var observeFirebase = true

    fun getMaxStorage(isLicensed: Boolean): Int = if (isLicensed) MAX_ITEM_STORAGE else MIN_ITEM_STORAGE

    var EMPTY_STRING = ""
    var BLANK_STRING = " "

    const val PAIR_SEPARATOR = ";"
    const val ITEM_SEPARATOR = "|"

    const val TAG_FILTER_CHIP = "com.kpstv.xclipper.tag"

    const val PHONE_PATTERN_REGEX = "(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}"
    const val EMAIL_PATTERN_REGEX = "([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)"
    const val URL_PATTERN_REGEX = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"
}