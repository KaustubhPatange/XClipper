using ClipboardManager.models;
using Components.viewModels;
using System;
using System.Collections.Generic;
using System.Diagnostics;
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
using System.Windows.Threading;
using static Components.MainHelper;

namespace Components
{
    public partial class PopupWindow : Window
    {
        #region Variable Definitions

        private string SAVED_TEXT;
        private DispatcherTimer popUpTimer;
        private TableCopy model;

        #endregion


        #region Constructor
        public PopupWindow()
        {
            InitializeComponent();

            var screen = System.Windows.SystemParameters.WorkArea;
            this.Left = screen.Right - 280 - this.Width - 20;
            this.Top = screen.Bottom - 450 - 10;
        }

        #endregion


        #region UI Events
        private void Window_Deactivated(object sender, EventArgs e)
        {
            //   Hide();
        }

        private void Window_KeyDown(object sender, KeyEventArgs e)
        {
            Debug.WriteLine("Popup Key Press: " + e.Key.ToString());
            if (e.Key == Key.Escape)
            {
                CloseWindow();
            }
            if (e.Key == Key.Down)
                _scrollViewer.ScrollToVerticalOffset(_scrollViewer.VerticalOffset + 30);
            if (e.Key == Key.Up)
                _scrollViewer.ScrollToVerticalOffset(_scrollViewer.VerticalOffset - 30);
            if (e.Key == Key.Right)
                _scrollViewer.ScrollToHorizontalOffset(_scrollViewer.HorizontalOffset + 30);
            if (e.Key == Key.Left)
                _scrollViewer.ScrollToHorizontalOffset(_scrollViewer.HorizontalOffset - 30);
            if (e.Key == Key.E && isCtrlPressed())
                ToggleEditMode(true);
        }

        private void EditButton_Clicked(object sender, RoutedEventArgs e)
        {
            ToggleEditMode(false);
        }

        private void ScrollViewer_Loaded(object sender, RoutedEventArgs e)
        {
            ((Rectangle)_scrollViewer.Template.FindName("Corner", _scrollViewer)).Fill = "#2D2D30".GetColor();
        }


        private void CloseButton_Clicked(object sender, RoutedEventArgs e)
        {
            CloseWindow();
        }

        #endregion


        #region UI Handling Functions

        /** This function will setup supplied model with this window. */
        public void SetPopUp(TableCopy model)
        {
            this.model = model;
            _tbText.Text = model.Text;
            _tbDateTime.Text = model.DateTime;
        }

        /** This will set TextBox editable based on the toggle button. */
        private void ToggleEditMode(bool IsInvokeFromShortcut)
        {
            // Only text content is supported, otherwise return.
            if (model.ContentType != "Text")
            {
                ShowToast("Editing is not supported for this format", true);
                return;
            }
            if (_toggleEditButton.IsChecked == false)
            {
                SetEditMode();
               if (IsInvokeFromShortcut) _toggleEditButton.IsChecked = true;
            }
            else
            {
                SetStopEditMode();
                if (IsInvokeFromShortcut) _toggleEditButton.IsChecked = false;
            }
        }

        /** Handles to close edit mode, also performs save operations. */
        private void SetStopEditMode()
        {
            if (SAVED_TEXT != _tbText.Text)
            {
                // Perform a save operation...
                model.Text = _tbText.Text;
                ClipWindowViewModel.GetInstance.UpdateData(model);
            }
            _tbText.IsReadOnly = true;
            _scrollViewer.BorderThickness = new Thickness(0);
            Keyboard.ClearFocus();
            _scrollViewer.Focus();
        }

        /** Set the textbox editable. */
        private void SetEditMode()
        {
            SAVED_TEXT = _tbText.Text;
            _tbText.IsReadOnly = false;
            _scrollViewer.BorderThickness = new Thickness(0.5);
            _tbText.SelectionStart = 0;
            _tbText.Focus();
        }

        /** This will show the toast at the bottom of window. */
        private void ShowToast(string message, bool error = false)
        {
            if (_popUpMenu.IsOpen)
            {
                popUpTimer.Stop();
            }
            _popUpMenu.IsOpen = true;
            if (error)
                _popUpPanel.Background = "#8E1400".GetColor();
            else
                _popUpPanel.Background = "#3F3F46".GetColor();
            _popUpText.Text = message;
            popUpTimer = new DispatcherTimer { Interval = TimeSpan.FromSeconds(2) };
            popUpTimer.Start();
            popUpTimer.Tick += (sender, args) =>
            {
                _popUpMenu.IsOpen = false;
                popUpTimer.Stop();
            };
        }

        /** Handles the close of the window. */
        private void CloseWindow()
        {
            if (_toggleEditButton.IsChecked == true)
                ToggleEditMode(true);
            _popUpMenu.IsOpen = false;
            Hide();
        }

        #endregion

    }
}
