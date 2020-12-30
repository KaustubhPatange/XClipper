using System;
using System.ComponentModel.Composition;
using System.Diagnostics;
using System.Net;
using System.Net.NetworkInformation;
using System.Runtime.InteropServices;
using System.Threading.Tasks;
using System.Windows.Threading;
using static Components.DefaultSettings;

namespace Components
{
    public class ConnectionHelper
    {
        private static ConnectionHelper connectionHelper = new ConnectionHelper();
        private DispatcherTimer networkPingTimer = new DispatcherTimer();

        public static void StartMonitoring()
        {
            connectionHelper.Attach();
        }

        #region Private methods

        /// <summary>
        /// Attaches the timer to find network state.
        /// </summary>
        private void Attach()
        {
            networkPingTimer.Interval = new TimeSpan(0,0,4);
            networkPingTimer.Tick += OnNetworkChange;
            networkPingTimer.Start();
        }

        private async void OnNetworkChange(object sender, EventArgs e)
        {
            networkPingTimer.Stop();
            int desc;
            var isConnected = InternetGetConnectedState(out desc, 0);
            if (isConnected)
            {
                try
                {
                    using (var client = new WebClient())
                    using (client.OpenRead("https://example.com/"))
                        isConnected = true;
                }catch 
                { 
                    isConnected = false;
                }
            }
            IsNetworkConnected = isConnected;
            networkPingTimer.Start();
        }

        [DllImport("wininet.dll")]
        private extern static bool InternetGetConnectedState(out int Description, int ReservedValue);

        #endregion
    }
}
