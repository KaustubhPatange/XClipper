using DesktopToast;
using System;
using System.Collections.Generic;
using System.Reflection;
using System.Runtime.InteropServices;

namespace Components
{
	/// <summary>
	/// Inherited class of notification activator (for Action Center of Windows 10)
	/// </summary>
	/// <remarks>The CLSID of this class must be unique for each application.</remarks>
	[Guid("2BFEE04A-C059-4D0E-BD35-2C15B61E7A06"), ComVisible(true), ClassInterface(ClassInterfaceType.None)]
	[ComSourceInterfaces(typeof(INotificationActivationCallback))]
	public class NotificationActivator : NotificationActivatorBase
	{
		public static void register()
		{
			NotificationActivator.RegisterComType(typeof(NotificationActivator), OnActivated);

			NotificationHelper.RegisterComServer(typeof(NotificationActivator), Assembly.GetExecutingAssembly().Location);
		}

		private const string MessageId = "Message";
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
