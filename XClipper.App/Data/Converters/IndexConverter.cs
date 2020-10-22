using System;
using System.Globalization;
using System.Windows.Controls;
using System.Windows.Data;

namespace Components
{
    [ValueConversion(typeof(ListViewItem), typeof(int))]
    public class IndexConverter : IValueConverter
    {
        public static IndexConverter Instance = new IndexConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            ListViewItem item = (ListViewItem)value;
            ListView listView = ItemsControl.ItemsControlFromItemContainer(item) as ListView;
            if (listView == null) return "";
            int index = listView.ItemContainerGenerator.IndexFromContainer(item);
            if (index < 9)
                return (index + 1).ToString();
            else if (index == 9)
                return 0.ToString();
            else
            return "";
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return Binding.DoNothing;
        }
    }
}
