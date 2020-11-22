using System.Collections.Specialized;
using System.IO;
using System.Windows.Media.Imaging;
using System.Windows.Forms;
using System.Drawing;

#nullable enable

namespace Components
{
    public static class ClipboardHelper
    {
        private static DataType? type;
        private static object? data;

        public static void Clear()
        {
            Clipboard.Clear();
        }

        public static void SetText(string data)
        {
            Clipboard.SetDataObject(data);
        }

        public static void SetImage(BitmapSource source)
        {
            System.Windows.Clipboard.SetImage(source);
        }

        public static void SetAudio(Stream stream)
        {
            System.Windows.Clipboard.SetAudio(stream);
        }

        public static void SetFileDropList(StringCollection collection)
        {
            Clipboard.SetFileDropList(collection);
        }

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
                Clipboard.Clear();
                switch(type)
                { 
                    case DataType.IMAGE:
                        SetImage((BitmapSource)data);
                        break;
                    case DataType.FILE_DROP:
                        SetFileDropList((StringCollection)data);
                        break;
                    case DataType.TEXT:
                        SetText((string)data);
                        break;
                    case DataType.AUDIO:
                        SetAudio((Stream)data);
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
