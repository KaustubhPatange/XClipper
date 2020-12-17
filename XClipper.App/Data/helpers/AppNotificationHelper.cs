using DesktopToast;
using NotificationsExtensions;
using NotificationsExtensions.Toasts;
using System;
using System.Reflection;
using System.Threading.Tasks;
using System.ComponentModel;
using System.Windows.Threading;
using System.Collections.Generic;
using static Components.TranslationHelper;

#nullable enable

namespace Components
{
	// TODO: Add a custom logo for ToastVisual.
	public static class AppNotificationHelper
	{

		/// <summary>
		/// A basic toast
		/// </summary>
		/// <param name="title"></param>
		/// <param name="message"></param>
		/// <param name="silent"></param>
		/// <param name="doOnActivated"></param>
		/// 
		[Obsolete("Replace with UWPToast")]
		public static async Task ShowBasicToast(Dispatcher dispatcher, string title, string? message = null, bool silent = false, 
			ToastAudioType audioType = ToastAudioType.DEFAULT, Action? doOnActivated = null)
		{
			var visual = new ToastVisual
			{
				BindingGeneric = new ToastBindingGeneric
				{
					Children =
					{
						new AdaptiveText { Text = title }
					}
				}
			};

			if (message != null)
				visual.BindingGeneric.Children.Add(new AdaptiveText { Text = message });

			var request = CreateRequest(visual, audioType, Silent: silent);

			var result = await ToastManager.ShowAsync(request).ConfigureAwait(false);

			if (result == ToastResult.Activated)
			{
				if (doOnActivated != null)
				{
					dispatcher?.Invoke(doOnActivated);
				}
			}
		}

		/// <summary>
		/// This will show notification for image type data.
		/// </summary>
		/// <param name="imagePath"></param>
		///

		[Obsolete("Replace with UWPToast")]
		public static async Task ShowImageToast(Dispatcher dispatcher, string imagePath, string? title = null, string? message = null,
			bool silent = false, ToastAudioType audioType = ToastAudioType.DEFAULT, Action? doOnActivated = null)
		{
            var visual = new ToastVisual
            {
                BindingGeneric = new ToastBindingGeneric
                {
                    Children =
                    {
                        new AdaptiveImage { Source = imagePath },
                    }
                }
            };

            if (title != null)
                visual.BindingGeneric.Children.Add(new AdaptiveText { Text = title });

            if (message != null)
                visual.BindingGeneric.Children.Add(new AdaptiveText { Text = message });

            var request = CreateRequest(visual, audioType, Silent: silent);

            var result = await ToastManager.ShowAsync(request).ConfigureAwait(false);

            if (result == ToastResult.Activated)
            {
                if (doOnActivated != null)
                {
                    dispatcher?.Invoke(doOnActivated);
                }
            }
        }

		/// <summary>
		/// A particular dialog for only one use case i.e to inform users about synchronization feature.
		/// </summary>
		/// <param name="dispatcher"></param>
		/// <param name="onLearnMoreClick"></param>
		/// <returns></returns>
        public static void ShowSyncDialog(Dispatcher dispatcher, Action? onLearnMoreClick = null)
        {
			var builder = new UWPToast.Builder(dispatcher);
			builder
				.AddImage(Properties.Resources.connectivity)
				.AddText(Translation.SYNC_DIALOG_TITLE)
				.AddText(Translation.SYNC_DIALOG_MESSAGE)
				.AddButton(Translation.MSG_CANCEL_SMALL)
				.AddButton(Translation.MSG_LEARN, onLearnMoreClick)
				.SetOnActivatedListener(onLearnMoreClick)
				.SetDuration(ToastDuration.Long)
				.SetAudioType(ToastAudioType.REMINDER)
				.build().ShowAsync();
		}

		public static void ShowIntroNotification(Dispatcher dispatcher, Action? onLearnMoreClick = null)
		{
			var builder = new UWPToast.Builder(dispatcher);
			builder
				.AddImage(Properties.Resources.introduction)
				.AddText(Translation.MSG_INTRO_TITLE)
				.AddText(Translation.MSG_INTRO_MESSAGE)
				.AddButton(Translation.MSG_CANCEL_SMALL)
				.AddButton(Translation.MSG_LEARN, onLearnMoreClick)
				.SetOnActivatedListener(onLearnMoreClick)
				.SetDuration(ToastDuration.Long)
				.SetAudioType(ToastAudioType.REMINDER)
				.build().ShowAsync();
		}

		public static void ShowPurchaseNotification(Dispatcher dispatcher, Action? onLearnMoreClick = null)
        {
			var builder = new UWPToast.Builder(dispatcher);
			builder
				.AddImage(Properties.Resources.payment)
				.AddText(Translation.MSG_PURCHASE_TITLE)
				.AddText(Translation.MSG_PURCHASE_MESSAGE)
				.AddButton(Translation.MSG_CANCEL_SMALL)
				.AddButton(Translation.MSG_LEARN, onLearnMoreClick)
				.SetOnActivatedListener(onLearnMoreClick)
				.SetDuration(ToastDuration.Long)
				.SetAudioType(ToastAudioType.REMINDER)
				.build().ShowAsync();
		}

        private static ToastRequest CreateRequest(ToastVisual visual, ToastAudioType audioType, IToastActions? actions = null, ToastDuration duration = ToastDuration.Short, bool Silent = false)
		{
			var toastContent = new ToastContent
			{
				Visual = visual,
				Actions = actions,
				Duration = duration,
				Audio = new NotificationsExtensions.Toasts.ToastAudio
				{
					Silent = Silent,
					Src = new Uri(EnumHelper.GetEnumDescription(audioType))
				}
			};

			return new ToastRequest
			{
				ToastXml = toastContent.GetContent(),
				ShortcutFileName = "XClipper.lnk",
				ShortcutTargetFilePath = Assembly.GetExecutingAssembly().Location,
				AppId = "XClipper",
				ActivatorId = typeof(NotificationActivator).GUID
			};
		}
	}
}
