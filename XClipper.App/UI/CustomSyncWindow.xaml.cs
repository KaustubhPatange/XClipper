namespace Components.UI
{
    public partial class CustomSyncWindow : CustomHelpWindow
    {
        public CustomSyncWindow()
        {
            InitializeComponent();

            DataContext = new CustomSyncViewModel();
        }

        public override void OnHelpButtonClicked()
        {
            // todo: Add link for Firebase help button.
        }
    }
}
