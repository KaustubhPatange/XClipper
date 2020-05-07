using System.IO;
using static Components.LicenseHandler;
using static Components.Constants;
using System.Windows.Input;
using GalaSoft.MvvmLight.Command;
using static Components.DefaultSettings;
using System.Windows;
using System;
using static Components.App;
using static Components.TranslationHelper;
using static Components.MainHelper;

namespace Components
{
    public class BuyViewModel : BaseViewModel
    {
        #region Constructor

        public BuyViewModel()
        {
            ActivateCommand = new RelayCommand(VerficationMethod);

            KEY = File.Exists(LicenseFilePath) ? File.ReadAllText(LicenseFilePath) : null;
            IACT = IsActivated(KEY);
        }

        #endregion

        #region Actual Bindings

        public ICommand ActivateCommand { get; set; }
        public string UID { get; private set; } = UniqueID;
        public string KEY { get; set; }
        public bool IACT { get; set; }

        #endregion

        #region Method Events

        private void VerficationMethod()
        {
            IACT = IsActivated(KEY);
            if (IACT == true)
            {
                IsPurchaseDone = true;
                File.WriteAllText(LicenseFilePath, KEY);
                var dialog = MessageBox.Show(Translation.MSG_PREMIUM_SUCCESS, Translation.MSG_INFO);
                if (dialog == MessageBoxResult.OK)
                {
                    RestartApplication();
                }
            }
            else MessageBox.Show(Translation.MSG_PREMIUM_ERR, Translation.MSG_ERR, MessageBoxButton.OK, MessageBoxImage.Error);
        }

        #endregion
    }
}
