using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using System.Windows.Media;

namespace Components
{
    [ValueConversion(typeof(bool), typeof(SolidColorBrush))]
    public class ForegroundTextBlockConverter : IValueConverter
    {
        public static ForegroundTextBlockConverter Instance = new ForegroundTextBlockConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var activated = (bool)value;
            if (activated)
            {
                var color = Application.Current.Resources["GreenBrush"] as SolidColorBrush;
                return color;
            }
            else
            {
                var color = Application.Current.Resources["ErrorBrush"] as SolidColorBrush;
                return color;
            }
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
