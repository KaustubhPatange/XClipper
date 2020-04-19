using System.Windows;
using System.Windows.Controls;
using static Components.MainHelper;

namespace Components
{
    public class CustomListView : ListView
    {
        public ScrollViewer GetScrollViewer
        {
            get { return GetScrollViewer(this) as ScrollViewer; }
        }

        public static readonly DependencyProperty ScrollViewerProperty =
            DependencyProperty.Register("GetScrollViewer", typeof(ScrollViewer), typeof(CustomListView), new PropertyMetadata(null));
    }
}
