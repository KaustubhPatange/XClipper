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

    const val APP_UPDATE_URI = "https://pastebin.com/raw/FRS7n7Fc" // TODO: Change this update uri
    const val APP_DOWNLOAD_URI = "https://play.google.com/store/apps/details?id=com.kpstv.xclipper"

    var APP_MAX_DEVICE = FB_MIN_DEVICE_CONNECTION
    var APP_MAX_ITEM = FB_MIN_ITEM_STORAGE

    var CLIP_DATA: String? = null
    var LOCAL_MAX_ITEM_STORAGE = 80

    var appList = ArrayList<AppPkg>()
    var blackListedApps: Set<String>? = null

    var UNDO_DELETE_SPAN: Long = 2500

    var DICTIONARY_LANGUAGE = "en"

    var DARK_THEME = true

    const val STAGGERED_SPAN_COUNT = 2
    const val STAGGERED_SPAN_COUNT_MIN = 1

    const val PERMISSION_REQUEST_CODE = 189

    const val DELAY_SPAN: Long = 20
    const val MAX_CHARACTER_TO_STORE = 1000

    const val STANDARD_DATE_FORMAT = "yyyyMMddHHmmss"

    var FB_ENDPOINT: String = ""
    var FB_API_KEY: String = ""
    var FB_APP_ID: String = ""
    var UID: String = "" // TODO: remove this eg: 1PAF8EB-4KR35L-1ICT12V-H7M3FM
    lateinit var DeviceID: String

    var bindToFirebase = true
    var observeFirebase = true

    var showSuggestion = false

    fun getMaxConnection(isLicensed: Boolean): Int =
        if (isLicensed) FB_MAX_DEVICE_CONNECTION else FB_MIN_DEVICE_CONNECTION

    fun getMaxStorage(isLicensed: Boolean): Int =
        if (isLicensed) FB_MAX_ITEM_STORAGE else FB_MIN_ITEM_STORAGE

    var EMPTY_STRING = ""
    var BLANK_STRING = " "

    const val PAIR_SEPARATOR = ";"
    const val ITEM_SEPARATOR = "|"

    const val DATABASE_NAME = "main.db"
    const val DATABASE_MIME_TYPE = "application/vnd.sqlite3"

    const val QR_SCAN_REQUEST_CODE = 153
    const val TAG_DIALOG_REQUEST_CODE = 100
    const val TAG_DIALOG_RESULT_CODE = 1
    const val UPDATE_REQUEST_CODE = 555

    const val TAG_FILTER_CHIP = "com.kpstv.xclipper.tag"

    const val UID_PATTERN_REGEX = "([\\w\\d]+)-([\\w\\d]+)-([\\w\\d]+)-([\\w\\d]+)"
    const val SINGLE_WORD_PATTERN_REGEX = "^[^https?][^\\s\\W]+\$"
    const val DICTIONARY_WORD_PATTERN_REGEX = "\"word\":[\\s]?\".*?\""
    const val DICTIONARY_DEFINITION_PATTERN_REGEX = "\"definition\":[\\s]?\".*?\""
    const val PHONE_PATTERN_REGEX = "(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}" // matches international numbers
    const val PHONE_PATTERN_REGEX1 = "(\\+\\d{2})?[\\s\\.]?(\\d{5})?[\\s.]?(\\d{5})" // matches some specific number patterns
    const val EMAIL_PATTERN_REGEX =
        "([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)"
    const val URL_PATTERN_REGEX =
        "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"
    const val MAP_PATTERN_REGEX =
        "[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)"

    const val APP_CLIP_DATA = "com.kpstv.xclipper.clip_data"

    const val ACTION_OPEN_APP = "com.kpstv.xclipper.open_app"
    const val ACTION_SMART_OPTIONS = "com.kpstv.xclipper.smart_options"
    const val ACTION_REPLACE_FRAG = "com.kpstv.yts.action_replace_frag"
    const val ACTION_INSERT_TEXT = "com.kpstv.xclipper.insert_text"
    const val ACTION_VIEW_CLOSE = "com.kpstv.xclipper.action_view_close"

    const val EXTRA_SERVICE_TEXT = "com.kpstv.xclipper.service_text"

    /** Preference Keys */
    const val TUTORIAL_PREF = "tutorial_pref"
    const val SERVICE_PREF = "service_pref"
    const val SUGGESTION_PREF = "suggestion_pref"
    const val LANG_PREF = "lang_pref"
    const val BLACKLIST_PREF = "blacklist_pref"
    const val CONNECT_PREF = "connect_pref"
    const val LOGOUT_PREF = "logout_pref"
    const val BIND_PREF = "bind_pref"
    const val UID_PREF = "uid_key"
    const val FB_APIKEY_PREF = "apiKey_pref"
    const val FB_APPID_PREF = "appId_pref"
    const val FB_PASSWORD_PREF = "password_pref"
    const val FB_ENDPOINT_PREF = "endpoint_pref"
    const val DARK_PREF = "dark_pref"
    const val IMPORT_PREF = "import_pref"
    const val EXPORT_PREF = "export_pref"
}