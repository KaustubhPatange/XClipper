using Newtonsoft.Json;
using RestSharp;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net.Http;
using System.Reflection;
using System.Threading.Tasks;
using System.Windows.Threading;
using static Components.Constants;

#nullable enable

namespace Components
{
    public class UpdaterService : IUpdater
    {
        private DispatcherTimer _timer = new();
        private RestClient client = new();
        private RestRequest request = new(GITHUB_RELEASE_URI, Method.GET);

        private Action<bool, List<ReleaseItem>?> block;
        public void Subscribe(Action<bool, List<ReleaseItem>?> block)
        {
            this.block = block;
            _timer.Interval = TimeSpan.FromMinutes(70);
           _timer.Tick += OnNeedToCheckUpdate;
           _timer.Start();
           OnNeedToCheckUpdate(null, EventArgs.Empty);
        }

        private void OnNeedToCheckUpdate(object sender, EventArgs e)
        {
            _timer.Stop();
            client.ExecuteAsync(request, (response) =>
            {
                _timer.Start();
                int appVersion = Assembly.GetExecutingAssembly().GetName().Version.ToString().Replace(".", "").ToInt(); // eg: 1000
                List<ReleaseItem>? releases = JsonConvert.DeserializeObject<List<ReleaseItem>>(response.Content);
                int newVersion = releases.FirstOrDefault().GetVersion();
                
                System.Windows.Application.Current.Dispatcher.Invoke(() =>
                {
                    block?.Invoke(newVersion > appVersion, releases);
                });
            });
        }

        public void Launch()
        {
            Process.Start(ApplicationWebsite);
        }
    }
}
