using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using Microsoft.Win32.SafeHandles;

namespace Components
{
    /// <summary>
    /// A keyboard watcher that hooks to native procedure to notify about Down & Up key events.
    /// </summary>
    public class CustomKeyboardWatcher : IDisposable
    {
        private long _lastRecordTime;
        private bool _isRunning = true;
        public event EventHandler<MacroEvent> OnKeyboardInput;
        private static HookResult handle;
        private static HookResult handle_back;

        private static int CHECKS_OFFSET = 2;
        private List<bool> checks = new();
        
        private static CustomKeyboardWatcher Instance;
        private CustomKeyboardWatcher()
        {
           Init();
        }

        public static CustomKeyboardWatcher Get()
        {
            if (Instance == null) Instance = new CustomKeyboardWatcher();
            return Instance;
        }

        public bool IsListening() => _isRunning;

        public void StopListening()
        {
            _isRunning = false;
        }

        public void StartListening()
        {
            _isRunning = true;
        }

        private void Init()
        {
            handle_back = RegisterSecondHook();
            handle = RegisterFirstHook();
            KeyDownEventHandler += OnKeyDown;
            KeyUpEventHandler += OnKeyUp;
        }
        
        private void OnKeyDown(object sender, KeyEventArgs args)
        {
            if (!_isRunning) return;
            long count = Environment.TickCount;
           // Debug.WriteLine("LastTime Record: " + (int)(count - _lastRecordTime));
            OnKeyboardInput?.Invoke(sender, new MacroEvent(MacroEventType.KeyDown, args, (int)(count - _lastRecordTime)));
            _lastRecordTime = count;
        }

        private void OnKeyUp(object sender, KeyEventArgs args)
        {
            if (!_isRunning) return;
            long count = Environment.TickCount;
            OnKeyboardInput?.Invoke(sender, new MacroEvent(MacroEventType.KeyUp, args, (int)(count - _lastRecordTime)));
            _lastRecordTime = count;
        }

        public void Dispose()
        {
            Debug.WriteLine("CustomKeyboardWatcher: Disposed()");
            KeyDownEventHandler -= OnKeyDown;
            KeyUpEventHandler -= OnKeyUp;
            handle.Dispose();
            handle_back.Dispose();
        }
        
        public event KeyEventHandler KeyDownEventHandler;
        public event KeyEventHandler KeyUpEventHandler;
        public void InvokeKeyDown(KeyEventArgsExtension e)
        {
            var handler = KeyDownEventHandler;
            if (handler == null || e.Handled || !e.IsKeyDown)
                return;
            handler(this, e);
        }
        
        public void InvokeKeyUp(KeyEventArgsExtension e)
        {
            var handler = KeyUpEventHandler;
            if (handler == null || e.Handled || !e.IsKeyUp)
                return;
            handler(this, e);
        }

        private int _firstTick = 0;
        private bool OnFirstCallback(CallbackData data)
        {
            _firstTick = Environment.TickCount;
            var eDownUp = FromRawDataGlobal(data);
            InvokeKeyDown(eDownUp);
            InvokeKeyUp(eDownUp);
            return !eDownUp.Handled;
        }

        private int _secondTick = 0;
        private bool OnSecondCallback(CallbackData data)
        {
            _secondTick = Environment.TickCount;
             var eDownUp = FromRawDataGlobal(data);
            if (_secondTick != _firstTick)
            {
                // TODO: Uncomment this to register checks.
                if (checks.Count >= CHECKS_OFFSET)
                {
                    handle = RegisterFirstHook();
                    checks.Clear();
                }
                else checks.Add(true);
            } else checks.Clear();
            return !eDownUp.Handled;
        }
        
        internal static HookProcedure _globalHookProc;
        internal static HookProcedure _globalHookProc_backup;
        internal delegate bool Callback(CallbackData data);
        internal delegate HookResult Subscribe(Callback callbck);
        public delegate IntPtr HookProcedure(int nCode, IntPtr wParam, IntPtr lParam);
        internal const int WH_KEYBOARD_LL = 13;
        
        private HookResult RegisterFirstHook() => HookGlobalKeyboard(_globalHookProc, OnFirstCallback);
        private HookResult RegisterSecondHook() => HookGlobalKeyboard(_globalHookProc_backup, OnSecondCallback);
        private HookResult HookGlobalKeyboard(HookProcedure procedure, Callback callback)
        {
            procedure = (code, param, lParam) => GetHookProcedure(code, param, lParam, callback);

            var s = Marshal.GetHINSTANCE(typeof(App).Module);
            var hookHandle = SetWindowsHookEx(
                WH_KEYBOARD_LL,
                procedure,
                s,
                0);

            if (hookHandle.IsInvalid)
                throw new Exception("Invalid handle");
            
            return new HookResult(hookHandle, procedure);
        }
        
        internal class HookResult : IDisposable
        {
            public HookResult(HookProcedureHandle handle, HookProcedure procedure)
            {
                Handle = handle;
                Procedure = procedure;
            }

            public HookProcedureHandle Handle { get; }

            public HookProcedure Procedure { get; }

            public void Dispose()
            {
                Handle.Dispose();
            }
        }
        private static IntPtr GetHookProcedure(int nCode, IntPtr wParam, IntPtr lParam, Callback callback)
        {
            var passThrough = nCode != 0;
           // Debug.WriteLine("PassThough: " + nCode);
            if (passThrough)
                return CallNextHookEx(IntPtr.Zero, nCode, wParam, lParam);

            var callbackData = new CallbackData(wParam, lParam);
            var continueProcessing = callback(callbackData);
            
            if (!continueProcessing)
            {
               // Debug.WriteLine("Unsubscribed from the hook");
                return new IntPtr(-1);
            }
            
            return CallNextHookEx(IntPtr.Zero, nCode, wParam, lParam);
        }
        
        public const int WM_KEYDOWN = 256;
        public const int WM_KEYUP = 257;
        public const int WM_SYSKEYDOWN = 260;
        public const int WM_SYSKEYUP = 261;
        internal static KeyEventArgsExtension FromRawDataGlobal(CallbackData data)
        {
            var wParam = data.WParam;
            var lParam = data.LParam;
            var keyboardHookStruct =
                (KeyboardHookStruct) Marshal.PtrToStructure(lParam, typeof(KeyboardHookStruct));

            var keyData = AppendModifierStates((Keys) keyboardHookStruct.VirtualKeyCode);

            var keyCode = (int) wParam;
            var isKeyDown = keyCode == WM_KEYDOWN || keyCode == WM_SYSKEYDOWN;
            var isKeyUp = keyCode == WM_KEYUP || keyCode == WM_SYSKEYUP;

            const uint maskExtendedKey = 0x1;
            var isExtendedKey = (keyboardHookStruct.Flags & maskExtendedKey) > 0;

            return new KeyEventArgsExtension(keyData, keyboardHookStruct.ScanCode, keyboardHookStruct.Time, isKeyDown,
                isKeyUp, isExtendedKey);
        }
        
        public const byte VK_CONTROL = 0x11;
        public const byte VK_SHIFT = 0x10;
        public const byte VK_MENU = 0x12;
        private static Keys AppendModifierStates(Keys keyData)
        {
            // Is Control being held down?
            var control = CheckModifier(VK_CONTROL);
            // Is Shift being held down?
            var shift = CheckModifier(VK_SHIFT);
            // Is Alt being held down?
            var alt = CheckModifier(VK_MENU);

            // Windows keys
            // # combine LWin and RWin key with other keys will potentially corrupt the data
            // notable F5 | Keys.LWin == F12, see https://globalmousekeyhook.codeplex.com/workitem/1188
            // and the KeyEventArgs.KeyData don't recognize combined data either

            // Function (Fn) key
            // # CANNOT determine state due to conversion inside keyboard
            // See http://en.wikipedia.org/wiki/Fn_key#Technical_details #

            return keyData |
                   (control ? Keys.Control : Keys.None) |
                   (shift ? Keys.Shift : Keys.None) |
                   (alt ? Keys.Alt : Keys.None);
        }
        private static bool CheckModifier(int vKey)
        {
            return (GetKeyState(vKey) & 0x8000) > 0;
        }
        
        internal class HookProcedureHandle : SafeHandleZeroOrMinusOneIsInvalid
        {
            private static bool _closing;

            static HookProcedureHandle()
            {
                Application.ApplicationExit += (sender, e) => { _closing = true; };
            }

            public HookProcedureHandle()
                : base(true)
            {
            }

            protected override bool ReleaseHandle()
            {
                //NOTE Calling Unhook during processexit causes deley
                if (_closing) return true;
                return false;
                // return HookNativeMethods.UnhookWindowsHookEx(handle) != 0;
            }
        }
        internal struct CallbackData
        {
            public CallbackData(IntPtr wParam, IntPtr lParam)
            {
                WParam = wParam;
                LParam = lParam;
            }

            public IntPtr WParam { get; }

            public IntPtr LParam { get; }
        }
        [StructLayout(LayoutKind.Sequential)]
        internal struct KeyboardHookStruct
        {
            /// <summary>
            ///     Specifies a virtual-key code. The code must be a value in the range 1 to 254.
            /// </summary>
            public int VirtualKeyCode;

            /// <summary>
            ///     Specifies a hardware scan code for the key.
            /// </summary>
            public int ScanCode;

            /// <summary>
            ///     Specifies the extended-key flag, event-injected flag, context code, and transition-state flag.
            /// </summary>
            public int Flags;

            /// <summary>
            ///     Specifies the Time stamp for this message.
            /// </summary>
            public int Time;

            /// <summary>
            ///     Specifies extra information associated with the message.
            /// </summary>
            public int ExtraInfo;
        }

        public class KeyEventArgsExtension : KeyEventArgs
        {
            public KeyEventArgsExtension(Keys keyData, int scanCode, int timestamp, bool isKeyDown, bool isKeyUp,
                bool isExtendedKey) : base(keyData)
            {
                ScanCode = scanCode;
                Timestamp = timestamp;
                IsKeyDown = isKeyDown;
                IsKeyUp = isKeyUp;
                IsExtendedKey = isExtendedKey;
            }
            
            /// <summary>
            ///     The hardware scan code.
            /// </summary>
            public int ScanCode { get; }

            /// <summary>
            ///     The system tick count of when the event occurred.
            /// </summary>
            public int Timestamp { get; }

            /// <summary>
            ///     True if event signals key down..
            /// </summary>
            public bool IsKeyDown { get; }

            /// <summary>
            ///     True if event signals key up.
            /// </summary>
            public bool IsKeyUp { get; }

            /// <summary>
            ///     True if event signals, that the key is an extended key
            /// </summary>
            public bool IsExtendedKey { get; }
        }


        [DllImport("user32.dll", CharSet = CharSet.Auto,
            CallingConvention = CallingConvention.StdCall, SetLastError = true)]
        internal static extern HookProcedureHandle SetWindowsHookEx(
            int idHook,
            HookProcedure lpfn,
            IntPtr hMod,
            int dwThreadId);
        
        [DllImport("user32.dll", CharSet = CharSet.Auto,
            CallingConvention = CallingConvention.StdCall)]
        internal static extern IntPtr CallNextHookEx(
            IntPtr idHook,
            int nCode,
            IntPtr wParam,
            IntPtr lParam);
        
        [DllImport("user32.dll", CharSet = CharSet.Auto, CallingConvention = CallingConvention.StdCall)]
        public static extern short GetKeyState(int vKey);
        
    }
    
    [Serializable]
    public class MacroEvent : EventArgs
    {
        public MacroEventType KeyMouseEventType;
        public EventArgs EventArgs;
        public int TimeSinceLastEvent;

        public MacroEvent(MacroEventType eventType, EventArgs eventArgs, int timeSinceLastEvent)
        {
            KeyMouseEventType = eventType;
            EventArgs = eventArgs;
            TimeSinceLastEvent = timeSinceLastEvent;
        }
    }
    
    [Flags]
    [Serializable]
    public enum MacroEventType
    {
        MouseMove = 1,
        MouseMoveExt = 2,
        MouseDown = 4,
        MouseDownExt = 8,
        MouseUp = 16, // 0x00000010
        MouseUpExt = 32, // 0x00000020
        MouseWheel = 64, // 0x00000040
        MouseWheelExt = 128, // 0x00000080
        MouseDragStarted = 256, // 0x00000100
        MouseDragFinished = 512, // 0x00000200
        MouseClick = 1024, // 0x00000400
        MouseDoubleClick = 2048, // 0x00000800
        KeyDown = 4096, // 0x00001000
        KeyUp = 8192, // 0x00002000
        KeyPress = 16384, // 0x00004000
    }
}