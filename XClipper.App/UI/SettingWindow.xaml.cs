using QRCoder;
using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media.Imaging;

namespace Components
{
    /// <summary>
    /// Interaction logic for Settings.xaml
    /// </summary>
    public partial class SettingWindow : Window
    {
        public SettingWindow(ISettingEventBinder binder = null)
        {
            InitializeComponent();

            DataContext = new SettingViewModel().Also((v) => { v.SetSettingBinder(binder); });

            Closing += (o, e) =>
            {
                e.Cancel = true;
                Hide();
            };
        }
    }
}
