using System;
using System.Globalization;
using System.Windows.Data;

namespace Components
{

    [ValueConversion(typeof(LicenseType), typeof(bool))]
    public class ActivateButtonConverter : IValueConverter
    {
        public static ActivateButtonConverter Instance = new ActivateButtonConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if ((LicenseType)value == LicenseType.Premium)
                return false;
            else return true;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
