using System.Windows;

namespace Components
{
    /// <summary>
    /// Interaction logic for Settings.xaml
    /// </summary>
    public partial class SettingWindow : Window
    {
        private SettingViewModel viewModel = new SettingViewModel();
        public SettingWindow(ISettingEventBinder binder = null)
        {
            InitializeComponent();

            viewModel.SetSettingBinder(binder);
            DataContext = viewModel;

            Closing += (o, e) =>
            {
                if (!viewModel.VerifyUnsavedSettings())
                {
                    e.Cancel = true;
                }
            };
        }
    }
}
