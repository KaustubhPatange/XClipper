using System;
using System.Collections.Generic;
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
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace Components.Controls
{
    public partial class ToolItem : UserControl
    {
        public ToolItem()
        {
            InitializeComponent();
            (this.Content as FrameworkElement).DataContext = this;
        }


        public static readonly DependencyProperty DescriptionProperty =
          DependencyProperty.Register(nameof(Description), typeof(string), typeof(ToolItem), new PropertyMetadata(""));

        public string Description
        {
            get => (string) GetValue(DescriptionProperty);
            set => SetValue(DescriptionProperty, value);
        }

        public static readonly DependencyProperty ButtonTextProperty =
          DependencyProperty.Register(nameof(ButtonText), typeof(string), typeof(ToolItem), new PropertyMetadata(""));

        public string ButtonText
        {
            get => (string) GetValue(ButtonTextProperty);
            set => SetValue(ButtonTextProperty, value);
        }

        public static readonly DependencyProperty ButtonCommandProperty =
            DependencyProperty.Register(nameof(ButtonCommand), typeof(ICommand), typeof(ToolItem), new PropertyMetadata(null));

        public ICommand ButtonCommand
        {
            get => (ICommand) GetValue(ButtonCommandProperty);
            set => SetValue(ButtonCommandProperty, value);
        }
        
        public static readonly DependencyProperty IsButtonEnabledProperty =
            DependencyProperty.Register(nameof(IsButtonEnabled), typeof(bool), typeof(ToolItem), new PropertyMetadata(true));

        public bool IsButtonEnabled
        {
            get => (bool) GetValue(IsButtonEnabledProperty);
            set => SetValue(IsButtonEnabledProperty, value);
        }
    }
}
