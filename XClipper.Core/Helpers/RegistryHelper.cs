using Microsoft.Win32;
using System.IO;

namespace Components
{
    /** This will provides some methods to operate registry entries.  */
    public class RegistryHelper
    {
        public static void AddApplicationToStartup(string path)
        {
            using (RegistryKey key = Registry.CurrentUser.OpenSubKey("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run", true))
            {
                key.SetValue(Path.GetFileNameWithoutExtension(path), "\"" + path + "\"");
            }
        }
        public static void RemoveApplicationFromStartup(string path)
        {
            using (RegistryKey key = Registry.CurrentUser.OpenSubKey("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run", true))
            {
                key.DeleteValue(Path.GetFileNameWithoutExtension(path), false);
            }
        }
    }
}
