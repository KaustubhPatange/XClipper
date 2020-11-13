using Gma.System.MouseKeyHook;
using Loamen.KeyMouseHook;
using System;
using System.Diagnostics;
using System.Windows.Forms;
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
        private bool quickPasteChord = false;

        public KeyHookUtility()
        {
            eventHookFactory = new KeyMouseFactory(Hook.GlobalEvents());
            keyboardWatcher = eventHookFactory.GetKeyboardWatcher();
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
            Dispose();
        }

        #endregion

        #region Keyboard Capture Events

        private void KeyboardWatcher_OnKeyboardInput(object sender, MacroEvent e)
        {
            var keyEvent = (KeyEventArgs)e.EventArgs;
            if (e.KeyMouseEventType == MacroEventType.KeyUp)
            {
                var key = keyEvent.KeyCode;

                if (quickPasteChord && IsNumericKeyPressed(key))
                {
                    Debug.WriteLine("Quick Paste: Done");
                    DoQuickPaste(ParseNumericKey(key));
                }

                if (!IsCtrlPressed() && !IsAltPressed() && !IsShitPressed())
                    quickPasteChord = false;

                // Process other keystrokes...
                if (IsCtrlPressed() && key == Keys.Oem5)
                {
                    Debug.WriteLine("QuickPaste chord activated");
                    quickPasteChord = true;
                }
                
                if (IsCtrl && !IsCtrlPressed()) return;

                if (IsAlt && !IsAltPressed()) return;

                if (IsShift && !IsShitPressed()) return;

                if (key != HotKey.ToEnum<Keys>()) return;

                hotKeyEvent?.Invoke();
            }
        }

        #endregion

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
