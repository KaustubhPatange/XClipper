using System;
using System.Globalization;
using System.Windows.Data;
using static Components.DefaultSettings;

namespace Components
{
    [ValueConversion(typeof(int), typeof(string))]
    public class CustomSyncTooltipConverter : IValueConverter
    {
        public static CustomSyncTooltipConverter Instance = new CustomSyncTooltipConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (IsPurchaseDone)
                return $"{Translation.SYNC_GREATER_TEXT} {value}";
            else return $"{Translation.SYNC_GREATER_TEXT} {value}, upgrade to unlock more";
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
