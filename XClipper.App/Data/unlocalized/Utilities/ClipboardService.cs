using Components.viewModels;
using System;
using System.IO;
using static Components.WhatToStoreHelper;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;
using static Components.Constants;
using static Components.TableHelper;
using ClipboardManager.models;
using System.Diagnostics;

namespace Components
{
    /** This will be used to manage clipboard activities. */
    public class ClipboardService : IKeyboardRecorder
    {
        #region Variable Declarations

        private bool ToRecord = false;
        private IClipboardUtlity binder;

        #endregion

        #region Contructor
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
            block.Invoke();
            StartRecording();
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

                string filePath = Path.Combine(ImageFolder, $"Images\\{DateTime.Now.ToFormattedDateTime()}.png");
                binder.GetClipImage.Save(filePath);

                AppSingleton.GetInstance.InsertContent(CreateTable(filePath, ContentTypes.Image));
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
