
using System;
using System.Windows;
using System.Windows.Controls;

namespace Components
{
    public class MenuItemProperties
    {

        #region Title Text

        public static readonly DependencyProperty TitleProperty =
            DependencyProperty.RegisterAttached("Title", typeof(string), typeof(MenuItemProperties), new PropertyMetadata(""));

        public static void SetTitle(MenuItem item, string text)
        {
            item.SetValue(TitleProperty, text);
        }

        public static string GetTitle(MenuItem item) => (string)item.GetValue(TitleProperty);

        #endregion

        #region Shortcut Text

        public static readonly DependencyProperty ShortcutProperty =
            DependencyProperty.RegisterAttached("Shortcut", typeof(string), typeof(MenuItemProperties), new PropertyMetadata(""));

        public static void SetShortcut(MenuItem item, string text)
        {
            item.SetValue(ShortcutProperty, text);
        }

        public static string GetShortcut(MenuItem item) => (string)item.GetValue(ShortcutProperty);

        #endregion
    }
}
