package com.kpstv.xclipper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kpstv.xclipper.data.converters.ClipDeserializer
import com.kpstv.xclipper.data.converters.DefinitionDeserializer
import com.kpstv.xclipper.data.model.AppPkg
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Definition

object App {

    val gson: Gson = GsonBuilder()
        .serializeNulls()
        .registerTypeAdapter(Clip::class.java, ClipDeserializer())
        .registerTypeAdapter(Definition::class.java, DefinitionDeserializer())
        .create()

    private const val FB_MIN_ITEM_STORAGE = 5
    private const val FB_MAX_ITEM_STORAGE = 20
    private const val FB_MIN_DEVICE_CONNECTION = 1
    private const val FB_MAX_DEVICE_CONNECTION = 5

    var APP_MAX_DEVICE = FB_MIN_DEVICE_CONNECTION
    var APP_MAX_ITEM = FB_MIN_ITEM_STORAGE

    var blackListedApps: Set<String>? = null

    // TODO: Move setting from general preference to special settings
    var DICTIONARY_LANGUAGE = "en"

    var LoadImageMarkdownText = true

    var FB_ENDPOINT: String = ""
    var FB_API_KEY: String = ""
    var FB_APP_ID: String = ""
    var AUTH_NEEDED: Boolean = false
    var UID: String = ""
    lateinit var DeviceID: String

    var bindToFirebase = true
    var bindDelete = false
    var runAutoSync = false
    var observeFirebase = true

    var showSuggestion = false
    var swipeToDelete = true
    var trimClipText = false

    fun getMaxConnection(isLicensed: Boolean): Int =
        if (isLicensed) FB_MAX_DEVICE_CONNECTION else FB_MIN_DEVICE_CONNECTION

    fun getMaxStorage(isLicensed: Boolean): Int =
        if (isLicensed) FB_MAX_ITEM_STORAGE else FB_MIN_ITEM_STORAGE

    /** Preference Keys */
    const val TUTORIAL_PREF = "tutorial_pref"
    const val SUGGESTION_PREF = "suggestion_pref"
    const val SWIPE_DELETE_PREF = "swipe_delete_pref"
    const val LANG_PREF = "lang_pref"
    const val BLACKLIST_PREF = "blacklist_pref"
    const val BIND_PREF = "bind_pref"
    const val BIND_DELETE_PREF = "bindDelete_pref"
    const val AUTO_SYNC_PREF = "autoSync_pref"
    const val TRIM_CLIP_PREF = "trim_clip_pref"
    const val SHOW_SEARCH_FEATURE = "to_show_search_feature"
}