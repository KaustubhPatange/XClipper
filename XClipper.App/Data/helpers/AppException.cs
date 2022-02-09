using System.IO;
using System.Text;
using System.Windows;
using System.Windows.Threading;
using static Components.Constants;
using System;
using System.Management;
using System.Dynamic;
using System.Collections.Generic;
using System.Reflection;
using System.Threading.Tasks;
using RestSharp;
using static Components.Core;
using static Components.DefaultSettings;
using SharpRaven;
using SharpRaven.Data;
using System.Linq;

#nullable enable

namespace Components
{
    public class AppException
    {
        #region Class Implementations

        private static bool IsAttached = false;
        private static AppException Instance = null;
        private AppException() { }

        public static void Init()
        {
            if (IsAttached) return; // Only once...

            if (Instance == null)
                Instance = new AppException();
            Instance.Attach();
            IsAttached = true;
        }

        #endregion

        private RavenClient ravenClient = new RavenClient(CRASH_SERVER);
        public void Attach()
        {
            AppDomain.CurrentDomain.UnhandledException += CurrentDomain_UnhandledException;
            Application.Current.DispatcherUnhandledException += Current_DispatcherUnhandledException;
            System.Windows.Forms.Application.ThreadException += Winform_ThreadException;
        }

        #region Private Members

        private async void Winform_ThreadException(object sender, System.Threading.ThreadExceptionEventArgs e)
        {
            await WriteCrashDetails("winform_report_", $"{e.Exception.Message}\n{e.Exception.StackTrace}", e.Exception).ConfigureAwait(false);
        }

        private async void Current_DispatcherUnhandledException(object sender, DispatcherUnhandledExceptionEventArgs e)
        {
            await WriteCrashDetails("app_report_", $"{e.Exception.Message}\n{e.Exception.StackTrace}", e.Exception).ConfigureAwait(false);
        }

        private async void CurrentDomain_UnhandledException(object sender, UnhandledExceptionEventArgs e)
        {
            Exception? exception = null;
            if (e.ExceptionObject is Exception) exception = e.ExceptionObject as Exception;
            await WriteCrashDetails("domain_report_", e.ExceptionObject.ToString(), exception).ConfigureAwait(false);
        }

        private async Task WriteCrashDetails(string prefix, string contents, Exception? exception = null)
        {
            new UWPToast.Builder(Application.Current.Dispatcher)
               .AddText(Translation.APP_CRASH)
               .AddText(Translation.MSG_IMAGE_SAVE_FAILED_TEXT)
               .build().ShowAsync();
            var dib = new StringBuilder();
            
            try
            {
                dib.AppendLine("-----------------------------------------------------------");
                // App Version
                dib.AppendLine($"App Version: {Assembly.GetExecutingAssembly().GetName().Version}");
                // OS
                dib.AppendLine(
                    $"OS: {Environment.OSVersion} {(Environment.Is64BitOperatingSystem ? "x64" : "x86")} {Environment.UserName}");
                // Display
                foreach (dynamic obj in GetAllObjects("Win32_DesktopMonitor"))
                {
                    if (obj.ScreenWidth != null)
                        dib.AppendLine(
                            $"Display: {obj.ScreenWidth}x{obj.ScreenHeight}, {obj.SystemName}, {obj.MonitorType}");
                    AddKeyToException(exception, "Display", $"{obj.ScreenWidth}x{obj.ScreenHeight}, {obj.SystemName}, {obj.MonitorType}");
                }

                // Memory
                foreach (dynamic obj in GetAllObjects("Win32_PhysicalMemory"))
                {
                    dib.AppendLine($"Memory: {((long) obj.Capacity).ToFileSizeApi()} {obj.Speed} MHz");
                    AddKeyToException(exception, "Memory", $"{((long)obj.Capacity).ToFileSizeApi()} {obj.Speed} MHz");
                }

                // Video Controller
                foreach (dynamic obj in GetAllObjects("Win32_VideoController"))
                {
                    dib.AppendLine($"VideoController: {obj.Name}, {((long) obj.AdapterRAM).ToFileSizeApi()}");
                    AddKeyToException(exception, "VideoController", $"{obj.Name}, {((long)obj.AdapterRAM).ToFileSizeApi()}");
                }

                // Processor
                foreach (dynamic obj in GetAllObjects("Win32_Processor"))
                {
                    dib.AppendLine($"ChipSet: {obj.Name} Threads/Cores: ({obj.ThreadCount}/{obj.NumberOfCores})");
                    AddKeyToException(exception, "ChipSet", $"{obj.Name} Threads/Cores: ({obj.ThreadCount}/{obj.NumberOfCores})");
                }

                // BIOS
                foreach (dynamic obj in GetAllObjects("Win32_BIOS"))
                {
                    dib.AppendLine($"BIOS: {obj.Name}");
                }

                dib.AppendLine("-----------------------------------------------------------");
                dib.AppendLine("");
            }
            catch (Exception ex)
            {
                dib.AppendLine($"Error: {ex}");
                dib.AppendLine("-----------------------------------------------------------");
                dib.AppendLine("");
            }

            dib.Append(contents);

            if (!Directory.Exists(ApplicationExceptionDirectory)) Directory.CreateDirectory(ApplicationExceptionDirectory);

            string crashFilePath = Path.Combine(ApplicationExceptionDirectory, prefix + CreateCrashSuffix() + ".txt");

            File.WriteAllText(crashFilePath, dib.ToString());

            // Filter our exceptions

            if (ignoreExceptionsStrings.Any(c => contents.Contains(c))) return;

            await ravenClient.CaptureAsync(new SentryEvent(exception)).ConfigureAwait(false);

            await SendReport(Environment.UserName, dib.ToString()).ConfigureAwait(false);
        }

        private async Task SendReport(string sender, string content)
        {
            var queryBuilder = new StringBuilder();
            queryBuilder.Append(BACKEND_SERVER)
                .Append("/report?sender=").Append(sender)
                .Append("&category=0"); // 0 means Desktop
            var client = new RestClient();
            var request = new RestRequest(queryBuilder.ToString(), Method.POST);
            request.AddParameter("text/plain", content, ParameterType.RequestBody);
            await client.ExecuteTaskAsync(request).ConfigureAwait(false);

            if (ExitOnCrash)
                Environment.Exit(1);
        }

        private string CreateCrashSuffix()
        {
            return DateTime.Now.ToFormattedDateTime();
        }

        public static List<dynamic> GetAllObjects(string className)
        {
            ManagementObjectCollection col = new ManagementObjectSearcher("SELECT * FROM " + className).Get();
            List<dynamic> objects = new List<dynamic>();

            foreach (ManagementObject obj in col)
            {
                var currentObject = new ExpandoObject() as IDictionary<string, Object>;

                foreach (PropertyData prop in obj.Properties)
                {
                    currentObject.Add(prop.Name, prop.Value);
                }

                objects.Add(currentObject);
            }

            return objects;
        }

        private static void AddKeyToException(Exception? exception, object key, object contents)
        {
            if (exception?.Data?.Contains(key) == false)
            {
                exception?.Data?.Add(key, contents);
            }
        }

        #endregion

        private readonly string[] ignoreExceptionsStrings = new[] { "" }; // @"C:\Program Files\XClipper\locales", @"C:\Windows\system32\locales" TODO: Add this if verified
    }
}
