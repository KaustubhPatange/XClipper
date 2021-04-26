using System;
using System.Security.Cryptography;
using System.Text;
using Components;

namespace XClipper
{
    public static class ClipHelper
    {
        /// <summary>
        /// Returns the MD5 hash for the string.
        /// </summary>
        public static string GetMD5(string text)
        {
            using (var md5 = MD5.Create())
            {
                StringBuilder sb = new();
                byte[] hash = md5.ComputeHash(Encoding.Default.GetBytes(text));
                foreach (byte bt in hash) {
                    sb.Append(bt.ToString("x2"));
                }
                return sb.ToString();
            }
        }

        /// <summary>
        /// Returns the active window process name eg: "chrome.exe".
        /// </summary>
        public static string GetActiveApp()
        {
            return ApplicationHelper.GetProcessName(ApplicationHelper.GetForegroundWindow());
        }

        /// <summary>
        /// Returns the active window title (i.e the name you see in the Task Manager).
        /// </summary>
        /// <returns></returns>
        public static string GetActiveAppTitle()
        {
            return ApplicationHelper.GetWindowTitle(ApplicationHelper.GetForegroundWindow());
        }
    }
}