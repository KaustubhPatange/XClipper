using System;
using System.Collections.Generic;

namespace Components
{
    public class AuthEventArgs : EventArgs
    {
        public Dictionary<string, string> JsonPairs { get; set; }
    }
}
