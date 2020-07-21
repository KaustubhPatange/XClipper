using static Components.DefaultSettings;
using static Components.Core;
using static Components.TranslationHelper;
using static Components.Constants;
using static Components.LicenseHandler;
using System.Windows.Input;
using GalaSoft.MvvmLight.Command;
using System.Windows;
using System.IO;
using System.Threading.Tasks;
using Microsoft.Win32;
using System.Xml.Linq;
using Components.Controls.Dialog;
using System.Text;

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
        public string FMCI { get; set; }
        public string FDCI { get; set; }
        public string FDCS { get; set; }
        public string FBAK { get; set; }
        public string FBAI { get; set; }
        public string UID { get; set; } = UniqueID;
        public int DMI { get; set; } 
        public int DMIL { get; set; } 
        public int DMC { get; set; } 
        public bool IAN { get; set; }
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

                MsgBoxHelper.ShowInfo(Translation.SYNC_EXPORT_SUCCESS);
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
                    if (pass == password)
                    {
                        File.Copy(ofd.FileName, CustomFirebasePath, true);

                        LoadFirebaseSetting();

                        checkForReset();

                        LoadDefaultConfigurations();

                        // Remove existing firebase credentials
                        RemoveFirebaseCredentials();

                        FirebaseSingleton.GetInstance.InitConfig();

                        MsgBoxHelper.ShowInfo(Translation.SYNC_IMPORT_SUCCESS);
                    }else
                        MsgBoxHelper.ShowError(Translation.SYNC_IMPORT_ERR2);
                    return;
                }catch { }

                MsgBoxHelper.ShowError(Translation.SYNC_IMPORT_ERR);
            }
        }


        private void SaveButtonClicked()
        {
            if (string.IsNullOrWhiteSpace(FBE) || string.IsNullOrWhiteSpace(FBAK) || string.IsNullOrWhiteSpace(FBAI) ||
                (IAN == string.IsNullOrWhiteSpace(FMCI) == string.IsNullOrWhiteSpace(FDCI) == string.IsNullOrWhiteSpace(FDCS)))
            {
                MsgBoxHelper.ShowError(Translation.MSG_FIELD_EMPTY);
                return;
            }

            var firebaseData = new FirebaseData
            {
                Endpoint = FBE,
                AppId = FBAI,
                ApiKey = FBAK,
                isAuthNeeded = IAN,
                DesktopAuth = new OAuth
                {
                    ClientId = FDCI,
                    ClientSecret = FDCS
                },
                MobileAuth = new OAuth
                {
                    ClientId = FMCI
                }
            };

            DatabaseMaxItem = DMI;
            DatabaseMaxItemLength = DMIL;
            DatabaseMaxConnection = DMC;

            WriteFirebaseSetting();

            LoadDefaultConfigurations();

            checkForReset();

            // Remove existing firebase credentials
            RemoveFirebaseCredentials();

            // Initialize new firebase Config
            FirebaseSingleton.GetInstance.InitConfig(firebaseData);

            MsgBoxHelper.ShowInfo(Translation.MSG_CONFIG_SAVE);
        }

        private void ResetButtonClicked()
        {
            if (File.Exists(CustomFirebasePath))
            {
                var result = MessageBox.Show(Translation.MSG_CONFIG_RESET, Translation.MSG_WARNING, MessageBoxButton.YesNo, MessageBoxImage.Warning);

                if (result == MessageBoxResult.Yes)
                {
                    if (FirebaseConfigurations.Count <= 0) // Make sure we don't fall under this method.
                    {
                        MsgBoxHelper.ShowError(Translation.MSG_UNKNOWN_ERR);
                        return;
                    }

                    DMI = DatabaseMaxItem = FB_MAX_ITEM;
                    DMC = DatabaseMaxConnection = FB_MAX_CONNECTION;
                    DMIL = DatabaseMaxItemLength = FB_MAX_LENGTH;
                    UID = UniqueID = UNIQUE_ID;

                    FBE = FMCI = FDCI = FDCS = FBAI = FBAK = string.Empty;

                    IAN = false;

                    File.Delete(CustomFirebasePath);

                    // Remove existing firebase credentials
                    RemoveFirebaseCredentials();

                    // Initialize with default config
                    FirebaseSingleton.GetInstance.InitConfig(FirebaseConfigurations[0]);

                    checkForReset();

                    // This will automatically load the default database encrypt password.
                    LoadApplicationSetting();

                    MsgBoxHelper.ShowInfo(Translation.MSG_CONFIG_RESET_SUCCESS);
                }
            }
        }

        private void LoadDefaultConfigurations()
        {
            if (FirebaseCurrent.Endpoint != FIREBASE_PATH)
            {
                FBE = FirebaseCurrent.Endpoint;
                FBAI = FirebaseCurrent.AppId;
                FBAK = FirebaseCurrent.ApiKey;

                IAN = FirebaseCurrent.isAuthNeeded;
                if (IAN)
                {
                    FDCI = FirebaseCurrent.DesktopAuth.ClientId;
                    FDCS = FirebaseCurrent.DesktopAuth.ClientSecret;
                    FMCI = FirebaseCurrent.MobileAuth.ClientId;
                }
            }
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
