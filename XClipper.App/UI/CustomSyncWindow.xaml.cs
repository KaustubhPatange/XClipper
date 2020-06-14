using System;
using System.Globalization;
using System.IO;
using System.Windows;
using System.Windows.Data;

namespace Components.UI
{
    /// <summary>
    /// Interaction logic for CustomSyncWindow.xaml
    /// </summary>
    public partial class CustomSyncWindow : Window
    {
        public CustomSyncWindow()
        {
            InitializeComponent();

            DataContext = new CustomSyncViewModel();
        }

    }
}
