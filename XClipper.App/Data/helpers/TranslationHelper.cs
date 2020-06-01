using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.AccessControl;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
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
        public string APP_SHOW = rm.GetString("app_show");
        public string APP_SETTINGS = rm.GetString("app_settings");
        public string APP_RESTART = rm.GetString("app_restart");
        public string APP_DELETE = rm.GetString("app_delete");
        public string APP_LICENSE = rm.GetString("app_license");
        public string APP_BACKUP = rm.GetString("app_backup");
        public string APP_RESTORE = rm.GetString("app_restore");
        public string APP_RECORD = rm.GetString("app_record");
        public string APP_IMPORT = rm.GetString("app_import");
        public string APP_HELP = rm.GetString("app_help");
        public string APP_EXIT = rm.GetString("app_exit");
        public string SETTINGS_GENERAL = rm.GetString("settings_general");
        public string SETTINGS_OTHER = rm.GetString("settings_other");
        public string SETTINGS_HOTKEY = rm.GetString("settings_hotkey");
        public string SETTINGS_SASS = rm.GetString("settings_sass");
        public string SETTINGS_PNS = rm.GetString("settings_pns");
        public string SETTINGS_TCL = rm.GetString("settings_tcl");
        public string SETTINGS_WTS = rm.GetString("settings_wts");
        public string SETTINGS_ADL = rm.GetString("settings_adl");
        public string SETTINGS_CAL = rm.GetString("settings_cal");
        public string SETTINGS_ISDB = rm.GetString("settings_isdb");
        public string SETTINGS_RESET_BTN = rm.GetString("settings_reset_btn");
        public string SETTINGS_SAVE_BTN = rm.GetString("settings_save_btn");
        public string SETTINGS_RESET = rm.GetString("settings_reset");
        public string SETTINGS_SAVE = rm.GetString("settings_save");
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
        public string MSG_WARNING = rm.GetString("msg_warning");
        public string MSG_ERR = rm.GetString("msg_err");
        public string MSG_PASSWORD = rm.GetString("msg_password");
        public string MSG_LEARN = rm.GetString("msg_learn");
        public string MSG_RESTART = rm.GetString("msg_restart");
        public string MSG_INFO = rm.GetString("msg_info");
        public string MSG_CANCEL = rm.GetString("msg_cancel");
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
    }
}
