using static Components.DefaultSettings;
using static Components.Constants;
using System;
using System.Diagnostics;
using System.Windows.Threading;
using System.Windows;

namespace Components
{
    public static class TimeStampHelper
    {
        public static void ShowRequiredNotifications()
        {

            ShowSyncDialogIfNecessary();
        }

        private static bool ShowIntroductionDialog()
        {
            if (IsIntroductionNecessary())
            {

            }
            return false;
        }

        private static bool ShowSyncDialogIfNecessary()
        {
            if (IsEnableSyncNecessary())
            {
                AppNotificationHelper.ShowSyncDialog(
                   dispatcher: Application.Current.Dispatcher,
                   onLearnMoreClick: () =>
                   {// TODO: If first launch show a dialog for getting started.
                    // TODO: Show notification about purchasing license after 4-5 days of launch.
                       Process.Start(DOCUMENTATION);
                   }
               );
                return true;
            }
            return false;
        }


        #region Internal methods

        private static bool IsIntroductionNecessary()
        {
            if (!TimeStamps.ShownIntroduction)
            {
                return true;
            }
            return false;
        }

        private static bool IsEnableSyncNecessary()
        {
            if (FirebaseCurrent != null) return false;
            if (string.IsNullOrWhiteSpace(TimeStamps.EnableSync))
            {
                TimeStamps.EnableSync = DateTime.Now.AddDays(2).ToFormattedDateTime(false);
                WriteTimeStampsSetting();
                return false;
            }
            long old = TimeStamps.EnableSync.ToLong();
            long current = DateTime.Now.ToFormattedDateTime(false).ToLong();
            if (current >= old)
            {
                TimeStamps.EnableSync = DateTime.Now.AddDays(2).ToFormattedDateTime(false);
                WriteTimeStampsSetting();
                return true;
            }
            return false;
        }

        private static bool IsPurchaseNotifyNecessary()
        {
            if (string.IsNullOrWhiteSpace(TimeStamps.PurchaseInfo))
            {
                TimeStamps.PurchaseInfo = DateTime.Now.AddDays(4).ToFormattedDateTime(false);
                WriteTimeStampsSetting();
                return false;
            }
            long old = TimeStamps.EnableSync.ToLong();
            long current = DateTime.Now.ToFormattedDateTime(false).ToLong();
            if (current >= old)
            {
                TimeStamps.PurchaseInfo = DateTime.Now.AddDays(4).ToFormattedDateTime(false);
                WriteTimeStampsSetting();
                return true;
            }
            return false;
        }

        #endregion
    }
}
