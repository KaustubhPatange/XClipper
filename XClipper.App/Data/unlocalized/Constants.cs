using System;
using System.IO;
using System.Windows.Forms;

namespace Components
{
    public static class Constants
    {
       
        public static string BaseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        public static string RoamingDirectory = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        public static string LicenseFilePath = Path.Combine(RoamingDirectory, "xclipper.lic");
        public static string BackupFolder = Path.Combine(BaseDirectory, "Backup");
        public static string DatabasePath = DatabasePath = Path.Combine(BaseDirectory, "data.db");

        #region Some Filter Constants

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
    }
}
