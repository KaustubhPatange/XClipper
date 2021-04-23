using System.IO;
using System.ComponentModel;
using static Components.Core;
using static Components.Constants;
using System.Xml.Linq;
using static Components.LicenseHandler;
using System.Collections.Generic;
using System;
using static Components.MainHelper;
using System.Windows.Forms;
using System.Windows.Navigation;
using System.Security.RightsManagement;
using System.Data.SqlTypes;
using System.Linq;

#nullable enable

namespace Components
{

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

    #region Setting Models

    public class Credential
    {
        /// <summary>
        /// This token will be used to make call to firebase database.
        /// </summary>
        public string AccessToken { get; set; } = string.Empty;
        /// <summary>
        /// Refresh token will be used to generate new access_Token
        /// </summary>
        public string RefreshToken { get; set; } = string.Empty;
        /// <summary>
        /// Use this to determine whether to generate a new access token or not.
        /// <code>
        /// if (DateTime.Now.ToFormattedDateTime(false) >= <see cref="FirebaseAccessToken"/>) {<br/>
        /// ......<br/>
        /// }<br/>
        /// </code>
        /// </summary>
        public long TokenRefreshTime { get; set; }
    }

    public class QRCodeData
    {
        public string UID { get; set; }
        public string EncryptedData { get; set; }
    }

    #endregion

    public static class DefaultSettings
    {

        #region Private Settings

        /// <summary>
        /// Will be used by <see cref="SettingWindow"/> to display QR code whenever
        /// <see cref="FirebaseSingleton.InitConfig(FirebaseData?)"/> is called (i.e Firebase initialized).
        /// </summary>
        private static QRCodeData? _qrData;

        /// <summary>
        /// Tells which license strategy has to be applied.
        /// </summary>
        private static LicenseType _licenseStrategy = LicenseType.Invalid;

        /// <summary>
        /// Max number of item to store in database.
        /// </summary>
        private static int _databaseMaxItem = FB_MAX_ITEM;

        /// <summary>
        /// Max number of item length to store in database.
        /// </summary>
        private static int _databaseMaxItemLength = FB_MAX_LENGTH;

        /// <summary>
        /// Determines the maximum number of device connections allowed.
        /// </summary>
        private static int _databaseMaxConnection = FB_MAX_CONNECTION;

        /// <summary>
        /// We will use this property to create a loading effect when
        /// app is checking for license.
        /// </summary>
        private static bool _IsCheckingForLicense = false;

        /// <summary>
        /// A string to hold if purchase complete.
        /// </summary>
        private static bool _IsPurchaseDone = false;
        
        /// <summary>
        /// Identifies whether device is connected to a network with internet access.
        /// </summary>
        private static bool _IsNetworkConnected = false;

        private static FirebaseData? _FirebaseCurrent = null;
        private static OAuth? _DesktopAuth = null;
        private static OAuth? _MobileAuth = null;
        #endregion

        #region Actual Settings

        /// <summary>
        /// This will set the start location of the app.
        /// </summary>
        public static XClipperLocation AppDisplayLocation { get; set; } = Settings.APP_DISPLAY_LOCATION;

        /// <summary>
        /// This tells what to store meaning only Text, Image, Files or All.
        /// </summary>
        public static XClipperStore WhatToStore { get; set; } = Settings.WHAT_TO_STORE;

        /// <summary>
        /// This tells the number of clip to store.
        /// </summary>
        public static int TotalClipLength { get; set; } = Settings.TOTAL_CLIP_LENGTH;

        /// <summary>
        /// This tells if Ctrl needs to be pressed in order to activate application.
        /// </summary>
        public static bool IsCtrl { get; set; } = Settings.IS_CTRL;

        /// <summary>
        /// This tells if Alt needs to be pressed in order to activate application.
        /// </summary>
        public static bool IsAlt { get; set; } = Settings.IS_ALT;

        /// <summary>
        /// This tells if Shift needs to be pressed in order to activate application.
        /// </summary>
        public static bool IsShift { get; set; } = Settings.IS_SHIFT;

        /// <summary>
        /// This will set Final Hot key of application.
        /// </summary>
        public static string HotKey { get; set; } = Settings.HOT_KEY;

        /// <summary>
        /// This will tell if application should start on System Startup.
        /// </summary>
        public static bool StartOnSystemStartup { get; set; } = Settings.START_ON_SYSTEM_STARTUP;

        /// <summary>
        /// This will check for application updates.
        /// </summary>
        public static bool CheckApplicationUpdates { get; set; } = Settings.CHECK_APP_UPDATES;

        /// <summary>
        /// This will tell if application should show notification when app is launched.
        /// </summary>
        public static bool DisplayStartNotification { get; set; } = Settings.DISPLAY_START_NOTIFICATION;

        /// <summary>
        /// This will sound when notification is shown.
        /// </summary>
        public static bool PlayNoticationSound { get; set; } = Settings.PLAY_NOTIFICATION_SOUND;

        /// <summary>
        /// Shows a notification if data change occurs from other devices.
        /// </summary>
        public static bool ShowDataChangeNotification { get; set; } = Settings.SHOW_DATA_CHANGE_NOTIFICATION;

        /// <summary>
        /// This will set the current language file to be use.
        /// </summary>
        public static string CurrentAppLanguage { get; set; } = Settings.CURRENT_LOCALE;

        /// <summary>
        /// A configuration to determine whether current database is password protected or not.
        /// </summary>
        public static bool IsSecureDB { get; set; } = Settings.IS_SECURE_DB;

        /// <summary>
        /// This string will hold the unique ID of this device.
        /// </summary>
        public static string UniqueID { get; set; } = UNIQUE_ID;

        /// <summary>
        /// <inheritdoc cref="_IsPurchaseDone"/>
        /// </summary>
        public static bool IsPurchaseDone
        {
            get { return _IsPurchaseDone; }
            set
            {
                if (value != _IsPurchaseDone)
                {
                    _IsPurchaseDone = value;
                    NotifyStaticPropertyChanged(nameof(IsPurchaseDone));

                    // Notify other property that depends on this value
                    NotifyStaticPropertyChanged(nameof(FirebaseMaxItem));
                    NotifyStaticPropertyChanged(nameof(FirebaseMaxItemLength));
                    NotifyStaticPropertyChanged(nameof(FirebaseMaxDevice));
                }
            }
        }

        /// <summary>
        /// <inheritdoc cref="_licenseStrategy"/>
        /// </summary>
        public static LicenseType LicenseStrategy
        {
            get { return _licenseStrategy; }
            set
            {
                if (value != _licenseStrategy)
                {
                    _licenseStrategy = value;
                    NotifyStaticPropertyChanged(nameof(LicenseStrategy));
                }
            }
        }

        /// <summary>
        /// Determines whether to use custom user input password or not.
        /// </summary>
        public static bool UseCustomPassword { get; set; } = Settings.USE_CUSTOM_PASSWORD;

        /// <summary>
        /// Stores the password in encrypted form.
        /// </summary>
        public static string CustomPassword { get; set; } = Settings.CUSTOM_PASSWORD;

        /// <summary>
        /// Max number of list to display in XClipper window.
        /// </summary>
        public static int TruncateList { get; set; } = Settings.TRUNCATE_LIST;

        /// <summary>
        /// <inheritdoc cref="_IsCheckingForLicense"/>
        /// </summary>
        public static bool IsCheckingForLicense
        {
            get { return _IsCheckingForLicense; }
            set
            {
                if (value != _IsCheckingForLicense)
                {
                    _IsCheckingForLicense = value;
                    NotifyStaticPropertyChanged(nameof(IsCheckingForLicense));
                }
            }
        }

        /// <summary>
        /// <inheritdoc cref="_databaseMaxItem"/>
        /// </summary>
        public static int DatabaseMaxItem
        {
            get { return _databaseMaxItem; }
            set
            {
                if (value != _databaseMaxItem)
                {
                    _databaseMaxItem = value;
                    NotifyStaticPropertyChanged(nameof(DatabaseMaxItem));
                }
            }
        }

        /// <summary>
        /// <inheritdoc cref="_databaseMaxItemLength"/>
        /// </summary>
        public static int DatabaseMaxItemLength
        {
            get { return _databaseMaxItemLength; }
            set
            {
                if (value != _databaseMaxItemLength)
                {
                    _databaseMaxItemLength = value;
                    NotifyStaticPropertyChanged(nameof(DatabaseMaxItemLength));
                }
            }
        }

        /// <summary>
        /// <inheritdoc cref="_databaseMaxConnection"/>
        /// </summary>
        public static int DatabaseMaxConnection
        {
            get { return _databaseMaxConnection; }
            set
            {
                if (value != _databaseMaxConnection)
                {
                    _databaseMaxConnection = value;
                    NotifyStaticPropertyChanged(nameof(DatabaseMaxConnection));
                }
            }
        }

        /// <summary>
        /// Max storage item that are allowed.
        /// </summary>
        public static int FirebaseMaxItem
        {
            get { return IsPurchaseDone ? SYNC_MAX_ITEM : SYNC_MIN_ITEM; }
        }

        /// <summary>
        /// Max storage for item length that are allowed.
        /// </summary>
        public static int FirebaseMaxItemLength
        {
            get { return IsPurchaseDone ? SYNC_MAX_LENGTH : SYNC_MIN_LENGTH; }
        }

        /// <summary>
        /// Max storage for devices that are allowed.
        /// </summary>
        public static int FirebaseMaxDevice
        {
            get { return IsPurchaseDone ? SYNC_MAX_CONNECTION : SYNC_MIN_CONNECTION; }
        }

        /// <summary> 
        /// <inheritdoc cref="_qrData"/> 
        /// </summary>
        public static QRCodeData? QRData
        {
            get { return _qrData; }
            set
            {
                if (value != _qrData)
                {
                    _qrData = value;
                    NotifyStaticPropertyChanged(nameof(QRData));
                }
            }
        }

        /// <summary>
        /// Password which will be used to decrypt item in database.
        /// </summary>
        public static string DatabaseEncryptPassword { get; set; } = Settings.DATABASE_ENCRYPT_PASSWORD;

        /// <summary>
        /// This stores all the credentials necessary for making connection with Firebase.
        /// </summary>
        public static Credential FirebaseCredential { get; set; } = new Credential();

        public static readonly List<IFirebaseDataListener> FirebaseDataListeners = new List<IFirebaseDataListener>();
        
        /// <summary>
        /// Stores the current configuration that is being used by Firebase Singleton and helper class.
        /// </summary>
        public static FirebaseData? FirebaseCurrent
        {
            get => _FirebaseCurrent;
            set
            {
                _FirebaseCurrent = value;
                UpdateFirebaseListeners();
            }
        }

        public static OAuth? DesktopAuth
        {
            get => _DesktopAuth;
            set
            {
                _DesktopAuth = value;
                UpdateFirebaseListeners();
            }
        }

        public static OAuth? MobileAuth
        {
            get => _MobileAuth;
            set
            {
                _MobileAuth = value;
                UpdateFirebaseListeners();
            }
        }

        /// <summary>
        /// When set to true it will allow syncing of local database with online database.<br/> A valid binding can be,<br/><br/> 
        /// 1. Data added locally then pushed to online database.<br/> 
        /// 2. Data removed locally and changes submitted to online database.<br/>
        /// 3. Data observation and device changes.<br/>
        /// 4. Data added to online database externally, respond to such changes locally.<br/>
        /// </summary>
        public static bool BindDatabase { get; set; } = Settings.BIND_DATABASE;

        /// <summary>
        /// When set to true, App will respond to delete request coming from database.
        /// </summary>
        public static bool BindDelete { get; set; } = Settings.BIND_DELETE;

        /// <summary>
        /// When set to true, App will respond to image related queries.
        /// </summary>
        public static bool BindImage { get; set; } = Settings.BIND_IMAGE;

        /// <summary>
        /// A setting that can use to decide whether to respond for quick paste.
        /// </summary>
        public static bool GlobalQuickPaste { get; set; } = true;

        /// <summary>
        /// This will let application exit when any crash occurs.
        /// </summary>
        public static bool ExitOnCrash { get; set; } = Settings.EXIT_ON_CRASH;

        /// <summary>
        /// If set to true XClipper will not show notification about any changes occurs to database.
        /// </summary>
        public static bool NoNotifyChanges { get; set; } = Settings.NO_NOTIFY_CHANGES;

        /// <summary>
        /// The application will use an experimental method of hot key detection goes by stream of key events<br/>
        /// for faster response. To opt out read <see href="https://kaustubhpatange.github.io/XClipper/docs/#/hidden">here</see>.
        /// </summary>
        public static bool UseExperimentalKeyCapture { get; set; } = Settings.USE_EXPERIMENTAL_KEY_CAPTURE;
        /// <summary>
        /// <inheritdoc cref="_IsNetworkConnected"/>
        /// </summary>
        public static bool IsNetworkConnected
        {
            get { return _IsNetworkConnected; }
            set
            {
                _IsNetworkConnected = value;
                NotifyStaticPropertyChanged(nameof(_IsNetworkConnected));
                NetworkChange?.Invoke();
            }
        }

        // Set of buffers that are used to store some external data on the go.
        public static Buffer CopyBuffer1 { get; set; } = Settings.CopyBuffer1;
        public static Buffer CopyBuffer2 { get; set; } = Settings.CopyBuffer2;

        #endregion

        /// <summary>
        /// Set of timestamps that will trigger certain notifications.
        /// </summary>

        public static class TimeStamps
        {
            public static string? EnableSync { get; set; } = string.Empty;
            public static bool ShownIntroduction { get; set; } = false;
            public static string? PurchaseInfo { get; set; } = string.Empty;
        }

        #region Notify Static PropertyChange

        public static event EventHandler<PropertyChangedEventArgs>? StaticPropertyChanged;
        internal static void NotifyStaticPropertyChanged(string propertyName)
        {
            StaticPropertyChanged?.Invoke(null, new PropertyChangedEventArgs(propertyName));
        }

        #endregion

        #region Methods

        private static void UpdateFirebaseListeners()
        {
            FirebaseDataListeners.ForEach(c => c.OnFirebaseDataChange());
        }

        #region Write

        public static void WriteSettings()
        {
            var document = new XDocument();

            var environment = new XElement(ENVIRONMENT);
            environment
                  .Add(
                     new XElement(nameof(ExitOnCrash), ExitOnCrash.ToString()),
                     new XElement(nameof(NoNotifyChanges), NoNotifyChanges.ToString()),
                     new XElement(nameof(UseExperimentalKeyCapture), UseExperimentalKeyCapture.ToString())
                     );
           
            var settings = new XElement(SETTINGS);
            settings
                .Add(
                    environment,
                    new XElement(nameof(AppDisplayLocation), AppDisplayLocation.ToString()),
                    new XElement(nameof(WhatToStore), WhatToStore.ToString()),
                    new XElement(nameof(TotalClipLength), TotalClipLength.ToString()),
                    new XElement(nameof(IsCtrl), IsCtrl.ToString()),
                    new XElement(nameof(IsAlt), IsAlt.ToString()),
                    new XElement(nameof(IsShift), IsShift.ToString()),
                    new XElement(nameof(HotKey), HotKey.ToString()),
                    new XElement(nameof(StartOnSystemStartup), StartOnSystemStartup.ToString()),
                    new XElement(nameof(CheckApplicationUpdates), CheckApplicationUpdates.ToString()),
                    new XElement(nameof(ShowDataChangeNotification), ShowDataChangeNotification.ToString()),
                    new XElement(nameof(DisplayStartNotification), DisplayStartNotification.ToString()),
                    new XElement(nameof(PlayNoticationSound), PlayNoticationSound.ToString()),
                    new XElement(nameof(IsSecureDB), IsSecureDB.ToString()),
                    new XElement(nameof(CurrentAppLanguage), CurrentAppLanguage.ToString()),
                    new XElement(nameof(CustomPassword), CustomPassword.Encrypt()),
                    new XElement(nameof(DatabaseEncryptPassword), DatabaseEncryptPassword.Encrypt()),
                    new XElement(nameof(UseCustomPassword), UseCustomPassword.ToString()),
                    new XElement(nameof(BindDatabase), BindDatabase.ToString()),
                    new XElement(nameof(BindDelete), BindDelete.ToString()),
                    new XElement(nameof(BindImage), BindImage.ToString())
                    );
            document.Add(settings);
            document.Save(SettingsPath);

            WriteBufferSetting();
        }

        /// <summary>
        /// Writes buffer settings to a file including their keymaps & last captured data.
        /// </summary>
        public static void WriteBufferSetting()
        {
            var document = new XDocument();
            var buffers = new XElement(BUFFERS);
            buffers.Add(Buffer.ToNode(CopyBuffer1));
            buffers.Add(Buffer.ToNode(CopyBuffer2));
            document.Add(buffers);
            document.Save(BufferFilePath);
        }

        /// <summary>
        /// This will write timestamps setting to a file.
        /// </summary>
        public static void WriteTimeStampsSetting()
        {
            var document = new XDocument();
            var timestamps = new XElement(TIMESTAMPS);
            timestamps
                 .Add(
                    new XElement(nameof(TimeStamps.EnableSync), TimeStamps.EnableSync),
                    new XElement(nameof(TimeStamps.ShownIntroduction), TimeStamps.ShownIntroduction),
                    new XElement(nameof(TimeStamps.PurchaseInfo), TimeStamps.PurchaseInfo)
                    );
            document.Add(timestamps);
            document.Save(TimeStampsPath);
        }

        /// <summary>
        /// This will write firebase setting to a file.
        /// </summary>
        public static void WriteFirebaseSetting()
        {
            if (FirebaseCurrent == null) return;
            var firebaseDoc = new XDocument();
            var config = new XElement(SETTINGS);
            config
                 .Add(
                     new XElement(nameof(UniqueID), UniqueID.Encrypt()),
                     new XElement(nameof(DatabaseEncryptPassword), DatabaseEncryptPassword.Encrypt()),
                     new XElement(nameof(DatabaseMaxItem), DatabaseMaxItem.ToString()),
                     new XElement(nameof(DatabaseMaxItemLength), DatabaseMaxItemLength.ToString()),
                     new XElement(nameof(DatabaseMaxConnection), DatabaseMaxConnection.ToString())
                 );
            var firebaseConfig = new XElement(FIREBASE);
            firebaseConfig
                .Add(
                    new XElement(nameof(FirebaseCurrent.Endpoint), FirebaseCurrent.Endpoint.ToString()),
                    new XElement(nameof(FirebaseCurrent.ApiKey), FirebaseCurrent.ApiKey.ToString()),
                    new XElement(nameof(FirebaseCurrent.AppId), FirebaseCurrent.AppId.ToString()),
                    new XElement(nameof(FirebaseCurrent.IsAuthNeeded), FirebaseCurrent.IsAuthNeeded.ToString()),
                    new XElement(nameof(FirebaseCurrent.IsEncrypted), FirebaseCurrent.IsEncrypted.ToString())
                );

            var desktopOAuth = new XElement(DESKTOP_AUTH);
            desktopOAuth
                .Add(
                    new XElement(nameof(DesktopAuth.ClientId), DesktopAuth.ClientId.ToString()),
                    new XElement(nameof(DesktopAuth.ClientSecret), DesktopAuth.ClientSecret?.ToString())
                );
            var mobileOAuth = new XElement(MOBILE_AUTH); // Mobile auth doesn't need ClientSecret
            mobileOAuth
                .Add(
                    new XElement(nameof(MobileAuth.ClientId), MobileAuth.ClientId.ToString())
                );

            firebaseConfig.Add(mobileOAuth);
            firebaseConfig.Add(desktopOAuth);

            config.Add(firebaseConfig);

            firebaseDoc.Add(config);
            firebaseDoc.Save(CustomFirebasePath);
        }

        /// <summary>
        /// Write Firebase Access, Refresh Token to a file.
        /// </summary>
        public static void WriteFirebaseCredentialSetting()
        {
            if (!IsValidCredential()) return;

            var doc = new XDocument();
            var config = new XElement(CREDENTIAL);
            config
                .Add(
                    new XElement(nameof(FirebaseCredential.AccessToken), FirebaseCredential.AccessToken.ToString()),
                    new XElement(nameof(FirebaseCredential.RefreshToken), FirebaseCredential.RefreshToken.ToString()),
                    new XElement(nameof(FirebaseCredential.TokenRefreshTime), FirebaseCredential.TokenRefreshTime.ToString())
                 );
            doc.Add(config);
            doc.Save(FirebaseCredentialPath);
        }

        /// <summary>
        /// Returns True if Firebase Access or Refresh Token are not null and empty.
        /// </summary>
        public static bool IsValidCredential()
        {
            return !(string.IsNullOrWhiteSpace(FirebaseCredential.AccessToken) ||
               string.IsNullOrWhiteSpace(FirebaseCredential.RefreshToken) ||
               FirebaseCredential.TokenRefreshTime == 0);
        }

        #endregion

        #region Read
        public static void LoadSettings()
        {
            if (!Directory.Exists(ApplicationDirectory))
            {
                Directory.CreateDirectory(ApplicationDirectory);
                Directory.CreateDirectory(ApplicationTempDirectory);
            }

            LoadApplicationSetting();
            LoadBufferSetting();
            LoadTimeStampsSetting();
            LoadFirebaseSetting();
            LoadFirebaseCredentials();
            Interpreter.LoadScripts();
        }

        /// <summary>
        /// This will load default application setting
        /// </summary>
        public static void LoadApplicationSetting()
        {
            if (!File.Exists(SettingsPath)) // If settings does not exist, write defaults
            {
                WriteSettings();
                return;
            }  
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
            CheckApplicationUpdates = settings.Element(nameof(CheckApplicationUpdates)).Value.ToBool();
            ShowDataChangeNotification = settings.Element(nameof(ShowDataChangeNotification)).Value.ToBool();
            DisplayStartNotification = settings.Element(nameof(DisplayStartNotification)).Value.ToBool();
            PlayNoticationSound = settings.Element(nameof(PlayNoticationSound)).Value.ToBool();
            BindDatabase = settings.Element(nameof(BindDatabase)).Value.ToBool();
            BindDelete = settings.Element(nameof(BindDelete)).Value.ToBool();
            BindImage = settings.Element(nameof(BindImage)).Value.ToBool();

            var environment = settings.Element(ENVIRONMENT);
            ExitOnCrash = environment.Element(nameof(ExitOnCrash)).Value.ToBool();
            NoNotifyChanges = environment.Element(nameof(NoNotifyChanges)).Value.ToBool();
            UseExperimentalKeyCapture = environment.Element(nameof(UseExperimentalKeyCapture)).Value.ToBool();
        }

        /// <summary>
        /// Loads the buffer setting if exist.
        /// </summary>
        public static void LoadBufferSetting()
        {
            if (!File.Exists(BufferFilePath)) return;

            var elements = XDocument.Load(BufferFilePath).Element(BUFFERS).Elements().ToList();
            CopyBuffer1 = Buffer.FromNode(elements[0]);
            CopyBuffer2 = Buffer.FromNode(elements[1]);
        }

        /// <summary>
        /// This will load timestamps setting from file if exist.
        /// </summary>
        public static void LoadTimeStampsSetting()
        {
            if (!File.Exists(TimeStampsPath)) // If settings does not exist, write defaults
            {
                WriteTimeStampsSetting();
                return;
            }
            var timestamps = XDocument.Load(TimeStampsPath).Element(TIMESTAMPS);
            TimeStamps.EnableSync = timestamps.Element(nameof(TimeStamps.EnableSync)).Value;
            TimeStamps.ShownIntroduction = timestamps.Element(nameof(TimeStamps.ShownIntroduction)).Value.ToBool();
            TimeStamps.PurchaseInfo = timestamps.Element(nameof(TimeStamps.PurchaseInfo)).Value;
        }

        /// <summary>
        /// This will load firebase configuration setting from the file if exist.
        /// </summary>
        public static void LoadFirebaseSetting()
        {
            // Loading custom firebase setting...
            if (File.Exists(CustomFirebasePath))
            {
                var doc = XDocument.Load(CustomFirebasePath);
                var settingDoc = doc.Element(SETTINGS);
                var firebaseDoc = settingDoc.Element(FIREBASE);
                var desktopDoc = firebaseDoc.Element(DESKTOP_AUTH);
                var mobileDoc = firebaseDoc.Element(MOBILE_AUTH);

                UniqueID = settingDoc.Element(nameof(UniqueID)).Value.Decrypt();
                DatabaseEncryptPassword = settingDoc.Element(nameof(DatabaseEncryptPassword)).Value.Decrypt();
                DatabaseMaxConnection = settingDoc.Element(nameof(DatabaseMaxConnection)).Value.ToInt();
                DatabaseMaxItemLength = settingDoc.Element(nameof(DatabaseMaxItemLength)).Value.ToInt();
                DatabaseMaxItem = settingDoc.Element(nameof(DatabaseMaxItem)).Value.ToInt();

                if (FirebaseCurrent == null)
                    FirebaseCurrent = new FirebaseData();

                FirebaseCurrent.Endpoint = firebaseDoc.Element(nameof(FirebaseCurrent.Endpoint)).Value;
                FirebaseCurrent.ApiKey = firebaseDoc.Element(nameof(FirebaseCurrent.ApiKey)).Value;
                FirebaseCurrent.AppId = firebaseDoc.Element(nameof(FirebaseCurrent.AppId)).Value;
                FirebaseCurrent.IsAuthNeeded = firebaseDoc.Element(nameof(FirebaseCurrent.IsAuthNeeded)).Value.ToBool();
                FirebaseCurrent.IsEncrypted = firebaseDoc.Element(nameof(FirebaseCurrent.IsEncrypted)).Value.ToBool();

                var DesktopAuth = new OAuth();
                DesktopAuth.ClientId = desktopDoc.Element(nameof(DesktopAuth.ClientId)).Value;
                DesktopAuth.ClientSecret = desktopDoc.Element(nameof(DesktopAuth.ClientSecret)).Value;

                var MobileAuth = new OAuth();
                MobileAuth.ClientId = mobileDoc.Element(nameof(MobileAuth.ClientId)).Value;

                DefaultSettings.DesktopAuth = DesktopAuth;
                DefaultSettings.MobileAuth = MobileAuth;
            }
        }

        /// <summary>
        /// This will be use to validate firebase settings to protect it from any external change
        /// </summary>
        public static void ValidateFirebaseSetting()
        {
            if (FirebaseCurrent == null) return;
            bool write = false;
            if (DatabaseMaxConnection > FirebaseMaxDevice)
            {
                DatabaseMaxConnection = FirebaseMaxDevice;
                write |= true;
            }
            if (DatabaseMaxItem > FirebaseMaxItem)
            {
                DatabaseMaxItem = FirebaseMaxItem;
                write |= true;
            }
            if (DatabaseMaxItemLength > FirebaseMaxItemLength)
            {
                DatabaseMaxItemLength = FirebaseMaxItemLength;
                write |= true;
            }
            if (write) WriteFirebaseSetting();
        }

        /// <summary>
        /// This will load Firebase credentials that to be used by <see cref="FirebaseSingleton"/>
        /// </summary>
        public static void LoadFirebaseCredentials()
        {
            // Load Firebase Credentials
            if (File.Exists(FirebaseCredentialPath))
            {
                var firebaseDoc = XDocument.Load(FirebaseCredentialPath).Element(CREDENTIAL);

                FirebaseCredential.AccessToken = firebaseDoc.Element(nameof(FirebaseCredential.AccessToken)).Value;
                FirebaseCredential.RefreshToken = firebaseDoc.Element(nameof(FirebaseCredential.RefreshToken)).Value;
                FirebaseCredential.TokenRefreshTime = firebaseDoc.Element(nameof(FirebaseCredential.TokenRefreshTime)).Value.ToLong();
            }
        }

        #endregion

        #region Delete

        /// <summary>
        /// This will remove current credentials so that we can re-auth.
        /// </summary>
        public static void RemoveFirebaseCredentials()
        {
            if (File.Exists(FirebaseCredentialPath)) File.Delete(FirebaseCredentialPath);
            FirebaseCredential = new Credential();
        }

        #endregion

        #endregion

        #region Event handlers

        public delegate void NetworkChangeEventHandler();

        private static event NetworkChangeEventHandler? NetworkChange;
        public static void AddNetworkChangeEvent(NetworkChangeEventHandler e) => NetworkChange += e;
        public static void RemoveNetworkChangeEvent(NetworkChangeEventHandler e) => NetworkChange -= e;

        #endregion
    }
}
