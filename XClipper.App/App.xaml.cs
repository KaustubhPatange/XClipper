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

namespace Components
{
    /** For language edit Solution Explorer/Locales/en.xaml and paste it to locales/en.xaml 
      * to create a fake linking between static and dynamic resource binding.
      */
    public partial class App : Application
    {
        #region Variable Declaration

        public static string BaseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        public static string AppStartupLocation = Assembly.GetExecutingAssembly().Location;
        private ClipboardUtility clipboardUtility = new ClipboardUtility();
        private KeyHookUtility hookUtility = new KeyHookUtility();
        private ClipWindow clipWindow;
        private WinForm.NotifyIcon notifyIcon;
        private SettingWindow settingWindow;
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

            AppSingleton.GetInstance.Init();

            LoadSettings();

            clipboardUtility.StartRecording();

            hookUtility.Subscribe(LaunchCodeUI);

            SetAppStartupEntry();
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

            /** This follow consequent code will make this app
             *  to run only single instance.
             */

            bool IsNewInstance = false;
            appMutex = new Mutex(true, rm.GetString("app_name"), out IsNewInstance);
            if (!IsNewInstance)
            {
                //var appNotify = new WinForm.NotifyIcon()
                //{
                //    Icon = AppResource.icon,
                //    Text = rm.GetString("app_name"),
                //    Visible = true
                //};
                //Dispatcher.BeginInvoke(new Action(() =>
                //{
                //    appNotify.BalloonTipText = rm.GetString("app_isrun_service");
                //    appNotify.ShowBalloonTip(3000);
                //}));
                App.Current.Shutdown();
            }

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
            var RecordMenuItem = CreateNewItem(rm.GetString("app_record"), RecordMenuClicked);
            RecordMenuItem.Checked = ToRecord;
            var AppExitMenuItem = CreateNewItem(rm.GetString("app_exit"), (o, e) =>
            {
                Shutdown();
            });

            return new WinForm.MenuItem[] { ShowMenuItem, CreateSeparator(), RecordMenuItem, SettingMenuItem, AppExitMenuItem };
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
  
        private void App_Exit(object sender, System.Windows.ExitEventArgs e)
        {
            hookUtility.Unsubscribe();
        }

        private void LaunchCodeUI()
        {
            if (clipWindow == null)
                clipWindow = new ClipWindow();

            if (!clipWindow.IsVisible)
            {
                clipWindow.Show();
            }
        } 

        #endregion
    }
}
