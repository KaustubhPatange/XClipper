using System;
using System.Globalization;
using System.Windows.Data;

namespace Components
{
    /// <summary>
    /// A type-safe converter to convert <see cref="F"/> to <see cref="T"/>
    /// </summary>
    /// <typeparam name="F"></typeparam>
    /// <typeparam name="T"></typeparam>
    public abstract class AbstractConverter<F, T> : IValueConverter
    {
        public abstract T Convert(F value);
        public abstract F ConvertBack(T value);

        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (value is F)
                return Convert((F)value);
            return null;
        }
        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (value is T)
                return ConvertBack((T)value);
            return null;
        }
    }
}
