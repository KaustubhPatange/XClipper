﻿using System.Text.RegularExpressions;
using static Components.DefaultSettings;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using ClipboardManager.models;
using static Components.LicenseHandler;
using System;
using static Components.Constants;

namespace Components
{
    public static class MainHelper
    {
        /** Return the DependencyObject if it is a ScrollViewer */
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
                switch(AppDisplayLocation)
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
                        Y = screen.Height /2 - mainY / 2;
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

        /** This will add or remove startup entry for the app. */
        public static void SetAppStartupEntry()
        {
            if (StartOnSystemStartup)
                RegistryHelper.AddApplicationToStartup(App.AppStartupLocation);
            else
                RegistryHelper.RemoveApplicationFromStartup(App.AppStartupLocation);
        }

        /** This method will check for license and activate some extra features. */
        public static void CheckForLicense()
        {
            IsPurchaseDone = IsActivated(new Uri(LicenseFilePath));
        }
        public static string FormatText(string text)
        {
            var partText = Regex.Replace(text, @"^\s+$[\r\n]*", " ", RegexOptions.Multiline);
            return Regex.Replace(partText, @"[\s]{2,}", " ");
        }
        public static int ParseNumericKey(Key e)
        {
            return Regex.Replace(e.ToString(), "[^0-9.]", "").ToInt();
        }

    }
}