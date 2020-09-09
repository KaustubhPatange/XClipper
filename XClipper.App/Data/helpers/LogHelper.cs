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
            var currentMethodName = "";
            if (method.DeclaringType.Name.Contains("<"))
                currentMethodName = Regex.Match(method.DeclaringType.Name, "([A-Z])\\w+").Value;
            else currentMethodName = method.ToString();

            if (!File.Exists(LOG_FILE))
                File.Create(LOG_FILE).Close();

            using (StreamWriter writer = new StreamWriter(LOG_FILE, true))
            {
                writer.Write($"[{DateTime.Now.ToFormattedDateTime()}] [{c}] [{currentMethodName}] - " + message + "\n");
                writer.Flush();
                writer.Close();
            }
        }
        //currentMethodName	{Void InitConfig(Components.FirebaseData)}	System.Reflection.MethodBase {System.Reflection.RuntimeMethodInfo}
        // currentMethodName	{Void MoveNext()}	System.Reflection.MethodBase {System.Reflection.RuntimeMethodInfo}
        // ((System.Reflection.RuntimeMethodInfo)currentMethodName).DeclaringType	{Name = "<CreateNewClient>d__25" FullName = "Components.FirebaseSingleton+<CreateNewClient>d__25"}	System.Type {System.RuntimeType}
    }
}
