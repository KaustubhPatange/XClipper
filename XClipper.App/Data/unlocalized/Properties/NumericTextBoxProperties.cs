using System.Windows;

namespace Components
{
    public class NumericTextBoxProperties
    {

        #region Minimum Value 

        public static readonly DependencyProperty MinNumberProperty =
         DependencyProperty.RegisterAttached("MinNumber", typeof(int), typeof(NumericTextBoxProperties), new PropertyMetadata(0));

        public static void SetMinNumber(NumericTextBox item, int text)
        {
            item.SetValue(MinNumberProperty, text);
        }
        public static int GetMinNumber(NumericTextBox item) => (int)item.GetValue(MinNumberProperty);

        #endregion


        #region Maximum Value 

        public static readonly DependencyProperty MaxNumberProperty =
         DependencyProperty.RegisterAttached("MaxNumber", typeof(int), typeof(NumericTextBoxProperties), new PropertyMetadata(0));

        public static void SetMaxNumber(NumericTextBox item, int text)
        {
            item.SetValue(MaxNumberProperty, text);
        }
        public static int GetMaxLength(NumericTextBox item) => (int)item.GetValue(MaxNumberProperty);

        #endregion
    }
}
