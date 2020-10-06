
using FireSharp.Core.EventStreaming;
using System;
using System.Collections.Generic;

namespace Components
{
    public interface IFirebaseBinder
    {
        void OnDataAdded(ValueAddedEventArgs e);
        void OnDataChanged(ValueChangedEventArgs e);
        void OnDataRemoved(ValueRemovedEventArgs e);

        /// <summary>
        /// This is a custom call which will determine if there is a
        /// removal of clip data in real-time.
        /// </summary>
        /// <param name="e"></param>
        void OnClipItemRemoved(RemovedEventArgs e);

        /// <summary>
        /// This will be called whenever there is currently no Access Token.
        /// </summary>
        /// <param name="ClientId"></param>
        /// <param name="ClientSecret"></param>
        void OnNeedToGenerateToken(string ClientId, string ClientSecret);

        /// <summary>
        /// When there is not configuration file this will be called.
        /// </summary>
        void OnNoConfigurationFound();

        /// <summary>
        /// When user select to not provide a configuration file, it should
        /// reset the firebase setting.
        /// </summary>
        void OnResetFirebaseConfig();

        /// <summary>
        /// This will trigger when an image is added to firebase storage.
        /// </summary>
        void OnImageAddedToStorage();
    }

    public class RemovedEventArgs : EventArgs
    {
        public RemovedEventArgs(string data)
        {
            this.data = data;
        }
        /// <summary>
        /// Decrypted Clip.data text
        /// </summary>
        public string data { get; private set; }
    }
}
