using static Components.App;

namespace Components
{
    public sealed class TranslationHelper
    {
        private static TranslationHelper Instance;
        public static TranslationHelper Translation
        {
            get
            {
                if (Instance != null)
                    return Instance;
                Instance = new TranslationHelper();
                return Instance;
            }
        }

        public string APP_NAME = rm.GetString("app_name");
        public string APP_START_SERVICE = rm.GetString("app_start_service");
        public string APP_ISRUN_SERVICE = rm.GetString("app_isrun_service");
        public string APP_UPDATE_TITLE = rm.GetString("app_update_title");
        public string APP_COPY_TITLE = rm.GetString("app_copy_title");
        public string APP_UPDATE_TEXT = rm.GetString("app_update_text");
        public string APP_SHOW = rm.GetString("app_show");
        public string APP_SETTINGS = rm.GetString("app_settings");
        public string APP_RESTART = rm.GetString("app_restart");
        public string APP_DELETE = rm.GetString("app_delete");
        public string APP_LICENSE = rm.GetString("app_license");
        public string APP_BACKUP = rm.GetString("app_backup");
        public string APP_RESTORE = rm.GetString("app_restore");
        public string APP_RECORD = rm.GetString("app_record");
        public string APP_IMPORT = rm.GetString("app_import");
        public string APP_CONFIG_SETTING = rm.GetString("app_config_setting");
        public string APP_UPDATE = rm.GetString("app_update");
        public string APP_HELP = rm.GetString("app_help");
        public string APP_EXIT = rm.GetString("app_exit");
        public string SETTINGS_GENERAL = rm.GetString("settings_general");
        public string SETTINGS_OTHER = rm.GetString("settings_other");
        public string SETTINGS_HOTKEY = rm.GetString("settings_hotkey");
        public string SETTINGS_SASS = rm.GetString("settings_sass");
        public string SETTINGS_DSN = rm.GetString("settings_dsn");
        public string SETTINGS_TCL = rm.GetString("settings_tcl");
        public string SETTINGS_WTS = rm.GetString("settings_wts");
        public string SETTINGS_ADL = rm.GetString("settings_adl");
        public string SETTINGS_CAL = rm.GetString("settings_cal");
        public string SETTINGS_ISDB = rm.GetString("settings_isdb");
        public string SETTINGS_RESET_BTN = rm.GetString("settings_reset_btn");
        public string SETTINGS_SAVE_BTN = rm.GetString("settings_save_btn");
        public string SETTINGS_RESET = rm.GetString("settings_reset");
        public string SETTINGS_SAVE = rm.GetString("settings_save");
        public string SETTINGS_FB_PASSWORD = rm.GetString("settings_fb_password");
        public string FILTER_HEADER = rm.GetString("filter_header");
        public string FILTER_TITLE = rm.GetString("filter_title");
        public string FILTER_INDEX = rm.GetString("filter_index");
        public string FILTER_PIN = rm.GetString("filter_pin");
        public string FILTER_UNPIN = rm.GetString("filter_unpin");
        public string FILTER_NEW = rm.GetString("filter_new");
        public string FILTER_OLD = rm.GetString("filter_old");
        public string FILTER_CS_DESC = rm.GetString("filter_cs_desc");
        public string FILTER_CS_ASC = rm.GetString("filter_cs_asc");
        public string FILTER_NONE = rm.GetString("filter_none");
        public string MSG_OK = rm.GetString("msg_ok");
        public string MSG_INFORMATION = rm.GetString("msg_information");
        public string MSG_DOWNLOAD = rm.GetString("msg_download");
        public string MSG_WARNING = rm.GetString("msg_warning");
        public string MSG_ERR = rm.GetString("msg_err");
        public string MSG_PASSWORD = rm.GetString("msg_password");
        public string MSG_LEARN = rm.GetString("msg_learn");
        public string MSG_RESTART = rm.GetString("msg_restart");
        public string MSG_INFO = rm.GetString("msg_info");
        public string MSG_CANCEL = rm.GetString("msg_cancel");
        public string MSG_CANCEL_SMALL = rm.GetString("msg_cancel_small");
        public string MSG_RESTORE_DB = rm.GetString("msg_restore_db");
        public string MSG_ENTER_PASS = rm.GetString("msg_enter_pass");
        public string MSG_PREMIUM_SUCCESS = rm.GetString("msg_premium_success");
        public string MSG_PREMIUM_ERR = rm.GetString("msg_premium_err");
        public string MSG_CLIP_IMPORT = rm.GetString("msg_clip_import");
        public string MSG_MERGE_ENCRYPT = rm.GetString("msg_merge_encrypt");
        public string MSG_DELETE_ALL = rm.GetString("msg_delete_all");
        public string MSG_DELETE_DB = rm.GetString("msg_delete_db");
        public string MSG_RESET_DATA = rm.GetString("msg_reset_data");
        public string MSG_RESET_DATA_SUCCESS = rm.GetString("msg_reset_data_success");
        public string MSG_FIELD_EMPTY = rm.GetString("msg_field_empty");
        public string MSG_CONFIG_SAVE = rm.GetString("msg_config_save");
        public string MSG_CONFIG_RESET = rm.GetString("msg_config_reset");
        public string MSG_CONFIG_RESET_SUCCESS = rm.GetString("msg_config_reset_success");
        public string MSG_FIREBASE_USER_ERROR = rm.GetString("msg_firebase_user_error");
        public string MSG_FIREBASE_CLIENT_ERR = rm.GetString("msg_firebase_client_err");
        public string MSG_FIREBASE_UNKNOWN_ERR = rm.GetString("msg_firebase_unknown_err");
        public string MSG_NEED_AUTH = rm.GetString("msg_need_auth");
        public string MSG_WRONG_SIGNIN = rm.GetString("msg_wrong_signin");
        public string MSG_UNKNOWN_ERR = rm.GetString("msg_unknown_err");
        public string POPUP_ADDED = rm.GetString("popup_added");
        public string POPUP_SPACE_FOCUS = rm.GetString("popup_space_focus");
        public string POPUP_EDIT_ERR = rm.GetString("popup_edit_err");
        public string POPUP_BLANK_ERR = rm.GetString("popup_blank_err");
        public string POPUP_FILE_ERR = rm.GetString("popup_file_err");
        public string CLIP_SEARCH_HINT = rm.GetString("clip_search_hint");
        public string CLIP_CTX_QI = rm.GetString("clip_ctx_qi");
        public string CLIP_CTX_DE = rm.GetString("clip_ctx_de");
        public string CLIP_CTX_TP = rm.GetString("clip_ctx_tp");
        public string CLIP_MSG_CONFIRM = rm.GetString("clip_msg_confirm");
        public string CLIP_FOLDER_COPY = rm.GetString("clip_folder_copy");
        public string CLIP_FILE_SELECT = rm.GetString("clip_file_select");
        public string CLIP_FILE_SELECT2 = rm.GetString("clip_file_select2");
        public string CLIP_CPL = rm.GetString("clip_cpl");
        public string BUY_UNIQUE = rm.GetString("buy_unique");
        public string BUY_KEY = rm.GetString("buy_key");
        public string BUY_ACTIVATE = rm.GetString("buy_activate");
        public string BUY_IS_ACTIVATE = rm.GetString("buy_is_activate");
        public string BUY_NOT_ACTIVATE = rm.GetString("buy_not_activate");
        public string SYNC_IMPORT = rm.GetString("sync_import");
        public string SYNC_IMPORT_SUCCESS = rm.GetString("sync_import_success");
        public string SYNC_IMPORT_ERR = rm.GetString("sync_import_err");
        public string SYNC_IMPORT_ERR2 = rm.GetString("sync_import_err2");
        public string SYNC_IMPORT_MSG = rm.GetString("sync_import_msg");
        public string SYNC_EXPORT = rm.GetString("sync_export");
        public string SYNC_EXPORT_SUCCESS = rm.GetString("sync_export_success");
        public string SYNC_ID_DEFAULT = rm.GetString("sync_id_default");
        public string SYNC_ID_CUSTOM = rm.GetString("sync_id_custom");
        public string UPDATE_TITLE = rm.GetString("update_title");
        public string UPDATE_DOWNLOAD_COMPLETE = rm.GetString("update_download_complete");
    }
}
