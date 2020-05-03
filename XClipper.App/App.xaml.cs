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
using System.Reflection;
using System.Windows;
using System.Threading;
using static Components.Constants;
using Microsoft.Win32;
using System.IO.Compression;
using static Components.PathHelper;
using System.Runtime.CompilerServices;
using System.Collections.Specialized;
using System.Windows.Controls;
using SQLite;
using static Components.TranslationHelper;
using ClipboardManager.models;

namespace Components
{
    /** For language edit Solution Explorer/Locales/en.xaml and paste it to locales/en.xaml 
      * to create a fake linking between static and dynamic resource binding.
      */
    public partial class App : Application
    {
        #region Variable Declaration

        public static string AppStartupLocation = Assembly.GetExecutingAssembly().Location;
        private KeyHookUtility hookUtility = new KeyHookUtility();
        private ClipWindow clipWindow;
        private WinForm.NotifyIcon notifyIcon;
        private SettingWindow settingWindow;
        private BuyWindow buyWindow;
        public static List<string> LanguageCollection = new List<string>();
        private Mutex appMutex;
        public static ResourceDictionary rm = new ResourceDictionary();

        // Some settings
        private bool ToRecord = true;

        #endregion

        #region Constructor

        public App()
        {

            LoadSettings();

            AppSingleton.GetInstance.Init();

            ClipSingleton.GetInstance.StartRecording();

            hookUtility.Subscribe(LaunchCodeUI);

            SetAppStartupEntry();

            CheckForLicense();
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
            var BuyWindowItem = CreateNewItem(Translation.APP_LICENSE, BuyMenuClicked);
            var RecordMenuItem = CreateNewItem(Translation.APP_RECORD, RecordMenuClicked).Also(s => { s.Checked = ToRecord; });
            var AppExitMenuItem = CreateNewItem(Translation.APP_EXIT, delegate { Shutdown(); });
            var DeleteMenuItem = CreateNewItem(Translation.APP_DELETE, DeleteDataClicked);
            var BackupMenuItem = CreateNewItem(Translation.APP_BACKUP, BackupClicked);
            var RestoreMenutItem = CreateNewItem(Translation.APP_RESTORE, RestoreClicked);
            var ImportDataItem = CreateNewItem(Translation.APP_IMPORT, ImportDataClicked);

            var HelpMenuItem = CreateNewItem(Translation.APP_HELP, (o, e) =>
            {
                Process.Start(new ProcessStartInfo("https://github.com/KaustubhPatange/XClipper"));
            });

            var items = new List<WinForm.MenuItem>() { ShowMenuItem, CreateSeparator(), BackupMenuItem, RestoreMenutItem, ImportDataItem, CreateSeparator(), HelpMenuItem, CreateSeparator(), RecordMenuItem, DeleteMenuItem, SettingMenuItem, CreateSeparator(), AppExitMenuItem };
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

                // Create a command sql connection...
                SQLiteConnection con = new SQLiteConnection(fileName);

            restartMethod:

                try
                {
                    // Retrieve a list of table...
                    var list = con.Table<TableCopy>().ToList();
                    con.Close();

                    // Merge tables into existing database...
                    AppSingleton.GetInstance.dataDB.InsertAll(list);
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

                            // Override exisiting SQL connection with password in it...
                            con = new SQLiteConnection(new SQLiteConnectionString(fileName, true, pass));

                            // Using goto restart the process...
                            goto restartMethod;
                        }
                    }
                }
            }
        }
        private void SettingMenuClicked(object sender, EventArgs e)
        {
            if (settingWindow != null)
                settingWindow.Close();

            settingWindow = new SettingWindow();
            settingWindow.ShowDialog();
        }

        private void BuyMenuClicked(object sender, EventArgs e)
        {
            if (buyWindow != null)
                buyWindow.Close();

            buyWindow = new BuyWindow();
            buyWindow.ShowDialog();
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
                ClipSingleton.GetInstance.StartRecording();
            else
                ClipSingleton.GetInstance.StopRecording();
        }

        #endregion

        #endregion

        #region Method Events

        private void CheckForOtherInstance()
        {
            bool IsNewInstance = false;
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

        #endregion
    }
}
