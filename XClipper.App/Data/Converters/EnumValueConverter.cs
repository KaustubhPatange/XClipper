using System;
using System.Globalization;
using System.Windows.Data;

namespace Components
{
    [ValueConversion(typeof(Enum), typeof(string))]
    public class EnumValueConverter : IValueConverter
    {
        public static EnumValueConverter Instance = new EnumValueConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return EnumHelper.GetEnumDescription((XClipperLocation)value);
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
