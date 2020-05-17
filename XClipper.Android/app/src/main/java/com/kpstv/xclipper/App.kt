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
}