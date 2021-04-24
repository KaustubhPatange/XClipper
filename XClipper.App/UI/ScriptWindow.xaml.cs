using System;
using System.ComponentModel;
using System.IO;
using System.Windows;
using Microsoft.Win32;
using PropertyChanged;
using static Components.TranslationHelper;

namespace Components.UI
{
    public partial class ScriptWindow : Window, INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged = (o, e) =>
        {
            ScriptWindow window = (ScriptWindow) o;
            window.UpdateProperties();
        };
        public ScriptWindow()
        {
            InitializeComponent();
            (this.Content as FrameworkElement)!.DataContext = this;
        }

        protected override void OnSourceInitialized(EventArgs e)
        {
            base.OnSourceInitialized(e);
            ScriptModel.PropertyChanged += (o, e) => PropertyChanged.Invoke(this, e);
            UpdateProperties();
        }

        public Action<Script> OnSaveClicked;
        public Script ScriptModel { get; set; } = new();
        public bool IsExpanderExpanded { get; set; } = false;
        public bool IsRunEnabled { get; set; }
        public bool IsSaveEnabled { get; set; }
        public string Output { get; set; } = string.Empty;

        public void UpdateProperties()
        {
            IsRunEnabled = ScriptModel.Code.IsNotEmpty();
            IsSaveEnabled = ScriptModel.Code.IsNotEmpty() && ScriptModel.Name.IsNotEmpty();
        }

        private void OnSaveButtonClicked(object sender, RoutedEventArgs e)
        {
            this.Close();
            OnSaveClicked?.Invoke(ScriptModel);
        }

        private void OnRunButtonClicked(object sender, RoutedEventArgs e)
        {
            IsExpanderExpanded = true;
            var result = Interpreter.Run(ScriptModel, Clipper.CreateSandbox());
            if (result is Interpreter.Result.Error)
            {
                Output = ((Interpreter.Result.Error) result).Message;
            }
            else if (result is Interpreter.Result.Success)
            {
                Output = $"Process exited with: {((Interpreter.Result.Success)result).ShouldProceed}";
            }
        }

        private void OnLoadFromFileClicked(object sender, RoutedEventArgs e)
        {
            var fd = new OpenFileDialog();
            fd.Title = "Open";
            fd.Filter = "CSScript|*.cs";
            if (fd.ShowDialog() == true)
            {
                ScriptModel.Code = File.ReadAllText(fd.FileName);
            }
        }

        public class Builder
        {
            private ScriptWindow scriptWindow = new();
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
