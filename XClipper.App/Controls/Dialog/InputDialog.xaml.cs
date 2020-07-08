using System.Windows;

#nullable enable

namespace Components.Controls.Dialog
{
    public partial class InputDialog : Window
    {
        public static string? Text;
        protected InputDialog()
        {
            InitializeComponent();
        }

        private void CancelButton_Click(object sender, RoutedEventArgs e)
        {
            DialogResult = false;
            Close();
        }
        private void OKButton_Click(object sender, RoutedEventArgs e)
        {
            DialogResult = true;
            Text = tbText.Text;
            Close();
        }

        public class Builder
        {
            private InputDialog dialog = new InputDialog();
            public Builder SetTitle(string value)
            {
                dialog.Title = value;
                return this;
            }
            public Builder SetTopMost(bool value)
            {
                dialog.Topmost = value;
                return this;
            }
            public Builder SetMessage(string value)
            {
                dialog.tbMsg.Text = value;
                return this;
            }
            public string? Show()
            {
                if (dialog.ShowDialog() == true)
                    return Text;
                else 
                    return null;
            }
        }
    }
}
