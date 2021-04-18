using Gma.System.MouseKeyHook;
using Loamen.KeyMouseHook;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;
using static Components.DefaultSettings;

#nullable enable

namespace Components
{
    public sealed class KeyHookUtility : IDisposable
    {
        #region Variable Declaration

        private KeyMouseFactory eventHookFactory;
        private KeyboardWatcher keyboardWatcher;
        private Action? hotKeyEvent;
        private Action? pasteEvent;
        private Action<int>? quickPasteEvent;

        public KeyHookUtility()
        { }

        #endregion

        #region Methods

        public void Init()
        {
            eventHookFactory = new KeyMouseFactory(Hook.GlobalEvents());
            keyboardWatcher = eventHookFactory.GetKeyboardWatcher();
            if (UseExperimentalKeyCapture)
                keyboardWatcher.OnKeyboardInput += KeyboardWatcher_OnKeyboardInput2;
            else
                keyboardWatcher.OnKeyboardInput += KeyboardWatcher_OnKeyboardInput;

            keyboardWatcher.Start(Hook.GlobalEvents());
        }

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

        /// <summary>
        /// Notifies when a paste action is triggered.
        /// </summary>
        /// <param name="block"></param>
        public void SubscribePasteEvent(Action block)
        {
            pasteEvent = block;
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
            
            if (ShouldPerformPasteAction(e, key))
            {
                Debug.WriteLine("Modifier Key: Should run paste command");
                keyStreams.Clear();
                pasteEvent?.Invoke();
                return;
            }
            
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

            if (quickPasteChord && KeyPressHelper.IsNumericKeyPressed(key))
            {
                Debug.WriteLine("Quick Paste: Done");
                DoQuickPaste2(KeyPressHelper.ParseNumericKey(key));
            }

            quickPasteChord = false;

            if (keyStreams.Any(c => c == Keys.LControlKey || c == Keys.RControlKey) && keyStreams.Any(c => c == Keys.Oem5))
            {
                Debug.WriteLine("QuickPaste chord activated");
                quickPasteChord = true;
                keyStreams.Clear();
            }

            Debug.WriteLine($"Keystream size: {keyStreams.Count}, Contents: [{string.Join(",", keyStreams)}], isCtrl: {isCtrl}, isAlt: {isAlt}, isShift: {isShift}, isHotKey: {isHotKey}");
        }

        private void KeyboardWatcher_OnKeyboardInput(object sender, MacroEvent e)
        {
            var keyEvent = (KeyEventArgs)e.EventArgs;
            Debug.WriteLine($"Key: {keyEvent.KeyCode}, Type: {e.KeyMouseEventType}");
            if (e.KeyMouseEventType == MacroEventType.KeyUp || e.KeyMouseEventType == MacroEventType.KeyPress)
            {
                var key = keyEvent.KeyCode;

                if (quickPasteChord && KeyPressHelper.IsNumericKeyPressed(key))
                {
                    Debug.WriteLine("Quick Paste: Done");
                    DoQuickPaste(KeyPressHelper.ParseNumericKey(key));
                }

                var isCtrl = KeyPressHelper.IsCtrlPressed();
                var isAlt = KeyPressHelper.IsAltPressed();
                var isShift = KeyPressHelper.IsShiftPressed();

                if (ShouldPerformPasteAction(e, key))
                {
                    Debug.WriteLine("Modifier Key: Should run paste command");
                    pasteEvent?.Invoke();
                    return;
                }

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

                if (!ClipWindow.EnqueuePaste)
                    hotKeyEvent?.Invoke();
            }
        }

        #endregion

        private bool ShouldPerformPasteAction(MacroEvent e, Keys key)
        {
            return ClipWindow.EnqueuePaste && e.KeyMouseEventType == MacroEventType.KeyUp &&
                   KeyPressHelper.IsModifierKey(key);
        }
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
