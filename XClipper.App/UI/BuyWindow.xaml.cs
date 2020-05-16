using System.Windows;

namespace Components
{
    public partial class BuyWindow : Window
    {
        public BuyWindow()
        {
            InitializeComponent();

            this.DataContext = new BuyViewModel();
        }
    }
}
