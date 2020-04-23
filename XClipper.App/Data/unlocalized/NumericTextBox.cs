using System;
using System.Linq;
using System.Text.RegularExpressions;
using System.Windows.Controls;
using System.Windows.Input;

namespace Components
{
    public class NumericTextBox : TextBox
    {
        protected override void OnPreviewTextInput(TextCompositionEventArgs e)
        {
            Regex regex = new Regex("[^0-9]+");
            e.Handled = regex.IsMatch(e.Text);
            base.OnPreviewTextInput(e);
        }
        //protected override void OnPreviewKeyDown(KeyEventArgs e)
        //{
        //    e.Handled = !TextIsNumeric(Text);
        //    base.OnPreviewKeyDown(e);
        //}
        //private bool TextIsNumeric(string input)
        //{
        //    return input.All(c => Char.IsDigit(c) || Char.IsControl(c));
        //}
    }
}
