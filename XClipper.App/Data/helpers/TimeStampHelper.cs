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
            ShowIntroductionDialog();
            ShowSyncDialogIfNecessary();
            ShowPurchaseDialogIfNecessary();
        }

        private static bool ShowIntroductionDialog()
        {
            if (IsIntroductionNecessary())
            {
                AppNotificationHelper.ShowIntroNotification(
                    dispatcher: Application.Current.Dispatcher,
                    onLearnMoreClick: () =>
                    {
                        Process.Start(DOC_INTRODUCTION);
                    }
                );
                return true;
            }
            return false;
        }

        private static bool ShowPurchaseDialogIfNecessary()
        {
            if (IsPurchaseNotifyNecessary())
            {
                AppNotificationHelper.ShowPurchaseNotification(
                    dispatcher: Application.Current.Dispatcher,
                    onLearnMoreClick: () =>
                    {
                        Process.Start(ApplicationWebsite);
                    }
                );
                return true;
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
                   {
                       Process.Start(DOC_SYNCHRONIZATION);
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
                TimeStamps.PurchaseInfo = DateTime.Now.AddDays(5).ToFormattedDateTime(false);
                WriteTimeStampsSetting();
                return false;
            }
            long old = TimeStamps.EnableSync.ToLong();
            long current = DateTime.Now.ToFormattedDateTime(false).ToLong();
            if (current >= old)
            {
                TimeStamps.PurchaseInfo = DateTime.Now.AddDays(5).ToFormattedDateTime(false);
                WriteTimeStampsSetting();
                return true;
            }
            return false;
        }

        #endregion
    }
}
