using System.Windows;
using System.Windows.Controls;
using static Components.MainHelper;

namespace Components
{
    public class CustomListView : ListView
    {
        public ScrollViewer ScrollViewer
        {
            get { return GetScrollViewer(this) as ScrollViewer; }
        }

        public static readonly DependencyProperty ScrollViewerProperty =
            DependencyProperty.Register("ScrollViewer", typeof(ScrollViewer), typeof(CustomListView), new PropertyMetadata(null));
    }
}
