using RestSharp;
using System;
using System.Threading.Tasks;
using static Components.Core;
using static Components.DefaultSettings;
using static Components.Constants;
using System.Net;
using Newtonsoft.Json.Linq;
using System.Windows.Threading;
using System.Windows;
using System.Linq;
using Newtonsoft.Json;
using System.Windows.Documents;
using System.Collections.Generic;

#nullable enable

namespace Components
{
    public class LicenseHelper : ILicense
    {
        public void Initiate(Action<Exception?> block)
        {
            Task.Run(async () =>
            {
                var responseText = await CheckForLicense().ConfigureAwait(false);
                Application.Current.Dispatcher.Invoke(delegate
                {
                    if (responseText != null)
                    {
                        var obj = JObject.Parse(responseText);
                        var uid = obj["data"]["applicationId"].ToString();
                        var type = obj["data"]["licenseType"].ToString().ToEnum<LicenseType>();

                        // We must firebase configuration data to this setting.
                        FirebaseConfigurations.AddRange(JsonConvert.DeserializeObject<List<FirebaseData>>(obj["firebaseData"].ToString()));
                        if (uid == UniqueID)
                        {
                            IsPurchaseDone = type != LicenseType.Invalid;
                            LicenseStrategy = type;
                            block(null);
                        }
                        else block(new Exception("The given application Id doesn't match the current UID"));
                    }
                });
            });
        }
        private async Task<string?> CheckForLicense()
        {
            var client = new RestClient($"{AUTHOR_SERVER}/xclipper/validate?uid={UniqueID}");
            client.Timeout = RESTSHARP_TIMEOUT;
            try
            {
                var response = await client.ExecuteTaskAsync(new RestRequest(Method.GET)).ConfigureAwait(false);
                if (response.StatusCode == HttpStatusCode.OK)
                    return response.Content;
            }
            finally { }
            return null;
        }
    }
}
