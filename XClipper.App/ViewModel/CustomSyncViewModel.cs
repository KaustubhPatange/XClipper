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
using Components.UI;

namespace Components
{
    public class CustomSyncViewModel : BaseViewModel
    {
        private ICustomSyncBinder binder;
        public CustomSyncViewModel(ICustomSyncBinder binder)
        {
            this.binder = binder;

            LoadDefaultConfigurations();

            SaveCommand = new RelayCommand(SaveButtonClicked);
            ImportCommand = new RelayCommand(ImportButtonClicked);
            ExportCommand = new RelayCommand(ExportButtonClicked);

            CheckExportEnabled();
        }

        #region Actual Bindings

        public ICommand SaveCommand { get; set; }
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
        public bool EE { get; set; } // Export enabled

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

                        LoadDefaultConfigurations();

                        // Remove existing firebase credentials
                        RemoveFirebaseCredentials();

                        CheckExportEnabled();

                        var result = MessageBox.Show(Translation.SYNC_IMPORT_SUCCESS, Translation.MSG_INFO, MessageBoxButton.YesNo, MessageBoxImage.Information);
                        if (result == MessageBoxResult.Yes)
                            SaveButtonClicked();
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

            FirebaseCurrent = new FirebaseData
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

            // Remove existing firebase credentials
            RemoveFirebaseCredentials();

            // Initialize new firebase Config
            FirebaseHelper.InitializeService();

            MsgBoxHelper.ShowInfo(Translation.MSG_CONFIG_SAVE);

            binder.OnCloseWindow();
        }

        private void LoadDefaultConfigurations()
        {
            if (FirebaseCurrent != null)
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

        private void CheckExportEnabled()
        {
            EE = File.Exists(CustomFirebasePath);
        }

        #endregion
    }
}
