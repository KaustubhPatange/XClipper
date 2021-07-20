using Components.UI;
using GalaSoft.MvvmLight.Command;
using static Components.Constants;
using System.Net;
using System.Windows.Input;
using System;
using System.Collections.Generic;
using System.Windows;
using System.Diagnostics;
using System.IO;
using System.Linq;

#nullable enable

namespace Components
{
    public class UpdateViewModel : BaseViewModel
    {
        private WebClient client = new WebClient();
        private ReleaseAsset? release;
        private string UpdatePackageFile;
        public UpdateViewModel(List<ReleaseItem>? updateModels)
        {
            MainButton = new RelayCommand(MainButtonClicked);

            var updateModel = updateModels.FirstOrDefault();
            release = updateModel.assets.FirstOrDefault(c => c.name.EndsWith(".exe", StringComparison.OrdinalIgnoreCase));
            TotalBytes = SizeHelper.FormatSizeWithSuffix(release?.size ?? 0);

            UpdatePackageFile = GetUpdatePackageFile(updateModel.GetVersion());
            
            InfoText = string.Format("{0} {1} Build ({2})\n\n", updateModel.tag_name, updateModel.prerelease ? "Nightly" : "Stable", updateModel.GetDatePretty());
            foreach (var releaseItem in updateModels)
            {
                InfoText += $"{releaseItem.GetFormattedBody()}\n\n";
            }

            if (release == null) IsUpdateEnabled = false;
        }

        #region Actual Bindings

        public string AppTitle { get; } = $"{Translation.UPDATE_TITLE} [{ApplicationVersion}]";
        public ICommand MainButton { get; set; }
        public int Progress { get; set; } = 0;
        public Status Define { get; set; } = Status.Completed;
        public string InfoText { get; set; } = string.Empty;
        public string Cancel { get; } = Translation.MSG_CANCEL_SMALL;
        public string Download { get; } = Translation.MSG_DOWNLOAD;
        public string RecievedBytes { get; set; } = "0 B";
        public string TotalBytes { get; set; } = "0 B";
        public bool IsUpdateEnabled { get; set; } = true;

        #endregion

        #region Methods

        private void MainButtonClicked()
        {
            if (!Directory.Exists(ApplicationTempDirectory)) Directory.CreateDirectory(ApplicationTempDirectory);

            if (Define == Status.Completed)
            {
                // Check if existing download already present.
                if (File.Exists(UpdatePackageFile))
                {
                    var fileInfo = new FileInfo(UpdatePackageFile);
                    if (fileInfo.Length == release?.size)
                    {
                        CallPostUpdate();
                        return;
                    }
                    else
                        // Delete the package coz it might be old version or partially downloaded.
                        File.Delete(UpdatePackageFile);
                }

                // Start the new downloading here.
                client = new WebClient();
                client.DownloadProgressChanged += (o, e) =>
                {
                    Progress = e.ProgressPercentage;
                    RecievedBytes = SizeHelper.FormatSizeWithSuffix(e.BytesReceived);
                    TotalBytes = SizeHelper.FormatSizeWithSuffix(e.TotalBytesToReceive);
                };
                client.DownloadFileCompleted += (o, e) =>
                {
                    Define = Status.Completed;
                    Application.Current.Dispatcher.Invoke(CallPostUpdate);
                };
                client.DownloadFileAsync(new Uri(release?.browser_download_url), UpdatePackageFile);

                // Change the status to downloading.
                Define = Status.Downloading;
            }
            else
            {
                client.CancelAsync();
                // Change the status to completed i.e either stopped or not yet started.
                Define = Status.Completed;

            }
        }

        private void CallPostUpdate()
        {
            // Make progress to zero
            Progress = 0;

            if (release?.size != new FileInfo(UpdatePackageFile).Length)
            {
                TotalBytes = "0 B";
                RecievedBytes = "0 B";
                MsgBoxHelper.ShowError(Translation.UPDATE_DOWNLOAD_SIZE_ERROR);
                return;
            }
            var result = MessageBox.Show(Translation.UPDATE_DOWNLOAD_COMPLETE, Translation.MSG_INFO, MessageBoxButton.OKCancel, MessageBoxImage.Information);
            if (result == MessageBoxResult.OK)
            {
                Process.Start(UpdatePackageFile);
                Application.Current.Shutdown();
            }
        }

        #endregion
    }
}
