using GalaSoft.MvvmLight.CommandWpf;
using PropertyChanged;
using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Windows.Input;
using static Components.MainHelper;
using MsgBox = System.Windows.Forms.MessageBox;
using static Components.DefaultSettings;

namespace Components
{
    [ImplementPropertyChanged]
    public class SettingViewModel : INotifyPropertyChanged
    {
        public SettingViewModel()
        {
            KeyDownCommand = new RelayCommand<KeyEventArgs>(OnKeyDown, null);
            SaveCommand = new RelayCommand(SaveButtonClicked);
            ResetCommand = new RelayCommand(ResetButtonClicked);

        }

        public event PropertyChangedEventHandler PropertyChanged = (sender, events) =>
        {

        };

        #region Actual Settings

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

        #endregion

        #region Method Events

        /** This event will be raised when Reset Button is Clicked */
        private void ResetButtonClicked()
        {
            SASS = StartOnSystemStartup = true;
            PNS = PlayNotifySound = true;
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
            MsgBox.Show(App.rm.GetString("settings_reset"));
        }

        /** This event will be raised when Save Button is Clicked */
        private void SaveButtonClicked()
        {
            StartOnSystemStartup = SASS;
            PlayNotifySound = PNS;
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
            MsgBox.Show(App.rm.GetString("settings_save"));
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
