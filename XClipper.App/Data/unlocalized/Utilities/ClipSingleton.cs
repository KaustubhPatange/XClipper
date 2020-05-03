using ClipboardManager.models;
using Components.viewModels;
using System;
using System.Collections.Generic;
using System.IO;
using WK.Libraries.SharpClipboardNS;
using System.Linq;
using static Components.WhatToStoreHelper;
using static Components.MainHelper;
using static Components.DefaultSettings;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;
using static Components.Constants;
using static Components.TableHelper;

namespace Components
{
    /** This singleton will be used to manage clipboard activities. */
    public sealed class ClipSingleton
    {
        #region Variable Declarations

        private SharpClipboard _clipboardFactory = new SharpClipboard();
        private bool isFirstLaunch = true;
        private bool ToRecord = false;
        private static ClipSingleton Instance;

        #endregion

        #region Contructor
        public static ClipSingleton GetInstance
        {
            get
            {
                if (Instance == null)
                    Instance = new ClipSingleton();
                return Instance;
            }
        }
        private ClipSingleton()
        {
            _clipboardFactory.ClipboardChanged += ClipboardChanged;
        }

        #endregion

        #region Methods

        /// <summary>
        /// Start monitoring clipboard activities.
        /// </summary>
        /// 
        public void StartRecording()
        {
            ToRecord = true;
        }
        /// <summary>
        /// Stop monitoring clipboard activities.
        /// </summary>
        public void StopRecording()
        {
            ToRecord = false;
        }

        /// <summary>
        /// This provides a block to perform actions with clipboard manager
        /// without monitoring it.
        /// </summary>
        /// <param name="block"></param>
        public void Ignore(Action block)
        {
            StopRecording();
            block.Invoke();
            StartRecording();
        } 

        #endregion

        #region Clipboard Capture Events

        private void ClipboardChanged(Object sender, ClipboardChangedEventArgs e)
        {
            /* There is a bug in library which automtically triggers this whenever
             * app is launched first time so I did a hack to fallback this call.
             */
            if (isFirstLaunch)
            {
                isFirstLaunch = false;
                return;
            }

            if (!ToRecord)
                return;

            /* We will capture copy/cut Text, Image (eg: PrintScr) and Files
             * and save it to database.
             */

            if (e.ContentType == ContentTypes.Text && ToStoreTextClips())
            {
                if (!string.IsNullOrWhiteSpace(_clipboardFactory.ClipboardText.Trim()))
                {
                    AppSingleton.GetInstance.InsertContent(CreateTable(_clipboardFactory.ClipboardText, ContentTypes.Text));
                }
            }
            else if (e.ContentType == ContentTypes.Image && ToStoreImageClips())
            {

                if (!Directory.Exists("Images")) Directory.CreateDirectory("Images");

                string filePath = Path.Combine(BaseDirectory, $"Images\\{DateTime.Now.ToFormattedDateTime()}.png");
                _clipboardFactory.ClipboardImage.Save(filePath);

                AppSingleton.GetInstance.InsertContent(CreateTable(filePath, ContentTypes.Image));
            }
            else if (e.ContentType == ContentTypes.Files && ToStoreFilesClips())
            {

                AppSingleton.GetInstance.InsertContent(CreateTable(_clipboardFactory.ClipboardFiles));

                _clipboardFactory.ClipboardFiles.Clear();
            }
        }

        #endregion
     
    }
}
