using System;
using System.Text.RegularExpressions;
using System.Windows.Controls;
using System.Windows.Input;
using static Components.NumericTextBoxProperties;


namespace Components
{
    public class NumericTextBox : TextBox
    {
        protected override void OnPreviewTextInput(TextCompositionEventArgs e)
        {
            // false means to provoke this text
            // true means to retain this text

            int max = Convert.ToInt32(GetValue(MaxNumberProperty));
            int min = Convert.ToInt32(GetValue(MinNumberProperty));

            Regex regex = new Regex("[0-9]+");
            if (regex.IsMatch(e.Text))
            {
                string currentValue;
                if (string.IsNullOrWhiteSpace(Text))
                    currentValue = "0";
                else 
                    currentValue = Text;

                int value = (currentValue + e.Text).ToInt();
                if (value >= min && value <= max)
                    e.Handled = false;
                else
                    e.Handled = true;
            }
            else e.Handled = true;
            base.OnPreviewTextInput(e);
        }
    }
}
