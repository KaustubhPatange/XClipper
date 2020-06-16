using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace Components
{
    public partial class BuyWindow : Window
    {
        public BuyWindow()
        {
            InitializeComponent();

            this.DataContext = new BuyViewModel();
        }
    }
}
