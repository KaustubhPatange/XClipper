namespace Components.UI
{
    public partial class CustomSyncWindow : CustomHelpWindow, ICustomSyncBinder
    {
        public CustomSyncWindow()
        {
            InitializeComponent();

            DataContext = new CustomSyncViewModel(this);
        }

        public void OnCloseWindow()
        {
            Close();
        }

        public override void OnHelpButtonClicked()
        {
            // todo: Add link for Firebase help button.
        }
    }

    public interface ICustomSyncBinder
    {
        void OnCloseWindow();
    }
}
