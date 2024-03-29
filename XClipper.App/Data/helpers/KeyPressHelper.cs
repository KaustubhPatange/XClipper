﻿using System.Windows.Input;
using System.Windows.Forms;
using System.Text.RegularExpressions;

namespace Components
{
    public static class KeyPressHelper
    {
        public static bool IsCtrlPressed() => (Keyboard.IsKeyDown(Key.RightCtrl) || Keyboard.IsKeyDown(Key.LeftCtrl));
        public static bool IsAltPressed() => (Keyboard.IsKeyDown(Key.LeftAlt) || Keyboard.IsKeyDown(Key.RightAlt));
        public static bool IsShiftPressed() => (Keyboard.IsKeyDown(Key.LeftShift) || Keyboard.IsKeyDown(Key.RightShift));
        public static bool IsNumericKeyPressed(Key e) => ((e >= Key.D0 && e <= Key.D9) || (e >= Key.NumPad0 && e <= Key.NumPad9));
        public static bool IsNumericKeyPressed(Keys e) => ((e >= Keys.D0 && e <= Keys.D9) || (e >= Keys.NumPad0 && e <= Keys.NumPad9));

        /// <summary>
        /// Parse the numeric value of the key press
        /// </summary>
        /// <param name="e"></param>
        /// <returns></returns>
        public static int ParseNumericKey(Key e)
        {
            return ParseKeyString(e.ToString());
        }

        /// <summary>
        /// <inheritdoc cref="ParseNumericKey(Key)"/>
        /// </summary>
        /// <param name="e"></param>
        /// <returns></returns>
        public static int ParseNumericKey(Keys e)
        {
            return ParseKeyString(e.ToString());
        }

        /// <summary>
        /// Detect if any modifier keys are pressed.
        /// </summary>
        /// <param name="key"></param>
        /// <returns></returns>
        public static bool IsModifierKey(Keys key)
        {
            return key == Keys.Alt || key == Keys.Control || key == Keys.ControlKey || key == Keys.LControlKey || key == Keys.RControlKey 
                || key == Keys.Shift || key == Keys.ShiftKey || key == Keys.LShiftKey || key == Keys.RShiftKey;
        }

        /// <summary>
        /// Determines if this key is a special key.
        /// </summary>
        /// <param name="key"></param>
        /// <returns></returns>
        public static bool IsSpecialKey(Key key)
        {
            return key == Key.LeftCtrl || key == Key.RightCtrl || key == Key.LeftShift
                   || key == Key.RightShift || key == Key.LeftAlt || key == Key.RightAlt
                   || key == Key.System || key == Key.LWin || key == Key.RWin;
        }
        
        public static Key ToInputKey(Keys k) => KeyInterop.KeyFromVirtualKey((int) k);
        public static Keys ToWindowsKey(Key k) => (Keys) KeyInterop.VirtualKeyFromKey(k);
        
        private static int ParseKeyString(string key) => Regex.Replace(key, "[^0-9.]", "").ToInt();
    }
}
