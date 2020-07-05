using Newtonsoft.Json.Linq;
using RestSharp;
using System;
using System.Threading.Tasks;
using static Components.DefaultSettings;

namespace Components
{
    public static class FirebaseHelper
    {
        public static async Task<bool> RefreshAccessToken(FirebaseData user)
        {
            var client = new RestClient($"https://oauth2.googleapis.com/token?client_id={user.Auth.ClientId}&client_secret=_{user.Auth.ClientSecret}&refresh_token={FirebaseRefreshToken}&grant_type=refresh_token");
            client.Timeout = 30 * 1000;
            var request = new RestRequest(Method.POST);
            request.AddHeader("Content-Type", "application/x-www-form-urlencoded");
            IRestResponse response = await client.ExecuteTaskAsync(request);
            if (response.StatusCode == System.Net.HttpStatusCode.OK)
            {
                var obj = new JObject(response.Content);
                HandleTokenDetails(obj["access_token"].ToString(), obj["refresh_token"].ToString());
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
            FirebaseAccessToken = accessToken;
            FirebaseRefreshToken = refreshToken;
            FirebaseTokenRefreshTime = DateTime.Now.ToFormattedDateTime(false).ToInt();
            WriteFirebaseCredentialSetting();
        }
    }
}
