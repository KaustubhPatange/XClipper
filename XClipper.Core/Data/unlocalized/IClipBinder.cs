using ClipboardManager.models;
using System.Collections.Generic;

namespace Components.viewModels
{
    public interface IClipBinder
    {
        void OnPopupTextEdited(List<TableCopy> models);
        void OnModelDeleted(List<TableCopy> models);
        void OnFilterTextEdit(string Text);
        void OnExitRequest();
    }
}
