using ClipboardManager.models;
using ClipboardManager.Properties;
using Gma.System.MouseKeyHook;
using Loamen.KeyMouseHook;
using SQLite;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;
using WK.Libraries.SharpClipboardNS;
using Components;
using static Components.DefaultSettings;
using static Components.MainHelper;
using static Components.CommonExtensions;
using static ClipboardManager.WhatToStoreHelper;
using static ClipboardManager.KeyboardHelpers;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;
using System.Linq;

namespace ClipboardManager.context
{
    public class Context: ApplicationContext
    {
        private NotifyIcon _notifyIcon;
        private System.Windows.Window _hiddenWindow;
        private System.ComponentModel.IContainer _components;
        private ContextMenuStrip _contextmenustrip = new ContextMenuStrip();
        private SharpClipboard _clipboardFactory = new SharpClipboard();
        private SettingWindow _settingWindow = new SettingWindow();
        private Process ClipWindowProcess;
        private readonly KeyMouseFactory eventHookFactory = new KeyMouseFactory(Hook.GlobalEvents());
        private readonly KeyboardWatcher keyboardWatcher;
        private List<MacroEvent> _macroEvents; private bool isFirstLaunch = true;

        private static string baseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        private string databasePath = Path.Combine(baseDirectory, "data.db");
        private SQLiteConnection dataDB;

        // Some settings
        private bool notify = true;
        private bool toRecord = true;

        public Context()
        {
            //string c = DateTime.Today.ToString();

            //var ss = DateTime.Parse(c);
            //MessageBox.Show(ss.Date.ToString());

            _macroEvents = new List<MacroEvent>();
            _hiddenWindow = new System.Windows.Window();
            _hiddenWindow.Hide();
            _components = new System.ComponentModel.Container();
            dataDB = new SQLiteConnection(databasePath);
            dataDB.CreateTable<TableCopy>();

            _clipboardFactory.ClipboardChanged += ClipboardChanged;

            SetClipboardOptions();

            _notifyIcon = new NotifyIcon(_components)
            {
                ContextMenuStrip = _contextmenustrip,
                Icon = Resources.icon,
                Text = "Clipper",
                Visible = true,
            };
            _notifyIcon.DoubleClick += (o, e) => ShowClipWindow();

            DisplayStatusMessage($"{_notifyIcon.Text}: Service started");

            keyboardWatcher = eventHookFactory.GetKeyboardWatcher();

            keyboardWatcher.OnKeyboardInput += KeyboardWatcher_OnKeyboardInput;
            
            keyboardWatcher.Start(Hook.GlobalEvents());

            ThreadExit += Context_ThreadExit;
        }

        private void KeyboardWatcher_OnKeyboardInput(object sender, MacroEvent e)
        {
            if (_macroEvents != null)
                _macroEvents.Add(e);

            var keyEvent = (KeyEventArgs)e.EventArgs;
            if (e.KeyMouseEventType.ToString().Contains("KeyUp"))
            {
                var keys = keyEvent.KeyCode;

                /** One of the wonderfull logic which came to my mind is implemented
                 *  below. If you understand this you will say WOW WTF... */

                if (IsCtrl && !isCtrlPressed(keyEvent, keys)) return;

                if (IsAlt && !isAltPressed(keyEvent, keys)) return;

                if (IsShift && !isShiftPressed(keyEvent, keys)) return;

                if (keys != HotKey.ToEnum<Keys>()) return;

                ShowClipWindow();

                //if (keys == Keys.Oem3 && isCtrlPressed(keyEvent, keys))
                //{
                    
                //}
            }
        }

        private void ShowClipWindow()
        {
            if (ClipWindowProcess!=null && !ClipWindowProcess.HasExited)
                return;
            ClipWindowProcess = new Process();
            ClipWindowProcess.StartInfo.FileName = "XClipper.Components.exe";
            ClipWindowProcess.Start();

            //ClipWindowProcess.StartInfo.CreateNoWindow = true;

            /** Instead of creating separate assembly for Components I could've
             *  merge the library in this application itself and could've used
             *  the below code to execute it.
             *  However there is some problem to this approach. Since I am 
             *  Registering global keyhook events even if ShowDialog is called
             *  it will always create a new instance of this hook thread which 
             *  is leading to a lot of memory leaks, slow key capturing, form
             *  lagging and stuff. So this above approach is made. */

            //if (_mainWindow != null)
            //    _mainWindow.Close();
            //_mainWindow = new Components.ClipWindow();
            //_mainWindow.Loaded += (o, e) => { keyboardWatcher.Stop(); };
            //_mainWindow.Closed += (o, e) => { keyboardWatcher.Start(Hook.GlobalEvents()); _mainWindow = null; };
            //_mainWindow.ShowDialog(); 
        }
        private void Context_ThreadExit(object sender, EventArgs e)
        {
            keyboardWatcher.Stop();
            eventHookFactory.Dispose();
            if (!ClipWindowProcess.HasExited) ClipWindowProcess.Kill();
            Debug.WriteLine("Exited Thread");
        }
       

        private void ClipboardChanged(Object sender, ClipboardChangedEventArgs e)
        {
            /* There is a bug in library which automtically triggers this whenever
             * app is launched first time so I did a hack to fallback this call.
             */
            if (isFirstLaunch)
            {
                isFirstLaunch = false;
                return;
            }

            if (!toRecord)
                return;


            /* We will capture copy/cut Text, Image (eg: PrintScr) and Files
             * and save it to database.
             */
            if (e.ContentType == ContentTypes.Text && ToStoreTextClips())
            {
                Debug.WriteLine("Type: Text");
                if (!string.IsNullOrWhiteSpace(_clipboardFactory.ClipboardText.Trim()))
                {
                    Debug.WriteLine(_clipboardFactory.ClipboardText);

                    InsertContent(Utils.CreateTable(_clipboardFactory.ClipboardText, ContentTypes.Text));
                }
            }
            else if (e.ContentType == ContentTypes.Image && ToStoreImageClips())
            {
                Debug.WriteLine("Type: Image");

                if (!Directory.Exists("Images")) Directory.CreateDirectory("Images");

                string filePath = Path.Combine(baseDirectory, $"Images\\{DateTime.Now.ToFormattedDateTime()}.png");
                _clipboardFactory.ClipboardImage.Save(filePath);

                InsertContent(Utils.CreateTable(filePath, ContentTypes.Image));
            }
            else if (e.ContentType == ContentTypes.Files && ToStoreFilesClips())
            {
                Debug.WriteLine("Type: Files");

                InsertContent(Utils.CreateTable(_clipboardFactory.ClipboardFiles));

                _clipboardFactory.ClipboardFiles.Clear();
            }
        }

        private void InsertContent(TableCopy model)
        {
            // Implementation of setting TotalClipLength
            var list = dataDB.Query<TableCopy>("select * from TableCopy").OrderByDescending(s => ParseDateTimeText(s.LastUsedDateTime)).ToList();
        
           // var list = dataDB.Table<TableCopy>().OrderByDescending(s => ParseDateTimeText(s.LastUsedDateTime)).ToList();
            if (list.Count >= TotalClipLength) list.RemoveAt(list.Count - 1);

            dataDB.Insert(model);
        }

        private void DisplayStatusMessage(string text, string message = null)
        {
            _hiddenWindow.Dispatcher.Invoke(delegate
            {
                if (notify)
                {
                    _notifyIcon.BalloonTipText = text;
                    if (message != null)
                        _notifyIcon.Text = message;
                    // The timeout is ignored on recent Windows
                    _notifyIcon.ShowBalloonTip(3000);
                }
            });
        }

        private void SetClipboardOptions()
        {
            _contextmenustrip.Items.Add(NewToolStripItem("Show", (o, e) => ShowClipWindow()));
            _contextmenustrip.Items.Add(new ToolStripSeparator());
            _contextmenustrip.Items.Add(NewToolStripItem("Settings", (o, e) =>
            {
                _settingWindow.ShowDialog();
            }));
            _contextmenustrip.Items.Add(NewToolStripItem("Exit", (o, e) =>
            {
                Application.Exit();
            }));
        }

        private ToolStripMenuItem NewToolStripItem(string Text, EventHandler handler)
        {
            var item = new ToolStripMenuItem(Text);
            if (handler != null)
            {
                item.Click += handler;
            }
            return item;
        }

    }
}
