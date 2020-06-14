using System;
using System.Globalization;
using System.IO;
using System.Windows.Data;

namespace Components
{
    [ValueConversion(typeof(string), typeof(bool))]
    public class FileVisibilityConverter : IValueConverter
    {
        public static FileVisibilityConverter Instance = new FileVisibilityConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return File.Exists(value as string);
        }
        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
