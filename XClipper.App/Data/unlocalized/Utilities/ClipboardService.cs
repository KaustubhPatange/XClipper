using Components.viewModels;
using System;
using System.IO;
using static Components.WhatToStoreHelper;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;
using static Components.Constants;
using static Components.TableHelper;
using ClipboardManager.models;
using System.Diagnostics;
using System.Windows.Threading;

#nullable enable

namespace Components
{
    /** This will be used to manage clipboard activities. */
    public class ClipboardService : IKeyboardRecorder
    {
        #region Variable Declarations

        private bool ToRecord = false;
        private IClipboardUtlity binder;
        private IClipServiceBinder? appBinder;

        #endregion

        #region Constructor
        public ClipboardService(IClipboardUtlity utility)
        {
            binder = utility;
            binder.BindUI(this);
        }

        #endregion

        #region IKeyboardRecorder Methods

        public void StartRecording()
        {
            ToRecord = true;
        }
        public void StopRecording()
        {
            ToRecord = false;
        }


        public void Ignore(Action block)
        {
            StopRecording();
            block?.Invoke();
            StartRecording();
        }

        public void SetAppBinder(IClipServiceBinder binder)
        {
            appBinder = binder;
        }

        #endregion

        #region Clipboard Capture Events

        public void OnChanged()
        {
            if (!ToRecord)
                return;

            /* We will capture copy/cut Text, Image (eg: PrintScr) and Files
             * and save it to database.
             */

            if (binder.ClipType == ContentType.Text && ToStoreTextClips())
            {
                if (!string.IsNullOrWhiteSpace(binder.GetClipText.Trim()))
                {
                    AppSingleton.GetInstance.InsertContent(CreateTable(binder.GetClipText, ContentTypes.Text));
                }
            }
            else if (binder.ClipType == ContentType.Image && ToStoreImageClips())
            {

                if (!Directory.Exists(ImageFolder)) Directory.CreateDirectory(ImageFolder);

                string filePath = Path.Combine(ImageFolder, $"{DateTime.Now.ToFormattedDateTime()}.png");

                // We will write it to memory stream before saving
                using (MemoryStream memory = new MemoryStream())
                {
                    using (FileStream fs = new FileStream(filePath, FileMode.Create, FileAccess.ReadWrite))
                    {
                        try
                        {
                            binder.GetClipImage.Save(memory, System.Drawing.Imaging.ImageFormat.Png);
                            byte[] bytes = memory.ToArray();
                            fs.Write(bytes, 0, bytes.Length);
                            fs.Flush();
                            fs.Close();
                            AppSingleton.GetInstance.InsertContent(CreateTable(filePath, ContentTypes.Image));
                        }
                        catch (Exception ex)
                        {
                            // There is a GDI+ as well as NullReference exception causing from below code.
                            LogHelper.Log(this, ex.Message + ex.StackTrace);
                            appBinder?.OnImageSaveFailed();
                        }
                    }
                }

            }
            else if (binder.ClipType == ContentType.Files && ToStoreFilesClips())
            {
                AppSingleton.GetInstance.InsertContent(CreateTable(binder.ClipFiles));

                binder.ClipFiles.Clear();
            }
        }

        #endregion
    }
}
