using System.Windows.Forms;

namespace ClipboardManager
{
    public static class KeyboardHelpers
    {
        public static bool isShiftPressed(KeyEventArgs keyEvent, Keys keys)
        {
            return keyEvent.Shift && keys != Keys.Shift && keys != Keys.LShiftKey &&
                    keys != Keys.RShiftKey && keys != Keys.ShiftKey;
        }

        public static bool isCtrlPressed(KeyEventArgs keyEvent, Keys keys)
        {
            return keyEvent.Control && keys != Keys.Control && keys != Keys.LControlKey &&
                    keys != Keys.RControlKey && keys != Keys.ControlKey;
        }

        public static bool isAltPressed(KeyEventArgs keyEvent, Keys keys)
        {
            return keyEvent.Alt && keys != Keys.RMenu && keys != Keys.LMenu &&
                      keys != Keys.Alt;
        }
    }
}
