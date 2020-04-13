using System.Windows;
using System.Windows.Controls;

namespace Components
{
    public static class ScrollViewerExtensions
    {
        /** Small note to say, this values are immutable */
        public static void HideVerticalScrollBar(this ScrollViewer viewer) => viewer.VerticalScrollBarVisibility = ScrollBarVisibility.Hidden; 
        public static void ShowVerticalScrollBar(this ScrollViewer viewer) => viewer.VerticalScrollBarVisibility = ScrollBarVisibility.Visible; 
       
    }
}
