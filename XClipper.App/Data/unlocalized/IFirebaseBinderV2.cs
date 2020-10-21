using System;
using System.Collections.Generic;
using System.Windows.Documents;

#nullable enable

namespace Components
{
    public interface IFirebaseBinderV2
    {
        /// <summary>
        /// <inheritdoc cref="IFirebaseBinder.OnNeedToGenerateToken(string, string)"/>
        /// </summary>
        /// <param name="ClientId"></param>
        /// <param name="ClientSecret"></param>
        void OnNeedToGenerateToken(string ClientId, string ClientSecret);

        /// <summary>
        /// <inheritdoc cref="IFirebaseBinder.SendNotification(string, string, System.Action?)"/>
        /// </summary>
        void SendNotification(string title, string message, Action? onActive = null);

        /// <summary>
        /// <inheritdoc cref="IFirebaseBinder.OnNoConfigurationFound"/>
        /// </summary>
        void OnNoConfigurationFound();

        /// <summary>
        /// <inheritdoc cref="IFirebaseBinder.OnResetFirebaseConfig"/>
        /// </summary>
        void OnResetFirebaseConfig();

        void OnClipItemAdded(List<string> unencryptedDataList);
        void OnClipItemRemoved(List<string> unencryptedDataList);
        void OnClipItemUpdated(string previousUnEncryptedData, string newUnEncryptedData);

        void OnDeviceAdded(Device device);
        void OnDeviceRemoved(Device device);
    }
}
