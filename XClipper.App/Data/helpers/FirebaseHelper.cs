﻿using FireSharp.Core.Config;
using FireSharp.Core.Interfaces;
using FireSharp.Core.Response;
using Newtonsoft.Json;
using RestSharp;
using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Diagnostics;
using System.Threading.Tasks;
using System.Windows;
using static Components.Constants;
using static Components.DefaultSettings;

#nullable enable

namespace Components
{
    /// <summary>
    /// An interface to listen any changes made to Firebase Data. Typically used for real-time updates.
    /// </summary>
    public interface IFirebaseDataListener
    {
        void OnFirebaseDataChange();
    }
    public static class FirebaseHelper
    {

        private const int TIMEOUT = 40;
        /// <summary>
        /// This will initialize the client safely. It will report to the user in case of any issue.<br/><br/>
        /// It will create a new instance after verifying <see cref="BindDatabase"/><br/>
        /// Must be called after <see cref="LoadFirebaseSetting"/><br/>
        /// </summary>
        public static async Task InitializeService(IFirebaseBinderV2? binder = null)
        {
            if (binder != null)
                FirebaseSingletonV2.GetInstance.BindUI(binder);
            if (BindDatabase)
            {
                if (!PerformSafetyChecks(doOnNoConfigurationFile: () =>
                {
                    binder?.OnNoConfigurationFound();
                }, doOnResetConfiguration: () =>
                {
                    binder?.OnResetFirebaseConfig();
                })) return;

                await FirebaseSingletonV2.GetInstance.Initialize();
                MainHelper.ToggleCurrentQRData();
            } else DeInitializeService();
        }

        /// <summary>
        /// De-initialize the service based on the value from <see cref="DefaultSettings.BindDatabase"/>
        /// </summary>
        public static void DeInitializeService()
        {
            if (!BindDatabase)
            {
                FirebaseSingletonV2.GetInstance.Deinitialize();
                MainHelper.ToggleCurrentQRData();
            }
        }

        public static bool PerformSafetyChecks(Action doOnNoConfigurationFile, Action? doOnResetConfiguration = null)
        {
            if (FirebaseCurrent == null)
            {
                var result = MessageBox.Show(Translation.SYNC_CONFIG_ERR, Translation.MSG_ERR, MessageBoxButton.YesNoCancel, MessageBoxImage.Error);
                if (result == MessageBoxResult.Yes)
                {
                    doOnNoConfigurationFile?.Invoke();
                }
                else if (result == MessageBoxResult.No)
                {
                    doOnResetConfiguration?.Invoke();
                }
                return false;
            }
            return true;
        }

        public static async Task<bool> RefreshAccessToken(FirebaseData? user)
        {
            if (user == null) return false;
            var client = new RestClient($"https://oauth2.googleapis.com/token?client_id={DesktopAuth.ClientId}&client_secret={DesktopAuth.ClientSecret}&refresh_token={FirebaseCredential.RefreshToken}&grant_type=refresh_token");
            client.Timeout = 30 * 1000;
            var request = new RestRequest(Method.POST);
            request.AddHeader("Content-Type", "application/x-www-form-urlencoded");
            try
            {
                IRestResponse response = await client.ExecuteTaskAsync(request).TimeoutAfter(TimeSpan.FromSeconds(TIMEOUT)).ConfigureAwait(false);
                if (response.StatusCode == System.Net.HttpStatusCode.OK)
                {
                    var jsonKeyPairs = JsonConvert.DeserializeObject<Dictionary<string, string>>(response.Content);
                    string access_token = jsonKeyPairs["access_token"];
                    HandleTokenDetails(access_token, FirebaseCredential.RefreshToken);
                    return true;
                }
            }
            catch (TimeoutException ex)
            {
                LogHelper.Log(typeof(FirebaseHelper), ex.Message + ex.StackTrace);
            }
            return false;
        }

        /// <summary>
        /// Determines whether it is necessary to refresh current access token.
        /// </summary>
        /// <returns></returns>
        public static bool NeedToRefreshToken() =>
            DateTime.Now.ToFormattedDateTime(false).ToLong() >= FirebaseCredential.TokenRefreshTime;

        /// <summary>
        /// This will set credential related settings in <see cref="DefaultSettings"/> and also
        /// save this setting to file using <see cref="WriteFirebaseCredentialSetting"/>
        /// </summary>
        /// <param name="accessToken"></param>
        /// <param name="refreshToken"></param>
        public static void HandleTokenDetails(string accessToken, string refreshToken)
        {
            FirebaseCredential.AccessToken = accessToken;
            FirebaseCredential.RefreshToken = refreshToken;
            // Generally, we should refresh token within frame of 50 min.
            FirebaseCredential.TokenRefreshTime = DateTime.Now.AddMinutes(50).ToFormattedDateTime(false).ToLong();
            WriteFirebaseCredentialSetting();
        }

        public static void AddContent(string data)
        {
            FirebaseSingletonV2.GetInstance.AddClip(data).RunAsync();
        }

        public static void UpdateContent(string oldData, string newData)
        {
            FirebaseSingletonV2.GetInstance.UpdateData(oldData, newData).RunAsync();
        }

        /**
         * There might be some case where some user manually try to change the access token from
         * credentials file or any other thing. In such case these safe methods are created to 
         * catch such errors & refresh the token.
         */
        #region Safe FirebaseClient Extensions

        /// <summary>
        /// Use this instead of <see cref="IFirebaseClient.UpdateAsync{T}(string, T)"/>. 
        /// It will automatically check for invalid access token and refresh it.
        /// </summary>
        /// <param name="client"></param>
        /// <param name="path"></param>
        /// <param name="data"></param>
        /// <returns></returns>
        public static async Task<FirebaseResponse> SafeUpdateAsync<T>(this IFirebaseClient client, string path, T data)
        {
            try
            {
                var response = await client.UpdateAsync(path, data).TimeoutAfter(TimeSpan.FromSeconds(TIMEOUT)).ConfigureAwait(false);
                return response;
            }
            catch (Exception ex)
            {
                CheckForTimedOutException(ex);
                if (ex.Message.Contains("401 (Unauthorized)"))
                {
                    if (await RefreshAccessToken(FirebaseCurrent).ConfigureAwait(false))
                    {
                        client.UpdateConfig(new FirebaseConfig { AccessToken = FirebaseCredential.AccessToken });
                        return await SafeUpdateAsync(client, path, data).ConfigureAwait(false);
                    }
                }
                LogHelper.Log(typeof(FirebaseHelper), ex.Message + ex.StackTrace);
                if (CheckForUnauthorizedRequest(ex))
                    return CreateUnAuthorizedException();
            }
            return null;
        }

        /// <summary>
        /// Use this instead of <see cref="IFirebaseClient.SetAsync{T}(string, T)"/>. 
        /// It will automatically check for invalid access token and refresh it.
        /// </summary>
        /// <param name="client"></param>
        /// <param name="path"></param>
        /// <param name="data"></param>
        /// <returns></returns>
        public static async Task<SetResponse> SafeSetAsync<T>(this IFirebaseClient client, string path, T data)
        {
            try
            {
                var response = await client.SetAsync(path, data).TimeoutAfter(TimeSpan.FromSeconds(TIMEOUT)).ConfigureAwait(false);
                return response;
            }
            catch (Exception ex)
            {
                CheckForTimedOutException(ex);
                if (ex.Message.Contains("401 (Unauthorized)"))
                {
                    if (await RefreshAccessToken(FirebaseCurrent).ConfigureAwait(false))
                    {
                        client.UpdateConfig(new FirebaseConfig { AccessToken = FirebaseCredential.AccessToken });
                        return await SafeSetAsync(client, path, data).ConfigureAwait(false);
                    }
                }
                CheckForUnauthorizedRequest(ex);

                LogHelper.Log(typeof(FirebaseHelper), ex.Message + ex.StackTrace);
            }
            return null;
        }

        /// <summary>
        /// Use this instead of <see cref="IFirebaseClient.GetAsync(string)"/>. 
        /// It will automatically check for invalid access token and refresh it.
        /// </summary>
        /// <param name="client"></param>
        /// <param name="path"></param>
        /// <returns></returns>
        public static async Task<FirebaseResponse> SafeGetAsync(this IFirebaseClient client, string path)
        {
            try
            {
                var response = await client.GetAsync(path).TimeoutAfter(TimeSpan.FromSeconds(TIMEOUT)).ConfigureAwait(false);
                return response;
            }
            catch (Exception ex)
            {
                CheckForTimedOutException(ex);
                if (ex.Message.Contains("401 (Unauthorized)"))
                {
                    if (await RefreshAccessToken(FirebaseCurrent).ConfigureAwait(false))
                    {
                        client.UpdateConfig(new FirebaseConfig { AccessToken = FirebaseCredential.AccessToken });
                        return await SafeGetAsync(client, path).ConfigureAwait(false);
                    }
                }
                LogHelper.Log(typeof(FirebaseHelper), ex.Message + ex.StackTrace);
                if (CheckForUnauthorizedRequest(ex))
                    return CreateUnAuthorizedException();
            }
            return null;
        }

        /// <summary>
        /// Use this instead of <see cref="IFirebaseClient.DeleteAsync(string)"/>. 
        /// It will automatically check for invalid access token and refresh it.
        /// </summary>
        /// <param name="client"></param>
        /// <param name="path"></param>
        /// <returns></returns>
        public static async Task<FirebaseResponse> SafeDeleteAsync(this IFirebaseClient client, string path)
        {
            try
            {
                var response = await client.DeleteAsync(path).TimeoutAfter(TimeSpan.FromSeconds(TIMEOUT)).ConfigureAwait(false);
                return response;
            }
            catch (Exception ex)
            {
                CheckForTimedOutException(ex);
                if (ex.Message.Contains("401 (Unauthorized)"))
                {
                    if (await RefreshAccessToken(FirebaseCurrent).ConfigureAwait(false))
                    {
                        client.UpdateConfig(new FirebaseConfig { AccessToken = FirebaseCredential.AccessToken });
                        return await SafeDeleteAsync(client, path).ConfigureAwait(false);
                    }
                }
                LogHelper.Log(typeof(FirebaseHelper), ex.Message + ex.StackTrace);
                if (CheckForUnauthorizedRequest(ex))
                    return CreateUnAuthorizedException();
            }
            return null;
        }

        /// <summary>
        /// If last actions didn't complete on time we should notify users & do 
        /// the following.
        /// </summary>
        public static void ShowSurpassMessage()
        {
            Process.Start(ACTION_NOT_COMPLETE_WIKI);
        }

        /// <summary>
        /// This will check if user signed with wrong account and therefore needs 
        /// to sign in again with proper account.
        /// </summary>
        /// <param name="ex"></param>
        private static bool CheckForUnauthorizedRequest(Exception ex)
        {
            if (ex.Message.Contains("Unauthorized request."))
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    RemoveFirebaseCredentials();
                    MsgBoxHelper.ShowError(Translation.MSG_WRONG_SIGNIN);
                    InitializeService();
                });
                return true;
            }
            return false;
        }

        /// <summary>
        /// If there is a timeout out exception we will shutdown the firebase client.
        /// 
        /// Appropriate measures must be taken to let the users know that there is either
        /// problem with their Internet connection or with XClipper.
        /// </summary>
        /// <param name="ex"></param>
        private static void CheckForTimedOutException(Exception ex)
        {
            if (ex.StackTrace.Contains("<TimeoutAfter>"))
            {
                if (!FirebaseSingletonV2.GetInstance.IsDeinitialized)
                {
                    //MessageBox.Show(Translation.SYNC_TIMEOUT_ERROR_TEXT, Translation.SYNC_TIMEOUT_ERROR_TITLE, MessageBoxButton.OK, MessageBoxImage.Error);
                    //FirebaseSingletonV2.GetInstance.Deinitialize();
                }
            }
        }

        private static FirebaseResponse CreateUnAuthorizedException()
        {
            return new FirebaseResponse("error", System.Net.HttpStatusCode.Unauthorized);
        }

        #endregion

        /**
         * Encryption logic for firebase data.
         */
        #region Encryption Extension

        public static string EncryptBase64(this string t, string password) => FirebaseCurrent?.IsEncrypted ?? true ? Core.EncryptBase64(t, password) : t;
        public static string DecryptBase64(this string t, string password) => FirebaseCurrent?.IsEncrypted ?? true ? Core.DecryptBase64(t, password) : t;

        #endregion
    }
}
