using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.IO;
using System.Windows.Media.Imaging;
using System.Windows.Forms;
using System.Drawing;
using System.Runtime.CompilerServices;
using System.Threading;
using ClipboardManager.models;

#nullable enable

namespace Components
{
    public static class ClipboardHelper
    {
        private static DataType? type;
        private static object? data;
        private static List<IClipboardListener> _listeners = new();
        
        public interface IClipboardListener
        {
            void OnGoingClipboardAction();
            void OnCompleteClipboardAction();
        }

        /// <summary>
        /// Registers a listener which allows us to listen clipboard pastes.
        /// </summary>
        /// <param name="listener"></param>
        public static void AddListener(IClipboardListener listener)
        {
            _listeners.Add(listener);
        }
        
        public static void RemoveListener(IClipboardListener listener)
        {
            _listeners.Remove(listener);
        }

        #region Clipboard simulation methods

        /// <summary>
        /// Simulates clipboard copy by first preserving the existing data sending Copy command,
        /// capture the clipboard & restore original clipboard.
        /// Currently only text data type is supported.
        /// </summary>
        /// <returns></returns>
        [MethodImpl(MethodImplOptions.Synchronized)]
        public static string PerformClipboardCopy()
        {
            _listeners.ForEach(l => l.OnGoingClipboardAction());
            string data = string.Empty;
            try
            {
                Preserve();
                System.Windows.Forms.SendKeys.SendWait("^c");
                Thread.Sleep(100);
                data = Clipboard.GetText();
                Consume();
            } catch { }
            _listeners.ForEach(l => l.OnCompleteClipboardAction());
            return data;
        }
        
        /// <summary>
        /// Simulates clipboard copy by first preserving the existing data sending Cut command,
        /// capture the clipboard & restore original clipboard.
        /// Currently only text data type is supported.
        /// </summary>
        /// <returns></returns>
        [MethodImpl(MethodImplOptions.Synchronized)]
        public static string PerformClipboardCut()
        {
            _listeners.ForEach(l => l.OnGoingClipboardAction());
            string data = string.Empty;
            try
            {
                Preserve();
                System.Windows.Forms.SendKeys.SendWait("^x");
                Thread.Sleep(100);
                data = Clipboard.GetText();
                Consume();
            } catch { }
            _listeners.ForEach(l => l.OnCompleteClipboardAction());
            return data;
        }
        
        /// <summary>
        /// Simulates clipboard pasting by first preserving the existing data, setting incoming data &
        /// performs Ctrl + V action to paste the data.
        /// </summary>
        [MethodImpl(MethodImplOptions.Synchronized)]
        public static void PerformClipboardPaste(string text)
        {
            _listeners.ForEach(l => l.OnGoingClipboardAction());
            string transformed = text;
            try
            {
                // Run paste scripts interpreter.
                if (!AbortPasteScripts(text, out transformed))
                {
                    Preserve();
                    SetText(transformed);
                    System.Windows.Forms.SendKeys.SendWait("^v");
                    Thread.Sleep(100);
                    Consume();
                }
            }
            catch (Exception e)
            {
                Debug.WriteLine(e.Message + "\n" +e.StackTrace);
            }
            _listeners.ForEach(l => l.OnCompleteClipboardAction());
        }

        #endregion

        public static void Clear()
        {
            Clipboard.Clear();
        }

        public static void SetText(string data)
        {
            try
            {
                Clipboard.SetText(data);
            } catch {  Clipboard.SetDataObject(data); }
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
                        if (data is BitmapSource)
                            SetImage((BitmapSource)data);
                        else if (data is System.Drawing.Bitmap)
                            SetImage(ToBitmapSource((System.Drawing.Bitmap)data));
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

        private static BitmapSource ToBitmapSource(System.Drawing.Bitmap bmp)
        {
            return System.Windows.Interop.Imaging.CreateBitmapSourceFromHBitmap(
                bmp.GetHbitmap(), 
                IntPtr.Zero, 
                System.Windows.Int32Rect.Empty, 
                BitmapSizeOptions.FromWidthAndHeight(bmp.Width, bmp.Height));
        }

        private static bool AbortPasteScripts(string text, out string transform)
        {
            var clip = Clipper.ForTextType(text);
            var result = Interpreter.BatchRunPasteScripts(clip);
            transform = clip.RawText;
            return result;
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
