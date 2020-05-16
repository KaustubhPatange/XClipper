using System.Collections.Generic;
using static Components.Constants;
using static Components.MainHelper;
using System.Windows;
using static Components.App;
using System.Windows.Input;
using Components.viewModels;
using static Components.TranslationHelper;

namespace Components
{
    /** Same reason as ClipWindow for not using viewModel */
    public partial class FilterWindow : Window
    {

        #region Constructor

        public FilterWindow()
        {
            InitializeComponent();
            
            double X = 0, Y = 0;

            CalculateXY(ref X, ref Y, this);

            this.Left = X;
            this.Top = Y;

            _lvFilter.MouseDoubleClick += _lvFilter_MouseDoubleClick;

            var list = new List<FilterModel>
            {
                new FilterModel(Translation.FILTER_PIN, CONTENT_FILTER_PINNED),
                new FilterModel(Translation.FILTER_UNPIN, CONTENT_FILTER_NON_PINNED),
                new FilterModel(Translation.FILTER_NEW, CONTENT_FILTER_NEWEST_FIRST),
                new FilterModel(Translation.FILTER_OLD, CONTENT_FILTER_OLDEST_FIRST),
                new FilterModel(Translation.FILTER_CS_DESC, CONTENT_FILTER_TEXTLENGTH_DESC),
                new FilterModel(Translation.FILTER_CS_ASC, CONTENT_FILTER_TEXTLENGTH_ASC),
                new FilterModel(rm.GetString("filter_none"), "")
            };

            _lvFilter.ItemsSource = list;

        }

        #endregion

        #region UI Handle Events

        private void FilterWindow_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Escape)
                CloseWindow();
            if (e.Key == Key.Enter || e.Key == Key.Return)
                FilterContent();
        }

        private void _lvFilter_MouseDoubleClick(object sender, MouseButtonEventArgs e)
        {
            FilterContent();
        }

        private void CloseButton_Clicked(object sender, RoutedEventArgs e)
        {
            CloseWindow();
        }

        #endregion

        #region UI Functions

        public void SetUpWindow(int Index)
        {
            _tbFilter.Text = $"{Translation.FILTER_INDEX}: {Index + 1}";
            _lvFilter.Focus();
        }

        private void FilterContent()
        {
            if (_lvFilter.SelectedItems.Count <= 0) return;

            AppSingleton.GetInstance.SetFilterText((_lvFilter.SelectedItem as FilterModel).Filter_Text);
        }

        private void CloseWindow()
        {
            Hide();
        }

        #endregion

    }
}
