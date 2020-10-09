using static Components.DefaultSettings;
using static Components.Constants;
using static Components.TranslationHelper;
using System.IO;
using System.Xml.Linq;
using FireSharp.Core.Interfaces;
using System.Collections.Generic;
using System;
using System.Threading.Tasks;
using FireSharp.Core.Config;
using FireSharp.Core;
using Newtonsoft.Json;
using System.Linq.Expressions;
using System.Linq;

#nullable enable

/**
 * A class which needs to be made for safe handling of Firebase data as
 * original V1 needs a complete refactoring.
 */
namespace Components
{
    public sealed class FirebaseSingletonV2
    {

        #region Variable declarations

        private bool isPreviousAddRemaining, isPreviousRemoveRemaining, isPreviousUpdateRemaining, isGlobalUserExecuting = false;

        private readonly List<string> addStack = new List<string>();
        private readonly List<string> removeStack = new List<string>();
        private readonly Dictionary<string, string> updateStack = new Dictionary<string, string>();
        private readonly List<object> globalUserStack = new List<object>();

        private const string USER_REF = "users";
        private const string CLIP_REF = "Clips";
        private const string DEVICE_REF = "Devices";

        private string UID;

        private User user;

        private IFirebaseClient client;
        private IFirebaseBinderV2? binder;

        #endregion

        #region Singleton Constructor

        private static FirebaseSingletonV2 Instance;
        public static FirebaseSingletonV2 GetInstance
        {
            get
            {
                if (Instance == null)
                    Instance = new FirebaseSingletonV2();
                return Instance;
            }
        }
        private FirebaseSingletonV2()
        { }

        #endregion

        #region Configuration methods

        public void Initialize()
        {
            UID = UniqueID;
            if (FirebaseCurrent == null) return;

            /* Load the previous state of user or if user is not null it means some configuration
             *  has changed and we should delete the previous state file to avoid any further errors.
             */
            if (user == null)
                LoadUserState();
            else
                File.Delete(UserStateFile);

            ClearAllStack();

            MainHelper.CreateCurrentQRData();

            if (FirebaseCurrent.isAuthNeeded)
            {
                if (!IsValidCredential())
                {
                    Log("Token not valid");
                    binder?.OnNeedToGenerateToken(FirebaseCurrent.DesktopAuth.ClientId, FirebaseCurrent.DesktopAuth.ClientSecret);
                    return;
                }
                else if (NeedToRefreshToken())
                {
                    Log("We need to refresh token");
                    CheckForAccessTokenValidity(); // PS: I don't care.
                    return;
                }
            }
        }

        /// <summary>
        /// This will be used to set binder at the start of the application.
        /// </summary>
        /// <param name="binder"></param>
        public void BindUI(IFirebaseBinderV2 binder)
        {
            this.binder = binder;
        }

        #endregion


        #region State persistence 

        public void SaveUserState()
        {
            if (user != null)
            {
                Log("Saved current user state");
                File.WriteAllText(UserStateFile, User.ToNode(user).ToString());
            }
        }

        public void LoadUserState()
        {
            if (File.Exists(UserStateFile))
            {
                try
                {
                    var xml = File.ReadAllText(UserStateFile);
                    user = User.FromNode(XElement.Parse(xml));
                    Log("Previous user state is restored");
                }
                catch
                {
                    Log("Invalid previous user state");
                }
            }
        }

        #endregion

        #region Public methods

        /// <summary>
        /// Determines whether it is necessary to refresh current access token.
        /// </summary>
        /// <returns></returns>
        public static bool NeedToRefreshToken() =>
            DateTime.Now.ToFormattedDateTime(false).ToLong() >= FirebaseCredential.TokenRefreshTime;

        #endregion

        #region Private methods
        private void Log(string? message = null)
        {
            LogHelper.Log(nameof(FirebaseSingletonV2), message);
        }

        private void ClearAllStack()
        {
            addStack.Clear(); isPreviousAddRemaining = false;
            removeStack.Clear(); isPreviousRemoveRemaining = false;
            globalUserStack.Clear(); isGlobalUserExecuting = false;
            updateStack.Clear(); isPreviousUpdateRemaining = false;
        }

        /// <summary>
        /// This will check if the access Token is valid or not. It will also 
        /// update the client with new access token.
        /// </summary>
        /// <returns></returns>
        private async Task<bool> CheckForAccessTokenValidity()
        {
            // When we don't need Auth for desktop client, we can return true.
            Log($"Checking for token : {FirebaseCurrent?.isAuthNeeded}");
            if (FirebaseCurrent?.isAuthNeeded == false) return true;

            if (!IsValidCredential())
            {
                Log("Credentials are not valid");
                if (FirebaseCurrent != null)
                    binder?.OnNeedToGenerateToken(FirebaseCurrent.DesktopAuth.ClientId, FirebaseCurrent.DesktopAuth.ClientSecret);
                else
                    MsgBoxHelper.ShowError(Translation.MSG_FIREBASE_USER_ERROR);
                return false;
            }
            if (NeedToRefreshToken())
            {
                Log("Need to refresh token");
                if (await FirebaseHelper.RefreshAccessToken(FirebaseCurrent).ConfigureAwait(false))
                {
                    CreateNewClient();
                    return true;
                }
            }
            else return true;

            return false;
        }

        private void CreateNewClient()
        {
            IFirebaseConfig config;
            if (FirebaseCurrent?.isAuthNeeded == true)
            {
                config = new FirebaseConfig
                {
                    AccessToken = FirebaseCredential?.AccessToken,
                    BasePath = FirebaseCurrent.Endpoint
                };
            }
            else
            {
                config = new FirebaseConfig
                {
                    BasePath = FirebaseCurrent.Endpoint
                };
            }
            if (client != null) client.Dispose();
            client = new FirebaseClient(config);

            SetUserCallback();
        }

        /// <summary>
        /// This sets call back to the binder events with an attached interface.<br/>
        /// Must be used after <see cref="FirebaseSingleton.BindUI(IFirebaseBinder)"/>
        /// </summary>
        private async void SetUserCallback()
        {
            Log();
            try
            {
                await client.OnAsync($"users/{UID}", 
                    onDataChange: async (o, a, c) =>
                {
                    if (!BindDatabase) return;

                    if (!await CheckForAccessTokenValidity().ConfigureAwait(false)) return;

                    User? firebaseUser = JsonConvert.DeserializeObject<User>(a.Data);

                    // Check for inconsistent data...
                    if (string.IsNullOrWhiteSpace(a.Data))
                    {
                        if (user != null)
                            await PushUser().ConfigureAwait(false);
                        else await RegisterUser().ConfigureAwait(false);
                    }

                    // If there is no new data then it's of no use...
                    if (firebaseUser == null) return;

                    // Perform data addition & removal operation...
                    if (user != null)
                    {
                        // Check for clip data addition...
                        var newClips = firebaseUser?.Clips?.Select(c => c.data).ToNotNullList();
                        var oldClips = user?.Clips?.Select(c => c.data).ToNotNullList();
                        foreach (var item in newClips.Except(oldClips))
                            binder?.OnClipItemAdded(item.DecryptBase64(DatabaseEncryptPassword));

                        // Check for clip data removal...
                        foreach (var item in oldClips.Except(newClips))
                            binder?.OnClipItemRemoved(item.DecryptBase64(DatabaseEncryptPassword));

                        // Check for device addition & removal...
                        var newDevices = firebaseUser?.Devices.ToList() ?? new List<Device>();
                        var oldDevices = user?.Devices.ToList() ?? new List<Device>();
                        foreach (var device in newDevices.Except(oldDevices))
                            binder?.OnDeviceAdded(device);
                        foreach (var device in oldDevices.Except(newDevices))
                            binder?.OnDeviceRemoved(device);

                        user = firebaseUser!;
                    }

                    user = firebaseUser!;

                }).ConfigureAwait(false);
            }
            catch (Exception ex)
            {
                if (ex.Message.Contains("401 (Unauthorized)"))
                {
                    if (await FirebaseHelper.RefreshAccessToken(FirebaseCurrent).ConfigureAwait(false))
                    {
                        CreateNewClient();
                    }
                    else MsgBoxHelper.ShowError(ex.Message);
                }
                LogHelper.Log(this, ex.StackTrace);
            }
        }

        /// <summary>
        /// This will push the current state of the user to the database
        /// </summary>
        /// <returns></returns>
        private async Task PushUser()
        {
            Log("Is User null: " + (user != null));
            if (user != null)
                await client.SafeSetAsync($"{USER_REF}/{UID}", user).ConfigureAwait(false);
        }

        /// <summary>
        /// Add an empty user to the node.
        /// </summary>
        /// <returns></returns>
        private async Task RegisterUser()
        {
            Log();
            var user = await FetchUser().ConfigureAwait(false);
            if (user == null)
            {
                var localUser = new User();
                await SetCommonUserInfo(localUser).ConfigureAwait(false);
                this.user = localUser;
                await PushUser().ConfigureAwait(false);
            }
            else this.user = user;
        }

        private async Task<User?> FetchUser()
        {
            Log();
            var data = await client.SafeGetAsync($"users/{UID}").ConfigureAwait(false);
            return data.ResultAs<User>();//.Also((user) => { this.user = user; });
        }

        private async Task SetCommonUserInfo(User user)
        {
            Log($"User null? {user == null}");
            var originallyLicensed = user.IsLicensed;

            // todo: Set some other details for user...
            user.MaxItemStorage = DatabaseMaxItem;
            user.TotalConnection = DatabaseMaxConnection;
            user.IsLicensed = IsPurchaseDone;
            user.LicenseStrategy = LicenseStrategy;

            if (originallyLicensed != IsPurchaseDone)
                await PushUser().ConfigureAwait(false);
        }
        #endregion
    }
}
