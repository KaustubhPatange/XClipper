using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using WinForm = System.Windows.Forms;
using AppResource = Components.Properties.Resources;
using static Components.DefaultSettings;
using static Components.MenuItemHelper;
using static Components.MainHelper;
using Components.viewModels;
using System.Windows;
using System.Threading;
using static Components.Constants;
using Microsoft.Win32;
using System.IO.Compression;
using static Components.PathHelper;
using SQLite;
using static Components.TranslationHelper;
using ClipboardManager.models;
using FireSharp.Core.EventStreaming;
using Autofac;
using Components.UI;
using System.Threading.Tasks;
using System.Text.RegularExpressions;
using System.Windows.Threading;
using System.Globalization;
using System.Drawing;
using System.Windows.Interop;

#nullable enable

namespace Components
{
    public partial class App : Application, ISettingEventBinder, IFirebaseBinder, IBuyEventBinder, IClipServiceBinder, IFirebaseBinderV2
    {
        #region Variable Declaration

        public static List<string> LanguageCollection = new List<string>();
        public static ResourceDictionary rm = new ResourceDictionary();
        private KeyHookUtility hookUtility = new KeyHookUtility();
        private QuickPasteHelper quickPasteHelper = new QuickPasteHelper();
        private ClipWindow clipWindow;
        private WinForm.NotifyIcon notifyIcon;
        private OAuthWindow authWindow;
        private SettingWindow settingWindow;
        private UpdateWindow updateWindow;
        private BuyWindow buyWindow;
        private CustomSyncWindow configWindow;
        private DeviceWindow deviceWindow;
        private IKeyboardRecorder recorder;
        private ILicense licenseService;
        private Mutex appMutex;
        private WinForm.MenuItem ConfigSettingItem, UpdateSettingItem;
        private Update? updateModel = null;

        // Some settings
        private bool ToRecord = true;

        #endregion

        #region Constructor

        public App()
        {
            AppModule.Configure();
            recorder = AppModule.Container.Resolve<IKeyboardRecorder>();
            licenseService = AppModule.Container.Resolve<ILicense>();

            LoadSettings();

            AppSingleton.GetInstance.Init();

            recorder.SetAppBinder(this);
            recorder.StartRecording();

            hookUtility.SubscribeHotKeyEvents(LaunchCodeUI);
            hookUtility.SubscribeQuickPasteEvent(QuickPasteHook);

            quickPasteHelper.Init(recorder);

            SetAppStartupEntry();
        }

        #endregion

        #region Method overloads

        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);

            LoadLanguageResource();

            CheckForOtherInstance();

            clipWindow = new ClipWindow();
            clipWindow.Hide();

            notifyIcon = new WinForm.NotifyIcon
            {
                Icon = AppResource.icon,
                Text = Translation.APP_NAME,
                ContextMenu = new WinForm.ContextMenu(DefaultItems()),
                Visible = true
            };

            notifyIcon.DoubleClick += (o, e) => LaunchCodeUI();
            DisplayNotifyMessage();

            ApplicationHelper.AttachForegroundProcess(delegate
            {
                clipWindow.CloseWindow();
            });

            FirebaseHelper.InitializeService(this);

            licenseService.Initiate(err =>
            {
                if (err is InvalidLicenseException) return;
                if (err != null)
                {
                    MsgBoxHelper.ShowError(err.Message);
                    return;
                }
                ActivateLicense();
            });

            NotificationActivator.register();
        }

        protected override void OnExit(ExitEventArgs e)
        {
            hookUtility.UnsubscribeAll();
            FirebaseSingletonV2.GetInstance.SaveUserState();
            ExplorerHelper.Unregister();

            base.OnExit(e);
        }

        #endregion

        #region ContextMenu

        #region Items

        /// <summary>
        /// This will return a list of items to be shown in system tray context list.
        /// </summary>
        /// <returns></returns>
        private WinForm.MenuItem[] DefaultItems()
        {
            var ShowMenuItem = CreateNewItem(Translation.APP_SHOW, delegate { LaunchCodeUI(); });
            var SettingMenuItem = CreateNewItem(Translation.APP_SETTINGS, SettingMenuClicked);
            var RestartMenuItem = CreateNewItem(Translation.APP_RESTART, RestartAppClicked);
            var BuyWindowItem = CreateNewItem(Translation.APP_LICENSE, BuyMenuClicked);
            var RecordMenuItem = CreateNewItem(Translation.APP_RECORD, RecordMenuClicked).Also(s => { s.Checked = ToRecord; });
            var AppExitMenuItem = CreateNewItem(Translation.APP_EXIT, delegate { Shutdown(); });
            var DeleteMenuItem = CreateNewItem(Translation.APP_DELETE, DeleteDataClicked);
            var BackupMenuItem = CreateNewItem(Translation.APP_BACKUP, BackupClicked);
            var RestoreMenutItem = CreateNewItem(Translation.APP_RESTORE, RestoreClicked);
            var ImportDataItem = CreateNewItem(Translation.APP_IMPORT, ImportDataClicked);
            ConfigSettingItem = CreateNewItem(Translation.APP_CONFIG_SETTING, ConfigSettingClicked);
            UpdateSettingItem = CreateNewItem(Translation.APP_UPDATE, UpdateSettingClicked).Also(c => c.Visible = false);

            var HelpMenuItem = CreateNewItem(Translation.APP_HELP, (o, e) =>
            {
                Process.Start(new ProcessStartInfo("https://github.com/KaustubhPatange/XClipper"));
            });

            var items = new List<WinForm.MenuItem>() { ShowMenuItem, RestartMenuItem, CreateSeparator(), BackupMenuItem, RestoreMenutItem, ImportDataItem, CreateSeparator(), HelpMenuItem, CreateSeparator(), RecordMenuItem, DeleteMenuItem, CreateSeparator(), BuyWindowItem, ConfigSettingItem, UpdateSettingItem, SettingMenuItem, CreateSeparator(), AppExitMenuItem };

            return items.ToArray();
        }

        #endregion

        #region Invokes

        private void UpdateSettingClicked(object sender, EventArgs e)
        {
            CheckForUpdates();
        }

        private void ImportDataClicked(object sender, EventArgs e)
        {
            // Create an open file dialog...
            var ofd = new OpenFileDialog
            {
                Title = Translation.CLIP_FILE_SELECT2,
                Filter = "Supported Formats|*.db;*.zip",
            };
            // Show the open file dialog and capture fileName...
            if (ofd.ShowDialog() == true)
            {
                // Store selected filename and tempDir into variable...
                var tmpDir = GetTemporaryPath();
                string fileName = ofd.FileName;

                // For zip file we will extract the database stored in it...
                if (Path.GetExtension(ofd.FileName).ToLower() == "zip")
                {
                    ZipFile.ExtractToDirectory(ofd.FileName, tmpDir);
                    fileName = Path.Combine(tmpDir, "data");
                }

                // Create a command SQL connection...
                SQLiteConnection con = new SQLiteConnection(fileName);

            restartMethod:

                try
                {
                    // Retrieve a list of table...
                    var list = con.Table<TableCopy>().ToList();
                    con.Close();

                    // Merge tables into existing database...
                    AppSingleton.GetInstance.InsertAll(list);
                    MsgBoxHelper.ShowInfo(Translation.MSG_CLIP_IMPORT);

                }
                catch (SQLiteException ex)
                {
                    // If exception "file is not a database caught". It is likely to be encrypted
                    if (ex.Message.Contains("file is not a database"))
                    {
                        var msg = MessageBox.Show(Translation.MSG_MERGE_ENCRYPT, Translation.MSG_WARNING, MessageBoxButton.YesNo, MessageBoxImage.Warning);
                        if (msg == MessageBoxResult.Yes)
                        {
                            // Decrypt the database by asking password to the user...
                            var pass = Microsoft.VisualBasic.Interaction.InputBox(Translation.MSG_ENTER_PASS, Translation.MSG_PASSWORD, CustomPassword);

                            // Override existing SQL connection with password in it...
                            using (var disposableConnection = new SQLiteConnection(new SQLiteConnectionString(fileName, true, pass)))
                            {
                                con = disposableConnection;
                            }

                            // Using goto restart the process...
                            goto restartMethod;
                        }
                    }
                    LogHelper.Log(this, ex.StackTrace);
                }
            }
        }
        private void ConfigSettingClicked(object sender, EventArgs e)
        {
            CallSyncWindow();
        }

        private void SettingMenuClicked(object sender, EventArgs e)
        {
            if (settingWindow != null)
                settingWindow.Close();

            settingWindow = new SettingWindow(this);
            settingWindow.ShowDialog();
        }

        private void BuyMenuClicked(object sender, EventArgs e)
        {
            CallBuyWindow();
        }

        private void DeleteDataClicked(object sender, EventArgs e)
        {
            var msg = MessageBox.Show(Translation.MSG_DELETE_ALL, Translation.MSG_WARNING, MessageBoxButton.YesNo, MessageBoxImage.Warning);
            if (msg == MessageBoxResult.Yes)
            {
                if (BindDatabase)
                {
                    msg = MessageBox.Show(Translation.MSG_DELETE_ALL_ONLINE, Translation.MSG_INFORMATION, MessageBoxButton.YesNo, MessageBoxImage.Information);
                }
                AppSingleton.GetInstance.DeleteAllData(msg == MessageBoxResult.Yes);
            }
        }

        private void RestoreClicked(object sender, EventArgs e)
        {
            if (!File.Exists(DatabasePath)) return;

            var ofd = new OpenFileDialog
            {
                Title = Translation.CLIP_FILE_SELECT,
                Filter = "zip|*.zip"
            };
            if (ofd.ShowDialog() == true)
            {
                var tmp = GetTemporaryPath();
                ZipFile.ExtractToDirectory(ofd.FileName, tmp);

                var db = Path.Combine(tmp, "data.db");
                var export = Path.Combine(BaseDirectory, "data.db");
                File.Copy(db, export);

                File.Delete(db); Directory.Delete(tmp);

                MsgBoxHelper.ShowInfo(Translation.MSG_RESTORE_DB);
            }
        }

        private void BackupClicked(object sender, EventArgs e)
        {
            if (!File.Exists(DatabasePath)) return;
            var sfd = new SaveFileDialog
            {
                FileName = "backup.zip",
                Title = Translation.CLIP_FILE_SELECT,
                Filter = "zip|*.zip"
            };
            if (sfd.ShowDialog() == true)
            {
                if (File.Exists(sfd.FileName)) File.Delete(sfd.FileName);

                var dir = GetTemporaryPath();
                var db = Path.Combine(dir, "data");
                File.Copy(DatabasePath, db);
                ZipFile.CreateFromDirectory(dir, sfd.FileName);

                File.Delete(db); Directory.Delete(dir);
            }
        }

        private void RecordMenuClicked(object sender, EventArgs e)
        {
            ToRecord = !ToRecord;

            ((WinForm.MenuItem)sender).Checked = ToRecord;
            if (ToRecord)
                recorder.StartRecording();
            else
                recorder.StopRecording();
        }

        #endregion

        #endregion

        #region IClipServiceBinder Events

        public void OnImageSaveFailed()
        {
            AppNotificationHelper.ShowBasicToast(
                dispatcher: Dispatcher,
                title: Translation.MSG_IMAGE_SAVE_FAILED_TITLE,
                message: Translation.MSG_IMAGE_SAVE_FAILED_TEXT,
                silent: !PlayNoticationSound,
                doOnActivated: () =>
                {
                    Process.Start("explorer.exe", ApplicationLogDirectory);
                }
            ).RunAsync();
        }

        #endregion

        #region IBuyEventBinder Events

        public void OnLicenseActivationSucceed()
        {
            ActivateLicense();
        }

        #endregion

        #region ISettingEventBinder Events

        public void OnConnectedDeviceClicked()
        {
            CallDeviceWindow();
        }

        public void OnDataResetButtonClicked()
        {
            // Close the setting window.
            settingWindow.Close();

            // A task to remove user.
            Task.Run(async () =>
            {
                await FirebaseSingleton.GetInstance.ResetUser().ConfigureAwait(false);

                RunOnMainThread(() =>
                {
                    MsgBoxHelper.ShowInfo(Translation.MSG_RESET_DATA_SUCCESS);
                });
            });
        }

        #endregion

        #region IFirebaseBinder Events 

        public void SendNotification(string title, string message, Action? onActive = null)
        {
            AppNotificationHelper.ShowBasicToast(
                dispatcher: Dispatcher,
                title: title,
                message: message,
                silent: !PlayNoticationSound,
                doOnActivated: onActive
            ).RunAsync();
        }

        public void OnNoConfigurationFound() => CallSyncWindow();

        public void OnResetFirebaseConfig()
        {
            // Reset the firebase configuration settings
            BindDatabase = BindDelete = false;
            WriteSettings();

            RemoveFirebaseCredentials();

            AppNotificationHelper.ShowBasicToast(
                dispatcher: Dispatcher,
                Translation.SYNC_DISABLED_TITLE,
                message: Translation.SYNC_DISABLED_TEXT,
                silent: !PlayNoticationSound
            ).RunAsync();
        }

        public void OnNeedToGenerateToken(string ClientId, string ClientSecret)
        {
            MsgBoxHelper.ShowWarning(Translation.MSG_NEED_AUTH);
            CallAuthWindow(ClientId, ClientSecret);
        }

        public void OnDataAdded(ValueAddedEventArgs e)
        {
            Debug.WriteLine("[Add] Path: " + e.Path + ", Change: " + e.Data);
            if (Regex.IsMatch(e.Path, DEVICE_REGEX_PATH_PATTERN))
            {
                Debug.WriteLine("Adding Device...");
                FirebaseSingleton.GetInstance.SetGlobalUserTask(true).RunAsync();
            }
        }

        public void OnDataChanged(ValueChangedEventArgs e)
        {
            // 1st value from real-time database is your last value in XClipper app.
            Debug.WriteLine("[Changed] Path: " + e.Path + ", Data: " + e.Data + ", OldData: " + e.OldData);
            if (e.Path.Contains(PATH_CLIP_DATA))
            {
                AppSingleton.GetInstance.CheckDataAndUpdate(e.Data, (data, type) =>
                {
                    ParseUpdateResult(data, type);
                });
            }
        }

        public void OnDataRemoved(ValueRemovedEventArgs e)
        {
            LogHelper.Log(this, "[Remove] Path: " + e.Path);
            Debug.WriteLine("[Remove] Path: " + e.Path);
            if (Regex.IsMatch(e.Path, CLIP_REGEX_PATH_PATTERN) || Regex.IsMatch(e.Path, CLIP_ITEM_REGEX_PATTERN)) // If all clips were removed
            {
                FirebaseSingleton.GetInstance.SetGlobalUserTask(true).RunAsync();
            }
            if (Regex.IsMatch(e.Path, DEVICE_REGEX_PATH_PATTERN))
            {
                Debug.WriteLine("Removing Device...");
                FirebaseSingleton.GetInstance.SetGlobalUserTask().RunAsync();
            }
        }

        public void OnClipItemRemoved(RemovedEventArgs e)
        {
            LogHelper.Log(this, "[Remove Clip] Data: " + e?.data);
            Debug.Write("[Removed Clip] Data: " + e?.data);
            Debug.WriteLine(", " + AppSingleton.GetInstance.DeleteClipData(e?.data));
        }

        #endregion

        #region IFirebaseBinderV2 Events

        public void OnClipItemAdded(List<string> unencryptedDataList)
        {
            Debug.WriteLine($"[V2 Added]: {unencryptedDataList}");
            if (unencryptedDataList.Count > 1)
            {
                foreach (string item in unencryptedDataList)
                    AppSingleton.GetInstance.CheckAndUpdateData(item);
                SendNotification(Translation.APP_NAME, $"{unencryptedDataList.Count} {Translation.MSG_CLIPS_ADDED_TEXT}");
            }
            else
            {
                AppSingleton.GetInstance.CheckAndUpdateData(unencryptedDataList[0], (data, type) =>
                {
                    ParseUpdateResult(data, type);
                });
            }
        }

        public void OnClipItemRemoved(List<string> unencryptedDataList)
        {
            Debug.WriteLine($"[V2 Removed]: {unencryptedDataList}");
            if (unencryptedDataList.Count > 1)
            {
                foreach (string item in unencryptedDataList)
                    AppSingleton.GetInstance.DeleteClipData(item);
                SendNotification(Translation.APP_NAME, $"{unencryptedDataList.Count} {Translation.MSG_CLIPS_REMOVED_TEXT}");
            }
            else
            {
                AppSingleton.GetInstance.DeleteClipData(unencryptedDataList[0]);
                SendNotification(Translation.SYNC_REMOVE_TITLE, $"{unencryptedDataList[0]}");
            }
        }

        public void OnClipItemUpdated(string previousUnEncryptedData, string newUnEncryptedData)
        {
            Debug.WriteLine($"[V2 Updated]: Old: {previousUnEncryptedData}, New: {newUnEncryptedData}");
            AppSingleton.GetInstance.UpdateClipItem(previousUnEncryptedData, newUnEncryptedData, () => SendNotification(Translation.SYNC_UPDATE_TITLE, newUnEncryptedData));
        }

        public void OnDeviceAdded(Device device)
        {
            AppNotificationHelper.ShowBasicToast(
                dispatcher: Dispatcher,
                title: $"{device?.model} {Translation.SYNC_DEVICE_ADDED}",
                silent: !PlayNoticationSound
            ).RunAsync();
        }

        public void OnDeviceRemoved(Device device)
        {
            AppNotificationHelper.ShowBasicToast(
                dispatcher: Dispatcher,
                title: $"{device?.model} {Translation.SYNC_DEVICE_REMOVED}",
                silent: !PlayNoticationSound
            ).RunAsync();
        }

        private void ParseUpdateResult(string data, ContentType type)
        {
            switch (type)
            {
                case ContentType.Text:
                    AppNotificationHelper.ShowBasicToast(
                        dispatcher: Dispatcher,
                        title: Translation.APP_COPY_TITLE,
                        message: data.Truncate(NOTIFICATION_TRUNCATE_TEXT),
                        silent: !PlayNoticationSound,
                        audioType: ToastAudioType.MAIL,
                        doOnActivated: () =>
                        {
                            var recorder = AppModule.Container.Resolve<IKeyboardRecorder>();
                            recorder.Ignore(() =>
                            {
                                Clipboard.SetText(data); // Set text as current clipboard.
                            });
                        }
                    ).RunAsync();
                    break;
                case ContentType.Image:
                    AppNotificationHelper.ShowImageToast(
                        dispatcher: Dispatcher,
                        imagePath: data,
                        title: Translation.APP_COPY_TITLE_IMAGE,
                        message: data,
                        silent: !PlayNoticationSound,
                        audioType: ToastAudioType.MAIL,
                        doOnActivated: () =>
                        {
                            Process.Start(data); // Show image on default image viewer.
                        }
                    ).RunAsync();
                    break;
            }
        }

        #endregion

        #region Method Events

        private void ActivateLicense()
        {
            CheckForUpdates();
            UpdateSettingItem.Visible = true;
            FirebaseSingletonV2.GetInstance.UpdateConfigurations();
            if (IsPurchaseDone) UpdateSettingItem.Visible = true;
        }

        private void CheckForUpdates()
        {
            if (!IsPurchaseDone || !CheckApplicationUpdates) return;
            var updater = AppModule.Container.Resolve<IUpdater>();
            updater.Check((isAvailable, model) =>
            {
                if (isAvailable)
                {
                    updateModel = model;

                    AppNotificationHelper.ShowBasicToast(
                        dispatcher: Dispatcher,
                        title: Translation.APP_UPDATE_TITLE,
                        message: Translation.APP_UPDATE_TEXT,
                        silent: !PlayNoticationSound,
                        doOnActivated: () =>
                        {
                            UpdateAction_BalloonTipClicked(this, EventArgs.Empty);
                        }
                    ).RunAsync();
                }
            });
        }

        private void UpdateAction_BalloonTipClicked(object sender, EventArgs e)
        {
            if (IsPurchaseDone)
            {
                CallUpdateWindow(updateModel?.Desktop);
                updateModel = null;
            }
            else
            {
                var result = MessageBox.Show(Translation.MSG_LICENSE_UPDATE, Translation.MSG_WARNING, MessageBoxButton.YesNo, MessageBoxImage.Warning);
                if (result == MessageBoxResult.Yes)
                {
                    AppModule.Container.Resolve<IUpdater>().Launch();
                }
            }
        }

        private void RestartAppClicked(object sender, EventArgs e)
        {
            var msg = MessageBox.Show(Translation.MSG_RESTART, Translation.MSG_INFO, MessageBoxButton.YesNo, MessageBoxImage.Warning);
            if (msg == MessageBoxResult.Yes)
                RestartApplication();
        }

        private void CheckForOtherInstance()
        {
            bool IsNewInstance;
            appMutex = new Mutex(true, Translation.APP_NAME, out IsNewInstance);
            if (!IsNewInstance)
            {
                App.Current.Shutdown();
            }
        }

        private void LoadLanguageResource()
        {
            foreach (var file in Directory.GetFiles("locales", "*.xaml"))
            {
                LanguageCollection.Add(file);
            }

            rm.Source = new Uri($"{BaseDirectory}\\{CurrentAppLanguage}", UriKind.RelativeOrAbsolute);

            Resources.MergedDictionaries.RemoveAt(Resources.MergedDictionaries.Count - 1);
            Resources.MergedDictionaries.Add(rm);
        }

        private void DisplayNotifyMessage()
        {
            if (DisplayStartNotification)
            {
                AppNotificationHelper.ShowBasicToast(
                    dispatcher: Dispatcher,
                    title: Translation.APP_START_SERVICE,
                    silent: !PlayNoticationSound
                ).RunAsync();
            }
        }

        private void QuickPasteHook(int number)
        {
            quickPasteHelper.DoPasteAction(number);
        }

        private void LaunchCodeUI()
        {
            clipWindow.WindowState = WindowState.Normal;

            if (!clipWindow.IsVisible)
            {
                clipWindow.Show();
                clipWindow._tbSearchBox.Focus();
                ApplicationHelper.GlobalActivate(clipWindow);
            }
            else
                clipWindow.CloseWindow();
        }

        private void CallUpdateWindow(Update.Windows? model)
        {
            if (model == null) return;
            if (updateWindow != null)
                updateWindow.Close();
            updateWindow = new UpdateWindow(model);
            updateWindow.ShowDialog();
        }

        private void CallDeviceWindow()
        {
            if (deviceWindow != null)
                deviceWindow.Close();

            deviceWindow = new DeviceWindow();
            deviceWindow.ShowDialog();
        }

        private void CallAuthWindow(string Id, string secret)
        {
            if (authWindow != null)
                authWindow.Close();

            authWindow = new OAuthWindow(Id, secret);
            if (authWindow.ShowDialog() == true)
            {
                FirebaseSingletonV2.GetInstance.Initialize();
            }
        }

        private void CallBuyWindow()
        {
            if (buyWindow != null)
                buyWindow.Close();

            buyWindow = new BuyWindow(this);
            buyWindow.ShowDialog();
        }

        private void CallSyncWindow()
        {
            if (configWindow != null)
                configWindow.Close();

            configWindow = new CustomSyncWindow();
            configWindow.ShowDialog();
        }

        #endregion
    }
}
