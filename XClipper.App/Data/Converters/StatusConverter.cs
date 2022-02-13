using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using System.Windows.Media;
using static Components.DefaultSettings;
using static Components.LicenseHandler;

namespace Components
{
    public class StatusConverter : AbstractConverter<bool, SolidColorBrush>
    {
        public static StatusConverter Instance = new StatusConverter();
        public override SolidColorBrush Convert(bool value)
        {
            return value
                ? Application.Current.Resources["GreenBrush"] as SolidColorBrush
                : new SolidColorBrush(Colors.Red);
        }

        public override bool ConvertBack(SolidColorBrush value) => throw new NotImplementedException();
    }
}
