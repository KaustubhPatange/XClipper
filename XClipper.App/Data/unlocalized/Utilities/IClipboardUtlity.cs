using ClipboardManager.models;
using System.Collections.Generic;
using System.Drawing;

namespace Components
{
    public interface IClipboardUtlity
    {
        List<string> ClipFiles { get; }
        ContentType ClipType { get; set; }
        Image GetClipImage { get; }
        string GetClipText { get; }

        void BindUI(IKeyboardRecorder binder);
    }
}