﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Media;
using System.Reflection;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Forms;
using System.Windows.Input;
using static Components.DefaultSettings;
using static Components.ApplicationHelper;
using Application = System.Windows.Application;
using KeyEventArgs = System.Windows.Forms.KeyEventArgs;

#nullable enable

namespace Components
{
    public sealed class KeyHookUtility : IDisposable
    {
        public interface IBufferInvokes
        {
            void OnBufferCopyAction(Buffer b);
            void OnBufferPasteAction(Buffer b);
            void OnBufferCutAction(Buffer b);
        }

        #region Variable Declaration

        private Action? hotKeyEvent;
        private Action? pasteEvent;
        private Action<int>? quickPasteEvent;
        private IBufferInvokes? bufferBinder;

        private BackgroundWorker _worker = new();

        private CustomKeyboardWatcher _keyboardWatcher;

        public KeyHookUtility()
        { }

        #endregion

        #region Methods

        public void Init()
        {
            _worker.DoWork += KeyboardBackground_Worker;
            _keyboardWatcher = CustomKeyboardWatcher.Get();
            _keyboardWatcher.OnKeyboardInput += (sender, args) =>
            {
                if (!_worker.IsBusy) _worker.RunWorkerAsync(args);
            };

            /*if (UseExperimentalKeyCapture)
                _keyboardWatcher.OnKeyboardInput += KeyboardWatcher_OnKeyboardInput2;
            else
                _keyboardWatcher.OnKeyboardInput += KeyboardWatcher_OnKeyboardInput;
            
            _keyboardWatcher.OnKeyboardInput += BufferCopy_OnKeyboardInput;*/
        }

        #region Subscribers

        /// <summary>
        /// Notifies when the hot keys are pressed.
        /// </summary>
        /// <param name="block"></param>
        public void SubscribeHotKeyEvents(Action block)
        {
            hotKeyEvent = block;
        }

        /// <summary>
        /// Notifies when a quick paste is occurred from Ctrl + K
        /// </summary>
        /// <param name="block"></param>
        public void SubscribeQuickPasteEvent(Action<int> block)
        {
            quickPasteEvent = block;
        }

        /// <summary>
        /// Notifies when a paste action is triggered.
        /// </summary>
        /// <param name="block"></param>
        public void SubscribePasteEvent(Action block)
        {
            pasteEvent = block;
        }

        /// <summary>
        /// Some invokes for buffer.
        /// </summary>
        /// <param name="block"></param>
        public void SubscribeBufferEvents(IBufferInvokes binder)
        {
            bufferBinder = binder;
        }

        #endregion
        
        public void StartListening()
        {
            _keyboardWatcher.StopListening();
        }
        
        public void StopListening()
        {
            _keyboardWatcher.StartListening();
        }

        public void UnsubscribeAll()
        {
            Debug.WriteLine("Dispose");
            Dispose();
        }

        #endregion


        #region Worker
        
        private void KeyboardBackground_Worker(object sender, DoWorkEventArgs args)
        {
            MacroEvent e = (MacroEvent) args.Argument;
            if (UseExperimentalKeyCapture)
                KeyboardWatcher_OnKeyboardInput2(null, e);
            else
                KeyboardWatcher_OnKeyboardInput(null, e);

            if (EnableCopyBuffer)
                BufferCopy_OnKeyboardInput(null, e);
        }

        #endregion

        #region Keyboard Capture Events
        
        private LinkedList<Keys> keyStreams = new();
        private const int KEY_STORE_SIZE = 4;
        private const int LAST_KEY_TIME_OFFSET = 400;
        private const int CLEAR_KEY_TIME_OFFSET = 200;

        private long TIME_LAST_KEY_OFFSET = 0;
        private bool quickPasteChord = false;
        private bool active = false;

        private bool isCtrl, isShift, isAlt;

        private List<Keys> NavigationKeys = new()
        {
            Keys.Up, Keys.Down, Keys.Right, Keys.Left, Keys.Home, Keys.PageUp, Keys.PageDown, Keys.End
        };
        
        private void KeyboardWatcher_OnKeyboardInput2(object sender, MacroEvent e)
        {
            var keyEvent = e.EventArgs as KeyEventArgs;
            if (keyEvent == null) return;
            var key = keyEvent.KeyCode;
            
            Debug.WriteLine($"Key: {key}, Event: {e.KeyMouseEventType}");
            
            if (keyStreams.Count >= KEY_STORE_SIZE)
                keyStreams.RemoveFirst();

            var timeLong = Environment.TickCount;
            // var timeLong = DateTime.Now.ToString("yyyyMMddHHmmssfff").ToLong();

            if (active)
            {
                if (TIME_LAST_KEY_OFFSET > 0 && timeLong - TIME_LAST_KEY_OFFSET <= LAST_KEY_TIME_OFFSET)
                {
                    // Debug.WriteLine("IsActive");
                    return;
                }

                active = false;
            }

            if (TIME_LAST_KEY_OFFSET > 0 && timeLong - TIME_LAST_KEY_OFFSET >= CLEAR_KEY_TIME_OFFSET)
                keyStreams.Clear();
            TIME_LAST_KEY_OFFSET = timeLong;

            if (!keyStreams.Any(c => c == key))
                keyStreams.AddLast(key);

            if (ShouldPerformPasteAction(e, key))
            {
                // Debug.WriteLine("Modifier Key: Should run paste command");
                keyStreams.Clear();
                if (pasteEvent != null) SendAction(pasteEvent);
                return;
            }
            
            isCtrl = keyStreams.Any(c => c == Keys.LControlKey || c == Keys.RControlKey);
            isShift = keyStreams.Any(c => c == Keys.LShiftKey || c == Keys.RShiftKey);
            isAlt = keyStreams.Any(c => c == Keys.Alt);

            // Hot key detection
            bool hotKeyCtrl;
            if (DefaultSettings.IsCtrl) hotKeyCtrl = isCtrl; else hotKeyCtrl = true;

            bool hotKeyShift;
            if (DefaultSettings.IsShift) hotKeyShift = isShift; else hotKeyShift = true;

            bool hotKeyAlt;
            if (DefaultSettings.IsAlt) hotKeyAlt = isAlt; else hotKeyAlt = true;

            Keys hotKey = DefaultSettings.HotKey.ToEnum<Keys>();
            // bool isHotKey = keyStroke.hotKey == hotKey;
            bool isHotKey = keyStreams.Any(c => c == hotKey);

            if (hotKeyCtrl && hotKeyAlt && hotKeyShift && isHotKey)
            {
                active = true;
                // keyStroke.Clear();
                keyStreams.Clear();
                if (hotKey != null) SendAction(hotKeyEvent);
            }

            // Quick paste cord 2
            if (quickPasteChord && KeyPressHelper.IsNumericKeyPressed(key))
            {
                // Debug.WriteLine("Quick Paste: Done");
                SendAction(() => DoQuickPaste2(KeyPressHelper.ParseNumericKey(key)));
            }

            quickPasteChord = false;

            // Quick paste cord 1
            if (keyStreams.Any(c => c == Keys.LControlKey || c == Keys.RControlKey) &&
                keyStreams.Any(c => c == Keys.Oem5))
            {
                // Debug.WriteLine("QuickPaste chord activated");
                quickPasteChord = true;
                keyStreams.Clear();
            }
            // Debug.WriteLine($"Keystream size: {keyStreams.Count}, Contents: [{string.Join(",", keyStreams)}]");
            
            /* TODO: Remove this Hotfix to over-come wrong invocation of hot key event.
             * Issue: Wrong invocation of hot key events or wrong triggers based on holding SuperKeys.
             * Produce: Type text, press a navigation key along with Ctrl modifier eg: Ctrl + Left then
             *          quickly press tilde (~) which would cause hot key event to be invoked without
             *          pressing the complete combination.
             * Reason: Occurs due to fast insertion of Keys in keyStreams and have very less time to
             *         remove them from the streams based on CLEAR_KEY_TIME_OFFSET.
             * Current Fix: Let's just clear the keyStream whenever one of these navigation keys defined
             *              in NavigationKeys list is pressed.
             */
            if (observeKeyUpsAndRemoveThem)
            {
                if (e.KeyMouseEventType == MacroEventType.KeyUp || key == hotKey)
                    keyStreams.Clear();
                else if (e.KeyMouseEventType == MacroEventType.KeyDown)
                    observeKeyUpsAndRemoveThem = false;
            }
            if (NavigationKeys.Contains(key))
            {
                observeKeyUpsAndRemoveThem = true;
                keyStreams.Clear();
            }
        }

        private bool observeKeyUpsAndRemoveThem = false;
        
        private void KeyboardWatcher_OnKeyboardInput(object sender, MacroEvent e)
        {
            var keyEvent = (KeyEventArgs) e.EventArgs;
            
            // Debug.WriteLine($"Key: {keyEvent.KeyCode}, Type: {e.KeyMouseEventType}");

            if (e.KeyMouseEventType == MacroEventType.KeyUp || e.KeyMouseEventType == MacroEventType.KeyPress)
            {
                var key = keyEvent.KeyCode;

                if (quickPasteChord && KeyPressHelper.IsNumericKeyPressed(key))
                {
                    // Debug.WriteLine("Quick Paste: Done");
                    DoQuickPaste(KeyPressHelper.ParseNumericKey(key));
                }

                isCtrl = KeyPressHelper.IsCtrlPressed();
                isAlt = KeyPressHelper.IsAltPressed();
                isShift = KeyPressHelper.IsShiftPressed();

                if (ShouldPerformPasteAction(e, key))
                {
                    // Debug.WriteLine("Modifier Key: Should run paste command");
                    pasteEvent?.Invoke();
                    return;
                }

                if (!isCtrl && !isAlt && !isShift)
                    quickPasteChord = false;

                // Process other keystrokes...
                if (isCtrl && key == Keys.Oem5)
                {
                    // Debug.WriteLine("QuickPaste chord activated");
                    quickPasteChord = true;
                }

                //LogHelper.LogKey(key.ToString());

                if (IsCtrl && !isCtrl) return;

                if (IsAlt && !isAlt) return;

                if (IsShift && !isShift) return;

                if (key != HotKey.ToEnum<Keys>()) return;

                //LogHelper.LogKey("Application should launch", true);

                if (!ClipWindow.EnqueuePaste)
                    hotKeyEvent?.Invoke();
            }
        }

        #endregion

        #region Buffer Capture

        private void BufferCopy_OnKeyboardInput(object sender, MacroEvent e)
        {
            var keyEvent = e.EventArgs as KeyEventArgs;
            if (keyEvent == null) return;
            var key = keyEvent.KeyCode;

            if (e.KeyMouseEventType != MacroEventType.KeyUp) return;

            if (!isCtrl && !isShift && !isAlt) return;
            
            // Buffer 1 detection
            if (IsKeymapActive(DefaultSettings.CopyBuffer1.Paste, isShift, isAlt, isCtrl, key))
                SendAction(() => bufferBinder?.OnBufferPasteAction(DefaultSettings.CopyBuffer1));
            else if (IsKeymapActive(DefaultSettings.CopyBuffer1.Copy, isShift, isAlt, isCtrl, key))
                SendAction(()=>bufferBinder?.OnBufferCopyAction(DefaultSettings.CopyBuffer1));
            else if (IsKeymapActive(DefaultSettings.CopyBuffer1.Cut, isShift, isAlt, isCtrl, key))
                SendAction(()=> bufferBinder?.OnBufferCutAction(DefaultSettings.CopyBuffer1));

            // Buffer 2 detection
            if (IsKeymapActive(DefaultSettings.CopyBuffer2.Paste, isShift, isAlt, isCtrl, key))
                SendAction(() => bufferBinder?.OnBufferPasteAction(DefaultSettings.CopyBuffer2));
            else if (IsKeymapActive(DefaultSettings.CopyBuffer2.Copy, isShift, isAlt, isCtrl, key))
                SendAction(() => bufferBinder?.OnBufferCopyAction(DefaultSettings.CopyBuffer2));
            else if (IsKeymapActive(DefaultSettings.CopyBuffer2.Cut, isShift, isAlt, isCtrl, key))
                SendAction(() => bufferBinder?.OnBufferCutAction(DefaultSettings.CopyBuffer2)); 
        }
        
        private bool IsKeymapActive(Keymap map, bool isShift, bool isAlt, bool isCtrl, Keys key)
        {
            bool execute = true;
            if (map.IsAlt) execute &= isAlt;
            if (map.IsCtrl) execute &= isCtrl;
            if (map.IsShift) execute &= isShift;
            if (map.HotKey != key.ToString()) execute &= false;
           // Debug.WriteLine($"Keymap: isCtrl: {isCtrl}, isAlt: {isAlt}, isShift: {isShift}, HotKey: {key}, ToExecute: {execute}");
            return execute;
        }

        #endregion
        
        /// <summary>
        /// Suppose hotkeys are Ctrl + Oem3.
        /// 
        /// Pressing Ctrl + Oem3 will launch the window &amp; pressing Oem3 while holding Ctrl
        /// will move the cursor down &amp; leaving the Ctrl will run paste command.
        /// </summary>
        /// <param name="e"></param>
        /// <param name="key"></param>
        /// <returns></returns>
        private static bool ShouldPerformPasteAction(MacroEvent e, Keys key)
        {
            return ClipWindow.EnqueuePaste && e.KeyMouseEventType == MacroEventType.KeyUp &&
                   KeyPressHelper.IsModifierKey(key);
        }


        private bool paste_running = false;
        private void DoQuickPaste2(int index)
        {
            if (paste_running) return;
            paste_running = true;
            Task.Run(async () =>
            {
                SendKeys.SendWait("{BKSP}");
                await Task.Delay(100).ConfigureAwait(false);
                System.Windows.Application.Current.Dispatcher.BeginInvoke(new Action(() =>
                {
                    if (index == 0)
                        quickPasteEvent?.Invoke(9);
                    else quickPasteEvent?.Invoke(index - 1);
                }));
                paste_running = false;
            });
        }

        private void DoQuickPaste(int index)
        {
            SendKeys.SendWait("{BKSP}");
            if (index == 0)
                quickPasteEvent?.Invoke(9);
            else quickPasteEvent?.Invoke(index - 1);
        }

        public void Dispose()
        {
            Debug.WriteLine("Disposed KeyHookUtility");
            quickPasteEvent = null;
            hotKeyEvent = null;
            _keyboardWatcher.Dispose();
            GC.SuppressFinalize(this);
        }
    }
    
    public class KeyStroke
    {
        public bool IsAlt { get; set; } = false;
        public bool IsCtrl { get; set; } = false;
        public bool isShift { get; set; } = false;
        public Keys hotKey { get; set; } = Keys.None;

        public void Clear()
        {
            IsAlt = false;
            IsCtrl = false;
            IsShift = false;
            hotKey = Keys.None;
        }
    }
}