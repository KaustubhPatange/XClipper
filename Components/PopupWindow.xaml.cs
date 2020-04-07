using ClipboardManager.models;
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
using System.Windows.Shapes;

namespace Components
{
    public partial class PopupWindow : Window
    {
        public PopupWindow()
        {
            InitializeComponent();

            var screen = System.Windows.SystemParameters.WorkArea;
            this.Left = screen.Right - 280 - this.Width - 20;
            this.Top = screen.Bottom - 450 - 10;
        }

        public void SetPopUp(TableCopy model)
        {
            _tbText.Text = model.Text;
            _tbDateTime.Text = model.DateTime;
        }


        private void Window_Deactivated(object sender, EventArgs e)
        {
         //   Hide();
        }

        private void Window_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Escape)
                Hide();
        }
    }
}
