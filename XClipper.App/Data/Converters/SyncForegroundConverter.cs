using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using System.Windows.Media;
using static Components.DefaultSettings;
using static Components.LicenseHandler;
using static Components.TranslationHelper;

namespace Components
{
    [ValueConversion(typeof(string), typeof(SolidColorBrush))]
    public class SyncIdForegroundConverter : IValueConverter
    {
        public static SyncIdForegroundConverter Instance = new SyncIdForegroundConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (UniqueID == UNIQUE_ID)
                return Application.Current.Resources["GreenBrush"] as SolidColorBrush;
            else
                return Application.Current.Resources["ErrorBrush"] as SolidColorBrush;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }

    [ValueConversion(typeof(string), typeof(string))]
    public class SyncIdTextConverter : IValueConverter
    {
        public static SyncIdTextConverter Instance = new SyncIdTextConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (UniqueID == UNIQUE_ID)
                return Translation.SYNC_ID_DEFAULT;
            else
                return Translation.SYNC_ID_CUSTOM;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
