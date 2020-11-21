using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Interop;
using System.Windows.Threading;

namespace Components
{
    public static class ApplicationHelper
    {

        #region Constants

        const UInt32 SWP_NOSIZE = 0x0001;
        const UInt32 SWP_NOMOVE = 0x0002;
        const UInt32 SWP_SHOWWINDOW = 0x0040;

        #endregion

        #region Methods

        private static DispatcherTimer dtimer;
        private static bool isExecuted;
        /// <summary>s
        /// This will provide a callback whenever the application process is not foreground.
        /// </summary>
        /// <param name="block"></param>
        public static void AttachForegroundProcess(Action block)
        {
            /** I do not support this logic, maybe in future I'll find a better solution. */
            dtimer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(100) };
            dtimer.Tick += delegate 
            {
                if (!IsActivated())
                {
                    if (isExecuted) return;
                    Debug.WriteLine("Deactivated()");
                    block.Invoke();
                    isExecuted = true;
                }
                else isExecuted = false;
            };
            dtimer.Start();
        }

        /// <summary>Returns true if the current application has focus, false otherwise</summary>
        public static bool IsActivated()
        {
            var activatedHandle = GetForegroundWindow();
            if (activatedHandle == IntPtr.Zero)
            {
                return false;       // No window is currently activated
            }

            var procId = Process.GetCurrentProcess().Id;
            int activeProcId;
            GetWindowThreadProcessId(activatedHandle, out activeProcId);

            return activeProcId == procId;
        }
  
        /// <summary>
        /// Activate a window from anywhere by attaching to the foreground window
        /// </summary>
        public static void GlobalActivate(this Window w)
        {

            //Get the process ID for this window's thread, you can also pass current process Id as well.
            var interopHelper = new WindowInteropHelper(w);
            var thisWindowThreadId = GetWindowThreadProcessId(interopHelper.Handle, IntPtr.Zero);

            //Get the process ID for the foreground window's thread
            var currentForegroundWindow = GetForegroundWindow();
            var currentForegroundWindowThreadId = GetWindowThreadProcessId(currentForegroundWindow, IntPtr.Zero);

            //Attach this window's thread to the current window's thread
            AttachThreadInput(currentForegroundWindowThreadId, thisWindowThreadId, true);

            //Set the window position
            SetWindowPos(interopHelper.Handle, new IntPtr(0), 0, 0, 0, 0, SWP_NOSIZE | SWP_NOMOVE | SWP_SHOWWINDOW);

            //Detach this window's thread from the current window's thread
            AttachThreadInput(currentForegroundWindowThreadId, thisWindowThreadId, false);

            //Show and activate the window
            if (w.WindowState == WindowState.Minimized) w.WindowState = WindowState.Normal;
            w.Show();
            w.Activate();
        }

        #endregion

        #region Imports

        [DllImport("user32.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        private static extern IntPtr GetForegroundWindow();

        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern int GetWindowThreadProcessId(IntPtr handle, out int processId);

        [DllImport("user32.dll")]
        private static extern uint GetWindowThreadProcessId(IntPtr hWnd, IntPtr ProcessId);

        [DllImport("user32.dll")]
        private static extern bool AttachThreadInput(uint idAttach, uint idAttachTo, bool fAttach);

        [DllImport("user32.dll")]
        public static extern bool SetWindowPos(IntPtr hWnd, IntPtr hWndInsertAfter, int X, int Y, int cx, int cy, uint uFlags);

        #endregion
    }
}
