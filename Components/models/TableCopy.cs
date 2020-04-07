using SQLite;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ClipboardManager.models
{

    public class TableCopy
    {
        [PrimaryKey, AutoIncrement]
        public int Id { get; set; }
        public string Text { get; set; }
        public string ContentType { get; set; }
        public string DateTime { get; set; }
        public int FileCount { get; set; }
    }
}
