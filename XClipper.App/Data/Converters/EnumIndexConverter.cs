using System;
using System.Globalization;
using System.Windows.Data;

namespace Components
{
    [ValueConversion(typeof(Enum), typeof(int))]
    public class EnumIndexConverter : IValueConverter
    {
        public static EnumIndexConverter Instance = new EnumIndexConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return Array.IndexOf(Enum.GetValues(value.GetType()), value);
        }
        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (Enum)(Enum.GetValues(targetType)).GetValue((int)value);
        }
    }
}
