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
using static Components.CommonExtensions;
using static Components.KeyPressHelper;
using MaterialDesignThemes.Wpf;
using System.IO;
using Microsoft.Win32;
using Microsoft.VisualBasic.FileIO;
using System.Collections.Specialized;
using static Components.TranslationHelper;
using System.Windows.Media.Imaging;
using Autofac;
using System.Windows.Media;

namespace Components
{
    /** Can't bind the viewModel to this class because of some complexity issues.
     *  
     *  Their is also another issue, since this app is solely based on key press events. Doing
     *  this using View Model would increase lots of complexity.
     */

    public partial class ClipWindow : Window, IClipBinder
    {

        #region Variable Definition

        private PopupWindow _popupWindow;
        private MaterialMessage _materialMsgBox;
        private FilterWindow _filterWindow;
        private QRWindow _qrWindow;
        private IKeyboardRecorder recoder;
        private bool isMouseKeyDown;

        #endregion


        #region Constructor

        public ClipWindow()
        {

            InitializeComponent();
            recoder = AppModule.Container.Resolve<IKeyboardRecorder>();
            AppSingleton.GetInstance.SetBinder(this);
            _popupWindow = new PopupWindow();
            _filterWindow = new FilterWindow();
            _qrWindow = new QRWindow();

            double X = 0, Y = 0;

            CalculateXY(ref X, ref Y, this);

            Left = X;
            Top = Y;

            /** Since when scrolling in listview and moving mouse outside the scope| DragDropEffects.Move
              * changes the mouse enter event which eventually hides scrollbar.
              * In order to prevent this we observer isMouseKeyDown variable. */

            PreviewMouseDown += (o, e) => { isMouseKeyDown = true; };
            PreviewMouseUp += (o, e) => { isMouseKeyDown = false; };

            /** Attaching a callback to observe listview items collection. It's same as 
             *  ListView.Items.CurrentChange event.
             */
            ((INotifyCollectionChanged)_lvClip.Items).CollectionChanged += ClipWindow_CollectionChanged;
        }

        #endregion


        #region UI Events

        #region Unlocalized

        /** A callback to handle INotifyCollectionChange Property */
        private void ClipWindow_CollectionChanged(object sender, NotifyCollectionChangedEventArgs e)
        {
            if (_lvClip.Items.Count > 0)
                _emptyContainer.Hide();
            else _emptyContainer.Visible();
        }

        /** Occurs when the input system reports an underlying drag-and-drop event that involves this element. */
        private void _lvClip_GiveFeedback(object sender, GiveFeedbackEventArgs e)
        {
            if (e.Effects.HasFlag(DragDropEffects.Copy))
            {
                CloseWindow();
            }
        }

        /** This will invoke when there is drag on the list Item. */
        private void _lvClip_MouseMove(object sender, MouseEventArgs e)
        {
            if (e.LeftButton == MouseButtonState.Pressed && _lvClip.SelectedItems.Count > 0)
            {
                var model = (TableCopy)_lvClip.SelectedItem;
                DataObject data = new DataObject();
                switch (model.ContentType)
                {
                    case ContentType.Text:
                        data.SetData(DataFormats.Text, model.RawText);
                        break;
                    case ContentType.Image:
                        StringCollection fileList = new StringCollection();
                        fileList.Add(model.ImagePath);
                        data.SetFileDropList(fileList);
                        break;
                    case ContentType.Files:
                        data.SetFileDropList(model.LongText.Split(',').ToCollection());
                        break;
                }
                DragDrop.DoDragDrop(_lvClip, data, DragDropEffects.Copy);
            }

        }

        /// <summary>
        /// Whenever mouse is placed on certain position on window, we will manipulate
        /// scrollViewer on listview.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Window_MouseEnter(object sender, MouseEventArgs e)
        {
            var point = e.GetPosition(sender as Window);
            var scrollViewer = GetScrollViewer(_lvClip) as ScrollViewer;
            //scrollViewer.Loaded -= ScrollViewer_Loaded;
            //scrollViewer.Loaded += ScrollViewer_Loaded;

            if (scrollViewer == null || isMouseKeyDown) return;
            if (point.X > 260)
                scrollViewer.ShowVerticalScrollBar();
            else
                scrollViewer.HideVerticalScrollBar();
        }

        //private void ScrollViewer_Loaded(object sender, RoutedEventArgs e)
        //{
        //    var scrollViewer = (sender as ScrollViewer);
        //    var color = Application.Current.Resources["BackgroundBrush"] as SolidColorBrush;
        //    ((System.Windows.Shapes.Rectangle)scrollViewer.Template.FindName("Corner", scrollViewer)).Fill = color;
        //}

        private async void CloseButtonClick(object sender, RoutedEventArgs e)
        {
            await Task.Run(() =>
            {
                Thread.Sleep(400);
            });
            _tbSearchBox.Clear();
            CloseWindow();
        }

        private void SearchTextChanged(object sender, TextChangedEventArgs e)
        {
            if (!string.IsNullOrWhiteSpace(_tbSearchBox.Text))
            {
                switch (_tbSearchBox.Text)
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
            switch (type)
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
            _qrWindow.CloseWindow();
        }

        #endregion

        #region Key Capture Events
        private void Window_KeyDown(object sender, KeyEventArgs e)
        {
            // This key bind will show qr window
            if (e.Key == Key.R && IsCtrlPressed())
                ShowQRWindow();

            // This key bind will set current item to clipboard
            if (e.Key == Key.C && IsCtrlPressed())
                SetCurrentClip();

            // This key bind will focus the SearchTextBox.
            if (e.Key == Key.Q && IsCtrlPressed())
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
                else if (_qrWindow.IsVisible)
                    _qrWindow.Hide();
                else
                    CloseWindow();
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

            // This key bind will show pop-up menu.
            if (e.Key == Key.Tab && IsCtrlPressed() && _lvClip.SelectedItems.Count > 0)
            {
                ShowPopupWindow(_lvClip.SelectedItem as TableCopy);
            }

            // This key bind will delete the selected items.
            if (e.Key == Key.Delete)
            {
                DeleteItemFunc();
            }

            // This key bind will toggle pin to the selected item.
            if (e.Key == Key.T && IsCtrlPressed())
            {
                TogglePinFunc();
            }

            // This key bind will show filter box
            if (e.Key == Key.F && IsCtrlPressed())
            {
                ShowFilterWindow();
            }

            // This key bind will handle Ctrl + Number key shortcut.
            if (IsNumericKeyPressed(e.Key) && IsCtrlPressed())
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
            // If pop up window is open, and space is pressed. It will put pop-up to focus.
            if (e.Key == Key.Space)
            {
                if (_popupWindow.IsVisible)
                    _popupWindow.Focus();
            }
        }

        #endregion

        #endregion


        #region ClipBinder Implementations

        public void OnExitRequest()
        {
            CloseWindow();
        }

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

        /// <summary>
        /// This will handle Toggle pin operation
        /// </summary>
        private void TogglePinFunc()
        {
            if (_lvClip.SelectedIndex == -1) return;
            AppSingleton.GetInstance.TogglePin(_lvClip.SelectedItem as TableCopy);
        }

        /// <summary>
        /// This will handle Delete operation
        /// </summary>
        private void DeleteItemFunc()
        {
            if (_lvClip.SelectedItems.Count <= 0) return;
            MaterialMsgBox
                  .SetMessage(Translation.CLIP_MSG_CONFIRM)
                  .SetType(MessageType.OKCancel)
                  .SetOwner(this)
                  .SetOnCancelClickListener(null)
                  .SetOnOKClickListener(() =>
                  {
                      AppSingleton.GetInstance.DeleteData((from TableCopy s in _lvClip.SelectedItems select s).ToList());
                  })
                  .ShowDialog();
        }

        /// <summary>
        /// This will return the Table Copy object from contextMenu
        /// </summary>
        /// <param name="sender"></param>
        /// <returns></returns>
        private TableCopy GetTableCopyFromSender(object sender) => (TableCopy)((ContextMenu)(((MenuItem)sender).Parent)).Tag;

        /// <summary>
        /// A Function which will return ListView card item.
        /// </summary>
        /// <param name="index"></param>
        /// <returns></returns>
        public Card FindCardItem(int index)
        {
            ListViewItem item = _lvClip.ItemContainerGenerator.ContainerFromIndex(index) as ListViewItem;
            if (item != null)
            {
                ContentPresenter templateParent = GetFrameworkElementByName<ContentPresenter>(item);
                DataTemplate dataTemplate = _lvClip.ItemTemplate;
                if (dataTemplate != null && templateParent != null)
                {
                    return (dataTemplate.FindName("Item_MaterialCard", templateParent) as Card);
                }
            }
            return null;
        }

        /// <summary>
        /// This function will update the last used time of the TableCopy set.
        /// </summary>
        /// <param name="model"></param>
        private void UpdateLastUsedTime(TableCopy model)
        {
            model.LastUsedDateTime = DateTime.Now.ToFormattedDateTime();
            AppSingleton.GetInstance.UpdateLastUsedTime(model);
        }

        /// <summary>
        /// This function will handle the onClick and Enter press on any item in the listView.
        /// </summary>
        /// <param name="index"></param>
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

        /// <summary>
        /// This will show message box window with onclick and stuff.
        /// </summary>
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

        public void SetupSource()
        {
            _lvClip.ItemsSource = AppSingleton.GetInstance.ClipData;
        }

        /// <summary>
        /// A pathway function to make a clean exit of the application
        /// </summary>
        public void CloseWindow()
        {
            _filterWindow.Hide();
            _popupWindow.CloseWindow();
            _qrWindow.CloseWindow();
            Hide();
            _lvClip.ItemsSource = null;
            _tbSearchBox.Clear();
        }

        /// <summary>
        /// This will show filter window.
        /// </summary>
        public void ShowFilterWindow()
        {
            HideAllWindows();
            _filterWindow.Show();
            _filterWindow.SetUpWindow(_lvClip.SelectedIndex);
        }

        /// <summary>
        /// This will show pop-up window using the TableCopy model.
        /// </summary>
        /// <param name="model"></param>
        public void ShowPopupWindow(TableCopy model)
        {
            HideAllWindows();
            _popupWindow.SetPopUp(model);
            _popupWindow.Show();
        }

        /// <summary>
        /// This will show qr window using the TableCopy model.
        /// </summary>
        /// <param name="model"></param>
        public void ShowQRWindow()
        {
            if (_lvClip.SelectedItems.Count <= 0) return;
            var model = (TableCopy)_lvClip.SelectedItem;

            HideAllWindows();
            if (model.ContentType == ContentType.Text && model.Text.Length <= 1000)
            {
                _qrWindow.SetUp(model.Text);
                _qrWindow.Show();
                Focus();
            }
        }

        /// <summary>
        /// Call this function whenever you want to display a window over other.
        /// </summary>
        private void HideAllWindows()
        {
            _popupWindow.Hide();
            _filterWindow.Hide();
            _qrWindow.Hide();
        }

        /// <summary>
        /// This function will copy files to the foreground window.
        /// </summary>
        /// <param name="files"></param>
        private void UpdateFilesWindow(List<string> files)
        {
            // This function will get active path in the explorer.exe...
            var pasteLocation = ExplorerHelper.GetActiveExplorerPath();

            // If location null then open dialog to save file explicitly...
            if (pasteLocation == null)
            {
                var fd = new FolderSelectDialog
                {
                    Title = Translation.CLIP_FOLDER_COPY
                };
                if (fd.Show())
                {
                    files.ForEach((file) => FileSystem.CopyFile(file, Path.Combine(fd.FileName, Path.GetFileName(file)), UIOption.AllDialogs));

                    // Finally Close the window...
                    CloseWindow();
                }
            }
            else
            {
                // We will minimize the window to get focus to previous window...
                WindowState = WindowState.Minimized;

                // Copy all the files to the location...
                files.ForEach((file) => FileSystem.CopyFile(file, Path.Combine(pasteLocation, Path.GetFileName(file)), UIOption.AllDialogs));

                // Finally Close the window...
                CloseWindow();
            }
        }

        /// <summary>
        /// This function will copy image to the foreground window.
        /// </summary>
        /// <param name="imgPath"></param>
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
                    Title = Translation.CLIP_CPL
                };
                if (sfd.ShowDialog() == true)
                {
                    File.Copy(imgPath, sfd.FileName, true);

                    // Finally Close the window...
                    CloseWindow();
                }
            }
            else
            {
                // We will minimize the window to get focus to previous window...
                WindowState = WindowState.Minimized;

                // Copy the image to the location...
                File.Copy(imgPath, Path.Combine(pasteLocation, Path.GetFileName(imgPath)), true);

                // Finally Close the window...
                CloseWindow();
            }
        }

        /// <summary>
        ///  This function will write text to the foreground window.
        /// </summary>
        /// <param name="text"></param>
        private void UpdateTextWindow(string text)
        {
            // We will close the window to obtain focus to success window.
            CloseWindow();

            // Saving clipboard...
            string clipboardText = Clipboard.GetText();

            // Send text to screen...
            Clipboard.Clear();  // Always clear the clipboard first
            Clipboard.SetText(text);
            System.Windows.Forms.SendKeys.SendWait("^v");
            Clipboard.SetText(clipboardText);
        }

        /// <summary>
        /// This function will focus the item of listview.
        /// </summary>
        /// <param name="index"></param>
        private void SetListViewFocus(int index)
        {
            _lvClip.SelectedIndex = index;
            ListViewItem item = _lvClip.ItemContainerGenerator.ContainerFromIndex(_lvClip.SelectedIndex) as ListViewItem;
            Keyboard.Focus(item);
            _lvClip.ScrollIntoView(_lvClip.SelectedItem);
        }

        /// <summary>
        /// This function will set the current selected item to active clipboard.
        /// </summary>
        private void SetCurrentClip()
        {
            var clip = (TableCopy)_lvClip.SelectedItem;

            recoder.Ignore(() =>
            {
                switch (clip.ContentType)
                {
                    case ContentType.Text:
                        Clipboard.SetText(clip.RawText);
                        break;
                    case ContentType.Image:
                        Clipboard.SetImage(new BitmapImage(new Uri(clip.ImagePath)));
                        break;
                    case ContentType.Files:
                        Clipboard.SetFileDropList(clip.LongText.Split(',').ToCollection());
                        break;
                }
            });
        }


        #endregion


        #region Menu Item Clicks

        private void DisplayQR_Clicked(object sender, RoutedEventArgs e)
        {
            ShowQRWindow();
        }

        private void SetCurrentItem_Click(object sender, RoutedEventArgs e)
        {
            SetCurrentClip();
        }

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
