using ClipboardManager.models;
using SQLite;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Windows;
using System.Windows.Media;
using Components.helpers;
using Components.viewModels;
using System.Threading.Tasks;
using System.Threading;
using System.Windows.Input;
using System.Windows.Controls;
using System.Runtime.InteropServices;
using System.Windows.Interop;
using System.Text;
using static Components.helpers.MainHelper;

namespace Components
{
    // TODO: Use SendKeys.Send to send the text...
    public partial class ClipWindow : Window, ClipBinder
    {
        private ClipWindowViewModel viewModel;
        private PopupWindow _popupWindow;
        private int lvIndex = -1;

        public ClipWindow()
        {
            InitializeComponent();

            viewModel = new ClipWindowViewModel().setBinder(this);
            _popupWindow = new PopupWindow();

            var screen = System.Windows.SystemParameters.WorkArea;
            this.Left = screen.Right - this.Width - 10;
            this.Top = screen.Bottom - this.Height - 10;

            _tbSearchBox.Focus();
        }

        public void ShowPopupWindow(TableCopy model)
        {
            _popupWindow.SetPopUp(model);
            _popupWindow.Show();
        }
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
                _lvClip.ItemsSource = viewModel.FilterData(_tbSearchBox.Text);
            }
            else _lvClip.ItemsSource = viewModel.ClipData;
        }

        private void ListViewItemDoubleClicked(object sender, MouseButtonEventArgs e)
        {
            ForegroundMainOperations();
        }

        private void Window_IsVisibleChanged(object sender, DependencyPropertyChangedEventArgs e)
        {
            _lvClip.ItemsSource = viewModel.ClipData;
        }

        private void _lvClip_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            lvIndex = _lvClip.SelectedIndex;

            /** We are also hiding it here since Down key is not detected
             *  when listview is in focus.
             */
            _popupWindow.Hide();
        }

        private void Window_KeyDown(object sender, KeyEventArgs e)
        {
            Debug.WriteLine("Pressed: " + e.Key.ToString());

            // Hide the pop up window, when any key is pressed.
            _popupWindow.Hide();
            
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
                if (_popupWindow.IsVisible)
                    _popupWindow.Hide();
                else 
                    Close();
            }
            
            // This key bind will do operations when Enter key is pressed.
            if ((e.Key == Key.Return || e.Key == Key.Enter) && _lvClip.SelectedItems.Count > 0)
            {
                ForegroundMainOperations();
            }

            if (e.Key == Key.Tab && isCtrlPressed() && _lvClip.SelectedItems.Count > 0)
            {
                ShowPopupWindow(_lvClip.SelectedItem as TableCopy);
            }
        }

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

        public void FindItem()
        {
            ListViewItem item = _lvClip.ItemContainerGenerator.ContainerFromIndex(_lvClip.SelectedIndex) as ListViewItem;
            if (item != null)
            {
                //get the item's template parent
                ContentPresenter templateParent = GetFrameworkElementByName<ContentPresenter>(item);
                //Debug.WriteLine(ToolTipService.GetToolTip(templateParent).GetType());
                //ToolTipService.SetShowDuration(templateParent, 1000);
                //get the DataTemplate that TextBlock in.
                DataTemplate dataTemplate = _lvClip.ItemTemplate;
                if (dataTemplate != null && templateParent != null)
                {

                    var card = dataTemplate.FindName("Item_MaterialCard", templateParent) as MaterialDesignThemes.Wpf.Card;
                   // ContentPresenter templateParent = GetFrameworkElementByName<ContentPresenter>(_myPopup);
                   
                    //var t = card.ToolTip as ToolTip;
                    //ToolTipService.SetToolTip(card, 1000);
                    //var tooltip = card.ToolTip as ToolTip;
                    ////  tooltip.os
                    //tooltip.IsOpen = true;

                    //tooltip.StaysOpen = true;
                    //Debug.WriteLine("Tooltip: " + card.ToolTip.GetType());

                }
                //if (textYear != null)
                //{
                //    MessageBox.Show(String.Format("Current item's Year is:{0}", textYear.Text));
                //}
            }
        }

        private static T GetFrameworkElementByName<T>(FrameworkElement referenceElement) where T : FrameworkElement
        {
            FrameworkElement child = null;
            for (Int32 i = 0; i < VisualTreeHelper.GetChildrenCount(referenceElement); i++)
            {
                child = VisualTreeHelper.GetChild(referenceElement, i) as FrameworkElement;
                if (child != null && child.GetType() == typeof(T))
                { break; }
                else if (child != null)
                {
                    child = GetFrameworkElementByName<T>(child);
                    if (child != null && child.GetType() == typeof(T))
                    {
                        break;
                    }
                }
            }
            return child as T;
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

        /** We are not closing the application instead we are hiding it. So that live
            data changes can be observed! */
        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            e.Cancel = true;
            Hide();
        }

        /** This event will automatically close this window when the whole
         *  application is out of focus. */
        private void Window_Deactivated(object sender, EventArgs e)
        {
           // Close();
        }

        public bool isCtrlPressed() => (Keyboard.IsKeyDown(Key.RightCtrl) || Keyboard.IsKeyDown(Key.LeftCtrl));
        public bool isShitPressed() => (Keyboard.IsKeyDown(Key.LeftShift) || Keyboard.IsKeyDown(Key.RightShift));
    }
}
