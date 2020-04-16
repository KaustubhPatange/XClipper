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
using static Components.Constants;
using static Components.DefaultSettings;
using static Components.CommonExtensions;
using MaterialDesignThemes.Wpf;
using System.IO;
using Microsoft.Win32;
using Microsoft.VisualBasic.FileIO;

namespace Components
{
    /** I can't use viewModel in this window! When I started coding this project I did static
     *  data binding and this view class which completely broke the ViewModel and with this
     *  code migrating to View Model will be pain in the ass.
     *  
     *  Their is also another issue, since this app is solely based on key press events. Doing
     *  this using View Model would increase lots of complexity. As for eg: I can't bind
     *  Ctrl + Q as Key only accepts single value. */
    public partial class ClipWindow : Window, ClipBinder
    {

        #region Variablel Definition

        private PopupWindow _popupWindow;
        private MaterialMessage _materialMsgBox;
        private FilterWindow _filterWindow;
        private bool isMouseKeyDown;

        #endregion


        #region Constructor

        Stopwatch stops = new Stopwatch();

        public ClipWindow()
        {
            stops.Start();

            InitializeComponent();

            AppSingleton.GetInstance.SetBinder(this);
            _popupWindow = new PopupWindow();
            _filterWindow = new FilterWindow();

            
            double X = 0, Y = 0;

            CalculateXY(ref X, ref Y, this);

            this.Left = X;
            this.Top = Y;

            /** Since when scrolling in listview and moving mouse outside the scope
              * changes the mouse enter event which eventually hides scrollbar.
              * In order to prevent this we observer isMouseKeyDown variable. */

            PreviewMouseDown += (o, e) => { isMouseKeyDown = true; };
            PreviewMouseUp += (o, e) => { isMouseKeyDown = false; };

            // Focus on the search editbox at start
            _tbSearchBox.Focus();

            Loaded += ClipWindow_Loaded;
        }

        private void ClipWindow_Loaded(object sender, RoutedEventArgs e)
        {
            stops.Stop();
            Debug.WriteLine("Time ellapsed: " + stops.ElapsedMilliseconds);
        }

        #endregion


        #region UI Events

        #region Unlocalised

        /** Whenever mouse is placed on certain position on window, we will manipulate
         *  ScollViewer on listview. */
        private void Window_MouseEnter(object sender, MouseEventArgs e)
        {
            var point = e.GetPosition(sender as Window);

            var scrollViewer = GetScrollViewer(_lvClip) as ScrollViewer;

            if (scrollViewer == null || isMouseKeyDown) return;
            if (point.X > 260)
                scrollViewer.ShowVerticalScrollBar();
            else
                scrollViewer.HideVerticalScrollBar();
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
                switch(_tbSearchBox.Text)
                {
                    case CONTENT_FILTER_TEXT:
                        _lvClip.ItemsSource = AppSingleton.GetInstance.FilterContentType(ContentType.Text);
                        break;
                    case CONTENT_FILTER_IMAGE:
                        _lvClip.ItemsSource = AppSingleton.GetInstance.FilterContentType(ContentType.Image);
                        break;
                    case CONTENT_FILTER_FILES:
                        _lvClip.ItemsSource = AppSingleton.GetInstance.FilterContentType(ContentType.Files);
                        break;
                    case CONTENT_FILTER_PINNED:
                        _lvClip.ItemsSource = AppSingleton.GetInstance.FilterPinned();
                        break;
                    case CONTENT_FILTER_NON_PINNED:
                        _lvClip.ItemsSource = AppSingleton.GetInstance.FilterUnpinned();
                        break;
                    case CONTENT_FILTER_OLDEST_FIRST:
                        _lvClip.ItemsSource = AppSingleton.GetInstance.FilterOldest();
                        break;
                    case CONTENT_FILTER_TEXTLENGTH_DESC:
                        _lvClip.ItemsSource = AppSingleton.GetInstance.FilterTextLengthDesc();
                        break;
                    case CONTENT_FILTER_TEXTLENGTH_ASC:
                        _lvClip.ItemsSource = AppSingleton.GetInstance.FilterTextLengthAsc();
                        break;
                    case CONTENT_FILTER_NEWEST_FIRST:
                        _lvClip.ItemsSource = AppSingleton.GetInstance.FilterNewest();
                        break;
                    default:
                        _lvClip.ItemsSource = AppSingleton.GetInstance.FilterData(_tbSearchBox.Text);
                        break;
                }
            }
            else _lvClip.ItemsSource = AppSingleton.GetInstance.ClipData;
        }
        private void ContentTypeButton_Clicked(object sender, RoutedEventArgs e)
        {
            var button = (Button)sender;
            var type = button.Content.ToString().ToEnum<ContentType>();
            switch(type)
            {
                case ContentType.Text:
                    _tbSearchBox.Text = CONTENT_FILTER_TEXT;
                    break;
                case ContentType.Image:
                    _tbSearchBox.Text = CONTENT_FILTER_IMAGE;
                    break;
                case ContentType.Files:
                    _tbSearchBox.Text = CONTENT_FILTER_FILES;
                    break;
            }
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

            /** We are also hiding it here since Down key is not detected
             *  when listview is in focus.
             */
            _popupWindow.Hide();
            _filterWindow.Hide();
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
                else if (_filterWindow.IsVisible)
                {
                    _filterWindow.Hide();
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
                DeleteItemFunc();
            }

            // This key bind will toggle pin to the selected item.
            if (e.Key == Key.P && isCtrlPressed())
            {
                TogglePinFunc();
            }

            // This key bind will show filter box
            if (e.Key == Key.F && isCtrlPressed())
            {
                ShowFilterWindow();
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


        #region ClipBinder Implementations

        /** This callback will change the text of Search TextBox*/
        public void OnFilterTextEdit(string Text)
        {
            _tbSearchBox.Text = Text;
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

        #endregion


        #region UI Handling Functions

        /** This will handle Toggle pin operation */

        private void TogglePinFunc()
        {
            if (_lvClip.SelectedIndex == -1) return;
            AppSingleton.GetInstance.TogglePin(_lvClip.SelectedItem as TableCopy);
        }

        /** This will handle Delete operation */
        private void DeleteItemFunc()
        {
            if (_lvClip.SelectedItems.Count <= 0) return;
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

        /** This will return the Table Copy object from contextMenu */
        private TableCopy GetTableCopyFromSender(object sender) => (TableCopy)((ContextMenu)(((MenuItem)sender).Parent)).Tag;

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
                }
            }
            return null;
        }

        /** This function will update the last used time of the TableCopy set. */
        private void UpdateLastUsedTime(TableCopy model)
        {
            model.LastUsedDateTime = DateTime.Now.ToFormattedDateTime();
            AppSingleton.GetInstance.UpdateLastUsedTime(model);
        }

        /** This function will handle the onClick and Enter press on any item
         *  in the listView. */
        private void ForegroundMainOperations(int index = -1)
        {
            // If more item is selected then we will parse only text type only...
            if (_lvClip.SelectedItems.Count > 1 && index == -1)
            {
                var builder = new StringBuilder();
                foreach (TableCopy model in _lvClip.SelectedItems)
                {
                    // if (copy.ContentType.ToEnum<ClipContentType>() == ClipContentType.Text)
                    if (model.ContentType == ContentType.Text)
                    {
                        builder.Append(model.Text).Append(Environment.NewLine);
                        UpdateLastUsedTime(model);
                    }
                }
                UpdateTextWindow(builder.ToString());
            }
            // We will filter the content type here...
            else
            {
                if (index == -1) index = _lvClip.SelectedIndex;
                var model = _lvClip.Items[index] as TableCopy;
                UpdateLastUsedTime(model);
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

        /** This will show filter window. */
        public void ShowFilterWindow()
        {
            if (_lvClip.SelectedItems.Count <= 0) return;
            if (_popupWindow.IsVisible)
                _popupWindow.Hide();
            _filterWindow.Show();
            _filterWindow.SetUpWindow(_lvClip.SelectedIndex);
        }

        /** This will show popup window using the TableCopy model */
        public void ShowPopupWindow(TableCopy model)
        {
            if (_filterWindow.IsVisible)
                _filterWindow.Hide();
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
                    files.ForEach((file) => FileSystem.CopyFile(file, Path.Combine(fd.FileName, Path.GetFileName(file)), UIOption.AllDialogs));

                    // Finally Close the window...
                    Close();
                }
            }
            else
            {
                // We will minimize the window to get focus to previous window...
                WindowState = WindowState.Minimized;

                // Copy all the files to the location...
                files.ForEach((file) => FileSystem.CopyFile(file, Path.Combine(pasteLocation, Path.GetFileName(file)), UIOption.AllDialogs));

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


        #region Menu Item Clicks
        private void QuickInfo_MenuItemClicked(object sender, RoutedEventArgs e)
        {
            var model = GetTableCopyFromSender(sender);
            ShowPopupWindow(model);
        }

        private void DeleteItem_Clicked(object sender, RoutedEventArgs e)
        {
            DeleteItemFunc();
        }

        private void TogglePinItem_Clicked(object sender, RoutedEventArgs e)
        {
            TogglePinFunc();
        }
        private void MenuItem_Click(object sender, RoutedEventArgs e)
        {
            ShowFilterWindow();
        }

        #endregion

    }
}
