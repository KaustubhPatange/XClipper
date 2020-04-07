using ClipboardManager.models;
using ClipboardManager.Properties;
using Gma.System.MouseKeyHook;
using Loamen.KeyMouseHook;
using SQLite;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Windows.Forms;
using WK.Libraries.SharpClipboardNS;
using static ClipboardManager.context.Extension;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;

namespace ClipboardManager.context
{
    public class Context: ApplicationContext
    {
        private NotifyIcon _notifyIcon;
        private System.Windows.Window _hiddenWindow;
        private System.ComponentModel.IContainer _components;
        private ContextMenuStrip _contextmenustrip = new ContextMenuStrip();
        private SharpClipboard _clipboardFactory = new SharpClipboard();
        private Components.ClipWindow _mainWindow = new Components.ClipWindow();

        private readonly KeyMouseFactory eventHookFactory = new KeyMouseFactory(Hook.GlobalEvents());
        private readonly KeyboardWatcher keyboardWatcher;
        private List<MacroEvent> _macroEvents; private bool isFirstLaunch = true;

        private static string baseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        private string databasePath = Path.Combine(baseDirectory, "data.db");
        private SQLiteConnection dataDB;

        private bool specialkeys = false, suppresskey = false;

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
            _mainWindow.Hide();
            _components = new System.ComponentModel.Container();
            dataDB = new SQLiteConnection(databasePath);
            dataDB.CreateTable<TableCopy>();

            _clipboardFactory.ClipboardChanged += ClipboardChanged;

            _contextmenustrip.Items.Add(NewToolStripItem("Exit", (o, e) =>
            {
                if (eventHookFactory != null)
                    eventHookFactory.Dispose();
                Application.Exit();
            }));

            _notifyIcon = new NotifyIcon(_components)
            {
                ContextMenuStrip = _contextmenustrip,
                Icon = Resources.icon,
                Text = "Clipper",
                Visible = true,
            };

            DisplayStatusMessage($"{_notifyIcon.Text}: Service started");

            keyboardWatcher = eventHookFactory.GetKeyboardWatcher();
            keyboardWatcher.OnKeyboardInput += (s, e) =>
            {
                if (_macroEvents != null)
                    _macroEvents.Add(e);

                var keyEvent = (System.Windows.Forms.KeyEventArgs)e.EventArgs;
                if (e.KeyMouseEventType.ToString().Contains("KeyUp"))
                {
                    var keys = keyEvent.KeyCode;

                    // Suppress next event
                    if (suppresskey)
                    {
                        suppresskey = false;
                        return;
                    }

                    if (isAltPressed(keyEvent, keys))
                    {
                        specialkeys = true;
                        if (isShiftPressed(keyEvent, keys))
                        {
                            if (keys == Keys.Space)
                            {
                                suppresskey = true;
                                _mainWindow.Show();
                                // TODO: Show clipper window here...
                            }
                        }
                    }
                    else specialkeys = false;
                }
            };
            keyboardWatcher.Start(Hook.GlobalEvents());
        }

        private bool isShiftPressed(KeyEventArgs keyEvent, Keys keys)
        {
            return keyEvent.Shift && keys != Keys.Shift && keys != Keys.LShiftKey &&
                    keys != Keys.RShiftKey && keys != Keys.ShiftKey;
        }

        private bool isAltPressed(KeyEventArgs keyEvent, Keys keys)
        {
            return keyEvent.Alt && keys != Keys.RMenu && keys != Keys.LMenu &&
                      keys != Keys.Alt;
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
            if (e.ContentType == ContentTypes.Text)
            {
                Debug.WriteLine("Type: Text");

                Debug.WriteLine(_clipboardFactory.ClipboardText);

                InsertContent(_clipboardFactory.ClipboardText, ContentTypes.Text);
            }
            else if (e.ContentType == ContentTypes.Image)
            {
                Debug.WriteLine("Type: Image");

                if (!Directory.Exists("Images")) Directory.CreateDirectory("Images");

                string filePath = Path.Combine(baseDirectory, $"Images\\{DateTime.Now.ToFormattedDateTime()}.png");
                _clipboardFactory.ClipboardImage.Save(filePath);

                InsertContent(filePath, ContentTypes.Image);
            }
            else if (e.ContentType == ContentTypes.Files)
            {
                Debug.WriteLine("Type: Files");

                InsertContent(string.Join(",", _clipboardFactory.ClipboardFiles.ToArray()), ContentTypes.Files);

                _clipboardFactory.ClipboardFiles.Clear();
            }
        }

        private void InsertContent(string text, ContentTypes type)
        {
            dataDB.Insert(Utils.CreateTable(text, type));
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
