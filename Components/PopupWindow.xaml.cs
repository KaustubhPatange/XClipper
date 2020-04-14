using ClipboardManager.models;
using Components.viewModels;
using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Shapes;
using System.Windows.Threading;
using static Components.MainHelper;

namespace Components
{
    /** Same reason as ClipWindow class, can't use View Model here */
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

            var screen = SystemParameters.WorkArea;
            this.Left = screen.Right - 285 - this.Width - 20;
            this.Top = screen.Bottom - 450 - 10;

        }

        #endregion


        #region UI Events

        #region Unlocalised
        private void Window_Deactivated(object sender, EventArgs e)
        {
            //   Hide();
        }

        private void EditButton_Clicked(object sender, RoutedEventArgs e)
        {
            ToggleEditMode();
        }

        private void ScrollViewer_Loaded(object sender, RoutedEventArgs e)
        {
            var color = Application.Current.Resources["BackgroundBrush"] as SolidColorBrush;
            ((Rectangle)_scrollViewer.Template.FindName("Corner", _scrollViewer)).Fill = color;
        }

        private void PreviewButton_Clicked(object sender, RoutedEventArgs e)
        {
            Process.Start(model.ImagePath);
        }
        private void CloseButton_Clicked(object sender, RoutedEventArgs e)
        {
            CloseWindow();
        }

        #endregion

        #region Key Binds
        private void Window_KeyDown(object sender, KeyEventArgs e)
        {
            Debug.WriteLine("Popup Key Press: " + e.Key.ToString());
            if (e.Key == Key.Escape)
            {
                CloseWindow();
            }

            /** By using debug console I found out that Pressing Space on main Window transfer keydown event
             *  to this window. Hence we also get same Space key press fired up here as well. 
             */
            if (e.Key == Key.Space) _tbFocusText.Hide();

            // This key bind will open the image in default image view application
            if ((e.Key == Key.Return || e.Key == Key.Enter) && model.ContentType == ContentType.Image)
            {
                Process.Start(model.ImagePath);
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
                ToggleEditMode();
        }

        #endregion

        #endregion


        #region UI Handling Functions

        /** This function will setup supplied model with this window. */
        public void SetPopUp(TableCopy model)
        {
            this.model = model;
            switch (model.ContentType)
            {
                case ContentType.Text:
                    _tbText.Text = model.RawText;
                    CommonTextFiles();
                    break;
                case ContentType.Image:
                    _tbText.Collapsed();
                    _imgView.Visible();
                    _toggleEditButton.Collapsed();
                    _btnPreview.Visible();

                    _imgView.Source = (new ImageSourceConverter()).ConvertFromString(model.ImagePath) as ImageSource;
                    break;
                case ContentType.Files:
                    var builder = new StringBuilder();
                    model.LongText.Split(',').ToList().ForEach((line) => builder.Append(line).Append(Environment.NewLine));
                    _tbText.Text = builder.ToString().Trim();

                    _toggleEditButton.Collapsed();

                    CommonTextFiles();
                    break;
            }
            _tbDateTime.Text = model.DateTime;
            _tbFocusText.Visible();
        }

        private void CommonTextFiles()
        {
            _btnPreview.Collapsed();
            _tbText.Visible();
            _imgView.Collapsed();
            _toggleEditButton.Visible();
        }

        /** This will set TextBox editable based on the toggle button. */
        private void ToggleEditMode()
        {
            // Only text content is supported, otherwise return.
            if (model.ContentType == ContentType.Image)
            {
                ShowToast("Editing is not supported for this format", true);
                return;
            }
            if (_toggleEditButton.IsChecked == false)
            {
                SetEditMode();
                _toggleEditButton.IsChecked = true;
            }
            else
            {
                SetStopEditMode();
                _toggleEditButton.IsChecked = false;
            }
        }

        /** Handles to close edit mode, also performs save operations. */
        private void SetStopEditMode()
        {
            if (SAVED_TEXT != _tbText.Text)
            {
                // Perform a save operation...

                if (string.IsNullOrWhiteSpace(_tbText.Text))
                {
                    ShowToast("Error: Cannot set a blank text", true);
                    return;
                }

                switch (model.ContentType)
                {
                    // Save operation for Text files
                    case ContentType.Text:
                        model.Text = model.LongText = FormatText(_tbText.Text);
                        model.RawText = _tbText.Text;
                        break;
                    case ContentType.Files:
                        var fileContentOK = true;
                        var lines = _tbText.Text.ToLines();
                        var i = 0;
                        foreach (string line in lines)
                            if (!File.Exists(line)) { Debug.WriteLine($"Error ({i}): " + line); fileContentOK = false; break; } else i++; 
                        if (fileContentOK)
                        {
                            model.Text = $"Copied Files - {lines.Length}";
                            model.LongText = string.Join(",", lines);
                            model.RawText = "";
                        }
                        else
                        {
                            _tbText.Text = SAVED_TEXT;
                            ShowToast("Error: Invalid file path(s)", true);
                        }
                        break;
                }
                AppSingleton.GetInstance.UpdateData(model);
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
            {
                var color = Application.Current.Resources["ErrorBrush"] as SolidColorBrush;
                _popUpPanel.Background = color;
            }
            else
            {
                var color = Application.Current.Resources["ForegroundBrush"] as SolidColorBrush;
                _popUpPanel.Background = color;
            }
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
                ToggleEditMode();
            _popUpMenu.IsOpen = false;
            Hide();
        }

        #endregion

    }
}
