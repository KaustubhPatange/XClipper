using ClipboardManager.models;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Windows;
using Components.viewModels;
using System.Threading.Tasks;
using System.Threading;
using System.Windows.Input;
using System.Windows.Controls;
using System.Linq;
using System.Text;
using static Components.MainHelper;
using MaterialDesignThemes.Wpf;
using System.Collections.Specialized;
using System.IO;
using Microsoft.Win32;
using Microsoft.VisualBasic.FileIO;
using System.Windows.Threading;

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

            AppSingleton.GetInstance.setBinder(this);
            _popupWindow = new PopupWindow();

            var screen = SystemParameters.WorkArea;
            this.Left = screen.Right - this.Width - 10;
            this.Top = screen.Bottom - this.Height - 10;

            /** Could've used viewModel instead to bind the data, but for some
             *  cases it becomes much complex, so I am going with simpler approach. */

            ((INotifyCollectionChanged)_lvClip.Items).CollectionChanged += ListView_CollectionChanged;

            // Focus on the search editbox at start
            _tbSearchBox.Focus();
        }


        #endregion


        #region UI Events

        #region Unlocalised

        /** A notifier which will observe listview collection change */
        private async void ListView_CollectionChanged(object sender, NotifyCollectionChangedEventArgs e)
        {
            await Task.Delay(300);
            var size = _lvClip.Items.Count;
            if (size >= 10) size = 10;
            for (int i = 0; i < size; i++)
            {
                if (i == 9)
                    FindCardIdTextBlockItem(i).Text = 0.ToString();
                else
                    FindCardIdTextBlockItem(i).Text = (i + 1).ToString();
            }
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
                _lvClip.ItemsSource = AppSingleton.GetInstance.FilterData(_tbSearchBox.Text);
            }
            else _lvClip.ItemsSource = AppSingleton.GetInstance.ClipData;
        }

        private void ListViewItemDoubleClicked(object sender, MouseButtonEventArgs e)
        {
            ForegroundMainOperations();
        }

        private void Window_IsVisibleChanged(object sender, DependencyPropertyChangedEventArgs e)
        {
            _lvClip.ItemsSource = AppSingleton.GetInstance.ClipData;
        }

        private void _lvClip_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            lvIndex = _lvClip.SelectedIndex;
            lvIndex = _lvClip.SelectedIndex;

            /** We are also hiding it here since Down key is not detected
             *  when listview is in focus.
             */
            _popupWindow.Hide();
        }

        ///** We are not closing the application instead we are hiding it. So that live
        //    data changes can be observed! */
        //private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        //{
        //    e.Cancel = true;
        //    Hide();
        //}

        private void Window_Closed(object sender, EventArgs e)
        {
            Application.Current.Dispatcher.InvokeShutdown();
        }

        #endregion

        #region Key Capture Events
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
                if (_popupWindow.IsVisible)
                {
                    _popupWindow.Hide();
                }
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
                    .SetOnOKClickListener(() =>
                    {
                        AppSingleton.GetInstance.DeleteData((from TableCopy s in _lvClip.SelectedItems select s).ToList());
                    })
                    .ShowDialog();
            }

            // This key bind will handle Ctrl + Number key shortcut.
            if (isNumericKeyPressed(e.Key) && isCtrlPressed())
            {
                var index = ParseNumericKey(e.Key);
                if (index == 0)
                    ForegroundMainOperations(9);
                else ForegroundMainOperations(index - 1);
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

        #endregion


        ///** This event will automatically close this window when the whole
        // *  application is out of focus. */
        //private void Window_Deactivated(object sender, EventArgs e)
        //{
        //    // Close();
        //}

        #endregion


        #region UI Handling Functions

        ///** This functionw ill be used to update source on listview. 
        // *  As it also sets some other details along with it. */
        //private void SetItemSource(List<TableCopy> models)
        //{
        //    _lvClip.ItemsSource = models;
        //    var size = _lvClip.Items.Count;
        //    if (size >= 10) size = 10;
        //    for (int i = 0; i < size; i++)
        //    {
        //        if (i == 9)
        //            FindCardIdTextBlockItem(i).Text = 0.ToString();
        //        else
        //            FindCardIdTextBlockItem(i).Text = (i + 1).ToString();
        //    }
        //}

        /** A Function which will return ListView card item. */
        public TextBlock FindCardIdTextBlockItem(int index)
        {
            ListViewItem item = _lvClip.ItemContainerGenerator.ContainerFromIndex(index) as ListViewItem;
            if (item != null)
            {
                ContentPresenter templateParent = GetFrameworkElementByName<ContentPresenter>(item);
                DataTemplate dataTemplate = _lvClip.ItemTemplate;
                if (dataTemplate != null && templateParent != null)
                {
                    var panel = (dataTemplate.FindName("Item_MaterialCard", templateParent) as Card)
                        .FindName("Item_StackPanel") as StackPanel;
                    return panel.FindName("Item_TextBlock") as TextBlock;
                    //ContentPresenter templateParent = GetFrameworkElementByName<ContentPresenter>(card);
                }
            }
            return null;
        }

        /** This callback will handle event when data is deleted. */
        public void OnModelDeleted(List<TableCopy> models)
        {
            _lvClip.ItemsSource = models;
            _lvClip.SelectedIndex = 0;
            _lvClip.Focus();
        }

        /** This callback will handle event when data is edited from PopUpWindow. */
        public void OnPopupTextEdited(List<TableCopy> models)
        {
            int index = _lvClip.SelectedIndex;
            _lvClip.ItemsSource = models;
            _lvClip.SelectedIndex = index;
            _popupWindow.Show();
            _popupWindow.Focus();
        }

        /** This function will handle the onClick and Enter press on any item
         *  in the listView. */
        private void ForegroundMainOperations(int index = -1)
        {
            // If more item is selected then we will parse only text type only...
            if (_lvClip.SelectedItems.Count > 1 && index == -1)
            {
                var builder = new StringBuilder();
                foreach (TableCopy copy in _lvClip.SelectedItems)
                {
                    // if (copy.ContentType.ToEnum<ClipContentType>() == ClipContentType.Text)
                    if (copy.ContentType == ContentType.Text)
                        builder.Append(copy.Text).Append(Environment.NewLine);
                }
                UpdateTextWindow(builder.ToString());
            }
            // We will filter the content type here...
            else
            {
                if (index == -1) index = _lvClip.SelectedIndex;
                var model = _lvClip.Items[index] as TableCopy;
                switch (model.ContentType)
                {
                    case ContentType.Text: UpdateTextWindow(model.Text); break;
                    case ContentType.Image: UpdateImageWindow(model.ImagePath); break;
                    case ContentType.Files:
                        if (model.LongText.Contains(","))
                        {
                            UpdateFilesWindow(model.LongText.Split(',').ToList());
                        }
                        else
                        {
                            UpdateFilesWindow(new List<string> { model.LongText });
                        }
                        break;
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

        public void CloseWindow()
        {
            Hide();
        }

        /** This will show popup window using the TableCopy model */
        public void ShowPopupWindow(TableCopy model)
        {
            _popupWindow.SetPopUp(model);
            _popupWindow.Show();
        }

        /** This function will copy files to the foreground window. */
        private void UpdateFilesWindow(List<string> files)
        {
            // This function will get active path in the explorer.exe...
            var pasteLocation = ExplorerHelper.GetActiveExplorerPath();

            // If location null then open dialog to save file explicitly...
            if (pasteLocation == null)
            {
                var fd = new FolderSelectDialog
                {
                    Title = "Select a folder to copy the files"
                };
                if (fd.Show())
                {
                    foreach (string file in files)
                    {
                        FileSystem.CopyFile(file, Path.Combine(fd.FileName, Path.GetFileName(file)), UIOption.AllDialogs);
                    }

                    // Finally Close the window...
                    Close();
                }
            }
            else
            {
                // We will minimize the window to get focus to previous window...
                WindowState = WindowState.Minimized;

                // Copy all the files to the location...
                foreach (string file in files)
                {
                    FileSystem.CopyFile(file, Path.Combine(pasteLocation, Path.GetFileName(file)), UIOption.AllDialogs);
                }

                // Finally Close the window...
                Close();
            }
        }

        /** This function will copy image to the foreground window. */
        private void UpdateImageWindow(string imgPath)
        {
            // This function will get active path in the explorer.exe...
            var pasteLocation = ExplorerHelper.GetActiveExplorerPath();

            // If location null then open dialog to save file explicitly...

            if (pasteLocation == null)
            {
                var ext = Path.GetExtension(imgPath);
                var sfd = new SaveFileDialog
                {
                    FileName = Path.GetFileName(imgPath),
                    Filter = $"{ext}|{ext}",
                    Title = "Choose a paste location"
                };
                if (sfd.ShowDialog() == true)
                {
                    File.Copy(imgPath, sfd.FileName, true);

                    // Finally Close the window...
                    Close();
                }
            }
            else
            {
                // We will minimize the window to get focus to previous window...
                WindowState = WindowState.Minimized;

                // Copy the image to the location...
                File.Copy(imgPath, Path.Combine(pasteLocation, Path.GetFileName(imgPath)), true);

                // Finally Close the window...
                Close();
            }
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

            // Finally Close the window...
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
