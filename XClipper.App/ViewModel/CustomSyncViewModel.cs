using static Components.DefaultSettings;
using static Components.Core;
using static Components.TranslationHelper;
using static Components.Constants;
using System.Windows.Input;
using GalaSoft.MvvmLight.Command;
using System;
using System.Windows;
using System.IO;

namespace Components
{
    public class CustomSyncViewModel : BaseViewModel
    {
        public CustomSyncViewModel()
        {
            if (FirebaseEndpoint != FIREBASE_PATH) FBE = FirebaseEndpoint;
            if (FirebaseSecret != FIREBASE_SECRET) FBS = FirebaseSecret;
            if (FirebaseAppId != FIREBASE_APP_ID) FBAI = FirebaseAppId;
            if (FirebaseApiKey != FIREBASE_API_KEY) FBAK = FirebaseApiKey;

            SaveCommand = new RelayCommand(SaveButtonClicked);
            ResetCommand = new RelayCommand(ResetButtonClicked);
        }

        #region Actual Bindings

        public ICommand SaveCommand { get; set; }
        public ICommand ResetCommand { get; set; }
        public string FBE { get; set; }
        public string FBS { get; set; }
        public string FBAK { get; set; }
        public string FBAI { get; set; }

        #endregion

        #region Methods

        private void SaveButtonClicked()
        {
            if (string.IsNullOrWhiteSpace(FBE) && string.IsNullOrWhiteSpace(FBS) && string.IsNullOrWhiteSpace(FBAK) &&
                string.IsNullOrWhiteSpace(FBAI))
            {
                MessageBox.Show(Translation.MSG_FIELD_EMPTY, Translation.MSG_ERR, MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }

            FBE = FirebaseEndpoint;
            FBS = FirebaseSecret;
            FBAI = FirebaseAppId;
            FBAK = FirebaseApiKey;

            WriteFirebaseSetting();

            MessageBox.Show(Translation.MSG_CONFIG_SAVE, Translation.MSG_INFO, MessageBoxButton.OK, MessageBoxImage.Information);
        }

        private void ResetButtonClicked()
        {
            if (File.Exists(CustomFirebasePath))
            {
                var result = MessageBox.Show(Translation.MSG_CONFIG_RESET, Translation.MSG_WARNING, MessageBoxButton.YesNo, MessageBoxImage.Warning);

                if (result == MessageBoxResult.Yes)
                {
                    FirebaseEndpoint = FIREBASE_PATH;
                    FirebaseSecret = FIREBASE_SECRET;
                    FirebaseAppId = FIREBASE_APP_ID;
                    FirebaseApiKey = FIREBASE_API_KEY;

                    FBE = FBS = FBAI = FBAK = string.Empty;

                    File.Delete(CustomFirebasePath);
                    MessageBox.Show(Translation.MSG_CONFIG_RESET_SUCCESS, Translation.MSG_INFO, MessageBoxButton.OK, MessageBoxImage.Information);
                }
            }
        }

        #endregion
    }
}
