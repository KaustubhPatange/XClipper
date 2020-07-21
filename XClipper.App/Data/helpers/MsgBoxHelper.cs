using System.Windows;
using static Components.TranslationHelper;

namespace Components
{
    public static class MsgBoxHelper
    {
        public static void ShowInfo(string message) =>
            MessageBox.Show(message, Translation.MSG_INFO, MessageBoxButton.OK, MessageBoxImage.Information);

        public static void ShowError(string message) =>
            MessageBox.Show(message, Translation.MSG_ERR, MessageBoxButton.OK, MessageBoxImage.Error);

        public static void ShowWarning(string message) =>
            MessageBox.Show(message, Translation.MSG_WARNING, MessageBoxButton.OK, MessageBoxImage.Warning);
    }
}
