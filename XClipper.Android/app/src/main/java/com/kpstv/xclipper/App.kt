package com.kpstv.xclipper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kpstv.xclipper.data.converters.CipDeserializer
import com.kpstv.xclipper.data.converters.DefinitionDeserializer
import com.kpstv.xclipper.data.model.AppPkg
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Definition

object App {

    val gson: Gson = GsonBuilder()
        .serializeNulls()
        .registerTypeAdapter(Clip::class.java, CipDeserializer())
        .registerTypeAdapter(Definition::class.java, DefinitionDeserializer())
        .create()

    private const val FB_MIN_ITEM_STORAGE = 5
    private const val FB_MAX_ITEM_STORAGE = 20
    private const val FB_MIN_DEVICE_CONNECTION = 1
    private const val FB_MAX_DEVICE_CONNECTION = 5

    var CLIP_DATA: String? = null
    var LOCAL_MAX_ITEM_STORAGE = 80

    var appList = ArrayList<AppPkg>()
    var blackListedApps: Set<String>? = null

    var UNDO_DELETE_SPAN: Long = 2500

    var DICTIONARY_LANGUAGE = "en"

    const val DELAY_SPAN: Long = 20
    const val MAX_CHARACTER_TO_STORE = 1000



    // TODO: Do all your jack jacks
    const val STANDARD_DATE_FORMAT = "yyyyMMddHHmmss"

    // TODO: Make a way to set and get this UID
    var UID: String = "1PAF8EB-4KR35L-1ICT12V-H7M3FM"
    lateinit var DeviceID: String

    var BindToFirebase = true
    var observeFirebase = true

    fun getMaxConnection(isLicensed: Boolean): Int = if (isLicensed) FB_MAX_DEVICE_CONNECTION else FB_MIN_DEVICE_CONNECTION
    fun getMaxStorage(isLicensed: Boolean): Int = if (isLicensed) FB_MAX_ITEM_STORAGE else FB_MIN_ITEM_STORAGE

    var EMPTY_STRING = ""
    var BLANK_STRING = " "

    const val PAIR_SEPARATOR = ";"
    const val ITEM_SEPARATOR = "|"

    const val TAG_DIALOG_REQUEST_CODE = 100
    const val TAG_DIALOG_RESULT_CODE = 1

    const val TAG_FILTER_CHIP = "com.kpstv.xclipper.tag"

    const val UID_PATTERN_REGEX = "([\\w\\d]+)-([\\w\\d]+)-([\\w\\d]+)-([\\w\\d]+)"
    const val SINGLE_WORD_PATTERN_REGEX = "^[^https?][^\\s\\W]+\$"
    const val DICTIONARY_WORD_PATTERN_REGEX = "\"word\":[\\s]?\".*?\""
    const val DICTIONARY_DEFINITION_PATTERN_REGEX = "\"definition\":[\\s]?\".*?\""
    const val PHONE_PATTERN_REGEX = "(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}"
    const val EMAIL_PATTERN_REGEX = "([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)"
    const val URL_PATTERN_REGEX = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"
    const val MAP_PATTERN_REGEX = "[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)"

    const val APP_CLIP_DATA = "com.kpstv.xclipper.clip_data"

    const val ACTION_OPEN_APP = "com.kpstv.xclipper.open_app"
    const val ACTION_SMART_OPTIONS = "com.kpstv.xclipper.smart_options"

    /** Preference Keys */
    const val SERVICE_PREF = "service_pref"
    const val LANG_PREF = "lang_pref"
    const val BLACKLIST_PREF = "blacklist_pref"
    const val CONNECT_PREF = "connect_pref"
    const val LOGOUT_PREF = "logout_pref"
    const val BIND_PREF = "bind_pref"
    const val UID_PREF = "uid_key"
}