using System.Windows.Input;
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

        private static int ParseKeyString(string key) => Regex.Replace(key, "[^0-9.]", "").ToInt();
    }
}
