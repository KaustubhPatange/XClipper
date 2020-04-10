using ClipboardManager.models;
using SQLite;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Windows;
using System.Windows.Media;
using Components.viewModels;
using System.Threading.Tasks;
using System.Threading;
using System.Windows.Input;
using System.Windows.Controls;
using System.Runtime.InteropServices;
using System.Windows.Interop;
using System.Text;
using static Components.MainHelper;

namespace Components
{
    public partial class ClipWindow : Window, ClipBinder
    {
        #region Variablel Definition
        
        private PopupWindow _popupWindow;
        private MaterialMessage _materialMsgBox;
        private int lvIndex = -1;

        #endregion


        #region Constructor

        /// <summary>
        /// The main constructor of the window
        /// </summary>
        public ClipWindow()
        {
            InitializeComponent();

            ClipWindowViewModel.GetInstance.setBinder(this);
            _popupWindow = new PopupWindow();

            var screen = SystemParameters.WorkArea;
            this.Left = screen.Right - this.Width - 10;
            this.Top = screen.Bottom -  this.Height - 10;

            // Focus on the search editbox at start
            _tbSearchBox.Focus();
        }

        #endregion


        #region UI Events
        private async void CloseButtonClick(object sender, RoutedEventArgs e)
        {
            await Task.Run(() =>
            {
                Thread.Sleep(400);
            });
            Close();
        }

        private void SearchTextChanged(object sender, TextChangedEventArgs e)
        {
            if (!string.IsNullOrWhiteSpace(_tbSearchBox.Text))
            {
                _lvClip.ItemsSource = ClipWindowViewModel.GetInstance.FilterData(_tbSearchBox.Text);
            }
            else _lvClip.ItemsSource = ClipWindowViewModel.GetInstance.ClipData;
        }

        private void ListViewItemDoubleClicked(object sender, MouseButtonEventArgs e)
        {
            ForegroundMainOperations();
        }

        private void Window_IsVisibleChanged(object sender, DependencyPropertyChangedEventArgs e)
        {
            _lvClip.ItemsSource = ClipWindowViewModel.GetInstance.ClipData;
        }


        private void _lvClip_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Debug.WriteLine("Selection Changed");
            lvIndex = _lvClip.SelectedIndex;

            /** We are also hiding it here since Down key is not detected
             *  when listview is in focus.
             */
            _popupWindow.Hide();
        }

        private void Window_KeyDown(object sender, KeyEventArgs e)
        {
            Debug.WriteLine("Pressed Key: " + e.Key.ToString());
            Debug.WriteLine("Pressed SystemKey: " + e.SystemKey.ToString());

            // This key bind will focus the SearchTextBox.
            if (e.Key == Key.Q && isCtrlPressed())
            {
                _tbSearchBox.Focus();
                _lvClip.SelectedIndex = -1;
            }

            /** This key bind will bring focus to the first item from listview
             *  when Down is pressed from SearchTextBox or listview reaches
             *  it's last index.
             */
            if (e.Key == Key.Down && ((!_lvClip.IsFocused || _lvClip.SelectedIndex == _lvClip.Items.Count - 1) && _lvClip.Items.Count > 0))
            {
                SetListViewFocus(0);
            }

            /** This key bind will bring focus to the last item if user press
             *  Up key when first item is selected in listview.
             */
            if (e.Key == Key.Up && ((!_lvClip.IsFocused || _lvClip.SelectedIndex == 0) && _lvClip.Items.Count > 1))
            {
                SetListViewFocus(_lvClip.Items.Count - 1);
            }

            // This key bind will close the Window.
            if (e.Key == Key.Escape)
            {
                if (_popupWindow.IsVisible || (_materialMsgBox!=null && _materialMsgBox.Visibility == Visibility.Visible))
                    _popupWindow.Hide();
                else
                    Close();
            }
            else
            {
                // Hide the pop up window, when any key is pressed except escape.
                _popupWindow.Hide();
            }

            // This key bind will do operations when Enter key is pressed.
            if ((e.Key == Key.Return || e.Key == Key.Enter) && _lvClip.SelectedItems.Count > 0)
            {
                ForegroundMainOperations();
            }

            // This key bind will show popup menu.
            if (e.Key == Key.Tab && isCtrlPressed() && _lvClip.SelectedItems.Count > 0)
            {
                ShowPopupWindow(_lvClip.SelectedItem as TableCopy);
            }

            // This key bind will delete the selected items.
            if (e.Key == Key.Delete)
            {
                MaterialMsgBox
                    .SetMessage("Are you sure? This can't be undone")
                    .SetType(MessageType.OKCancel)
                    .SetOwner(this)
                    .SetOnCancelClickListener(null)
                    .SetOnOKClickListener(null)
                    .ShowDialog();
            }
        }


        /** Some key events are not detected by KeyDown so we use 
            PreviewKeyDown instead. */
        private void Window_PreviewKeyDown(object sender, KeyEventArgs e)
        {
            Debug.WriteLine("WPKD: " + e.Key.ToString());
            Debug.WriteLine("WPKD: " + e.SystemKey.ToString());
            // If pop up window is open, and space is pressed. It will put popup to focus.
            if (e.Key == Key.Space)
            {
                if (_popupWindow.IsVisible)
                    _popupWindow.Focus();
            }
        }


        /** We are not closing the application instead we are hiding it. So that live
            data changes can be observed! */
        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            e.Cancel = true;
            Hide();
        }


        ///** This event will automatically close this window when the whole
        // *  application is out of focus. */
        //private void Window_Deactivated(object sender, EventArgs e)
        //{
        //    // Close();
        //}

        #endregion


        #region UI Handling Functions

        /** This callback will handle when data is changed from various sources.
           One of the source is when data is edited when changed from PopUpWindow. */
        public void OnUpdate(List<TableCopy> models)
        {
            int index = _lvClip.SelectedIndex;
            _lvClip.ItemsSource = models;
            _lvClip.SelectedIndex = index;
            _popupWindow.Show();
            _popupWindow.Focus();
        }

        /** This function will handle the onClick and Enter press on any item
         *  in the listView. */
        private void ForegroundMainOperations()
        {
            // If more item is selected then we will parse only text type only...
            if (_lvClip.SelectedItems.Count > 1)
            {
                var builder = new StringBuilder();
                foreach (TableCopy copy in _lvClip.SelectedItems)
                {
                    if (copy.ContentType.ToEnum<ClipContentType>() == ClipContentType.Text)
                        builder.Append(copy.Text).Append(Environment.NewLine);
                }
                UpdateTextWindow(builder.ToString());
            }
            // We will filter the content type here...
            else
            {
                var model = _lvClip.SelectedItems[0] as TableCopy;
                switch (model.ContentType.ToEnum<ClipContentType>())
                {
                    case ClipContentType.Text: UpdateTextWindow(model.Text); break;
                    case ClipContentType.Image: var s = 0; break;
                    case ClipContentType.Files: var t = 0; break;
                }
            }
        }

        /** This will show message box window with onclick and stuff.. */
        public MaterialMessage MaterialMsgBox
        {
            get
            {
                if (_materialMsgBox != null && _materialMsgBox.IsActive)
                    _materialMsgBox.Close();
                _materialMsgBox = new MaterialMessage();
                return _materialMsgBox;
            }
        }

        /** This will show popup window using the TableCopy model */
        public void ShowPopupWindow(TableCopy model)
        {
            _popupWindow.SetPopUp(model);
            _popupWindow.Show();
        }

        /** This function will write text to the foreground window. */
        private void UpdateTextWindow(string text)
        {
            // We will minimize the window to get focus to previous window...
            WindowState = WindowState.Minimized;

            // Saving clipboard...
            string clipboardText = Clipboard.GetText();

            // Send text to screen...
            Clipboard.Clear();  // Always clear the clipboard first
            Clipboard.SetText(text);
            System.Windows.Forms.SendKeys.SendWait("^v");
            Clipboard.SetText(clipboardText);

            // Close the window...
            Close();
        }

        /** This function will focus the item of listview. */
        private void SetListViewFocus(int index)
        {
            _lvClip.SelectedIndex = index;
            ListViewItem item = _lvClip.ItemContainerGenerator.ContainerFromIndex(_lvClip.SelectedIndex) as ListViewItem;
            Keyboard.Focus(item);
            _lvClip.ScrollIntoView(_lvClip.SelectedItem);
        }

        #endregion
    }
}
