using RestSharp;
using System;
using System.Threading.Tasks;
using static Components.Core;
using static Components.DefaultSettings;
using static Components.Constants;
using System.Net;
using Newtonsoft.Json.Linq;
using System.Windows;
using Newtonsoft.Json;
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
                // Set this to true to indicate that we are checking license.
                IsCheckingForLicense = true;

                var responseText = await CheckForLicense().ConfigureAwait(false);
                Application.Current.Dispatcher.Invoke(delegate
                {
                    if (responseText != null)
                    {
                        var obj = JObject.Parse(responseText);

                        if (obj["status"].ToString() == "error")
                        {
                            block?.Invoke(new InvalidLicenseException(obj["message"].ToString()));
                            return;
                        }
                        var uid = obj["data"]["applicationId"].ToString();
                        var type = obj["data"]["licenseType"].ToString().ToEnum<LicenseType>();

                        if (uid == UniqueID)
                        {
                            IsPurchaseDone = type != LicenseType.Invalid;
                            LicenseStrategy = type;
                            block?.Invoke(null);
                        }
                        else block?.Invoke(new Exception("The given application Id doesn't match the current UID"));
                    }
                });

                // Set this to false to indicate that license checking is completed.
                IsCheckingForLicense = false;
            });
        }
        private async Task<string?> CheckForLicense()
        {
            var client = new RestClient(VALIDATION_SERVER(UniqueID));
            client.Timeout = RESTSHARP_TIMEOUT;
            try
            {
                var response = await client.ExecuteTaskAsync(new RestRequest(Method.GET)).ConfigureAwait(false);
                if (response.StatusCode == HttpStatusCode.OK)
                    return response.Content;
            }catch (Exception ex)
            {
                LogHelper.Log(this, ex.StackTrace);
            }
            finally { }
            return null;
        }
    }

    [Serializable]
    public class InvalidLicenseException : Exception
    {
        public InvalidLicenseException(string message) : base(message)
        { }

        public InvalidLicenseException()
        { }

        public InvalidLicenseException(string message, Exception innerException) : base(message, innerException)
        { }

        protected InvalidLicenseException(System.Runtime.Serialization.SerializationInfo serializationInfo, System.Runtime.Serialization.StreamingContext streamingContext)
        {
            throw new NotImplementedException();
        }
    }
}
