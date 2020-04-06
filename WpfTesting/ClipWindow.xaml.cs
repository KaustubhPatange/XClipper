using ClipboardManager.models;
using SQLite;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Windows;
using System.Windows.Media;
using WpfTesting.helpers;
using WpfTesting.viewModels;

namespace WpfTesting
{
    public partial class ClipWindow : Window, ClipBinder
    {
        private ClipWindowViewModel viewModel;
        private int lvIndex=-1;
        public ClipWindow()
        {
            InitializeComponent();

            viewModel = new ClipWindowViewModel().setBinder(this);

            var desktopWorkingArea = System.Windows.SystemParameters.WorkArea;
            this.Left = desktopWorkingArea.Right - this.Width;
            this.Top = desktopWorkingArea.Bottom - this.Height;

            
            this.Focus();
        }

        private void Window_IsVisibleChanged(object sender, DependencyPropertyChangedEventArgs e)
        {
            _lvClip.ItemsSource = viewModel.ClipData;
        }

        private void lvClip_PrevivewMouseLeftButtonDown(object sender, System.Windows.Input.MouseButtonEventArgs e)
        {
            Debug.WriteLine($"Mouse Clicked: {lvIndex}");
        }

        private void _lvClip_TargetUpdated(object sender, System.Windows.Data.DataTransferEventArgs e)
        {
            Debug.WriteLine($"Target Updated: {_lvClip.SelectedIndex}");
        }

        private void _lvClip_SelectionChanged(object sender, System.Windows.Controls.SelectionChangedEventArgs e)
        {
            lvIndex = _lvClip.SelectedIndex;
        }

        private void Window_KeyDown(object sender, System.Windows.Input.KeyEventArgs e)
        {
            Debug.WriteLine(e.Key.ToString());
            if (e.Key == System.Windows.Input.Key.Down && (!_lvClip.IsFocused || _lvClip.SelectedIndex == _lvClip.Items.Count - 1))
            {
                _lvClip.Focus();
                _lvClip.SelectedIndex = 0;
                _lvClip.ScrollIntoView(_lvClip.SelectedItem);
            }
        }
    }
}
