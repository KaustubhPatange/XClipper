using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using System.Windows.Media;

namespace Components
{
    [ValueConversion(typeof(bool), typeof(SolidColorBrush))]
    public class BackgroundConverter : IValueConverter
    {
        public static BackgroundConverter Instance = new BackgroundConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var pinned = (bool)value;
            if (pinned)
            {
                var color = Application.Current.Resources["PinnedBrush"] as SolidColorBrush;
                return color;
            }else
            {
                var color = Application.Current.Resources["AccentBrush"] as SolidColorBrush;
                return color;
            }
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
