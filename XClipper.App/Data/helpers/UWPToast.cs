using DesktopToast;
using NotificationsExtensions;
using NotificationsExtensions.Toasts;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows;
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
        /// <summary>
        /// A data structure to hold all necessary Toast options.
        /// </summary>
        private class ToastOption
        {
            public Action? OnActivatedListener;
            public Action? OnCancelledListener;
            public Dictionary<string, Action?> Listeners = new Dictionary<string, Action?>();
            public ToastRequest? request;
            public List<string> ImagePaths = new List<string>();
            public bool IsActionDispatched { get; set; } = false;
        }

        /// <summary>
        /// An internal table which will be used to find & invoke actions on toast objects.
        /// The reason this exist is because [OnActivated] will be called whenever user action is provoked 
        /// where you will receive all the arguments & the thing about it is, it should be register only at once.
        /// 
        /// This is a really bad design from Microsoft, but any ways these table will maintain those and once 
        /// toast visibility is finished it will clear the allocated memory space as well.
        /// </summary>
        private static Dictionary<string, ToastOption> ToastOptions = new Dictionary<string, ToastOption>();

        /// <summary>
        /// Each Toast will maintain it's unique Id which will help to distinguish other toast options from itself.
        /// </summary>
        private string uniqueId;
        private UWPToast(string uniqueId) 
        {
            this.uniqueId = uniqueId;
        }

        public async Task Show()
        {
            var option = ToastOptions.First(c => c.Key == uniqueId).Value;
            var result = await ToastManager.ShowAsync(option.request).ConfigureAwait(false);
            if (result.ToString() == "Activated" && option.OnActivatedListener != null  && !option.IsActionDispatched)
            {
                Application.Current.Dispatcher?.Invoke(option.OnActivatedListener);
            } else if (result.ToString() == "UserCanceled" && option.OnCancelledListener != null)
            {
                Application.Current.Dispatcher?.Invoke(option.OnCancelledListener);
                
            }
            foreach(var imagePath in option.ImagePaths) File.Delete(imagePath);

            ToastOptions.Remove(uniqueId);
        }

        public static void OnActivated(string arguments, Dictionary<string, string> _)
        {
            if (string.IsNullOrWhiteSpace(arguments)) return;

            var key = arguments.Split('_')[0];
            var option = ToastOptions.First(c => c.Key == key).Value;
            if (arguments.StartsWith($"{key}_action="))
            {
                option.IsActionDispatched = true;
               // ToastOptions = true;
                string? result = arguments?.Substring($"{key}_action=".Length);
                foreach (var entry in option.Listeners)
                {
                    if (result == entry.Key)
                    {
                        if (entry.Value != null)
                        {
                            Application.Current.Dispatcher.Invoke(entry.Value);
                        }
                    }
                }
            }
        }

        public class Builder
        {
            private ToastOption option = new ToastOption();
            private ToastAudioType AudioType = ToastAudioType.DEFAULT;
            private ToastDuration Duration = ToastDuration.Short;
            private bool IsSilent = false;
            private string uniqueId = RandomString(8);
            private ToastVisual Visual = new ToastVisual
            {
                BindingGeneric = new ToastBindingGeneric()
            };
            private ToastActionsCustom Actions = new ToastActionsCustom();
            private UWPToast uwpToast;

            public Builder(Dispatcher dispatcher)
            {
                uwpToast = new UWPToast(uniqueId);
            }

            public UWPToast build()
            {
                UWPToast.ToastOptions.Add(uniqueId, option);
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
                    ShortcutComment = uniqueId,
                    ShortcutTargetFilePath = Assembly.GetExecutingAssembly().Location,
                    AppId = "XClipper",
                    ActivatorId = typeof(NotificationActivator).GUID
                };

                option.request = request;

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
                var path = Path.GetTempFileName();
                option.ImagePaths.Add(Path.GetTempFileName());
                File.Delete(path);
                bitmap.Save(path);
                return AddImage(path);
            }

            public Builder AddImage(string path)
            {                
                Visual.BindingGeneric.Children.Add(new AdaptiveImage { Source = path });
                return this;
            }

            /// <summary>
            /// Add a top displaying image.
            /// </summary>
            /// <param name="imagePath"></param>
            /// <returns></returns>
            public Builder SetHeroImage(string path)
            {
                Visual.BindingGeneric.HeroImage = new ToastGenericHeroImage { Source = path };
                return this;
            }

            public Builder SetHeroImage(Bitmap bitmap)
            {
                var path = Path.GetTempFileName();
                option.ImagePaths.Add(Path.GetTempFileName());
                File.Delete(path);
                bitmap.Save(path);
                return SetHeroImage(path);
            }

            public Builder AddButton(string text, Action? onActivated = null)
            {
                var action = Regex.Replace(text, "[\\s]+", "-");
                option.Listeners.Add(action, onActivated);
                Actions.Buttons.Add(new ToastButton(text, $"{uniqueId}_action=" + action));
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

            public Builder SetOnActivatedListener(Action? listener)
            {
                option.OnActivatedListener = listener;
                return this;
            }

            public Builder SetOnCancelledListener(Action? listener)
            {
                option.OnCancelledListener = listener;
                return this;
            }
        }

        private static Random random = new Random();
        private static string RandomString(int length)
        {
            const string chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            return new string(Enumerable.Repeat(chars, length)
              .Select(s => s[random.Next(s.Length)]).ToArray());
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
