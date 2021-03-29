using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace Components
{
    public class UrlVisibilityConverter : IValueConverter
    {
        public static UrlVisibilityConverter Instance = new UrlVisibilityConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            string url = value as string;
            if (MainHelper.IsUrl(url))
                return Visibility.Visible;
            else
                return Visibility.Collapsed;
        }
        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => throw new NotImplementedException();
    }
}
