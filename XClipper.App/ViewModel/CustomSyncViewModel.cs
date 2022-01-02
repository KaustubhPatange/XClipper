﻿using static Components.DefaultSettings;
using static Components.Constants;
using System.Windows.Input;
using GalaSoft.MvvmLight.Command;
using System.Windows;
using System.IO;
using Microsoft.Win32;
using System.Xml.Linq;
using Components.Controls.Dialog;
using Components.UI;

#nullable enable

namespace Components
{
    public class CustomSyncViewModel : BaseViewModel, IFirebaseDataListener
    {
        private ICustomSyncBinder binder;
        public CustomSyncViewModel(ICustomSyncBinder binder)
        {
            this.binder = binder;

            LoadDefaultConfigurations();
            FirebaseDataListeners.Add(this);

            Subscribe();

            SaveCommand = new RelayCommand(SaveButtonClicked);
            ImportCommand = new RelayCommand(ImportButtonClicked);
            ExportCommand = new RelayCommand(ExportButtonClicked);
            EncryptCommand = new RelayCommand(ChangeDatabaseEncryption);

            CheckExportEnabled();
        }

        public void Subscribe() => FirebaseDataListeners.Add(this);
        public void UnSubscribe() => FirebaseDataListeners.Remove(this);


        #region Actual Bindings

        public ICommand SaveCommand { get; set; }
        public ICommand ImportCommand { get; set; }
        public ICommand ExportCommand { get; set; }
        public ICommand EncryptCommand { get; set; }

        public bool ProgressiveWork { get; set; } = false;
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
        public bool EFD { get; set; } // To encrypt firebase database?

        #endregion

        #region IFirebaseDataListener

        public void OnFirebaseDataChange()
        {
            LoadDefaultConfigurations();
        }

        #endregion

        #region Methods

        private void ChangeDatabaseEncryption()
        {
            if (EFD != FirebaseCurrent.IsEncrypted)
            {
                var result = MessageBox.Show(Translation.MSG_ENCRYPT_DATABASE, Translation.MSG_INFORMATION, MessageBoxButton.YesNoCancel, MessageBoxImage.Information);
                if (result == MessageBoxResult.Yes)
                {
                    ProgressiveWork = true;

                    FirebaseSingletonV2.GetInstance.MigrateClipData(
                        action: EFD ? MigrateAction.Encrypt : MigrateAction.Decrypt,
                        onSuccess: () =>
                        {
                            ProgressiveWork = false;
                            FirebaseCurrent.IsEncrypted = EFD;
                            
                            MainHelper.ToggleCurrentQRData();
                            
                            WriteFirebaseSetting();
                            MsgBoxHelper.ShowInfo(Translation.MSG_ENCRYPT_DATABASE_SUCCESS);
                        },
                        onError: () =>
                        {
                            ProgressiveWork = false;
                            EFD = FirebaseCurrent.IsEncrypted;
                            MsgBoxHelper.ShowError(Translation.MSG_ENCRYPT_DATABASE_FAILED);
                        }
                    ).RunAsync();
                }
                else EFD = FirebaseCurrent.IsEncrypted;
            }
        }

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
                    }
                    else
                        MsgBoxHelper.ShowError(Translation.SYNC_IMPORT_ERR2);
                    return;
                }
                catch { }

                MsgBoxHelper.ShowError(Translation.SYNC_IMPORT_ERR);
            }
        }


        private void SaveButtonClicked()
        {
            if (string.IsNullOrWhiteSpace(FBE) || string.IsNullOrWhiteSpace(FBAK) || string.IsNullOrWhiteSpace(FBAI))
            {
                MsgBoxHelper.ShowError(Translation.MSG_FIELD_EMPTY);
                return;
            }
            if (IAN && (string.IsNullOrWhiteSpace(FMCI) || string.IsNullOrWhiteSpace(FDCI) || string.IsNullOrWhiteSpace(FDCS)))
            {
                MsgBoxHelper.ShowError(Translation.MSG_FIELD_EMPTY);
                return;
            }

            var newData = new FirebaseData
            {
                Endpoint = FBE,
                AppId = FBAI,
                ApiKey = FBAK,
                IsAuthNeeded = IAN,
                IsEncrypted = EFD,
            };


            var desktopAuth = new OAuth
            {
                ClientId = FDCI,
                ClientSecret = FDCS
            };

            var mobileAuth = new OAuth
            {
                ClientId = FMCI
            };

            // If both data contents are same then no need to update
            bool toUpdate = false;
            toUpdate |= !newData.Equals(FirebaseCurrent);
            toUpdate |= !desktopAuth.Equals(DesktopAuth);
            toUpdate |= !mobileAuth.Equals(MobileAuth);

            if (!toUpdate)
            {
                if (DMI == DatabaseMaxItem && DatabaseMaxConnection == DMC && DatabaseMaxItemLength == DMIL)
                    return;
            }

            DatabaseMaxItem = DMI;
            DatabaseMaxItemLength = DMIL;
            DatabaseMaxConnection = DMC;

            FirebaseCurrent = newData;
            MobileAuth = mobileAuth;
            DesktopAuth = desktopAuth;

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

                IAN = FirebaseCurrent.IsAuthNeeded;
                EFD = FirebaseCurrent?.IsEncrypted ?? false;
            }

            FDCI = DesktopAuth?.ClientId;
            FDCS = DesktopAuth?.ClientSecret;
            FMCI = MobileAuth?.ClientId;

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
