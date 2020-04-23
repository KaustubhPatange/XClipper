using System.ComponentModel;
using System.Diagnostics;
using System.Windows;
using System.Windows.Input;

namespace Components
{
    /// <summary>
    /// Interaction logic for Settings.xaml
    /// </summary>
    public partial class SettingWindow : Window
    {
        public SettingWindow()
        {
            InitializeComponent();

            DataContext = new SettingViewModel();

            Closing += (o, e) => {
                e.Cancel = true;
                Hide();
            };

        }
    }
}
