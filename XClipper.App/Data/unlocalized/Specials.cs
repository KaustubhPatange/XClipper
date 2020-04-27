using ClipboardManager.models;
using System;
using System.Security.RightsManagement;

namespace Components
{
    public static class Specials
    {
        // Some special extensions for Core class
        [Obsolete("The method is obsolute due to advent of sqlite3", true)]
        public static TableCopy Encrypt(this TableCopy t, bool value)
        {
            if (value) t.Encrypt();
            return t;
        }
        [Obsolete("The method is obsolute due to advent of sqlite3", true)]
        public static TableCopy Decrypt(this TableCopy t, bool value)
        {
            if (value) t.Decrypt();
            return t;
        }
    }
}
