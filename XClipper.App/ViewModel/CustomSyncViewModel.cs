using static Components.DefaultSettings;
using static Components.Core;
using static Components.TranslationHelper;
using static Components.Constants;
using static Components.LicenseHandler;
using System.Windows.Input;
using GalaSoft.MvvmLight.Command;
using System;
using System.Windows;
using System.IO;
using System.Threading.Tasks;
using Microsoft.Win32;
using System.Xml.Linq;
using Components.Controls;
using Components.Controls.Dialog;

namespace Components
{
    public class CustomSyncViewModel : BaseViewModel
    {
        public CustomSyncViewModel()
        {
            LoadDefaultConfigurations();

            SaveCommand = new RelayCommand(SaveButtonClicked);
            ResetCommand = new RelayCommand(ResetButtonClicked);
            ImportCommand = new RelayCommand(ImportButtonClicked);
            ExportCommand = new RelayCommand(ExportButtonClicked);

            checkForReset();
        }


        #region Actual Bindings

        public ICommand SaveCommand { get; set; }
        public ICommand ResetCommand { get; set; }
        public ICommand ImportCommand { get; set; }
        public ICommand ExportCommand { get; set; }
        public string FBE { get; set; }
        public string FBS { get; set; }
        public string FBAK { get; set; }
        public string FBAI { get; set; }
        public string UID { get; set; } = UniqueID;
        public int DMI { get; set; } 
        public int DMIL { get; set; } 
        public int DMC { get; set; } 
        public bool ResetEnabled { get; set; } = false;

        #endregion

        #region Methods
        private void ExportButtonClicked()
        {
            var sfd = new SaveFileDialog();
            sfd.Title = Translation.SYNC_IMPORT;
            sfd.FileName = Path.GetFileName(CustomFirebasePath);
            sfd.Filter = ".xml|*.xml";
            if (sfd.ShowDialog() == true)
            {
                File.Copy(CustomFirebasePath, sfd.FileName, true);

                MessageBox.Show(Translation.SYNC_EXPORT_SUCCESS, Translation.MSG_INFO, MessageBoxButton.OK, MessageBoxImage.Information);
            }
        }

        /// <summary>
        /// Import button will ask the password of the end-to-end encryption to import the config.
        /// </summary>
        private void ImportButtonClicked()
        {
            var ofd = new OpenFileDialog();
            ofd.Title = Translation.SYNC_IMPORT;
            ofd.Filter = ".xml|*.xml";
            if (ofd.ShowDialog() == true)
            {
                try
                {
                    var doc = XDocument.Load(ofd.FileName).Element(SETTINGS);
                    var password = doc.Element(nameof(DatabaseEncryptPassword)).Value.Decrypt();
                    var pass = new InputDialog.Builder()
                        .SetTitle(Translation.MSG_PASSWORD)
                        .SetMessage(Translation.SYNC_IMPORT_MSG)
                        .SetTopMost(true)
                        .Show();
                  //  var pass = Microsoft.VisualBasic.Interaction.InputBox(Translation.SYNC_IMPORT_MSG, Translation.MSG_PASSWORD, string.Empty);
                    if (pass == password)
                    {
                        File.Copy(ofd.FileName, CustomFirebasePath, true);

                        LoadFirebaseSetting();

                        checkForReset();

                        LoadDefaultConfigurations();

                        FirebaseSingleton.GetInstance.InitConfig();

                        MessageBox.Show(Translation.SYNC_IMPORT_SUCCESS, Translation.MSG_INFO, MessageBoxButton.OK, MessageBoxImage.Information);
                    }else
                        MessageBox.Show(Translation.SYNC_IMPORT_ERR2, Translation.MSG_ERR, MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }catch { }

                MessageBox.Show(Translation.SYNC_IMPORT_ERR, Translation.MSG_ERR, MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }


        private void SaveButtonClicked()
        {
            if (string.IsNullOrWhiteSpace(FBE) || string.IsNullOrWhiteSpace(FBS) || string.IsNullOrWhiteSpace(FBAK) ||
                string.IsNullOrWhiteSpace(FBAI))
            {
                MessageBox.Show(Translation.MSG_FIELD_EMPTY, Translation.MSG_ERR, MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }

            FirebaseEndpoint = FBE;
            FirebaseSecret = FBS;
            FirebaseAppId = FBAI;
            FirebaseApiKey = FBAK;
            DatabaseMaxItem = DMI;
            DatabaseMaxItemLength = DMIL;
            DatabaseMaxConnection = DMC;

            WriteFirebaseSetting();

            checkForReset();

            // Initialize new firebase Config
            FirebaseSingleton.GetInstance.InitConfig();

            Task.Run(async () => await FirebaseSingleton.GetInstance.SubmitConfigurations());

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
                    DMI = DatabaseMaxItem = FB_MAX_ITEM;
                    DMC = DatabaseMaxConnection = FB_MAX_CONNECTION;
                    DMIL = DatabaseMaxItemLength = FB_MAX_LENGTH;
                    UID = UniqueID = UNIQUE_ID;

                    FBE = FBS = FBAI = FBAK = string.Empty;

                    File.Delete(CustomFirebasePath);

                    FirebaseSingleton.GetInstance.InitConfig();

                    checkForReset();

                    // This will automatically load the default database encrypt password.
                    LoadApplicationSetting();

                    MessageBox.Show(Translation.MSG_CONFIG_RESET_SUCCESS, Translation.MSG_INFO, MessageBoxButton.OK, MessageBoxImage.Information);
                }
            }
        }

        private void LoadDefaultConfigurations()
        {
            if (FirebaseEndpoint != FIREBASE_PATH) FBE = FirebaseEndpoint;
            if (FirebaseSecret != FIREBASE_SECRET) FBS = FirebaseSecret;
            if (FirebaseAppId != FIREBASE_APP_ID) FBAI = FirebaseAppId;
            if (FirebaseApiKey != FIREBASE_API_KEY) FBAK = FirebaseApiKey;
            DMI = DatabaseMaxItem;
            DMC = DatabaseMaxConnection;
            DMIL = DatabaseMaxItemLength;
            UID = UniqueID;
        }
        private void checkForReset()
        {
            ResetEnabled = File.Exists(CustomFirebasePath);
        }

        #endregion
    }
}
