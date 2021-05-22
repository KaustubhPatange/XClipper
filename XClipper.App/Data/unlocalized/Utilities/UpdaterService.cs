using Newtonsoft.Json;
using RestSharp;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net.Http;
using System.Reflection;
using System.Threading.Tasks;
using static Components.Constants;

#nullable enable

namespace Components
{
    public class UpdaterService : IUpdater
    {
        public void Check(Action<bool, List<ReleaseItem>?>? block)
        {
            var client = new RestClient();
            var request = new RestRequest(GITHUB_RELEASE_URI, Method.GET);
            client.ExecuteAsync(request, (response) =>
            {
                float appVersion = Assembly.GetExecutingAssembly().GetName().Version.ToString().Replace(".", "").ToInt() / (float)1000; // eg: 1000
                List<ReleaseItem>? releases = JsonConvert.DeserializeObject<List<ReleaseItem>>(response.Content);
                float newVersion = releases.FirstOrDefault().GetVersion() / (float)1000;
                
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
