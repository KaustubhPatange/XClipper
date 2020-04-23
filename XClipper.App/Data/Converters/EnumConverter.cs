using System;
using System.Globalization;
using System.Windows.Data;
using System.Linq;
using static Components.EnumHelper;

namespace Components
{
    [ValueConversion(typeof(Enum), typeof(string[]))]
    public class EnumConverter : IValueConverter
    {
        public static EnumConverter Instance = new EnumConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            string[] p = Enum.GetValues(value.GetType()) as string[];
            return p;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
