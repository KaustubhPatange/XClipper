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
using static Components.Core;
using System.Windows.Controls;

namespace Components
{
    public class SettingViewModel : BaseViewModel
    {

        #region Constructor

        private bool previousSecureDBValue;
        private string previousPassword;

        public SettingViewModel()
        {
            KeyDownCommand = new RelayCommand<KeyEventArgs>(OnKeyDown, null);
            SaveCommand = new RelayCommand(SaveButtonClicked);
            ResetCommand = new RelayCommand(ResetButtonClicked);

            previousSecureDBValue = IsSecureDB;
            previousPassword = CustomPassword;
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
        public bool UCP { get; set; } = UseCustomPassword;
        public string CP { get; set; } = CustomPassword;
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
                        is_secure_db = value;
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
            UCP = UseCustomPassword = false;
            CP = CustomPassword = CONNECTION_PASS.Decrypt();
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
            UseCustomPassword = UCP;
            IsShift = KEY_IS;
            IsAlt = KEY_IA;
            TotalClipLength = TCL;
            HotKey = KEY_HK;
            CurrentAppLanguage = CAL;
            SetAppStartupEntry();

            ToggleCustomPassword();

            ToggleSecureDatabase();

            WriteSettings();

            MessageBox.Show(App.rm.GetString("settings_save"));
        }

        /** This method will set which password to use for database encryption */
        private void ToggleCustomPassword()
        {
            if (UseCustomPassword)
            {
                CustomPassword = CP;
            }
            else
            {
                CustomPassword = CONNECTION_PASS.Decrypt();
            }
        }

        /** This will delete and create new secure database. */
        private void ToggleSecureDatabase()
        {
            if (previousSecureDBValue != is_secure_db)
            {
                RecreateDatabase();
            }
            else if (previousPassword != CP)
            {
                var result = MessageBox.Show(rm.GetString("msg_delete_db"), rm.GetString("msg_warning"), MessageBoxButton.YesNo, MessageBoxImage.Warning);
                if (result == MessageBoxResult.Yes)
                {
                    RecreateDatabase();
                }else
                {
                    // Restore the value
                    CP = CustomPassword = CONNECTION_PASS.Decrypt();
                }
            }
        }

        private void RecreateDatabase()
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
