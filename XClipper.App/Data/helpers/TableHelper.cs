using ClipboardManager.models;
using System;
using System.Collections.Generic;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;
using static Components.DefaultSettings;
using static Components.MainHelper;

namespace Components
{
    public static class TableHelper
    {
        public static TableCopy CreateTable(List<string> files)
        {
            var table = new TableCopy();
            table.Text = $"Copied Files - {files.Count}";
            table.LongText = string.Join(",", files.ToArray());
            table.ContentType = ContentType.Files;
            table.DateTime = table.LastUsedDateTime = DateTime.Now.ToFormattedDateTime();

            return table;
        }
        public static TableCopy CreateTable(string RawData, ContentTypes type)
        {
            var table = new TableCopy();
            switch (type)
            {
                case ContentTypes.Text:
                    table.LongText = FormatText(RawData);
                    table.Text = table.LongText.Truncate(150);
                    table.RawText = RawData;
                    table.ContentType = ContentType.Text;
                    break;
                case ContentTypes.Image:
                    table.Text = table.LongText = "";
                    table.ImagePath = RawData;
                    table.ContentType = ContentType.Image;
                    break;
            }
            table.DateTime = table.LastUsedDateTime = DateTime.Now.ToFormattedDateTime();

            return table;
        }
    }
}
