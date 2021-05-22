using System;
using System.IO;
using System.Reflection;
using System.Windows.Forms;

namespace Components
{
    public static class Constants
    {
        #region Directory Constants

        public static string ApplicationLocation = Assembly.GetExecutingAssembly().Location;
        public static string BaseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        public static string RoamingDirectory = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        public static string ApplicationDirectory = Path.Combine(RoamingDirectory, "XClipper");
        public static string ApplicationScriptsDirectory = Path.Combine(ApplicationDirectory, "scripts");
        public static string ApplicationTempDirectory = Path.Combine(ApplicationDirectory, ".cache");
        public static string ApplicationLogDirectory = Path.Combine(ApplicationDirectory, "logs");
        public static string ApplicationKeyLogDirectory = Path.Combine(ApplicationLogDirectory, "keylogs");
        public static string ApplicationExceptionDirectory = Path.Combine(ApplicationLogDirectory, "crash-reports");
        public static string SettingsPath = Path.Combine(ApplicationDirectory, "config.xml");
        public static string TimeStampsPath = Path.Combine(ApplicationTempDirectory, "timestamp.xml");
        public static string LicenseFilePath = Path.Combine(ApplicationDirectory, "key.lic");
        public static string BackupFolder = Path.Combine(ApplicationDirectory, "Backup");
        public static string ImageFolder = Path.Combine(ApplicationDirectory, "Image");
        public static string DatabasePath = Path.Combine(ApplicationDirectory, "data.db");
        public static string CustomFirebasePath = Path.Combine(ApplicationDirectory, "custom-firebase.xml");
        public static string FirebaseCredentialPath = Path.Combine(ApplicationDirectory, "credentials-firebase.xml");
        public static string QRImageFilePath = Path.Combine(ApplicationTempDirectory, "qr.png");
        public static string LogFilePath = Path.Combine(ApplicationLogDirectory, "xclipper");
        public static string KeyLogFilePath = Path.Combine(ApplicationKeyLogDirectory, "keylog");
        public static string UserStateFile = Path.Combine(ApplicationDirectory, "user.xml");
        public static string IgnoreFilePath = Path.Combine(ApplicationDirectory, ".ignore");
        public static string BufferFilePath = Path.Combine(ApplicationDirectory, "buffers.xml");
        public static string CopyScriptsPath = Path.Combine(ApplicationScriptsDirectory, "copy");
        public static string PasteScriptsPath = Path.Combine(ApplicationScriptsDirectory, "paste");

        public static string GetUpdatePackageFile(int version) => Path.Combine(ApplicationTempDirectory, $"package-update-{version}.exe");

        #endregion

        #region Some application specific constants

        public static string ApplicationVersion = Assembly.GetExecutingAssembly().GetName().Version.ToString();
        public static string ApplicationWebsite = "https://kaustubhpatange.github.io/XClipper";
        public const string GITHUB_RELEASE_URI = "https://api.github.com/repos/adb-over-wifi/demo-release/releases"; // TODO: Change update uri https://api.github.com/repos/KaustubhPatange/XClipper/releases
        public const string OAUTH_TOKEN_URI = "https://oauth2.googleapis.com/token";
        public const string ACTION_NOT_COMPLETE_WIKI = "https://kaustubhpatange.github.io/XClipper/docs/#/faqs#q-last-x-actions-didnt-complete-what-to-do";
        public const string SETTINGS = "Settings";
        public const string ENVIRONMENT = "Environment";
        public const string TIMESTAMPS = "Timestamps";
        public const string CREDENTIAL = "Credential";
        public const string BUFFERS = "Buffers";
        public const string FIREBASE = "Firebase";
        public const string DESKTOP_AUTH = "DesktopAuth";
        public const string MOBILE_AUTH = "MobileAuth";
        public const int NOTIFICATION_TRUNCATE_TEXT = 150;
        public const int RESTSHARP_TIMEOUT = 15 * 3000;

        #endregion

        #region Some Filter Constants

        public const string CONTENT_FILTER_NONE = "";
        public const string CONTENT_FILTER_TEXT = "{ContentType=Text}";
        public const string CONTENT_FILTER_IMAGE = "{ContentType=Image}";
        public const string CONTENT_FILTER_FILES = "{ContentType=Files}";
        public const string CONTENT_FILTER_NON_PINNED = "{Filter=Unpinned}";
        public const string CONTENT_FILTER_PINNED = "{Filter=Pinned}";
        public const string CONTENT_FILTER_OLDEST_FIRST = "{Filter=Oldest}";
        public const string CONTENT_FILTER_NEWEST_FIRST = "{Filter=Newest}";
        public const string CONTENT_FILTER_TEXTLENGTH_DESC = "{Filter=TextLength-desc}";
        public const string CONTENT_FILTER_TEXTLENGTH_ASC = "{Filter=TextLength-asc}";

        #endregion

        #region Database Constants

        public const int FB_MAX_ITEM = 10;
        public const int FB_MAX_LENGTH = 1000;
        public const int FB_MAX_CONNECTION = 1;
        public const string FB_DEFAULT_PASS = "JfbY+e0iD/RMVJDOF5MWphCDGB45G/0kLtF4Vv+sbF7SL3VdbP3GyMgvPVz3e56e1pmrJct0LRMHH2Sz+hLASpqz/1YTvV3GF6e7pHBcuWVpuzkMDHEDJG3IXRj9VIFA";

        public const int SYNC_MIN_ITEM = 10;
        public const int SYNC_MIN_LENGTH = 1500;
        public const int SYNC_MIN_CONNECTION = 1;

        public const int SYNC_MAX_ITEM = 120;
        public const int SYNC_MAX_LENGTH = 10000;
        public const int SYNC_MAX_CONNECTION = 10;

        #endregion

        #region Database Path Constants

        public const string DEVICE_REGEX_PATH_PATTERN = @"\/Devices\/sdk";
        public const string CLIP_REGEX_PATH_PATTERN = @"\/Clips";
        public const string CLIP_ITEM_REGEX_PATTERN = @"^\/(\d+)$";
        public const string PATH_CLIP_DATA = "/Clips/data";
        public const string PATH_CLIP_IMAGE_DATA = @"^(!\[)(.*?)(])(\((https?:\/\/.*?)\))$";

        public const string PATH_CLIP_REGEX_PATTERN = @"\/Clips\/(\d+)";
        public const string PATH_DEVICE_REGEX_PATTERN = @"\/Devices\/(\d+)";
        public const string PATH_DEVICES = @"/Devices";
        public const string PATH_CLIPS = @"/Clips";

        #endregion

        #region Help links

        public const string MOBILE_APP = "https://github.com/KaustubhPatange/XClipper/tree/master/XClipper.Android";

        public const string UPGRADE_LICENSE = "https://kaustubhpatange.github.io/XClipper"; // TODO: Edit: No need yet! change this which is used in UpgradeTipItem.

        public const string DOCUMENTATION = "https://kaustubhpatange.github.io/XClipper/docs";
        public const string DOC_INTRODUCTION = "https://kaustubhpatange.github.io/XClipper/docs/#/introduction";
        public const string DOC_SYNCHRONIZATION = "https://kaustubhpatange.github.io/XClipper/docs/#/sync";
        public const string DOC_SECURE_LOCAL = "https://kaustubhpatange.github.io/XClipper/docs/#/protect#local-database";
        public const string DOC_SECURE_REMOTE = "https://kaustubhpatange.github.io/XClipper/docs/#/protect#remote-database";
        public const string DOC_PURCHASE_MIGRATION = "https://kaustubhpatange.github.io/XClipper/docs/#/migrating";
        public const string DOC_MANUAL_ACTIVATION = "https://kaustubhpatange.github.io/XClipper/docs/#/activation";

        public const string DOC_SCRIPTING = "https://kaustubhpatange.github.io/XClipper/docs/#/scripting";
        public const string EXAMPLES_SCRIPTING = "https://kaustubhpatange.github.io/XClipper/docs/#/scripting?id=examples";

        #endregion

        #region Default Settings 

        public class Settings
        {
            public const XClipperLocation APP_DISPLAY_LOCATION = XClipperLocation.BottomRight;
            public const XClipperStore WHAT_TO_STORE = XClipperStore.All;
            public const int TOTAL_CLIP_LENGTH = 80;
            public const bool IS_CTRL = true;
            public const bool IS_ALT = false;
            public const bool IS_SHIFT = false;
            public const string HOT_KEY = "Oem3";
            public const bool START_ON_SYSTEM_STARTUP = false;
            public const bool CHECK_APP_UPDATES = true;
            public const bool DISPLAY_START_NOTIFICATION = true;
            public const bool PLAY_NOTIFICATION_SOUND = true;
            public const bool SHOW_DATA_CHANGE_NOTIFICATION = true;
            public const string CURRENT_LOCALE = "locales\\en.xaml";
            public const bool IS_SECURE_DB = false;
            public const bool USE_CUSTOM_PASSWORD = false;
            public static readonly string CUSTOM_PASSWORD = Core.CONNECTION_PASS.Decrypt();
            public static readonly string DATABASE_ENCRYPT_PASSWORD = FB_DEFAULT_PASS.Decrypt();
            public const int TRUNCATE_LIST = 20;
            public const bool BIND_DATABASE = false;
            public const bool BIND_DELETE = false;
            public const bool BIND_IMAGE = false;
            public const bool EXIT_ON_CRASH = true;
            public const bool NO_NOTIFY_CHANGES = false;
            public const bool USE_EXPERIMENTAL_KEY_CAPTURE = true;
            public static Buffer CopyBuffer1
            {
                get
                {
                    Buffer b = new();
                    b.Copy.IsCtrl = true;
                    b.Copy.HotKey = Keys.Oemplus.ToString();

                    b.Paste.IsCtrl = true;
                    b.Paste.IsShift = true;
                    b.Paste.HotKey = Keys.Oemplus.ToString();
                    return b;
                }
            }

            public static Buffer CopyBuffer2
            {
                get
                {
                    Buffer b = new();
                    b.Copy.IsCtrl = true;
                    b.Copy.HotKey = Keys.OemMinus.ToString();

                    b.Paste.IsCtrl = true;
                    b.Paste.IsShift = true;
                    b.Paste.HotKey = Keys.OemMinus.ToString();
                    return b;
                }
            }
        }

        #endregion
    }
}
