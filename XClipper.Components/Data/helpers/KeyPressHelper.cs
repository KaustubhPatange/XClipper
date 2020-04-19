using System.Windows.Input;

namespace Components
{
    public static class KeyPressHelper
    {
        public static bool IsCtrlPressed() => (Keyboard.IsKeyDown(Key.RightCtrl) || Keyboard.IsKeyDown(Key.LeftCtrl));
        public static bool IsAltPressed() => (Keyboard.IsKeyDown(Key.LeftAlt) || Keyboard.IsKeyDown(Key.RightAlt));
        public static bool IsShitPressed() => (Keyboard.IsKeyDown(Key.LeftShift) || Keyboard.IsKeyDown(Key.RightShift));
        public static bool IsNumericKeyPressed(Key e) => ((e >= Key.D0 && e <= Key.D9) || (e >= Key.NumPad0 && e <= Key.NumPad9));
    }
}
