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
using Components.UI;
using static Components.TranslationHelper;

namespace Components.Controls.Settings
{
    public partial class ScriptingPage : UserControl
    {
        public ScriptingPage()
        {
            InitializeComponent();
            (this.Content as FrameworkElement)!.DataContext = this;
        }

        public int SelectedCopyScriptIndex { get; set; }
        public int SelectedPasteScriptIndex { get; set; }

        public bool IsCopyButtonsEnabled => CopyScript.Count > 0 ? SelectedCopyScriptIndex != -1 : false;
        public bool IsPasteButtonsEnabled => PasteScript.Count > 0 ? SelectedPasteScriptIndex != -1 : false;

        public List<Script> CopyScript = new(Interpreter.OnCopyScripts);
        public List<Script> PasteScript = new(Interpreter.OnPasteScripts);
        
        #region Event Handlers
        
        private void OnCopyAddButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalAddButton(CopyScript, () => Interpreter.UpdateCopyScript(CopyScript));
        }

        private void OnCopyEditButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalEditButton(CopyScript, SelectedCopyScriptIndex, () => Interpreter.UpdateCopyScript(CopyScript));
        }

        private void OnCopyDeleteButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalDeleteButton(CopyScript, SelectedCopyScriptIndex, () => Interpreter.UpdateCopyScript(CopyScript));
        }

        private void OnPasteAddButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalAddButton(PasteScript, () => Interpreter.UpdateCopyScript(PasteScript));
        }

        private void OnPasteEditButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalEditButton(PasteScript, SelectedPasteScriptIndex, () => Interpreter.UpdateCopyScript(PasteScript));
        }

        private void OnPasteDeleteButton_Clicked(object sender, RoutedEventArgs e)
        {
            InternalDeleteButton(PasteScript, SelectedPasteScriptIndex, () => Interpreter.UpdateCopyScript(PasteScript));
        }
        
        #endregion

        #region Internal Methods

        private void InternalAddButton(List<Script> scripts, Action OnSave)
        {
            new ScriptWindow.Builder()
                .SetOnSave((script) =>
                {
                    bool exist = scripts.Any(c => c.Code == script.Code);
                    if (!exist)
                    {
                        scripts.Add(script);
                        OnSave.Invoke();
                        MsgBoxHelper.ShowInfo(Translation.SCRIPTING_SCRIPT_SAVED);
                    } else MsgBoxHelper.ShowError(Translation.SCRIPTING_DUPLICATE_EXIST);
                })
                .Show(this.Parent as Window);
        }

        private void InternalEditButton(List<Script> scripts, int index, Action OnSave)
        {
            new ScriptWindow.Builder()
                .SetScript(scripts[index])
                .SetOnSave((script) =>
                {
                    bool exist = scripts.Any(c => c.Code == script.Code);
                    if (!exist)
                    {
                        scripts[index] = script;
                        OnSave.Invoke();
                        MsgBoxHelper.ShowInfo(Translation.SCRIPTING_SCRIPT_SAVED);
                    } else MsgBoxHelper.ShowError(Translation.SCRIPTING_DUPLICATE_EXIST);
                })
                .Show(this.Parent as Window);
        }

        private void InternalDeleteButton(List<Script> scripts, int index, Action OnDelete)
        {
            var result = MessageBox.Show(Translation.SCRIPTING_SCRIPT_DELETE_TEXT,
                Translation.SCRIPTING_SCRIPT_DELETE_TITLE, MessageBoxButton.YesNoCancel, MessageBoxImage.Warning);
            if (result == MessageBoxResult.Yes)
            {
                scripts.RemoveAt(index);
                OnDelete.Invoke();
            }
        }

        #endregion
    }
}
