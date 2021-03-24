using System;
using System.Diagnostics;
using System.IO;
using System.Runtime.CompilerServices;
using System.Text.RegularExpressions;
using static Components.Constants;

#nullable enable

namespace Components
{
    public static class LogHelper
    {
        private static string LOG_FILE = $"{LogFilePath}-{DateTime.Now.ToFormattedDateTime()}.log";
        private static string KEY_LOG_FILE = $"{KeyLogFilePath}-{DateTime.Now.ToFormattedDateTime()}.log";
        /// <summary>
        /// This will provide current time in milliseconds.
        /// </summary>
        /// <returns></returns>
        public static long GetCurrentMilliseconds() => DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;


        /// <summary>
        /// Prints data to log file.
        /// </summary>
        /// <param name="message"></param>
        public static void Log(object c, string? message = null)
        {
            var method = new StackTrace().GetFrame(2).GetMethod();
            string currentMethodName;
            if (method.DeclaringType.Name.Contains("<"))
                currentMethodName = Regex.Match(method.DeclaringType.Name, "([A-Z])\\w+").Value;
            else currentMethodName = method.ToString();

            if (!Directory.Exists(ApplicationLogDirectory)) Directory.CreateDirectory(ApplicationLogDirectory);
            if (!File.Exists(LOG_FILE)) File.Create(LOG_FILE).Close();

            try
            {
                using (StreamWriter writer = new StreamWriter(LOG_FILE, true))
                {
                    writer.Write($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss.SSS}] [{c}] [{currentMethodName}] - " + message + "\n");
                    writer.Flush();
                    writer.Close();
                }
            }
            catch (IOException e) { Debug.WriteLine($"{e.Message}"); }
        }

        public static void LogKey(string key, bool custom = false)
        {
            if (!Directory.Exists(ApplicationKeyLogDirectory)) Directory.CreateDirectory(ApplicationKeyLogDirectory);
            if (!File.Exists(KEY_LOG_FILE)) File.Create(KEY_LOG_FILE).Close();

            try
            {
                using (StreamWriter writer = new StreamWriter(KEY_LOG_FILE, true))
                {
                    if (!custom)
                        writer.Write($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss.SSS}] Key pressed: {key}\n");
                    else
                        writer.Write($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss.SSS}] {key}\n");
                    writer.Flush();
                    writer.Close();
                }
            }
            catch (IOException e) { Debug.WriteLine($"{e.Message}"); }
        }

        /// <summary>
        /// This method will open the current log file
        /// </summary>
        public static void OpenLogFile()
        {
            Process.Start(LOG_FILE);
        }
    }
}
