using Gma.System.MouseKeyHook;
using Loamen.KeyMouseHook;
using System;
using System.Diagnostics;
using System.Windows.Forms;
using static Components.DefaultSettings;
using static Components.KeyPressHelper;

namespace Components
{
    public class KeyHookUtility
    {
        #region Variable Declaration

        private KeyMouseFactory eventHookFactory;
        private KeyboardWatcher keyboardWatcher;
        private Action block;

        #endregion

        #region Methods

        public void Subscribe(Action block)
        {
            this.block = block;

            eventHookFactory = new KeyMouseFactory(Hook.GlobalEvents());
            keyboardWatcher = eventHookFactory.GetKeyboardWatcher();
            keyboardWatcher.OnKeyboardInput += KeyboardWatcher_OnKeyboardInput;

            keyboardWatcher.Start(Hook.GlobalEvents());
        }

        public void Unsubscribe()
        {
            keyboardWatcher.Stop();
            keyboardWatcher.Dispose();
            eventHookFactory.Dispose();
        }

        #endregion

        #region Keyboard Capture Events

        private void KeyboardWatcher_OnKeyboardInput(object sender, MacroEvent e)
        {
            var keyEvent = (KeyEventArgs)e.EventArgs;
            if (e.KeyMouseEventType == MacroEventType.KeyUp)
            {
                var key = keyEvent.KeyCode;

                if (IsCtrl && !IsCtrlPressed()) return;

                if (IsAlt && !IsAltPressed()) return;

                if (IsShift && !IsShitPressed()) return;

                if (key != HotKey.ToEnum<Keys>()) return;

                block.Invoke();
            }
        }

        #endregion
    }
}
