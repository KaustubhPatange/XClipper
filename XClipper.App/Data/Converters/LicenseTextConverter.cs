using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace Components
{
    [ValueConversion(typeof(LicenseType), typeof(Visibility))]
    public class LicenseTextConverter : IValueConverter
    {
        public static LicenseTextConverter Instance = new LicenseTextConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if ((LicenseType)value == LicenseType.Premium)
                return Visibility.Hidden;
            else 
                return Visibility.Visible;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
