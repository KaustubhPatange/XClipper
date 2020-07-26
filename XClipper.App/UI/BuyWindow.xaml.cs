using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace Components
{
    public partial class BuyWindow : Window
    {
        public BuyWindow(IBuyEventBinder binder)
        {
            InitializeComponent();

            this.DataContext = new BuyViewModel(binder);
        }
    }
}
