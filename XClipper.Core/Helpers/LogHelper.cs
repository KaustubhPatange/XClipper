using System;

namespace Components
{
    public static class LogHelper
    {
        public static long GetCurrentMilliseconds() => DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
    }
}
