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
            string internalRawData = RawData;
            // Runs the copy interpreter
            if ((type == ContentTypes.Text || type == ContentTypes.Image) && !string.IsNullOrWhiteSpace(RawData))
            {
                var clipper = Create(RawData, type);
                if (clipper == null) return null;
                if (Interpreter.BatchRunCopyScripts(clipper)) return null;
                
                // Transform data
                if (clipper.RawText != null) internalRawData = clipper.RawText;
                if (clipper.ImagePath != null) internalRawData = clipper.ImagePath;
            }
            
            var table = new TableCopy();
            switch (type)
            {
                case ContentTypes.Text:
                    table.LongText = FormatText(internalRawData);
                    table.Text = table.LongText.Truncate(150);
                    table.RawText = internalRawData;
                    table.ContentType = ContentType.Text;
                    break;
                case ContentTypes.Image:
                    table.Text = table.LongText = "";
                    table.ImagePath = internalRawData;
                    table.ContentType = ContentType.Image;
                    break;
            }
            table.DateTime = table.LastUsedDateTime = DateTime.Now.ToFormattedDateTime();

            return table;
        }

        public static Clipper Create(string RawData, ContentTypes type)
        {
            if (type == ContentTypes.Text)
                return new Clipper(RawData, null, ContentType.Text);
            if (type == ContentTypes.Image)
                return new Clipper(null, RawData, ContentType.Image);
            return null;
        }
    }
}
