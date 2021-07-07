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

    var CLIP_DATA: String? = null
    const val LOCAL_MAX_ITEM_STORAGE = 200

    var appList: List<AppPkg> = listOf()
    var blackListedApps: Set<String>? = null

    var UNDO_DELETE_SPAN: Long = 2500

    var DICTIONARY_LANGUAGE = "en"

    var DARK_THEME = true

    var LoadImageMarkdownText = true

    const val STAGGERED_SPAN_COUNT = 2
    const val STAGGERED_SPAN_COUNT_MIN = 1

    const val PERMISSION_REQUEST_CODE = 189
    const val AUTH_REQUEST_CODE = 210

    const val DELAY_SPAN: Long = 20
    const val DELAY_FIREBASE_SPAN: Long = 3000
    const val MAX_CHARACTER_TO_STORE = 1000

    const val STANDARD_DATE_FORMAT = "yyyyMMddHHmmss"

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

    var EMPTY_STRING = ""
    var BLANK_STRING = " "

    const val PAIR_SEPARATOR = ";"
    const val ITEM_SEPARATOR = "|"

    const val QR_SCAN_REQUEST_CODE = 153
    const val UPDATE_REQUEST_CODE = 555

    const val TAG_FILTER_CHIP = "com.kpstv.xclipper.tag"

    const val UID_PATTERN_REGEX = "([\\w\\d]+)-([\\w\\d]+)-([\\w\\d]+)-([\\w\\d]+)"
    const val SINGLE_WORD_PATTERN_REGEX = "^[^https?][^\\s\\W]+\$"
    const val DICTIONARY_WORD_PATTERN_REGEX = "\"word\":[\\s]?\".*?\""
    const val DICTIONARY_DEFINITION_PATTERN_REGEX = "\"definition\":[\\s]?\".*?\""
    const val PHONE_PATTERN_REGEX = "(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}" // matches international numbers
    const val PHONE_PATTERN_REGEX1 = "(\\+[\\d-]{1,4})[\\s\\.]?(\\d{5})[\\s\\.]?(\\d{5})" // matches some specific number patterns
    const val EMAIL_PATTERN_REGEX =
        "([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)"
    const val URL_PATTERN_REGEX =
        "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"
    const val MAP_PATTERN_REGEX =
        "[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)°?\\s*(N|S|E|W)?,\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)°?\\s*(N|S|E|W)?"
    const val DATE_PATTERN_REGEX =
        "(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[13-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})\$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))\$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})"

    const val MARKDOWN_IMAGE_ONLY_REGEX = "^(!\\[)(.*?)(])(\\((https?://.*?)\\))$"

    const val PREMIUM_PRICE_REGEX = "id=\\\"premium-card-amount\\\">(.*?)<"

    const val APP_CLIP_DATA = "com.kpstv.xclipper.clip_data"
    const val NOTIFICATION_CODE = "com.kpstv.xclipper.notification_code"

    const val ACTION_OPEN_APP = "com.kpstv.xclipper.open_app"
    const val ACTION_SMART_OPTIONS = "com.kpstv.xclipper.smart_options"
    const val ACTION_REPLACE_FRAG = "com.kpstv.yts.action_replace_frag"
    const val ACTION_INSERT_TEXT = "com.kpstv.xclipper.insert_text"
    const val ACTION_DISABLE_SERVICE = "com.kpstv.xclipper.disable_service"
    const val ACTION_VIEW_CLOSE = "com.kpstv.xclipper.action_view_close"
    const val ACTION_NODE_INFO = "com.kpstv.xclipper.action_node_text"

    const val EXTRA_NODE_CURSOR = "com.kpstv.xclipper.extra_node_cursor"
    const val EXTRA_NODE_TEXT = "com.kpstv.xclipper.extra_node_text"
    const val EXTRA_SERVICE_TEXT = "com.kpstv.xclipper.service_text"
    const val EXTRA_SERVICE_TEXT_LENGTH = "com.kpstv.xclipper.service_text_word_length"
    const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
    const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"

    /** Preference Keys */
    const val TUTORIAL_PREF = "tutorial_pref"
    const val SERVICE_PREF = "service_pref"
    const val SUGGESTION_PREF = "suggestion_pref"
    const val SWIPE_DELETE_PREF = "swipe_delete_pref"
    const val LANG_PREF = "lang_pref"
    const val BLACKLIST_PREF = "blacklist_pref"
    const val CONNECT_PREF = "connect_pref"
    const val LOGOUT_PREF = "logout_pref"
    const val BIND_PREF = "bind_pref"
    const val BIND_DELETE_PREF = "bindDelete_pref"
    const val AUTO_SYNC_PREF = "autoSync_pref"
    const val FORCE_REMOVE_PREF = "forceRemove_pref"
    const val HELP_PREF = "help_pref"
    const val UID_PREF = "uid_key"
    const val AUTH_NEEDED_PREF = "authNeed_pref"
    const val FB_API_KEY_PREF = "apiKey_pref"
    const val FB_APP_ID_PREF = "appId_pref"
    const val FB_PASSWORD_PREF = "password_pref"
    const val FB_ENDPOINT_PREF = "endpoint_pref"
    const val DARK_PREF = "dark_pref"
    const val IMPORT_PREF = "import_pref"
    const val EXPORT_PREF = "export_pref"
    const val IMAGE_MARKDOWN_PREF = "image_markdown_pref"
    const val TRIM_CLIP_PREF = "trim_clip_pref"
    const val SHOW_SEARCH_FEATURE = "to_show_search_feature"
}