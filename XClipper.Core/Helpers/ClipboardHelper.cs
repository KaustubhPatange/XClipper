using System.Collections.Specialized;
using System.IO;
using System.Windows;
using System.Windows.Media.Imaging;

#nullable enable

namespace Components
{
    public static class ClipboardHelper
    {
        private static DataType? type;
        private static object? data;

        /// <summary>
        /// This will return the contents of clipboard.
        /// Note: They shouldn't be stored in memory for long time. Consume it quickly as you can!
        /// </summary>
        /// <returns></returns>
        public static void Preserve()
        {
            if (Clipboard.ContainsText())
            {
                data = Clipboard.GetText();
                type = DataType.TEXT;
            } else if (Clipboard.ContainsFileDropList())
            {
                data = Clipboard.GetFileDropList();
                type = DataType.FILE_DROP;
            } else if (Clipboard.ContainsAudio())
            {
                data = Clipboard.GetAudioStream();
                type = DataType.AUDIO;
            } else if (Clipboard.ContainsImage())
            {
                data = Clipboard.GetImage();
                type = DataType.IMAGE;
            }
        }

        /// <summary>
        /// Consumes the last preserved clipboard data
        /// </summary>
        public static void Consume()
        {
            if (data != null && type != null)
            {
                switch(type)
                {
                    case DataType.IMAGE:
                        Clipboard.SetImage((BitmapSource)data);
                        break;
                    case DataType.FILE_DROP:
                        Clipboard.SetFileDropList((StringCollection)data);
                        break;
                    case DataType.TEXT:
                        Clipboard.SetText((string)data);
                        break;
                    case DataType.AUDIO:
                        Clipboard.SetAudio((Stream)data);
                        break;
                }
                data = null;
                type = null;
            }
        }

        /// <summary>
        /// Returns the preserved clipboard type.
        /// </summary>
        /// <returns></returns>
        public static DataType GetPreservedClipboardType()
        {
            return type ?? DataType.NONE;
        }

        public enum DataType
        {
            TEXT,
            IMAGE,
            FILE_DROP,
            AUDIO,
            NONE
        }
    }
}
