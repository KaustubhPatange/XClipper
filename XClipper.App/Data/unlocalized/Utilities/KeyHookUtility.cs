using Gma.System.MouseKeyHook;
using Loamen.KeyMouseHook;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Windows.Threading;
using static Components.DefaultSettings;
using static Components.KeyPressHelper;

#nullable enable

namespace Components
{
    public sealed class KeyHookUtility : IDisposable
    {
        #region Variable Declaration

        private KeyMouseFactory eventHookFactory;
        private KeyboardWatcher keyboardWatcher;
        private Action? hotKeyEvent;
        private Action<int>? quickPasteEvent;

        public KeyHookUtility()
        {
            eventHookFactory = new KeyMouseFactory(Hook.GlobalEvents());
            keyboardWatcher = eventHookFactory.GetKeyboardWatcher();
            if (UseExperimentalKeyCapture)
                keyboardWatcher.OnKeyboardInput += KeyboardWatcher_OnKeyboardInput2;
            else
                keyboardWatcher.OnKeyboardInput += KeyboardWatcher_OnKeyboardInput;

            keyboardWatcher.Start(Hook.GlobalEvents());
        }

        #endregion

        #region Methods

        /// <summary>
        /// Notifies when the hot keys are pressed.
        /// </summary>
        /// <param name="block"></param>
        public void SubscribeHotKeyEvents(Action block)
        {
            hotKeyEvent = block;
        }

        /// <summary>
        /// Notifies when a quick paste is occurred from Ctrl + K
        /// </summary>
        /// <param name="block"></param>
        public void SubscribeQuickPasteEvent(Action<int> block)
        {
            quickPasteEvent = block;
        }

        public void UnsubscribeAll()
        {
            Debug.WriteLine("Dispose");
            Dispose();
        }

        #endregion

        #region Keyboard Capture Events

        private List<Keys> keyStreams = new List<Keys>();
        private const int KEY_STORE_SIZE = 4;

        private long TIME_LAST_KEY_OFFSET = 0;
        private bool quickPasteChord = false;
        private bool active = false;
        private void KeyboardWatcher_OnKeyboardInput2(object sender, MacroEvent e)
        {
            var keyEvent = e.EventArgs as KeyEventArgs;
            if (keyEvent == null) return;
            var key = keyEvent.KeyCode;
            if (keyStreams.Count >= KEY_STORE_SIZE)
                keyStreams.RemoveAt(0);

            var timeLong = DateTime.Now.ToString("yyyyMMddHHmmssfff").ToLong();

            if (active)
            {
                if (TIME_LAST_KEY_OFFSET > 0 && timeLong - TIME_LAST_KEY_OFFSET <= 500)
                {
                    Debug.WriteLine("IsActive");
                    return;
                }
                active = false;
            }

            if (TIME_LAST_KEY_OFFSET > 0 && timeLong - TIME_LAST_KEY_OFFSET >= 500)
                keyStreams.Clear();
            TIME_LAST_KEY_OFFSET = timeLong;

            if (!keyStreams.Any(c => c == key))
                keyStreams.Add(key);
            else
                return;

            bool isCtrl;
            if (DefaultSettings.IsCtrl)
                isCtrl = keyStreams.Any(c => c == Keys.LControlKey || c == Keys.RControlKey);
            else
                isCtrl = true;

            bool isShift;
            if (DefaultSettings.IsShift)
                isShift = keyStreams.Any(c => c == Keys.LShiftKey || c == Keys.RShiftKey);
            else
                isShift = true;

            bool isAlt;
            if (DefaultSettings.IsAlt)
                isAlt = keyStreams.Any(c => c == Keys.Alt);
            else
                isAlt = true;

            Keys hotKey = DefaultSettings.HotKey.ToEnum<Keys>();
            bool isHotKey = keyStreams.Any(c => c == hotKey);

            if (isCtrl && isAlt && isShift && isHotKey)
            {
                active = true;
                hotKeyEvent?.Invoke();
            }

            if (quickPasteChord && IsNumericKeyPressed(key))
            {
                Debug.WriteLine("Quick Paste: Done");
                DoQuickPaste2(ParseNumericKey(key));
            }

            quickPasteChord = false;

            if (keyStreams.Any(c => c == Keys.LControlKey || c == Keys.RControlKey) && keyStreams.Any(c => c == Keys.Oem5))
            {
                Debug.WriteLine("QuickPaste chord activated");
                quickPasteChord = true;
                keyStreams.Clear();
            }

            Debug.WriteLine($"Keystream size: {keyStreams.Count}, Contents: [{string.Join(",", keyStreams)}], isCtrl: {isCtrl}, isAlt: {isAlt}, isShift: {isShift}, isHotKey: {isHotKey}");

            //if (e.KeyMouseEventType == MacroEventType.KeyDown)
            //{
            //    prevKeyDown = key;
            //}
            //if (e.KeyMouseEventType == MacroEventType.KeyUp)
            //{
            //    prevKeyUp = key;
            //}

            //if ((prevKeyUp == Keys.LControlKey || prevKeyDown == Keys.RControlKey) && prevKeyDown == Keys.Oem3)
            //{
            //    prevKeyUp = null;
            //    prevKeyDown = null;
            //    hotKeyEvent?.Invoke();
            //}
            //Debug.WriteLine($"Key: {keyEvent.KeyCode}, Type: {e.KeyMouseEventType}, PrevKeyUp: {prevKeyUp}, PrevKeyDown: {prevKeyDown}");

        }

        private void KeyboardWatcher_OnKeyboardInput(object sender, MacroEvent e)
        {
            var keyEvent = (KeyEventArgs)e.EventArgs;
            Debug.WriteLine($"Key: {keyEvent.KeyCode}, Type: {e.KeyMouseEventType}");
            if (e.KeyMouseEventType == MacroEventType.KeyUp || e.KeyMouseEventType == MacroEventType.KeyPress)
            {
                var key = keyEvent.KeyCode;

                if (quickPasteChord && IsNumericKeyPressed(key))
                {
                    Debug.WriteLine("Quick Paste: Done");
                    DoQuickPaste(ParseNumericKey(key));
                }

                var isCtrl = IsCtrlPressed();
                var isAlt = IsAltPressed();
                var isShift = IsShiftPressed();

                if (!isCtrl && !isAlt && !isShift)
                    quickPasteChord = false;

                // Process other keystrokes...
                if (isCtrl && key == Keys.Oem5)
                {
                    Debug.WriteLine("QuickPaste chord activated");
                    quickPasteChord = true;
                }

                //LogHelper.LogKey(key.ToString());
                
                if (IsCtrl && !isCtrl) return;

                if (IsAlt && !isAlt) return;

                if (IsShift && !isShift) return;

                if (key != HotKey.ToEnum<Keys>()) return;

                //LogHelper.LogKey("Application should launch", true);

                hotKeyEvent?.Invoke();
            }
        }

        #endregion

        private void DoQuickPaste2(int index)
        {
            Task.Run(async () =>
            {
                SendKeys.SendWait("{BKSP}");
                await Task.Delay(100).ConfigureAwait(false);
                System.Windows.Application.Current.Dispatcher.Invoke(delegate
                {
                    if (index == 0)
                        quickPasteEvent?.Invoke(9);
                    else quickPasteEvent?.Invoke(index - 1);
                });
            });
        }

        private void DoQuickPaste(int index)
        {
            SendKeys.SendWait("{BKSP}");
            if (index == 0)
                quickPasteEvent?.Invoke(9);
            else quickPasteEvent?.Invoke(index - 1);
        }

        public void Dispose()
        {
            quickPasteEvent = null;
            hotKeyEvent = null;
            eventHookFactory.Dispose();
            keyboardWatcher.Dispose();
            GC.SuppressFinalize(this);
        }
    }
}
