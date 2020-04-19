using SHDocVw;
using System;
using System.Runtime.InteropServices;
using System.Text;

namespace Components
{
    public static class ExplorerHelper
    {
        public static string GetActiveExplorerPath()
        {
            IntPtr handle = GetForegroundWindow();

            var shell = new Shell32.Shell();

            foreach (InternetExplorer window in shell.Windows())
            {
                if (window.HWND == (int)handle)
                {
                    var shellWindow = window.Document as Shell32.IShellFolderViewDual2;

                    if (shellWindow != null)
                    {
                        var currentFolder = shellWindow.Folder.Items().Item();

                        if (currentFolder == null || currentFolder.Path.StartsWith("::"))
                        {
                            const int nChars = 256;
                            StringBuilder Buff = new StringBuilder(nChars);
                            if (GetWindowText(handle, Buff, nChars) > 0)
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


        [DllImport("user32.dll")]
        private static extern IntPtr GetForegroundWindow();

        [DllImport("user32.dll")]
        static extern int GetWindowText(IntPtr hWnd, StringBuilder text, int count);
    }
}
