using System;
using System.Windows;

namespace Components.UI
{
    public partial class ScriptWindow : Window
    {
        public ScriptWindow()
        {
            InitializeComponent();
            (this.Content as FrameworkElement)!.DataContext = this;
        }

        public Action<Script> OnSaveClicked;
        public Script ScriptModel { get; set; } = new Script();

        public string Output { get; set; } = string.Empty;

        private void OnSaveButtonClicked(object sender, RoutedEventArgs e)
        {

        }

        private void OnRunButtonClicked(object sender, RoutedEventArgs e)
        {

        }

        private void OnLoadFromFileClicked(object sender, RoutedEventArgs e)
        {

        }

        public class Builder
        {
            private ScriptWindow scriptWindow = new ScriptWindow();
            public Builder SetScript(Script script)
            {
                scriptWindow.ScriptModel = script;
                return this;
            }

            public Builder SetOnSave(Action<Script> block)
            {
                scriptWindow.OnSaveClicked = block;
                return this;
            }
            
            public void Show(Window owner)
            {
                scriptWindow.Owner = owner;
                scriptWindow.WindowStartupLocation = WindowStartupLocation.CenterOwner;
                scriptWindow.ShowDialog();
            }
        }
    }
}
