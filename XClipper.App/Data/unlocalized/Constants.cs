using System;
using System.IO;
using System.Reflection;
using System.Windows.Forms;

namespace Components
{
    public static class Constants
    {

        public static bool isFileExist()
        {
            return true;
        }

        public static string ApplicationLocation = Assembly.GetExecutingAssembly().Location;
        public static string BaseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        public static string RoamingDirectory = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        public static string ApplicationDirectory = Path.Combine(RoamingDirectory, "XClipper");
        public static string ApplicationTempDirectory = Path.Combine(ApplicationDirectory, ".cache");
        public static string SettingsPath = Path.Combine(ApplicationDirectory, "config.xml");
        public static string LicenseFilePath = Path.Combine(ApplicationDirectory, "key.lic");
        public static string BackupFolder = Path.Combine(ApplicationDirectory, "Backup");
        public static string ImageFolder = Path.Combine(ApplicationDirectory, "Image");
        public static string DatabasePath = Path.Combine(ApplicationDirectory, "data.db");
        public static string CustomFirebasePath = Path.Combine(ApplicationDirectory, "custom-firebase.xml");
        public static string FirebaseCredentialPath = Path.Combine(ApplicationDirectory, "credentials-firebase.xml");
        public static string QRImageFilePath = Path.Combine(ApplicationTempDirectory, "qr.png");
        public static string UpdatePackageFile = Path.Combine(ApplicationTempDirectory, "package-update.exe");

        public static string ApplicationVersion = Assembly.GetExecutingAssembly().GetName().Version.ToString();

        #region Some application specific constants

        public const string UPDATE_URI = "https://pastebin.com/raw/FRS7n7Fc"; // todo: Change update uri
        public const string OAUTH_TOKEN_URI = "https://oauth2.googleapis.com/token";
        public const string SETTINGS = "Settings";
        public const string CREDENTIAL = "Credential";
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

        public const int FB_MAX_ITEM = 20;
        public const int FB_MAX_LENGTH = 10000;
        public const int FB_MAX_CONNECTION = 5;
        public const string FB_DEFAULT_PASS = "JfbY+e0iD/RMVJDOF5MWphCDGB45G/0kLtF4Vv+sbF7SL3VdbP3GyMgvPVz3e56e1pmrJct0LRMHH2Sz+hLASpqz/1YTvV3GF6e7pHBcuWVpuzkMDHEDJG3IXRj9VIFA";

        #endregion

        #region Database Path Constants

        public const string DEVICE_REGEX_PATH_PATTERN = @"\/Devices\/sdk";
        public const string PATH_CLIP_DATA = "/Clips/data";

        #endregion
    }
}
