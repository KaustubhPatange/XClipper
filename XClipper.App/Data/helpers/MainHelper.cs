using System.Text.RegularExpressions;
using static Components.DefaultSettings;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System;
using static Components.Constants;
using System.IO;
using System.Diagnostics;
using System.Windows.Threading;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using RestSharp;
using System.Drawing;
using RestSharp.Extensions;
using System.Net.Http;

namespace Components
{
    public static class MainHelper
    {
        /** Return the DependencyObject if it is a ScrollViewer */
        // TODO; Categorization required. Put things into their respective places.
        public static DependencyObject GetScrollViewer(DependencyObject o)
        {
            if (o is ScrollViewer)
            { return o; }

            for (int i = 0; i < VisualTreeHelper.GetChildrenCount(o); i++)
            {
                var child = VisualTreeHelper.GetChild(o, i);
                var result = GetScrollViewer(child);
                if (result == null)
                {
                    continue;
                }
                else
                {
                    return result;
                }
            }
            return null;
        }

        public static long ParseDateTimeText(string text)
        {
            return text.Replace("-", "").Replace(" ", "").ToLong();
        }
        public static void CalculateXY(ref double X, ref double Y, Window window = null)
        {
            var screen = SystemParameters.WorkArea;
            double mainX = Application.Current.MainWindow.Width;
            double mainY = Application.Current.MainWindow.Height;
            double dx = 0, dy = 0;
            if (window != Application.Current.MainWindow)
            {
                switch (AppDisplayLocation)
                {
                    case XClipperLocation.BottomRight:
                        dx = window.Width + 10;
                        break;
                    case XClipperLocation.BottomLeft:
                        dx = mainX + 10;
                        break;
                    case XClipperLocation.TopRight:
                        dx = window.Width + 10;
                        break;
                    case XClipperLocation.TopLeft:
                        dx = mainX + 10;
                        break;
                    case XClipperLocation.Center:
                        X = screen.Width / 2 - mainX / 2 + mainX + 10;
                        Y = screen.Height / 2 - mainY / 2;
                        return;
                }
            }

            switch (AppDisplayLocation)
            {
                case XClipperLocation.BottomRight:
                    X = screen.Right - mainX - dx - 10;
                    Y = screen.Bottom - mainY - 10;
                    break;
                case XClipperLocation.BottomLeft:
                    X = screen.Left + dx + 10;
                    Y = screen.Bottom - mainY - 10;
                    break;
                case XClipperLocation.TopRight:
                    X = screen.Right - mainX - dx - 10;
                    Y = screen.Top + dy + 10;
                    break;
                case XClipperLocation.TopLeft:
                    X = screen.Left + dx + 10;
                    Y = screen.Top + 10;
                    break;
                case XClipperLocation.Center:
                    X = screen.Width / 2 - window.Width / 2;
                    Y = screen.Height / 2 - window.Height / 2;
                    break;
            }
        }

        /// <summary>
        /// This will add or remove startup entry for the app.
        /// </summary>
        public static void SetAppStartupEntry()
        {
            if (StartOnSystemStartup)
                RegistryHelper.AddApplicationToStartup(ApplicationLocation);
            else
                RegistryHelper.RemoveApplicationFromStartup(ApplicationLocation);
        }

        /// <summary>
        /// This method will restart the application.
        /// </summary>
        public static void RestartApplication()
        {
            var file = Path.Combine(ApplicationDirectory, "restart.bat");
            var batch = $@"
@echo off
timeout 2
start XClipper.exe
DEL ""%~f0""";

            File.WriteAllText(file, batch);
            using (var p = new Process())
            {
                p.StartInfo.FileName = file;
                p.StartInfo.UseShellExecute = false;
                p.StartInfo.WorkingDirectory = BaseDirectory;
                p.StartInfo.CreateNoWindow = true;
                p.Start();
            }
            Application.Current.Shutdown();
        }

        [Obsolete("The method is of no use")]
        public static void ActivatePaidFeatures()
        {
            if (IsPurchaseDone && LicenseStrategy == LicenseType.Premium)
            {
                DatabaseMaxItem = FB_MAX_ITEM;
                DatabaseMaxItemLength = FB_MAX_LENGTH;
                DatabaseMaxConnection = FB_MAX_CONNECTION;
            }
        }

        public static void RunOnMainThread(Action block)
        {
            Dispatcher.CurrentDispatcher.Invoke(block);
        }
        public static string FormatText(string text)
        {
            var partText = Regex.Replace(text, @"^\s+$[\r\n]*", " ", RegexOptions.Multiline);
            return Regex.Replace(partText, @"[\s]{2,}", " ");
        }

        [DllImport("Shlwapi.dll", CharSet = CharSet.Auto)]
        //  public static extern Int32 StrFormatByteSize(long fileSize, [MarshalAs(UnmanagedType.LPTStr)] StringBuilder buffer, int bufferSize);
        static extern Int32 StrFormatByteSize(long fileSize, [MarshalAs(UnmanagedType.LPWStr)] StringBuilder buffer, int bufferSize);
        /// <summary>
        /// Return a file size created by the StrFormatByteSize API function. 
        /// </summary>
        public static string ToFileSizeApi(this long fileSize)
        {
            StringBuilder sb = new StringBuilder(20);
            StrFormatByteSize(fileSize, sb, 20);
            return sb.ToString();
        }

        public static string GetProductVersion(string exeFile)
        {
            return FileVersionInfo.GetVersionInfo(exeFile).ProductVersion;
        }

        /// <summary>
        /// Set current QR data which will be used by SettingsWindow to generate QR code details.
        /// </summary>
        /// <returns></returns>
        public static bool CreateCurrentQRData()
        {
            if (FirebaseCurrent == null || !BindDatabase)
            {
                QRData = null;
                return false;
            }
            string mobileAuthDetails = FirebaseCurrent.IsAuthNeeded ? $"true;{MobileAuth.ClientId}" : "false;";
            string encryptedPassword = FirebaseCurrent.IsEncrypted ? DatabaseEncryptPassword : ""; // If not encrypted set no password.
            QRData = new QRCodeData
            {
                UID = UniqueID,
                EncryptedData = ($"{FirebaseCurrent.AppId};{FirebaseCurrent.ApiKey};{FirebaseCurrent.Endpoint}" +
                $";{encryptedPassword};{mobileAuthDetails};{BindDatabase}")
                .EncryptBase64Common()
            };
            return true;
        }

        public static async Task DownloadFile(Uri url, string outImagePath)
        {
            using (HttpClient client = new HttpClient())
            {
                using (HttpResponseMessage response = await client.GetAsync(url).ConfigureAwait(false))
                using (Stream dataStream = await response.Content.ReadAsStreamAsync().ConfigureAwait(false))
                using (FileStream fileOutStream = File.Create(outImagePath))
                {
                    dataStream.Seek(0, SeekOrigin.Begin);
                    dataStream.CopyTo(fileOutStream);
                }
            }
        }

        public static bool IsUrl(string text)
        {
            return Regex.IsMatch(text, @"https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()!@:%_\+.~#?&\/\/=]*)");
        }

        public static void LaunchUrl(string url)
        {
            if (IsUrl(url))
            {
                Process.Start(url);
            }
        }

        public static bool IsHotKeysPressed(bool isAlt, bool isCtrl, bool isShift, string key = null)
        {
            bool execute = true;
            if (DefaultSettings.IsAlt == true) execute &= isAlt;
            if (DefaultSettings.IsCtrl == true) execute &= isCtrl;
            if (DefaultSettings.IsShift == true) execute &= isShift;
            if ((DefaultSettings.HotKey != key) && key != null) execute &= false;
            return execute;
        }
    }
}
