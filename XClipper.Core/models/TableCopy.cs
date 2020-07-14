using SQLite;
using SQLiteNetExtensions.Attributes;
namespace ClipboardManager.models
{
    public class TableCopy
    {
        [PrimaryKey, AutoIncrement]
        public int Id { get; set; }

        /// <summary>
        ///  Main Text which will be displayed in the Card Title.
        /// </summary>
        public string Text { get; set; }
        /// <summary>
        /// Sub Text which will be displayed in the Card Subtitle.
        /// </summary>
        public string LongText { get; set; }
        /// <summary>
        /// This will decide the content type of the TableCopy.
        /// </summary>
        public ContentType ContentType { get; set; }
        /// <summary>
        /// Date and time in string when this clipboard has been added.
        /// </summary>
        public string DateTime { get; set; }
        /// <summary>
        /// This will store the last time this clip was used in string.
        /// </summary>
        public string LastUsedDateTime { get; set; }
        /// <summary>
        /// This will contain image path of the clipboard image file.
        /// </summary>
        public string ImagePath { get; set; }
        /// <summary>
        /// This will store the unformatted text.
        /// </summary>
        public string RawText { get; set; }
        /// <summary>
        /// This will tell if the given item is pinned or not.
        /// </summary>
        public bool IsPinned { get; set; } = false;

        /// <summary>
        /// This will return this model for binding purpose.
        /// </summary>
        public TableCopy Model { get { return this; } }
    }

    public enum ContentType
    {
        Text = 0, Image = 1, Files = 2
    }
}
