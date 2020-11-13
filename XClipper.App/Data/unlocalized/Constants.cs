using System;
using System.IO;
using System.Reflection;

namespace Components
{
    public static class Constants
    {
        public static bool isFileExist()
        {
            return true;
        }

        #region Directory Constants

        public static string ApplicationLocation = Assembly.GetExecutingAssembly().Location;
        public static string BaseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        public static string RoamingDirectory = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        public static string ApplicationDirectory = Path.Combine(RoamingDirectory, "XClipper");
        public static string ApplicationTempDirectory = Path.Combine(ApplicationDirectory, ".cache");
        public static string ApplicationLogDirectory = Path.Combine(ApplicationDirectory, "logs");
        public static string SettingsPath = Path.Combine(ApplicationDirectory, "config.xml");
        public static string LicenseFilePath = Path.Combine(ApplicationDirectory, "key.lic");
        public static string BackupFolder = Path.Combine(ApplicationDirectory, "Backup");
        public static string ImageFolder = Path.Combine(ApplicationDirectory, "Image");
        public static string DatabasePath = Path.Combine(ApplicationDirectory, "data.db");
        public static string CustomFirebasePath = Path.Combine(ApplicationDirectory, "custom-firebase.xml");
        public static string FirebaseCredentialPath = Path.Combine(ApplicationDirectory, "credentials-firebase.xml");
        public static string QRImageFilePath = Path.Combine(ApplicationTempDirectory, "qr.png");
        public static string LogFilePath = Path.Combine(ApplicationLogDirectory, "xclipper");
        public static string UpdatePackageFile = Path.Combine(ApplicationTempDirectory, "package-update.exe");
        public static string UserStateFile = Path.Combine(ApplicationDirectory, "user.xml");

        #endregion


        #region Some application specific constants

        public static string ApplicationVersion = Assembly.GetExecutingAssembly().GetName().Version.ToString();
        public static string ApplicationWebsite = "https://kaustubhpatange.github.io/XClipper";
        [Obsolete("Use GITHUB_RELEASE_URI", true)]
        public const string UPDATE_URI = "https://pastebin.com/raw/FRS7n7Fc"; // TODO; Change update uri https://raw.githubusercontent.com/KaustubhPatange/XClipper/master/UPDATE
        public const string GITHUB_RELEASE_URI = "https://api.github.com/repos/adb-over-wifi/demo-release/releases"; // TODO: Change update uri https://api.github.com/repos/KaustubhPatange/XClipper/releases
        public const string OAUTH_TOKEN_URI = "https://oauth2.googleapis.com/token";
        public const string ACTION_NOT_COMPLETE_WIKI = "https://github.com/KaustubhPatange/XClipper/wiki/FAQs#q-last-x-actions-didnt-complete-what-to-do";
        public const string SETTINGS = "Settings";
        public const string CREDENTIAL = "Credential";
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

        public const int FB_MAX_ITEM = 15;
        public const int FB_MAX_LENGTH = 1000;
        public const int FB_MAX_CONNECTION = 5;
        public const string FB_DEFAULT_PASS = "JfbY+e0iD/RMVJDOF5MWphCDGB45G/0kLtF4Vv+sbF7SL3VdbP3GyMgvPVz3e56e1pmrJct0LRMHH2Sz+hLASpqz/1YTvV3GF6e7pHBcuWVpuzkMDHEDJG3IXRj9VIFA";

        public const int SYNC_MIN_ITEM = 15;
        public const int SYNC_MIN_LENGTH = 2000;
        public const int SYNC_MIN_CONNECTION = 5;

        public const int SYNC_MAX_ITEM = 200;
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

        public const string MOBILE_APP = "https://github.com/KaustubhPatange/XClipper"; // TODO; change it with playstore url or github XClipper.Android url

        public const string DATA_SYNCHRONIZATION = "https://github.com/KaustubhPatange/XClipper/wiki/Data-Synchronization";
        public const string MANUAL_ACTIVATION = "https://github.com/KaustubhPatange/XClipper/wiki/Manual-License-Activation";
        public const string MIGRATION_GUIDE = "https://github.com/KaustubhPatange/XClipper/wiki/Migrating-License";
        public const string SECURE_DB = "https://github.com/KaustubhPatange/XClipper/wiki/Using-secure-database";
        public const string SECURE_FIREBASE = "https://github.com/KaustubhPatange/XClipper/wiki/Securing-Database";

        public const string UPGRADE_LICENSE = "https://kaustubhpatange.github.io/XClipper"; // TODO; change this which is used in UpgradeTipItem

        #endregion
    }
}
