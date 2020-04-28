using ClipboardManager.models;
//using ClipboardManager.Properties;
using Gma.System.MouseKeyHook;
using SQLite;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using WinForm = System.Windows.Forms;
using AppResource = Components.Properties.Resources;
using WK.Libraries.SharpClipboardNS;
using Components;
using static Components.DefaultSettings;
using static Components.MenuItemHelper;
using static Components.MainHelper;
using static Components.CommonExtensions;
using static Components.WhatToStoreHelper;
using static Components.KeyPressHelper;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;
using System.Linq;
using Components.viewModels;
using System.Resources;
using System.Reflection;
using System.Windows.Controls;
using System.Windows;
using System.Threading;
using System.Security;
using static Components.Constants;
using Microsoft.Win32;
using System.IO.Compression;
using static Components.PathHelper;

namespace Components
{
    /** For language edit Solution Explorer/Locales/en.xaml and paste it to locales/en.xaml 
      * to create a fake linking between static and dynamic resource binding.
      */
    public partial class App : Application
    {
        #region Variable Declaration

        public static string AppStartupLocation = Assembly.GetExecutingAssembly().Location;
        private ClipboardUtility clipboardUtility = new ClipboardUtility();
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
            Exit += App_Exit;

            LoadSettings();

            AppSingleton.GetInstance.Init();

            clipboardUtility.StartRecording();

            hookUtility.Subscribe(LaunchCodeUI);

            SetAppStartupEntry();

            CheckForLicense();
        }


        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);

            foreach (var file in Directory.GetFiles("locales", "*.xaml"))
            {
                LanguageCollection.Add(file);
            }

            rm.Source = new Uri($"{BaseDirectory}\\{CurrentAppLanguage}", UriKind.RelativeOrAbsolute);

            Resources.MergedDictionaries.RemoveAt(Resources.MergedDictionaries.Count - 1);
            Resources.MergedDictionaries.Add(rm);

    

            bool IsNewInstance = false;
            appMutex = new Mutex(true, rm.GetString("app_name"), out IsNewInstance);
            if (!IsNewInstance)
            {
 
                App.Current.Shutdown();
            }

            clipWindow = new ClipWindow();
            clipWindow.Hide();

            notifyIcon = new WinForm.NotifyIcon
            {
                Icon = AppResource.icon,
                Text = rm.GetString("app_name"),
                ContextMenu = new WinForm.ContextMenu(DefaultItems()),
                Visible = true
            };

            notifyIcon.DoubleClick += (o, e) => LaunchCodeUI();

            DisplayNotifyMessage();
        }

        #endregion

        #region Method Events

        private void DisplayNotifyMessage()
        {
            if (PlayNotifySound)
            {
                Dispatcher.BeginInvoke(new Action(() =>
                {
                    notifyIcon.BalloonTipText = rm.GetString("app_start_service");
                    notifyIcon.ShowBalloonTip(3000);
                }));
            }
        }

        private WinForm.MenuItem[] DefaultItems()
        {
            var ShowMenuItem = CreateNewItem(rm.GetString("app_show"), (o, e) =>
            {
                LaunchCodeUI();
            });
            var SettingMenuItem = CreateNewItem(rm.GetString("app_settings"), (o, e) =>
            {
                if (settingWindow != null)
                    settingWindow.Close();

                settingWindow = new SettingWindow();
                settingWindow.ShowDialog();
            });
            var BuyWindowItem = CreateNewItem(rm.GetString("app_license"), (o, e) =>
            {
                if (buyWindow != null)
                    buyWindow.Close();

                buyWindow = new BuyWindow();
                buyWindow.ShowDialog();
            });
            var RecordMenuItem = CreateNewItem(rm.GetString("app_record"), RecordMenuClicked);
            RecordMenuItem.Checked = ToRecord;
            var AppExitMenuItem = CreateNewItem(rm.GetString("app_exit"), (o, e) =>
            {
                Shutdown();
            });


            var BackupMenuItem = CreateNewItem(rm.GetString("app_backup"), BackupClicked);
            var RestoreMenutItem = CreateNewItem(rm.GetString("app_restore"), RestoreClicked);

            var HelpMenuItem = CreateNewItem(rm.GetString("app_help"), (o, e) =>
            {
                Process.Start(new ProcessStartInfo("https://github.com/KaustubhPatange/XClipper"));
            });

            var items = new List<WinForm.MenuItem>() { ShowMenuItem, CreateSeparator(), BackupMenuItem, RestoreMenutItem, CreateSeparator(), HelpMenuItem, CreateSeparator(), RecordMenuItem, SettingMenuItem, CreateSeparator(), AppExitMenuItem };
            if (!IsPurchaseDone) items.Insert(1, BuyWindowItem);
            return items.ToArray();
        }

        private void RestoreClicked(object sender, EventArgs e)
        {
            if (!File.Exists(DatabasePath)) return;

            var ofd = new OpenFileDialog
            {
                Title = rm.GetString("clip_file_select"),
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

                MessageBox.Show(rm.GetString("msg_restore_db"), rm.GetString("msg_information"));
            }
        }

        private void BackupClicked(object sender, EventArgs e)
        {
            if (!File.Exists(DatabasePath)) return;
            var sfd = new SaveFileDialog
            {
                FileName = "backup.zip",
                Title = rm.GetString("clip_file_select"),
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
                clipboardUtility.StartRecording();
            else
                clipboardUtility.StopRecording();
        }

        private void App_Exit(object sender, ExitEventArgs e)
        {
            hookUtility.Unsubscribe();
        }

        private void LaunchCodeUI()
        {

            if (!clipWindow.IsVisible)
            {
                clipWindow.Show();
            }
        }

        #endregion
    }
}
