using System.Diagnostics;
using static Components.Constants;

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
            Process.Start(DOC_SYNCHRONIZATION);
        }
    }

    public interface ICustomSyncBinder
    {
        void OnCloseWindow();
    }
}
