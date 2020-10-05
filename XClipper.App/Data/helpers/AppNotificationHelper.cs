using DesktopToast;
using NotificationsExtensions;
using NotificationsExtensions.Toasts;
using System;
using System.Collections.Generic;
using System.Reflection;
using static Components.TranslationHelper;
using static Components.Constants;
using Autofac;
using System.Windows;
using System.Diagnostics;
using System.IO;

namespace Components
{
	// TODO: Add a custom logo for ToastVisual.

	public static class AppNotificationHelper
	{
		public static void register()
		{
			NotificationActivator.RegisterComType(typeof(NotificationActivator), OnActivated);

			NotificationHelper.RegisterComServer(typeof(NotificationActivator), Assembly.GetExecutingAssembly().Location);
		}

		/// <summary>
		/// This will show notification for Text type data.
		/// </summary>
		/// <param name="rawText"></param>
		public static async void DisplayToastForText(string rawText)
		{
			var visual = new ToastVisual
			{
				BindingGeneric = new ToastBindingGeneric
				{
					Children =
					{
						new AdaptiveText { Text = Translation.APP_COPY_TITLE },
						new AdaptiveText { Text = rawText.Truncate(NOTIFICATION_TRUNCATE_TEXT) },
					}
				}
			};

			var request = CreateRequest(visual);

			var result = await ToastManager.ShowAsync(request).ConfigureAwait(false);

			if (result == ToastResult.Activated)
			{
				var recorder = AppModule.Container.Resolve<IKeyboardRecorder>();
				recorder.Ignore(() =>
				{
					Clipboard.SetText(rawText);
				});
			}
		}

		/// <summary>
		/// This will show notification for image type data.
		/// </summary>
		/// <param name="imagePath"></param>
		public static async void DisplayToastForImage(string imagePath)
		{
			var visual = new ToastVisual
			{
				BindingGeneric = new ToastBindingGeneric
				{
					Children =
					{
						new AdaptiveImage { Source = imagePath },
						new AdaptiveText { Text = Translation.APP_COPY_TITLE_IMAGE },
						new AdaptiveText { Text = imagePath },
					}
				}
			};

			var request = CreateRequest(visual);

			var result = await ToastManager.ShowAsync(request).ConfigureAwait(false);

			if (result == ToastResult.Activated)
			{
				Process.Start(imagePath);
			}
		}

		private const string MessageId = "Message";
		
		private static ToastRequest CreateRequest(ToastVisual visual, ToastDuration duration = ToastDuration.Short, bool Silent = false)
		{
			var toastContent = new ToastContent
			{
				Visual = visual,
				Duration = duration,
				Audio = new NotificationsExtensions.Toasts.ToastAudio
				{
					Silent = Silent,
					Src = new Uri("ms-winsoundevent:Notification.Mail")
				}
			};

			return new ToastRequest
			{
				ToastXml = toastContent.GetContent(),
				ShortcutFileName = "XClipper.Wpf.lnk",
				ShortcutTargetFilePath = Assembly.GetExecutingAssembly().Location,
				AppId = "XClipper.Wpf",
				ActivatorId = typeof(NotificationActivator).GUID
			};
		}

		private static void OnActivated(string arguments, Dictionary<string, string> data)
		{
			var result = "Activated";
			if ((arguments?.StartsWith("action=")).GetValueOrDefault())
			{
				result = arguments.Substring("action=".Length);

				if ((data?.ContainsKey(MessageId)).GetValueOrDefault())
				{
					// dispatch data[MessageId] for interactive toasts.
				}
			}
			// dispatch result to know if toast is activated (click or interacted).
		}
	}
}
