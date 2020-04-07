using ClipboardManager.models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;

namespace ClipboardManager.context
{
    public static class Utils
    {
        public static TableCopy CreateTable(string Text, ContentTypes type)
        {
            var table = new TableCopy();
            table.Text = Text;

            string typeText = "Text";
            switch(type)
            {
                case ContentTypes.Text: typeText = "Text"; break;
                case ContentTypes.Image: typeText = "Image"; break;
                case ContentTypes.Files: typeText = "Files"; break;
            }

            table.ContentType = typeText;
            table.DateTime = System.DateTime.Now.ToFormattedDateTime();
            if (type == ContentTypes.Files && Text.Contains(","))
            {
                table.FileCount = Text.Split(',').Length;
            }
            else if (type == ContentTypes.Files) table.FileCount = 1;
            else table.FileCount = 0;
            return table;
        }
    }
}
