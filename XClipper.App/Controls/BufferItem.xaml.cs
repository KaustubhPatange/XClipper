using GalaSoft.MvvmLight.Command;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;

#nullable enable

namespace Components.Controls
{
    public partial class BufferItem : UserControl
    {
        public BufferItem()
        {
            InitializeComponent();
            (this.Content as FrameworkElement)!.DataContext = this;
            CopyKeyDownCommand = new RelayCommand<KeyEventArgs>(OnCopyKeyDown, null);
            PasteKeyDownCommand = new RelayCommand<KeyEventArgs>(OnPasteKeyDown, null);
            CutKeyDownCommand = new RelayCommand<KeyEventArgs>(OnCutKeyDown, null);
        }

        public RelayCommand<KeyEventArgs> CopyKeyDownCommand { get; set; }
        public RelayCommand<KeyEventArgs> PasteKeyDownCommand { get; set; }
        public RelayCommand<KeyEventArgs> CutKeyDownCommand { get; set; }

        #region Buffer Property

        public static readonly DependencyProperty BufferModelProperty =
            DependencyProperty.RegisterAttached(nameof(BufferModel), typeof(Buffer), typeof(BufferItem), new PropertyMetadata(null));

        public Buffer BufferModel
        {
            get => (Buffer)GetValue(BufferModelProperty);
            set => SetValue(BufferModelProperty, value);
        }

        #endregion

        #region Title Property 

        public static readonly DependencyProperty TitleProperty =
           DependencyProperty.RegisterAttached(nameof(Title), typeof(string), typeof(BufferItem), new PropertyMetadata(string.Empty));

        public string Title
        {
            get => (string)GetValue(TitleProperty);
            set => SetValue(TitleProperty, value);
        }

        #endregion

        #region KeyDownEvents

        private void OnCopyKeyDown(KeyEventArgs args)
        {
            if (!KeyPressHelper.IsSpecialKey(args.Key)) BufferModel.Copy.HotKey = args.Key.ToString();
        }
        
        private void OnPasteKeyDown(KeyEventArgs args)
        {
            if (!KeyPressHelper.IsSpecialKey(args.Key)) BufferModel.Paste.HotKey = args.Key.ToString();
        }
        
        private void OnCutKeyDown(KeyEventArgs args)
        {
            if (!KeyPressHelper.IsSpecialKey(args.Key)) BufferModel.Cut.HotKey = args.Key.ToString();
        }

        #endregion

        private void OnShowButtonClicked(object sender, RoutedEventArgs e)
        {
            MessageBox.Show(BufferModel.Data, "Data");
        }
    }
}
