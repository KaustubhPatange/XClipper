
using FireSharp.Core.EventStreaming;

namespace Components
{
    public interface IFirebaseBinder
    {
        void OnDataAdded(ValueAddedEventArgs e);
        void OnDataChanged(ValueChangedEventArgs e);
        void OnDataRemoved(ValueRemovedEventArgs e);

        /// <summary>
        /// This will be called whenever there is currently no Access Token.
        /// </summary>
        /// <param name="ClientId"></param>
        /// <param name="ClientSecret"></param>
        void OnNeedToGenerateToken(string ClientId, string ClientSecret);
    }
}
