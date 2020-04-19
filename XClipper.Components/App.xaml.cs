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

namespace Components
{
    public partial class App : System.Windows.Application
    {

        public static string BaseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        private ClipboardUtility clipboardUtility = new ClipboardUtility();
        private KeyHookUtility hookUtility = new KeyHookUtility();
        private ClipWindow clipWindow;
        private WinForm.NotifyIcon notifyIcon;
        private ResourceManager rm;
        private SettingWindow settingWindow;

        // Some settings
        private bool ToRecord = true;

        public App()
        {
            LoadCompleted += App_LoadCompleted;
            Exit += App_Exit;

            rm = new ResourceManager("Components.Locales.app", Assembly.Load(Assembly.GetExecutingAssembly().GetName()));

            AppSingleton.GetInstance.Init();

            clipboardUtility.StartRecording();
            hookUtility.Subscribe(LaunchCodeUI);

            notifyIcon = new WinForm.NotifyIcon
            {
                Icon = AppResource.icon,
                Text = "XClipper",
                ContextMenu = new WinForm.ContextMenu(DefaultItems()),
                Visible = true
            };

            notifyIcon.DoubleClick += (o, e) => LaunchCodeUI();
            
            DisplayNotifyMessage();
        }

        private void DisplayNotifyMessage()
        {
            if (PlayNotifySound)
            {
                Dispatcher.BeginInvoke(new Action(() =>
                {
                    notifyIcon.BalloonTipText = $"{notifyIcon.Text}: Service Started";
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
            Debug.WriteLine("ToRecord: " + ToRecord);

            ((WinForm.MenuItem)sender).Checked = ToRecord;
            if (ToRecord)
                clipboardUtility.StartRecording();
            else
                clipboardUtility.StopRecording();
        }
        private void App_LoadCompleted(object sender, System.Windows.Navigation.NavigationEventArgs e)
        {
            Debug.WriteLine("Executed");
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
    }
}
