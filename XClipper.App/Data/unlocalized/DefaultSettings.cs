using System.IO;
using System.ComponentModel;
using static Components.Core;
using static Components.Constants;
using System.Xml.Linq;

namespace Components
{
    public static class DefaultSettings
    {

        #region Variable Definitions

        private const string SETTINGS = "Settings";

        #endregion


        #region Actual Settings

        /// <summary>
        /// This will set the start location of the app.
        /// </summary>
        public static XClipperLocation AppDisplayLocation { get; set; } = XClipperLocation.BottomRight;

        /// <summary>
        /// This tells what to store meaning only Text, Image, Files or All.
        /// </summary>
        public static XClipperStore WhatToStore { get; set; } = XClipperStore.All;

        /// <summary>
        /// This tells the number of clip to store.
        /// </summary>
        public static int TotalClipLength { get; set; } = 20;

        /// <summary>
        /// This tells if Ctrl needs to be pressed in order to activate application.
        /// </summary>
        public static bool IsCtrl { get; set; } = true;

        /// <summary>
        /// This tells if Alt needs to be pressed in order to activate application.
        /// </summary>
        public static bool IsAlt { get; set; } = false;

        /// <summary>
        /// This tells if Shift needs to be pressed in order to activate application.
        /// </summary>
        public static bool IsShift { get; set; } = false;

        /// <summary>
        /// This will set Final Hot key of application.
        /// </summary>
        public static string HotKey { get; set; } = "Oem3";

        /// <summary>
        /// This will tell if application should start on System Startup.
        /// </summary>
        public static bool StartOnSystemStartup { get; set; } = true;

        /// <summary>
        /// This will tell if application should play sound when started.
        /// </summary>
        public static bool PlayNotifySound { get; set; } = true;

        /// <summary>
        /// This will set the current language file to be use.
        /// </summary>
        public static string CurrentAppLanguage { get; set; } = "locales\\en.xaml";

        /// <summary>
        /// A configuration to password protect database.
        /// </summary>
        public static bool IsSecureDB { get; set; } = false;

        /// <summary>
        /// A string to hold if purchase complete.
        /// </summary>
        public static bool IsPurchaseDone { get; set; }

        /// <summary>
        /// This will hold which license strategy has to be applied.
        /// </summary>
        public static LicenseType LicenseStrategy { get; set; }

        /// <summary>
        /// Determines whether to use custom user input password or not.
        /// </summary>
        public static bool UseCustomPassword { get; set; } = false;

        /// <summary>
        /// Stores the password in encrypted form.
        /// </summary>
        public static string CustomPassword { get; set; } = CONNECTION_PASS.Decrypt();

        /// <summary>
        /// Max number of list to display in XClipper window.
        /// </summary>
        public static int TruncateList { get; set; } = 20;

        /// <summary>
        /// Max number of item to store in database.
        /// </summary>
        public static int DatabaseMaxItem { get; set; } = 5;

        /// <summary>
        /// Max number of item length to store in database.
        /// </summary>
        public static int DatabaseMaxItemLength { get; set; } = 1000;

        /// <summary>
        /// Determines the maximum number of device connections allowed.
        /// </summary>
        public static int DatabaseMaxConnection { get; set; } = 1;

        /// <summary>
        /// Password which will be used to decrypt item in database.
        /// </summary>
        public static string DatabaseEncryptPassword { get; set; } = FB_DEFAULT_PASS.Decrypt();

        /// <summary>
        /// The endpoint Firebase URL https://example-pathio.com
        /// </summary>
        public static string FirebaseEndpoint { get; set; } = FIREBASE_PATH;

        /// <summary>
        /// The secret Auth key which will be used to make connection to database.
        /// </summary>
        public static string FirebaseSecret { get; set; } = FIREBASE_SECRET;

        /// <summary>
        /// The app Id of mobile package name that is needed to make connection for the mobile App.
        /// </summary>
        public static string FirebaseAppId { get; set; } = FIREBASE_APP_ID;

        /// <summary>
        /// A web API key for Firebase database.
        /// </summary>
        public static string FirebaseApiKey { get; set; } = FIREBASE_API_KEY;

        /// <summary>
        /// When set to true it will allow syncing of local database with online database.<br/> A valid binding can be,<br/><br/> 
        /// 1. Data added locally then pushed to online database.<br/> 
        /// 2. Data removed locally and changes submitted to online database.<br/>
        /// 3. Data observation and device changes.<br/>
        /// </summary>
        public static bool BindDatabase { get; set; } = false;
        //    todo: 3. Data added to online database externally, respond to such changes locally.
        #endregion


        #region Methods

        public static void WriteSettings()
        {
            var document = new XDocument();
            var settings = new XElement(SETTINGS);
            settings
                .Add(
                    new XElement(nameof(AppDisplayLocation), AppDisplayLocation.ToString()),
                    new XElement(nameof(WhatToStore), WhatToStore.ToString()),
                    new XElement(nameof(TotalClipLength), TotalClipLength.ToString()),
                    new XElement(nameof(IsCtrl), IsCtrl.ToString()),
                    new XElement(nameof(IsAlt), IsAlt.ToString()),
                    new XElement(nameof(IsShift), IsShift.ToString()),
                    new XElement(nameof(HotKey), HotKey.ToString()),
                    new XElement(nameof(StartOnSystemStartup), StartOnSystemStartup.ToString()),
                    new XElement(nameof(PlayNotifySound), PlayNotifySound.ToString()),
                    new XElement(nameof(IsSecureDB), IsSecureDB.ToString()),
                    new XElement(nameof(CurrentAppLanguage), CurrentAppLanguage.ToString()),
                    new XElement(nameof(CustomPassword), CustomPassword.Encrypt()),
                    new XElement(nameof(DatabaseEncryptPassword), DatabaseEncryptPassword.Encrypt()),
                    new XElement(nameof(UseCustomPassword), UseCustomPassword.ToString()),
                    new XElement(nameof(BindDatabase), BindDatabase.ToString())
                    );
            document.Add(settings);
            document.Save(SettingsPath);
        }

        public static void LoadSettings()
        {
            if (!Directory.Exists(ApplicationDirectory)) Directory.CreateDirectory(ApplicationDirectory);
            if (!File.Exists(SettingsPath)) return;  // Return if settings does not exist, so it will use defaults

            var settings = XDocument.Load(SettingsPath).Element(SETTINGS);

            AppDisplayLocation = settings.Element(nameof(AppDisplayLocation)).Value.ToEnum<XClipperLocation>();
            WhatToStore = settings.Element(nameof(WhatToStore)).Value.ToEnum<XClipperStore>();
            TotalClipLength = settings.Element(nameof(TotalClipLength)).Value.ToInt();
            IsCtrl = settings.Element(nameof(IsCtrl)).Value.ToBool();
            IsAlt = settings.Element(nameof(IsAlt)).Value.ToBool();
            IsShift = settings.Element(nameof(IsShift)).Value.ToBool();
            HotKey = settings.Element(nameof(HotKey)).Value;
            CustomPassword = settings.Element(nameof(CustomPassword)).Value.Decrypt();
            DatabaseEncryptPassword = settings.Element(nameof(DatabaseEncryptPassword)).Value.Decrypt();
            IsSecureDB = settings.Element(nameof(IsSecureDB)).Value.ToBool();
            UseCustomPassword = settings.Element(nameof(UseCustomPassword)).Value.ToBool();
            CurrentAppLanguage = settings.Element(nameof(CurrentAppLanguage)).Value;
            StartOnSystemStartup = settings.Element(nameof(StartOnSystemStartup)).Value.ToBool();
            PlayNotifySound = settings.Element(nameof(PlayNotifySound)).Value.ToBool();
            BindDatabase = settings.Element(nameof(BindDatabase)).Value.ToBool();

            // Loading custom firebase setting...
            if (File.Exists(CustomFirebasePath))
            {
                var firebaseDoc = XDocument.Load(CustomFirebasePath).Element(SETTINGS);

                FirebaseEndpoint = firebaseDoc.Element(nameof(FirebaseEndpoint)).Value;
                FirebaseSecret = firebaseDoc.Element(nameof(FirebaseSecret)).Value;
                FirebaseAppId = firebaseDoc.Element(nameof(FirebaseAppId)).Value;
                FirebaseApiKey = firebaseDoc.Element(nameof(FirebaseApiKey)).Value;
            }
        }

        /// <summary>
        /// This will write firebase setting to a file.
        /// </summary>
        public static void WriteFirebaseSetting()
        {
            if (FirebaseEndpoint == FIREBASE_PATH || FirebaseSecret == FIREBASE_SECRET) return;
            var firebaseDoc = new XDocument();
            var config = new XElement(SETTINGS);
            config
                 .Add(
                     new XElement(nameof(FirebaseEndpoint), FirebaseEndpoint.ToString()),
                     new XElement(nameof(FirebaseSecret), FirebaseSecret.ToString()),
                     new XElement(nameof(FirebaseAppId), FirebaseAppId.ToString()),
                     new XElement(nameof(FirebaseApiKey), FirebaseApiKey.ToString())
                 );
            firebaseDoc.Add(config);
            firebaseDoc.Save(CustomFirebasePath);
        }

        #endregion

    }

    #region Setting Enums

    public enum XClipperStore
    {
        [Description("Text Only")]
        Text = 0,
        [Description("Image Only")]
        Image = 1,
        [Description("Files Only")]
        Files = 2,
        [Description("Everything")]
        All = 3
    }

    public enum XClipperLocation
    {
        [Description("Bottom Right")]
        BottomRight = 0,
        [Description("Bottom Left")]
        BottomLeft = 1,
        [Description("Top Right")]
        TopRight = 2,
        [Description("Top Left")]
        TopLeft = 3,
        [Description("Center")]
        Center = 4
    }

    #endregion
}
