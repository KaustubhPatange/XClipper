using ClipboardManager.models;
using Components.viewModels;
using System;
using System.Drawing;
using System.IO;
using System.Runtime.InteropServices;
using System.Windows;
using System.Windows.Media.Imaging;
using WK.Libraries.SharpClipboardNS;

#nullable enable

namespace Components
{
    public class QuickPasteHelper
    {
        #region Variable Declaration

        private IKeyboardRecorder? recorder;

        #endregion

        #region Public methods

        public void Init(IKeyboardRecorder recorder)
        {
            this.recorder = recorder;
        }

        [STAThread]
        public void DoPasteAction(int clipNumber)
        {
            var model = AppSingleton.GetInstance.GetDataAt(clipNumber);
            if (model != null)
            {
               try
                {
                    switch (model.ContentType)
                    {
                        case ContentType.Text:
                            recorder?.Ignore(() =>
                            {
                                ClipboardHelper.Preserve();
                                Clipboard.SetText(model.RawText);
                                System.Windows.Forms.SendKeys.SendWait("^v");
                                ClipboardHelper.Consume();
                            });
                            break;
                        case ContentType.Image:
                            recorder?.Ignore(() =>
                            {
                                if (File.Exists(model.ImagePath))
                                {
                                    ClipboardHelper.Preserve();
                                    Clipboard.SetImage(new BitmapImage(new Uri(model.ImagePath)));
                                    ClipboardHelper.Consume();
                                }
                            });
                            break;

                        case ContentType.Files:
                            // todo:
                            break;
                    }
                }catch (ExternalException)
                {
                    // Cannot access clipboard at this time
                }
            }
        }

        #endregion
    }
}
