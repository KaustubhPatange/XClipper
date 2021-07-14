using ClipboardManager.models;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Runtime.CompilerServices;
using WK.Libraries.SharpClipboardNS;

namespace Components
{
    public sealed class ClipboardUtlity : IClipboardUtlity, IDisposable
    {
        private bool isFirstLaunch = true;

        private long lastRecordMilliSeconds = 0;
        private long offset = 500;
        private SharpClipboard factory = new SharpClipboard();
        private ImageCompare imageCompare = new ImageCompare();

        private Bitmap lastImageSaved;

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
        public void BindUI(IKeyboardRecorder binder)
        {
            factory.ClipboardChanged += (o, e) =>
            {
                try
                {
                    /* There is a bug in library which automtically triggers this whenever
                 * app is launched first time so I did a hack to fallback this call.
                 */
                    if (isFirstLaunch)
                    {
                        isFirstLaunch = false;
                        return;
                    }

                    /**
                     * We will debounce the record span by the <see cref="offset"/>
                     * to prevent any duplicate capture.
                     */
                    var currentMilliSeconds = GetCurrentMilliSeconds();

                    if ((lastRecordMilliSeconds + offset) > currentMilliSeconds)
                        return;

                    lastRecordMilliSeconds = GetCurrentMilliSeconds();

                    if (e.ContentType != SharpClipboard.ContentTypes.Other)
                    {
                        ClipType = (ContentType)(int)e.ContentType;
                        if (e.ContentType == SharpClipboard.ContentTypes.Image)
                        {
                            if (ProcessImage()) return;
                        }
                        binder.OnChanged();
                    }
                } catch { }
            };
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        private bool ProcessImage()
        {
            try
            {
                var bmp = new Bitmap(GetClipImage);
                if (lastImageSaved != null)
                {
                    bool areSame = imageCompare.IsMatch(bmp, lastImageSaved);
                    if (areSame)
                    {
                        lastImageSaved = new Bitmap(bmp);
                        bmp.Dispose();
                        return true;
                    }
                }
                lastImageSaved = new Bitmap(bmp);
                bmp.Dispose();
            }
            catch (Exception)
            { }
            return false;
        }

        public void Dispose()
        {
            factory.Dispose();
            lastImageSaved.Dispose();
            GC.SuppressFinalize(this);
        }

        #region Private Methods

        private long GetCurrentMilliSeconds()
        {
            return DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
        }

        #endregion
    }
}
