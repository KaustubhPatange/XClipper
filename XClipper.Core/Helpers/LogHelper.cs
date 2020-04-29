using System;

namespace Components
{
    public static class LogHelper
    {
        /// <summary>
        /// This will provide current time in milliseconds.
        /// </summary>
        /// <returns></returns>
        public static long GetCurrentMilliseconds() => DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
    }
}
