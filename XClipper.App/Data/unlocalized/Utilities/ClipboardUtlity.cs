using ClipboardManager.models;
using System.Collections.Generic;
using System.Drawing;
using WK.Libraries.SharpClipboardNS;

namespace Components
{
    public class ClipboardUtlity : IClipboardUtlity
    {
        private bool isFirstLaunch = true;
        private SharpClipboard factory = new SharpClipboard();
        public void BindUI(IKeyboardRecorder binder)
        {
            factory.ClipboardChanged += (o, e) =>
            {
                /* There is a bug in library which automtically triggers this whenever
                 * app is launched first time so I did a hack to fallback this call.
                 */
                if (isFirstLaunch)
                {
                    isFirstLaunch = false;
                    return;
                }

                if (e.ContentType != SharpClipboard.ContentTypes.Other)
                {
                    ClipType = (ContentType)(int)e.ContentType;
                    binder.OnChanged();
                }
            };
        }

        public ContentType ClipType { get; set; }

        public string GetClipText
        {
            get { return factory.ClipboardText; }
        }

        public List<string> ClipFiles
        {
            get { return factory.ClipboardFiles; }
        }

        public Image GetClipImage
        {
            get { return factory.ClipboardImage; }
        }

    }
}
