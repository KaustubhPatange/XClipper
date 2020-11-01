using SHDocVw;
using System;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Threading;

#nullable enable

namespace Components
{
    public static class ExplorerHelper
    {

        #region Public methods

        private static DispatcherTimer? dtimer;
        static IntPtr lastCurrentHandle = IntPtr.Zero;

        public static void Register(IntPtr handle)
        {
            dtimer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(250) };
            dtimer.Tick += delegate
            {
                IntPtr temp = GetForegroundWindow();
                Debug.WriteLine($"Handle: ${handle}, Temp: {temp}, Last: ${lastCurrentHandle}");
                if ((int)handle != (int)temp)
                    lastCurrentHandle = temp;
            };
            dtimer.Start();
        }

        public static void Unregister()
        {
            dtimer?.Stop();
        }

        public static string GetLastActiveExplorerPath()
        {
            ShellWindows shellWindows = new ShellWindows();

            foreach (InternetExplorer window in shellWindows)
            {
                //     Debug.Write("WindowFName: " + window.FullName + $", ({window.HWND} == {(int)handle}), ({(window.Document as Shell32.IShellFolderViewDual2).Folder.Items().Item().Path})\n");
                if (window.HWND == (int)lastCurrentHandle)
                {
                    var shellWindow = window.Document as Shell32.IShellFolderViewDual2;

                    if (shellWindow != null)
                    {
                        var currentFolder = shellWindow.Folder.Items().Item();

                        if (currentFolder == null || currentFolder.Path.StartsWith("::"))
                        {
                            const int nChars = 256;
                            StringBuilder Buff = new StringBuilder(nChars);
                            if (GetWindowText(lastCurrentHandle, Buff, nChars) > 0)
                            {
                                return Buff.ToString();
                            }
                        }
                        else
                        {
                            return currentFolder.Path;
                        }
                    }
                    break;
                }
            }
            return null;
        }

        #endregion


        #region PInvokes

        [DllImport("user32.dll")]
        static extern int GetWindowText(IntPtr hWnd, StringBuilder text, int count);

        [DllImport("user32.dll")]
        [return: MarshalAs(UnmanagedType.Bool)]
        static extern bool IsWindowVisible(IntPtr hWnd);

        [DllImport("user32.dll")]
        static extern IntPtr GetLastActivePopup(IntPtr hWnd);

        [DllImport("user32.dll", ExactSpelling = true)]
        static extern IntPtr GetForegroundWindow();

        #endregion
    }
}
