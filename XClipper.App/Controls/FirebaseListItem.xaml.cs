using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace Components.Controls
{
    public partial class FirebaseListItem : UserControl
    {
        public FirebaseListItem()
        {
            InitializeComponent();
        }

        #region Item Max Value Property

        public static readonly DependencyProperty ItemMaxValueProperty =
            DependencyProperty.RegisterAttached("ItemMaxValue", typeof(int), typeof(FirebaseListItem), new PropertyMetadata(0));
        public static void SetItemMaxValue(FirebaseListItem element, int value) => element.SetValue(ItemMaxValueProperty, value);
        public static string GetItemMaxValue(FirebaseListItem element) => (string)element.GetValue(ItemMaxValueProperty);

        #endregion

        #region Item Value Property

        public static readonly DependencyProperty ItemValueProperty =
            DependencyProperty.RegisterAttached("ItemValue", typeof(int), typeof(FirebaseListItem), new PropertyMetadata(0));
        public static void SetItemValue(FirebaseListItem element, int value) => element.SetValue(ItemValueProperty, value);
        public static string GetItemValue(FirebaseListItem element) => (string)element.GetValue(ItemValueProperty);

        #endregion

        #region Title Property

        public static readonly DependencyProperty TitleProperty =
            DependencyProperty.RegisterAttached("Title", typeof(string), typeof(FirebaseListItem), new PropertyMetadata(""));

        public static void SetTitle(FirebaseListItem element, string Text)
        {
            element.SetValue(TitleProperty, Text);
        }

        public static string GetTitle(FirebaseListItem element) => (string)element.GetValue(TitleProperty);

        #endregion
    }
}
