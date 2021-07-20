using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Diagnostics;
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
using Components.UI;

namespace Components.Controls.Settings
{
    public partial class ScriptingPage : UserControl
    {
        public ScriptingPage()
        {
            InitializeComponent();
            (this.Content as FrameworkElement)!.DataContext = this;
            Interpreter.OnCopyScripts.CollectionChanged += (o, e) => _copylistView.ItemsSource = Interpreter.OnCopyScripts;
            Interpreter.OnPasteScripts.CollectionChanged += (o, e) => _pastelistView.ItemsSource = Interpreter.OnPasteScripts;
        }

        public int SelectedCopyScriptIndex { get; set; }
        public int SelectedPasteScriptIndex { get; set; }

        public bool IsCopyButtonsEnabled => Interpreter.OnCopyScripts.Count > 0 ? SelectedCopyScriptIndex != -1 : false;
        public bool IsPasteButtonsEnabled => Interpreter.OnCopyScripts.Count > 0 ? SelectedPasteScriptIndex != -1 : false;

        #region Event Handlers
        
        private void OnCopyAddButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalAddButton(Interpreter.OnCopyScripts);
        }

        private void OnCopyEditButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalEditButton(Interpreter.OnCopyScripts, SelectedCopyScriptIndex);
        }

        private void OnCopyDeleteButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalDeleteButton(Interpreter.OnCopyScripts, SelectedCopyScriptIndex);
        }

        private void OnPasteAddButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalAddButton(Interpreter.OnPasteScripts);
        }

        private void OnPasteEditButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalEditButton(Interpreter.OnPasteScripts, SelectedPasteScriptIndex);
        }

        private void OnPasteDeleteButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalDeleteButton(Interpreter.OnPasteScripts, SelectedPasteScriptIndex);
        }
        
        #endregion

        #region Internal Methods

        private void InternalAddButton(ObservableCollection<Script> scripts)
        {
            new ScriptWindow.Builder()
                .SetOnSave((script) =>
                {
                    bool exist = scripts.Any(c => c.Code == script.Code);
                    if (!exist)
                    {
                        scripts.Add(script);
                        MsgBoxHelper.ShowInfo(Translation.SCRIPTING_SCRIPT_SAVED);
                    } else MsgBoxHelper.ShowError(Translation.SCRIPTING_DUPLICATE_EXIST);
                })
                .Show(Window.GetWindow(this));
        }

        private void InternalEditButton(ObservableCollection<Script> scripts, int index)
        {
            new ScriptWindow.Builder()
                .SetScript(scripts[index])
                .SetOnSave((script) =>
                {
                    scripts[index] = script;
                    MsgBoxHelper.ShowInfo(Translation.SCRIPTING_SCRIPT_SAVED);
                })
                .Show(Window.GetWindow(this));
        }

        private void InternalDeleteButton(ObservableCollection<Script> scripts, int index)
        {
            var result = MessageBox.Show(Translation.SCRIPTING_SCRIPT_DELETE_TEXT,
                Translation.SCRIPTING_SCRIPT_DELETE_TITLE, MessageBoxButton.YesNoCancel, MessageBoxImage.Warning);
            if (result == MessageBoxResult.Yes)
            {
                scripts.RemoveAt(index);
            }
        }

        #endregion
    }
}
