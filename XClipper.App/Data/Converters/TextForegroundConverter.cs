using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using System.Windows.Media;

namespace Components
{
    [ValueConversion(typeof(bool), typeof(SolidColorBrush))]
    public class TextForegroundConverter : IValueConverter
    {
        public static TextForegroundConverter Instance = new TextForegroundConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var IsEnabled = (bool)value;
            if (!IsEnabled)
                return Brushes.Gray;
            else
                return Brushes.Black;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
