using DesktopToast;
using NotificationsExtensions;
using NotificationsExtensions.Toasts;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.IO;
using System.Reflection;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows.Threading;

#nullable enable

namespace Components
{
    public static class UWPToastExtensions
    {
        public static void ShowAsync(this UWPToast t) => t.Show().RunAsync();
    }
    public class UWPToast
    {
        internal string? ImagePath;
        internal Dictionary<string, Action?> Listeners = new Dictionary<string, Action?>();
        internal ToastRequest? request;
        internal Dispatcher? dispatcher;

        private UWPToast() { }

        public async Task Show()
        {
            NotificationActivator.RegisterComType(typeof(NotificationActivator), OnActivated);
            var result = await ToastManager.ShowAsync(request).ConfigureAwait(false);
            NotificationActivator.UnregisterComType();
            if (File.Exists(ImagePath)) File.Delete(ImagePath);
        }

        private void OnActivated(string arguments, Dictionary<string, string> data)
        {
            if ((arguments?.StartsWith("action=")).GetValueOrDefault())
            {
                string? result = arguments?.Substring("action=".Length);
                foreach(var entry in Listeners)
                {
                    if (result == entry.Key)
                    {
                        if (entry.Value != null)
                        {
                            dispatcher?.Invoke(entry.Value);
                        }
                    }
                }
            }
        }

        public class Builder
        {
            private UWPToast uwpToast = new UWPToast();
            private ToastAudioType AudioType = ToastAudioType.DEFAULT;
            private ToastDuration Duration = ToastDuration.Short;
            private bool IsSilent = false;
            private ToastVisual Visual = new ToastVisual
            {
                BindingGeneric = new ToastBindingGeneric()
            };
            private ToastActionsCustom Actions = new ToastActionsCustom();
            public Builder(Dispatcher dispatcher)
            {
                uwpToast.dispatcher = dispatcher;
            }

            public UWPToast build()
            {
                var toastContent = new ToastContent
                {
                    Visual = Visual,
                    Actions = Actions,
                    Duration = Duration,
                    Audio = new NotificationsExtensions.Toasts.ToastAudio
                    {
                        Silent = IsSilent,
                        Src = new Uri(EnumHelper.GetEnumDescription(AudioType))
                    }
                };

                var request = new ToastRequest
                {
                    ToastXml = toastContent.GetContent(),
                    ShortcutFileName = "XClipper.lnk",
                    ShortcutTargetFilePath = Assembly.GetExecutingAssembly().Location,
                    AppId = "XClipper",
                    ActivatorId = typeof(NotificationActivator).GUID
                };

                uwpToast.request = request;

                return uwpToast;
            }

            public Builder AddText(string text)
            {
                Visual.BindingGeneric.Children.Add(new AdaptiveText { Text = text });
                return this;
            } 

            /// <summary>
            /// Construct from AppResources
            /// </summary>
            /// <param name="bitmap"></param>
            /// <returns></returns>
            public Builder AddImage(Bitmap bitmap)
            {
                uwpToast.ImagePath = Path.GetTempFileName();
                File.Delete(uwpToast.ImagePath);
                bitmap.Save(uwpToast.ImagePath);
                Visual.BindingGeneric.Children.Add(new AdaptiveImage { Source = uwpToast.ImagePath });
                return this;
            }

            public Builder AddImage(string path)
            {
                Visual.BindingGeneric.Children.Add(new AdaptiveImage { Source = path });
                return this;
            }

            public Builder AddButton(string text, Action? onActivated = null)
            {
                var action = Regex.Replace(text, "[\\s]+", "-");
                uwpToast.Listeners.Add(action, onActivated);
                Actions.Buttons.Add(new ToastButton(text, "action=" + action));
                return this;
            }

            public Builder SetDuration(ToastDuration duration)
            {
                Duration = duration;
                return this;
            }

            public Builder SetAudioType(ToastAudioType type)
            {
                AudioType = type;
                return this;
            }

            public Builder SetSilent(bool silent)
            {
                IsSilent = silent;
                return this;
            }
        }
    }

    public enum ToastAudioType
    {
        [Description("ms-winsoundevent:Notification.Default")]
        DEFAULT,
        [Description("ms-winsoundevent:Notification.Mail")]
        MAIL,
        [Description("ms-winsoundevent:Notification.Reminder")]
        REMINDER,
    }
}
