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

namespace Components
{
    public class ClipboardUtility
    {
        #region Variable Declarations

        private SharpClipboard _clipboardFactory = new SharpClipboard();
        private bool isFirstLaunch = true;
        private bool ToRecord = false;

        #endregion

        #region Contructor

        public ClipboardUtility()
        {
            _clipboardFactory.ClipboardChanged += ClipboardChanged;
        }

        #endregion

        #region Methods

        public void StartRecording()
        {
            ToRecord = true;
        }
        public void StopRecording()
        {
            ToRecord = false;
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
                    InsertContent(CreateTable(_clipboardFactory.ClipboardText, ContentTypes.Text));
                }
            }
            else if (e.ContentType == ContentTypes.Image && ToStoreImageClips())
            {

                if (!Directory.Exists("Images")) Directory.CreateDirectory("Images");

                string filePath = Path.Combine(App.BaseDirectory, $"Images\\{DateTime.Now.ToFormattedDateTime()}.png");
                _clipboardFactory.ClipboardImage.Save(filePath);

                InsertContent(CreateTable(filePath, ContentTypes.Image));
            }
            else if (e.ContentType == ContentTypes.Files && ToStoreFilesClips())
            {

                InsertContent(CreateTable(_clipboardFactory.ClipboardFiles));

                _clipboardFactory.ClipboardFiles.Clear();
            }
        }

        #endregion

        #region Insert Content

        private void InsertContent(TableCopy model)
        {
            // Implementation of setting TotalClipLength 
            var list = AppSingleton.GetInstance.dataDB.Query<TableCopy>("select * from TableCopy").OrderByDescending(s => ParseDateTimeText(s.LastUsedDateTime)).ToList();
            foreach (var c in list)
            {
                if (c.ContentType == model.ContentType)
                {
                    switch (model.ContentType)
                    {
                        case ContentType.Text:
                            if (model.Text == c.Text) return;
                            break;
                        case ContentType.Image:
                            if (model.ImagePath == c.ImagePath) return;
                            break;
                        case ContentType.Files:
                            if (model.LongText == c.LongText) return;
                            break;
                    }
                }
            }
            if (list.Count >= TotalClipLength) list.RemoveAt(list.Count - 1);

            AppSingleton.GetInstance.dataDB.Insert(model);
        }

        public TableCopy CreateTable(List<string> files)
        {
            var table = new TableCopy();
            table.Text = $"Copied Files - {files.Count}";
            table.LongText = string.Join(",", files.ToArray());
            table.ContentType = ContentType.Files;
            table.DateTime = table.LastUsedDateTime = DateTime.Now.ToFormattedDateTime();
            return table;
        }
        public TableCopy CreateTable(string RawData, ContentTypes type)
        {
            var table = new TableCopy();
            switch (type)
            {
                case ContentTypes.Text:
                    table.Text = table.LongText = MainHelper.FormatText(RawData);
                    table.RawText = RawData;
                    table.ContentType = ContentType.Text;
                    break;
                case ContentTypes.Image:
                    table.Text = "";
                    table.LongText = "";
                    table.ImagePath = RawData;
                    table.ContentType = ContentType.Image;
                    break;
            }
            table.DateTime = table.LastUsedDateTime = DateTime.Now.ToFormattedDateTime();
            return table;
        }

        #endregion
    }
}
