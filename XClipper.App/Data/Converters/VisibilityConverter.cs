using System;
using System.Globalization;
using System.Runtime.Remoting;
using System.Windows;
using System.Windows.Data;

namespace Components
{
    [ValueConversion(typeof(bool), typeof(Visibility))]
    public class VisibilityConverter : IValueConverter
    {
        public static VisibilityConverter Instance = new VisibilityConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var obj = (bool)value;
            if (obj)
                return Visibility.Visible;
            else
                return Visibility.Collapsed;

        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
