using System;
using System.IO;
using static Components.Constants;

namespace Components
{
    public static class LogHelper
    {
        /// <summary>
        /// This will provide current time in milliseconds.
        /// </summary>
        /// <returns></returns>
        public static long GetCurrentMilliseconds() => DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;

        /// <summary>
        /// Prints data to log file.
        /// </summary>
        /// <param name="message"></param>
        public static void Log(object c, string message)
        {            
            if (!File.Exists(LogFilePath))
                File.Create(LogFilePath).Close();

            using (StreamWriter writer = new StreamWriter(LogFilePath, true))
            {
                writer.Write($"[{DateTime.Now.ToFormattedDateTime()}][{c.ToString()}] - {message}");
                writer.Flush();
                writer.Close();
            }
        }
    }
}
