using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Data;
using static Components.App;

namespace Components
{
    [ValueConversion(typeof(bool), typeof(string))]
    public class ContentTextblockConverter : IValueConverter
    {
        public static ContentTextblockConverter Instance = new ContentTextblockConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            bool activated = (bool)value;
            if (activated)
                return rm.GetString("buy_is_activate");
            else
                return rm.GetString("buy_not_activate");
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
