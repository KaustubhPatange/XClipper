using System.Windows;
using System.Windows.Controls;

namespace Components
{
    public static class VisibilityExtensions
    {
        #region StackPanel
        public static void Hide(this StackPanel t)
        {
            t.Visibility = Visibility.Hidden;
        }
        public static void Visible(this StackPanel t)
        {
            t.Visibility = Visibility.Visible;
        }
        public static void Collapsed(this StackPanel t)
        {
            t.Visibility = Visibility.Collapsed;
        }

        #endregion

        #region Grid
        public static void Hide(this Grid t)
        {
            t.Visibility = Visibility.Hidden;
        }
        public static void Visible(this Grid t)
        {
            t.Visibility = Visibility.Visible;
        }
        public static void Collapsed(this Grid t)
        {
            t.Visibility = Visibility.Collapsed;
        }

        #endregion

        #region TextBlock
        public static void Hide(this TextBlock t)
        {
            t.Visibility = Visibility.Hidden;
        }
        public static void Visible(this TextBlock t)
        {
            t.Visibility = Visibility.Visible;
        }
        public static void Collapsed(this TextBlock t)
        {
            t.Visibility = Visibility.Collapsed;
        }

        #endregion

        #region TextBox
        public static void Hide(this TextBox t)
        {
            t.Visibility = Visibility.Hidden;
        }
        public static void Visible(this TextBox t)
        {
            t.Visibility = Visibility.Visible;
        }
        public static void Collapsed(this TextBox t)
        {
            t.Visibility = Visibility.Collapsed;
        }

        #endregion

        #region Image
        public static void Hide(this Image t)
        {
            t.Visibility = Visibility.Hidden;
        }
        public static void Visible(this Image t)
        {
            t.Visibility = Visibility.Visible;
        }
        public static void Collapsed(this Image t)
        {
            t.Visibility = Visibility.Collapsed;
        }

        #endregion

        #region Control
        public static void Hide(this Control t)
        {
            t.Visibility = Visibility.Hidden;
        }
        public static void Visible(this Control t)
        {
            t.Visibility = Visibility.Visible;
        }
        public static void Collapsed(this Control t)
        {
            t.Visibility = Visibility.Collapsed;
        }

        #endregion
    }
}
