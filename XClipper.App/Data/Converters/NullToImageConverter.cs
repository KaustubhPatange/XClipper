using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace Components
{
    /**
     * This converter is used to fix the ClipWindow's Image component which receives the "null" value when initialized.
     * Upon receiving null (which is not an image) it throws this exception. To resolve if we pass DependencyProperty.UnsetValue
     * 
     * Fixed exception:
     * System.Windows.Data Error: 23 : Cannot convert '<null>' from type '<null>' to type 'System.Windows.Media.ImageSource' for 'en-US' culture with default conversions;
     */
    [ValueConversion(typeof(string), typeof(string))]
    public class NullToImageConverter : IValueConverter
    {
        public static NullToImageConverter Instance = new NullToImageConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (value == null)
                return DependencyProperty.UnsetValue;
            return value;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return Binding.DoNothing;
        }
    }
}
