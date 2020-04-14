using SQLite;
using SQLiteNetExtensions.Attributes;
namespace ClipboardManager.models
{
    public class TableCopy
    {
        [PrimaryKey, AutoIncrement]
        public int Id { get; set; }

        // Main Text which will be displayed in the Card Title.
        public string Text { get; set; }
        // Sub Text which will be displayed in the Card Subtitle.
        public string LongText { get; set; }
        // This will decide the content type of the TableCopy.
        public ContentType ContentType { get; set; }
        // Date and time in string when this clipboard has been added.
        public string DateTime { get; set; }
        //// This will show the file count in the files. (Only specific to file type)
        //public int FileCount { get; set; }

        // This will contain image path of the clipboard image file.
        public string ImagePath { get; set; }
        // This will store the unformatted text.
        public string RawText { get; set; }
        // This will tell if the given item is pinned or not.
        public bool IsPinned { get; set; } = false;

        // This will return this model for binding purpose.
        public TableCopy Model { get { return this; } }
    }

    public enum ContentType
    {
        Text = 0, Image = 1, Files = 2
    }
}
