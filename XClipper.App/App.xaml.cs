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
using static Components.LicenseHandler;
using FireSharp.EventStreaming;
using Autofac;
using FireSharp.Extensions;
using Components.UI;
using System.Threading.Tasks;
using System.Text.RegularExpressions;

namespace Components
{
    /** For language edit Solution Explorer/Locales/en.xaml and paste it to locales/en.xaml 
      * to create a fake linking between static and dynamic resource binding.
      */
    public partial class App : Application, ISettingEventBinder, IFirebaseBinder
    {
        #region Variable Declaration

        public ISettingEventBinder binder;
        private KeyHookUtility hookUtility = new KeyHookUtility();
        private ClipWindow clipWindow;
        private WinForm.NotifyIcon notifyIcon;
        private SettingWindow settingWindow;
        private BuyWindow buyWindow;
        private CustomSyncWindow configWindow;
        private DeviceWindow deviceWindow;
        private IKeyboardRecorder recorder;
        public static List<string> LanguageCollection = new List<string>();
        private Mutex appMutex;
        public static ResourceDictionary rm = new ResourceDictionary();

        // Some settings
        private bool ToRecord = true;

        #endregion

        #region Constructor

        public App()
        {
            AppModule.Configure();
            recorder = AppModule.Container.Resolve<IKeyboardRecorder>();

            LoadSettings();

            AppSingleton.GetInstance.Init();

            FirebaseSingleton.GetInstance.Init(UniqueID);

            recorder.StartRecording();

            hookUtility.Subscribe(LaunchCodeUI);

            SetAppStartupEntry();

            CheckForLicense();

            ActivatePaidFeatures();
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

            binder = this;

            FirebaseSingleton.GetInstance.SetCallback(this);
        }

        protected override void OnExit(ExitEventArgs e)
        {
            hookUtility.Unsubscribe();

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
            var ConfigSettingItem = CreateNewItem(Translation.APP_CONFIG_SETTING, ConfigSettingClicked);

            var HelpMenuItem = CreateNewItem(Translation.APP_HELP, (o, e) =>
            {
                Process.Start(new ProcessStartInfo("https://github.com/KaustubhPatange/XClipper"));
            });

            var items = new List<WinForm.MenuItem>() { ShowMenuItem, RestartMenuItem, CreateSeparator(), BackupMenuItem, RestoreMenutItem, ImportDataItem, CreateSeparator(), ConfigSettingItem , CreateSeparator(), HelpMenuItem, CreateSeparator(), RecordMenuItem, DeleteMenuItem, SettingMenuItem, CreateSeparator(), AppExitMenuItem };
            if (!IsPurchaseDone) items.Insert(1, BuyWindowItem);
            return items.ToArray();
        }


        #endregion

        #region Invokes

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
                    MessageBox.Show(Translation.MSG_CLIP_IMPORT, Translation.MSG_INFO);

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
                            con = new SQLiteConnection(new SQLiteConnectionString(fileName, true, pass));

                            // Using goto restart the process...
                            goto restartMethod;
                        }
                    }
                }
            }
        }
        private void ConfigSettingClicked(object sender, EventArgs e)
        {
            if (configWindow != null)
                configWindow.Close();

            configWindow = new CustomSyncWindow();
            configWindow.ShowDialog();
        }

        private void SettingMenuClicked(object sender, EventArgs e)
        {
            if (settingWindow != null)
                settingWindow.Close();

            settingWindow = new SettingWindow(binder);
            settingWindow.ShowDialog();
        }

        private void BuyMenuClicked(object sender, EventArgs e)
        {
            CallBuyWindow();
        }

        private void DeleteDataClicked(object sender, EventArgs e)
        {
            var msg = MessageBox.Show(Translation.MSG_DELETE_ALL, Translation.MSG_WARNING, MessageBoxButton.YesNo, MessageBoxImage.Warning);
            if (msg == MessageBoxResult.Yes) AppSingleton.GetInstance.DeleteAllData();
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

                MessageBox.Show(Translation.MSG_RESTORE_DB, Translation.MSG_INFORMATION);
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

        #region ISettingEventBinder Events

        public void OnBuyButtonClicked()
        {
            CallBuyWindow();
        }

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
                await FirebaseSingleton.GetInstance.RemoveUser();
                
                RunOnMainThread(() =>
                {
                    MessageBox.Show(Translation.MSG_RESET_DATA_SUCCESS, Translation.MSG_INFO, MessageBoxButton.OK, MessageBoxImage.Information);
                });
            });
        }

        #endregion

        #region IFirebaseBinder Events 

        public void OnDataAdded(ValueAddedEventArgs e)
        {
         //   AppSingleton.GetInstance.CheckDataAndUpdate(e.Data);

//Added:1764456878cf916a, Path: /users/1PAF8EB-4KR35L-1ICT12V-H7M3FM/Devices/id
//Added:POCO F1, Path: /users/1PAF8EB-4KR35L-1ICT12V-H7M3FM/Devices/model
//Added:29, Path: /users/1PAF8EB-4KR35L-1ICT12V-H7M3FM/Devices/sdk

            // Add user when node is inserted
            if (Regex.IsMatch(e.Path, DEVICE_REGEX_PATH_PATTERN))
            {
                Debug.WriteLine("Adding Device...");
                Task.Run(async () => { await FirebaseSingleton.GetInstance.SetGlobalUser(); });
            }

        }

        public void OnDataChanged(ValueChangedEventArgs e)
        {
            // 1st value from real-time database is your 5th one in XClipper window.
            // todo: Do something on Changed

            if (e.Path.Contains(PATH_CLIP_DATA))
            {
                AppSingleton.GetInstance.CheckDataAndUpdate(e.Data);
                Debug.WriteLine("Path: " + e.Path + ", Changed:" + e.Data + ", Old Data: " + e.OldData);
            }
              
        }

        public void OnDataRemoved(ValueRemovedEventArgs e)
        {
            // Remove user node get's deleted
            if (Regex.IsMatch(e.Path, DEVICE_REGEX_PATH_PATTERN))
            {
                Debug.WriteLine("Removing Device...");
                Task.Run(async () => { await FirebaseSingleton.GetInstance.SetGlobalUser(); });
            }
        }


        #endregion

        #region Method Events

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
            if (PlayNotifySound)
            {
                Dispatcher.BeginInvoke(new Action(() =>
                {
                    notifyIcon.BalloonTipText = Translation.APP_START_SERVICE;
                    notifyIcon.ShowBalloonTip(3000);
                }));
            }
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

        private void CallDeviceWindow()
        {
            if (deviceWindow != null)
                deviceWindow.Close();

            deviceWindow = new DeviceWindow();
            deviceWindow.ShowDialog();
        }

        private void CallBuyWindow()
        {
            if (buyWindow != null)
                buyWindow.Close();

            buyWindow = new BuyWindow();
            buyWindow.ShowDialog();
        }


        #endregion
    }
}
