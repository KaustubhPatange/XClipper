using System.Windows;
using System.Windows.Controls;

namespace Components.Controls
{
    public partial class GroupSeparator : UserControl
    {
        public static readonly DependencyProperty HeaderProperty =
               DependencyProperty.RegisterAttached("Header", typeof(string), typeof(GroupSeparator), new PropertyMetadata(""));

        public static void SetHeader(GroupSeparator element, string text)
        {
            element.SetValue(HeaderProperty, text);
        }

        public static string GetHeader(GroupSeparator element) => element.GetValue(HeaderProperty) as string;
        public GroupSeparator()
        {
            InitializeComponent();
        }
    }
}
