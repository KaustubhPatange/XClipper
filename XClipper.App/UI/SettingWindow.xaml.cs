using System;
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

        protected override void OnSourceInitialized(EventArgs e)
        {
            base.OnSourceInitialized(e);
            
            updateFbPasswordComponent();
            bindComponents();
        }

        private void bindComponents()
        {
            _togglefbPasswordChange.Checked += (sender, args) => updateFbPasswordComponent();
            _togglefbPasswordChange.Unchecked += (sender, args) => updateFbPasswordComponent();
            _fbPasswordBox.PasswordChanged += (sender, args) =>
            {
                _fbPasswordTextBox.Text = _fbPasswordBox.Password;
            };
        }

        private void updateFbPasswordComponent()
        {
            _fbPasswordBox.Password = _fbPasswordTextBox.Text;
            if (!viewModel.ShowFirebasePassword)
            {
                _fbPasswordTextBox.Visibility = Visibility.Collapsed;
                _fbPasswordBox.Visibility = Visibility.Visible;
            }
            else
            {
                _fbPasswordTextBox.Visibility = Visibility.Visible;
                _fbPasswordBox.Visibility = Visibility.Collapsed;
            }
        }
    }
}
