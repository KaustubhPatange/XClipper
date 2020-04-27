using GalaSoft.MvvmLight.CommandWpf;
using System.Windows.Input;
using static Components.MainHelper;
using static Components.DefaultSettings;
using System.Windows;
using static Components.App;
using System.IO;
using static Components.Constants;
using System;
using Components.viewModels;

namespace Components
{
    public class SettingViewModel : BaseViewModel
    {

        #region Constructor

        private bool previousSecureDBValue;
        public SettingViewModel()
        {
            KeyDownCommand = new RelayCommand<KeyEventArgs>(OnKeyDown, null);
            SaveCommand = new RelayCommand(SaveButtonClicked);
            ResetCommand = new RelayCommand(ResetButtonClicked);

            previousSecureDBValue = IsSecureDB;
        }

        #endregion

        #region Actual Settings

        private bool is_secure_db { get; set; } = IsSecureDB;

        // For Start application on system startup.
        public ICommand SaveCommand { get; set; }
        public ICommand ResetCommand { get; set; }
        public RelayCommand<KeyEventArgs> KeyDownCommand { get; set; }
        public bool SASS { get; set; } = StartOnSystemStartup;
        public bool PNS { get; set; } = PlayNotifySound;
        public XClipperStore WTS { get; set; } = WhatToStore;
        public XClipperLocation ADL { get; set; } = AppDisplayLocation;
        public bool KEY_IC { get; set; } = IsCtrl;
        public bool KEY_IS { get; set; } = IsShift;
        public bool KEY_IA { get; set; } = IsAlt;
        public int TCL { get; set; } = TotalClipLength;
        public string KEY_HK { get; set; } = HotKey;
        public string CAL { get; set; } = CurrentAppLanguage;
        public bool ISDB
        {
            get { return is_secure_db; }
            set
            {
                if (value == true != previousSecureDBValue)
                {
                    var result = MessageBox.Show(rm.GetString("msg_delete_db"), rm.GetString("msg_warning"), MessageBoxButton.YesNo, MessageBoxImage.Warning);
                    if (result == MessageBoxResult.Yes)
                    {
                        is_secure_db = true;
                        return;
                    }
                }
                is_secure_db = false;
            }
        }

        #endregion

        #region Method Events

        /** This event will be raised when Reset Button is Clicked */
        private void ResetButtonClicked()
        {
            SASS = StartOnSystemStartup = true;
            PNS = PlayNotifySound = true;
           // ISDB = IsSecureDB = true;
            WhatToStore = WTS = XClipperStore.All;
            AppDisplayLocation = ADL = XClipperLocation.BottomRight;
            IsCtrl = KEY_IC = true;
            IsAlt = KEY_IA = false;
            IsShift = KEY_IS = false;
            HotKey = KEY_HK = "Oem3";
            CurrentAppLanguage = CAL = "locales\\en.xaml";
            TotalClipLength = TCL = 20;
            SetAppStartupEntry();
            WriteSettings();
            MessageBox.Show(App.rm.GetString("settings_reset"));
        }

        /** This event will be raised when Save Button is Clicked */
        private void SaveButtonClicked()
        {
            StartOnSystemStartup = SASS;
            PlayNotifySound = PNS;
            IsSecureDB = ISDB;
            WhatToStore = WTS;
            AppDisplayLocation = ADL;
            IsCtrl = KEY_IC;
            IsShift = KEY_IS;
            IsAlt = KEY_IA;
            TotalClipLength = TCL;
            HotKey = KEY_HK;
            CurrentAppLanguage = CAL;
            SetAppStartupEntry();
            WriteSettings();

            ToggleSecureDatabase();

            MessageBox.Show(App.rm.GetString("settings_save"));
        }
        /** This will delete and create new secure database. */
        private void ToggleSecureDatabase()
        {
            if (previousSecureDBValue != is_secure_db) 
            {
                // Create backup folder if does not exist...
                if (!Directory.Exists(BackupFolder)) Directory.CreateDirectory(BackupFolder);

                // Close connection to database...
                AppSingleton.GetInstance.dataDB.Close();

                // Create a backup file...
                File.Move(DatabasePath, Path.Combine(BackupFolder, $"data-{DateTime.Now.ToFormattedDateTime(false)}.db"));

                // Instantiate the database...
                AppSingleton.GetInstance.Init();
            }
        }

        /** This event will observe Hot Key value. */
        private void OnKeyDown(KeyEventArgs args)
        {
            //if (args.IsRepeat)
            //    return;

            if (args.Key != Key.LeftCtrl && args.Key != Key.RightCtrl && args.Key != Key.LeftShift
                && args.Key != Key.RightShift && args.Key != Key.LeftAlt && args.Key != Key.RightAlt
                && args.Key != Key.System)
                KEY_HK = args.Key.ToString();
        }

        #endregion

    }
}
