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
                
                releases = ApplyFilterBasedOnUpdateChannel(releases);
                
                int newVersion = releases?.FirstOrDefault().GetVersion() ?? 0;
                
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
        
        // Transforms the list based on filter channel.
        private List<ReleaseItem>? ApplyFilterBasedOnUpdateChannel(List<ReleaseItem>? releases)
        {
            if (releases == null) return null;
            var filtered = releases.Where(c => c.assets.Any(d => d.browser_download_url.EndsWith(".exe"))).ToList();

            ReleaseItem? update = null;
            if (DefaultSettings.UpdateChannel == Settings.UpdateChannel.Nightly)
            {
                update = filtered.Find(c => c.prerelease);
            }
            var stableBuild = filtered.Find(c => !c.prerelease);
            if (stableBuild.GetVersion() > (update?.GetVersion() ?? 0))
            {
                update = stableBuild;
            }

            if (update != null)
            {
                var index = filtered.IndexOf(update);
                return filtered.GetRange(index, filtered.Count);
            }

            return filtered;
        }
    }
}
