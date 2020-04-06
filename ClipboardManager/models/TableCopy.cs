using SQLite;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ClipboardManager.context;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;

namespace ClipboardManager.models
{

    public class TableCopy
    {
        public TableCopy(string Text, ContentTypes type)
        {
            this.Text = Text;
            this.ContentType = type;
            this.DateTime = System.DateTime.Now.ToFormattedDateTime();
            if (type == ContentTypes.Files && Text.Contains(","))
            {
                this.FileCount = Text.Split(',').Length;
            } else if (type == ContentTypes.Files) this.FileCount = 1;
            else this.FileCount = 0;
        }

        [PrimaryKey, AutoIncrement]
        public int Id { get; set; }
        public string Text { get; set; }
        public ContentTypes ContentType { get; set; }
        public string DateTime { get; set; }
        public int FileCount { get; set; }
    }
}
