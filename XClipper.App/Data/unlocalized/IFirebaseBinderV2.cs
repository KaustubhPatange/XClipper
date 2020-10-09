using System;

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

        void OnClipItemAdded(string unencryptedData);
        void OnClipItemRemoved(string path);

        void OnDeviceAdded(Device device);
        void OnDeviceRemoved(Device device);
    }
}
