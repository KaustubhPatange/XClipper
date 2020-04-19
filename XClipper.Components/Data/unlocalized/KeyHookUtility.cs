using Gma.System.MouseKeyHook;
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

        private IKeyboardMouseEvents m_GlobalHook;
        private Action block;

        #endregion

        #region Methods

        public void Subscribe(Action block)
        {
            this.block = block;

            m_GlobalHook = Hook.GlobalEvents();
            m_GlobalHook.KeyDown += M_GlobalHook_KeyDown;
        }

        public void Unsubscribe()
        {
            m_GlobalHook.KeyDown -= M_GlobalHook_KeyDown;
            m_GlobalHook.Dispose();
        }

        #endregion

        #region Keyboard Capture Events

        private void M_GlobalHook_KeyDown(object sender, KeyEventArgs e)
        {
            if (IsCtrl && !IsCtrlPressed()) return;

            if (IsAlt && !IsAltPressed()) return;

            if (IsShift && !IsShitPressed()) return;

            if (e.KeyCode != HotKey.ToEnum<Keys>()) return;

            block.Invoke();
        }

        #endregion
    }
}
