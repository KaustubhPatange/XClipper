using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using System.Windows.Media;

namespace Components
{
    [ValueConversion(typeof(bool), typeof(SolidColorBrush))]
    public class ForegroundConverter : IValueConverter
    {
        public static ForegroundConverter Instance = new ForegroundConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var pinned = (bool)value;
            if (pinned)
            {
                var color = Application.Current.Resources["BackgroundBrush"] as SolidColorBrush;
                return color;
            }
            else
            {
                var color = Colors.WhiteSmoke;
                return new SolidColorBrush(color);
            }
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
