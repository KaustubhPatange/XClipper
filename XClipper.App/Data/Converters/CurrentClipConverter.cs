using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using System.Windows.Media;

namespace Components
{
    [ValueConversion(typeof(string), typeof(SolidColorBrush))]
    public class CurrentClipConverter : IValueConverter
    {
        public static CurrentClipConverter Instance = new CurrentClipConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            try
            {
                var text = Clipboard.GetText();
                if (value as string == text)
                {
                    var color = Application.Current.Resources["CurrentClipBrush"] as SolidColorBrush;
                    return color;
                }
            }
            catch { }
            return new SolidColorBrush(Colors.White);
        }
        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => Binding.DoNothing;
    }
}
