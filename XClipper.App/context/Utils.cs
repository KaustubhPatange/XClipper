using ClipboardManager.models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using SQLiteNetExtensions.Attributes;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;
using Components;

namespace ClipboardManager.context
{
    public static class Utils
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
            switch(type)
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
    }
}
