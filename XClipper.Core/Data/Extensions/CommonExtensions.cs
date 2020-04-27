
using System;
using System.Text.RegularExpressions;
using System.Windows;
using System.Windows.Media;

namespace Components
{
    public static class CommonExtensions
    {
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
       
        public enum ClipContentType
        {
            Text, Image, Files
        }

        public static string ToFormattedDateTime(this DateTime value, bool useHypens)
        {
            if (useHypens)
                return value.ToString("yyyy-MM-dd HH-mm-ss");
            else 
                return value.ToString("yyyyMMddHHmmss");
        }

        public static string ToFormattedDateTime(this DateTime value)
        {
            return value.ToFormattedDateTime(true);
        }


        public static string GetString(this ResourceDictionary t, string key) => (string)t[key];

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
    }
}
