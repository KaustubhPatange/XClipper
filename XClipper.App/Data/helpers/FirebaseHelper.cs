using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using RestSharp;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using static Components.DefaultSettings;

namespace Components
{
    public static class FirebaseHelper
    {
        public static async Task<bool> RefreshAccessToken(FirebaseData user)
        {
            var client = new RestClient($"https://oauth2.googleapis.com/token?client_id={user.DesktopAuth.ClientId}&client_secret={user.DesktopAuth.ClientSecret}&refresh_token={FirebaseCredential.RefreshToken}&grant_type=refresh_token");
            client.Timeout = 30 * 1000;
            var request = new RestRequest(Method.POST);
            request.AddHeader("Content-Type", "application/x-www-form-urlencoded");
            IRestResponse response = await client.ExecuteTaskAsync(request);
            if (response.StatusCode == System.Net.HttpStatusCode.OK)
            {
                var jsonKeyPairs = JsonConvert.DeserializeObject<Dictionary<string, string>>(response.Content);
                string access_token = jsonKeyPairs["access_token"];
                HandleTokenDetails(access_token, FirebaseCredential.RefreshToken);
                return true;
            }
            return false;
        }

        /// <summary>
        /// This will set credential related settings in <see cref="DefaultSettings"/> and also
        /// save this setting to file using <see cref="DefaultSettings.WriteFirebaseCredentialSetting"/>
        /// </summary>
        /// <param name="accessToken"></param>
        /// <param name="refreshToken"></param>
        public static void HandleTokenDetails(string accessToken, string refreshToken)
        {
            FirebaseCredential.AccessToken = accessToken;
            FirebaseCredential.RefreshToken = refreshToken;
            // Generally, we create a refresh time of 50 min.
            FirebaseCredential.TokenRefreshTime = (DateTime.Now.ToFormattedDateTime(false).ToLong() + 5000);
            WriteFirebaseCredentialSetting();
        }
    }
}
