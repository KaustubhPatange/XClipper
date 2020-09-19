using Newtonsoft.Json;
using RestSharp;
using System;
using System.Diagnostics;
using System.Reflection;
using static Components.Constants;

#nullable enable

namespace Components
{
    public class UpdaterService : IUpdater
    {
        public void Check(Action<bool, Update?>? block)
        {
            var client = new RestClient(UPDATE_URI);
            var request = new RestRequest();
            client.ExecuteAsync(request, (response) =>
            {
                Update? updateInfo = JsonConvert.DeserializeObject<Update>(response.Content);

                int version = updateInfo?.Desktop.Version.Replace(".", "").ToInt() ?? 0; // eg: 1001
                int appVersion = Assembly.GetExecutingAssembly().GetName().Version.ToString().Replace(".", "").ToInt(); // eg: 1000

                if (version > appVersion)
                    block?.Invoke(true, updateInfo);
                else
                    block?.Invoke(false, null);
            });
        }

        public void Launch()
        {
            Process.Start(ApplicationWebsite);
        }
    }
}
