using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Windows;
using System.Windows.Forms;

namespace Components.UI
{
    public partial class UpdateWindow : Window
    {
        private UpdateViewModel updateViewModel;
        public UpdateWindow(List<ReleaseItem>? updateModel)
        {
            InitializeComponent();

            updateViewModel = new UpdateViewModel(updateModel);
            DataContext = updateViewModel;
        }

        protected override void OnSourceInitialized(EventArgs e)
        {
            base.OnSourceInitialized(e);
            Focus();
        }
    }

    public enum Status
    {
        Downloading,
        Completed
    }
}
