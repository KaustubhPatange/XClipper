using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;

namespace Components
{
    public static class MainHelper
    {
        public static string AppBackground = "#2D2D30";

        /// <summary>
        /// Extension method to return an enum value of type T for the given string.
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="value"></param>
        /// <returns></returns>
        public static T ToEnum<T>(this string value)
        {
            return (T)Enum.Parse(typeof(T), value, true);
        }
        public static SolidColorBrush GetColor(this string hexColor)
        {
            return (SolidColorBrush)(new BrushConverter().ConvertFrom(hexColor));
        }
        public static int ToInt(this string s) => Convert.ToInt32(s);
        public enum ClipContentType
        {
            Text, Image, Files

        }
        public static bool IsOpened(this Window window)
        {
            return Application.Current.Windows.Cast<Window>().Any(x => x.GetHashCode() == window.GetHashCode());
        }
        public static T GetFrameworkElementByName<T>(FrameworkElement referenceElement) where T : FrameworkElement
        {
            FrameworkElement child = null;
            for (Int32 i = 0; i < VisualTreeHelper.GetChildrenCount(referenceElement); i++)
            {
                child = VisualTreeHelper.GetChild(referenceElement, i) as FrameworkElement;
                if (child != null && child.GetType() == typeof(T))
                { break; }
                else if (child != null)
                {
                    child = GetFrameworkElementByName<T>(child);
                    if (child != null && child.GetType() == typeof(T))
                    {
                        break;
                    }
                }
            }
            return child as T;
        }

        public static string[] ToLines(this string text) => Regex.Split(text, "\r\n|\r|\n");
        public static bool isCtrlPressed() => (Keyboard.IsKeyDown(Key.RightCtrl) || Keyboard.IsKeyDown(Key.LeftCtrl));
        public static bool isShitPressed() => (Keyboard.IsKeyDown(Key.LeftShift) || Keyboard.IsKeyDown(Key.RightShift));

        public static bool isNumericKeyPressed(Key e) => ((e >= Key.D0 && e <= Key.D9) || (e >= Key.NumPad0 && e <= Key.NumPad9));

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
