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
        private IFirebaseBinder? binder;

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
        public void BindUI(IFirebaseBinder binder)
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
                    binder.OnNeedToGenerateToken(FirebaseCurrent.DesktopAuth.ClientId, FirebaseCurrent.DesktopAuth.ClientSecret);
                else
                    MsgBoxHelper.ShowError(Translation.MSG_FIREBASE_USER_ERROR);
                return false;
            }
            if (FirebaseCurrent == null)
            {
                MsgBoxHelper.ShowError(Translation.MSG_FIREBASE_USER_ERROR);
                return false;
            }
            if (NeedToRefreshToken())
            {
                Log("Need to refresh token");
                if (await FirebaseHelper.RefreshAccessToken(FirebaseCurrent).ConfigureAwait(false))
                {
                    await CreateNewClient().ConfigureAwait(false);
                    return true;
                }
            }
            else return true;

            return false;
        }

        private async Task CreateNewClient()
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
        }

        #endregion
    }
}
