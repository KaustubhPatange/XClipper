
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Imaging;

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

        public static string GetName(this Enum value)
        {
            return Enum.GetName(value.GetType(), value);
        }
        public static SolidColorBrush GetColor(this string hexColor)
        {
            return (SolidColorBrush)(new BrushConverter().ConvertFrom(hexColor));
        }

        public enum ClipContentType
        {
            Text, Image, Files
        }

        /// <summary>
        /// Creates a date format with yyyy-MM-dd HH-mm-ss if <paramref name="useHyphens"/> is true.<br/>
        /// Creates a date format with yyyyMMddHHmmss if <paramref name="useHyphens"/> is false.
        /// </summary>
        /// <param name="value"></param>
        /// <param name="useHyphens"></param>
        /// <returns></returns>
        public static string ToFormattedDateTime(this DateTime value, bool useHyphens)
        {
            if (useHyphens)
                return value.ToString("yyyy-MM-dd HH-mm-ss");
            else
                return value.ToString("yyyyMMddHHmmss");
        }

        /// <summary>
        /// Returns the date with yyyy-MM-dd format.
        /// </summary>
        /// <param name="value"></param>
        /// <returns></returns>
        public static string ToFormattedDate(this DateTime value)
        {
            return value.ToString("yyyy-MM-dd");
        }

        /// <summary>
        /// Calls <see cref="ToFormattedDateTime(DateTime, bool)"/> with hyphens yyyy-MM-dd HH-mm-ss
        /// </summary>
        /// <param name="value"></param>
        /// <returns></returns>
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


        public static BitmapImage ToImageSource(this Bitmap bitmap)
        {
            using (MemoryStream memory = new MemoryStream())
            {
                bitmap.Save(memory, System.Drawing.Imaging.ImageFormat.Bmp);
                memory.Position = 0;
                BitmapImage bitmapimage = new BitmapImage();
                bitmapimage.BeginInit();
                bitmapimage.StreamSource = memory;
                bitmapimage.CacheOption = BitmapCacheOption.OnLoad;
                bitmapimage.EndInit();

                return bitmapimage;
            }
        }

        public static async Task<TResult> TimeoutAfter<TResult>(this Task<TResult> task, TimeSpan timeout)
        {
            using (var timeoutCancellationTokenSource = new CancellationTokenSource())
            {
                var completedTask = await Task.WhenAny(task, Task.Delay(timeout, timeoutCancellationTokenSource.Token));
                if (completedTask == task)
                {
                    timeoutCancellationTokenSource.Cancel();
                    return await task;  // Very important in order to propagate exceptions
                }
                else
                {
                    throw new TimeoutException("The operation has timed out.");
                }
            }
        }
    }
}
