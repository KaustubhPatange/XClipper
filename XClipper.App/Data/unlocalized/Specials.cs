using ClipboardManager.models;
using System.Security.RightsManagement;

namespace Components
{
    public static class Specials
    {
        // Some special extensions for Core class
        public static TableCopy Encrypt(this TableCopy t, bool value)
        {
            if (value) t.Encrypt();
            return t;
        }

        public static TableCopy Decrypt(this TableCopy t, bool value)
        {
            if (value) t.Decrypt();
            return t;
        }
    }
}
