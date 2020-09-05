using Autofac;
using FireSharp.Core;
using FireSharp.Core.Config;
using FireSharp.Core.Interfaces;
using System;
using System.Collections.Generic;
using System.Data;
using System.Linq;
using System.Threading.Tasks;
using static Components.DefaultSettings;
using static Components.TranslationHelper;
using static Components.FirebaseHelper;
using System.Windows;
using static Components.MainHelper;
using Microsoft.VisualBasic.Logging;
using System.CodeDom.Compiler;
using System.Windows.Media.Converters;
using System.Data.SqlTypes;

#nullable enable

// todo: Write what this class does.

namespace Components
{
    public sealed class FirebaseSingleton
    {
        #region Variable Declaration

        /// <summary>
        /// We will set a boolean which will let me know if there is on going operation is going.
        /// </summary>
        private bool isPreviousAddRemaining, isPreviousRemoveRemaining, isGlobalUserExecuting = false;
        private bool isClientInitialized = false;
        private readonly List<string> addStack = new List<string>();
        private readonly List<string> removeStack = new List<string>();
        private readonly List<object> globalUserStack = new List<object>();

        private static FirebaseSingleton Instance;
        private IFirebaseClient client;
        private IFirebaseBinder binder;
        private string UID;
        private bool alwaysForceInvoke = false;
        private User user;

        /// <summary>
        /// Since <see cref="InitConfig(FirebaseData?)"/> is used in many cases it is not safe to call <br/>
        /// <see cref="SetCallback"/> more than once. This boolean will make sure to call it once.
        /// </summary>
        private bool isBinded = false;

        #endregion

        #region Singleton Constructor

        public static FirebaseSingleton GetInstance
        {
            get
            {
                if (Instance == null)
                    Instance = new FirebaseSingleton();
                return Instance;
            }
        }
        private FirebaseSingleton()
        { }

        #endregion

        #region Private Methods

        /// <summary>
        /// This will check if the access Token is valid or not. It will also 
        /// update the client with new access token.
        /// </summary>
        /// <returns></returns>
        private async Task<bool> CheckForAccessTokenValidity()
        {
            // When we don't need Auth for desktop client, we can return true.
            if (FirebaseCurrent?.isAuthNeeded == false) return true;

            if (!IsValidCredential())
            {
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
                if (await RefreshAccessToken(FirebaseCurrent).ConfigureAwait(false))
                {
                    await CreateNewClient().ConfigureAwait(false);
                    return true;
                }
            }
            else return true;

            return false;
        }
        private async Task<User> _GetUser()
        {
            var data = await client.SafeGetAsync($"users/{UID}").ConfigureAwait(false);
            if (data == null || data.Body == "null") // Sometimes it catch to this exception which is due to unknown error.
            {
                return await RegisterUser().ConfigureAwait(false);
            }
            else return data.ResultAs<User>();//.Also((user) => { this.user = user; });
        }

        private void SetCommonUserInfo(User user)
        {
            // todo: Set some other details for user...
            user.MaxItemStorage = DatabaseMaxItem;
            user.TotalConnection = DatabaseMaxConnection;
            user.IsLicensed = IsPurchaseDone;
            user.LicenseStrategy = LicenseStrategy;
        }

        private void CheckForDataRemoval(User? firebaseUser)
        {
            if (firebaseUser != null && user != null && BindDelete)
            {
                if (firebaseUser.Clips == null) return;
                var items = user.Clips?.ConvertAll(c => c.data).Except(firebaseUser.Clips?.ConvertAll(c => c.data));
                foreach (var data in items ?? new List<string>())
                    binder.OnClipItemRemoved(new RemovedEventArgs(data.DecryptBase64(DatabaseEncryptPassword)));
            }
        }

        /// <summary>
        /// This must be called whenever client is changed.
        /// </summary>
        private async Task CreateNewClient()
        {
            // We will set isBinded to false since we are creating a new client.
            isBinded = false;
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
            client = new FirebaseClient(config);

            await SetGlobalUserTask(true).ConfigureAwait(false);

            // BindUI is already set, make sure to set callback to it.
            SetCallback();

            isClientInitialized = true;
        }

        #endregion

        #region Configuration Methods

        /// <summary>
        /// Initializes the Firebase client. Must be called if credentials are changed.
        /// </summary>
        /// <param name="data"></param>
        public void InitConfig(FirebaseData? data = null)
        {
            isClientInitialized = false;

            UID = UniqueID;
            if (data == null && FirebaseConfigurations.Count > 0)
            {
                FirebaseCurrent = FirebaseConfigurations[0];
            }
            else FirebaseCurrent = data;
            if (FirebaseCurrent != null)
            {
                CreateCurrentQRData(); // Create QR data for settings window.
                if (FirebaseCurrent.isAuthNeeded)
                {
                    if (!IsValidCredential())
                    {
                        binder.OnNeedToGenerateToken(FirebaseCurrent.DesktopAuth.ClientId, FirebaseCurrent.DesktopAuth.ClientSecret);
                        return;
                    }
                    else if (NeedToRefreshToken())
                    {
                        CheckForAccessTokenValidity(); // PS: I don't care.
                        return;
                    }
                }
                CreateNewClient();
            }
            else
                MsgBoxHelper.ShowError(Translation.MSG_FIREBASE_UNKNOWN_ERR);
        }

        /// <summary>
        /// This will submit configuration change to database.
        /// </summary>
        /// <returns></returns>
        public async Task SubmitConfigurationsTask()
        {
            if (!BindDatabase) return;
            await SetGlobalUserTask().ConfigureAwait(false);

            SetCommonUserInfo(user);

            await client.SafeSetAsync($"users/{UID}", user).ConfigureAwait(false);
        }

        /// <summary>
        /// This will load the user from the firebase database.<br/>
        /// Returns True if user is valid.
        /// </summary>
        /// <param name="forceInvoke">Forcefully load the data even if user is not null.</param>
        /// <returns>True if user exist</returns>
        public async Task<bool> SetGlobalUserTask(bool forceInvoke = false)
        {
            if (isGlobalUserExecuting)
            {
                globalUserStack.Add(new object());
                return false;
            }
            isGlobalUserExecuting = true;

            // Return if database observing is disabled.
            if (!BindDatabase)
            {
                clearAwaitedGlobalUserTask();
                return false;
            }

            if (client == null)
            {
                MsgBoxHelper.ShowError(Translation.MSG_FIREBASE_CLIENT_ERR);
                clearAwaitedGlobalUserTask();
               
                // todo: Do something when client isn't initialized
                /* 
                 * We can implement a call stack to this, all you need to do is to make
                 * a stack that accepts data & operation name. Once this client is
                 * initialized we can do our job.
                 */
                return false;
            }

            if (await CheckForAccessTokenValidity().ConfigureAwait(false) && (alwaysForceInvoke || user == null || forceInvoke))
            {
                var firebaseUser = await _GetUser().ConfigureAwait(false);

                SetCommonUserInfo(firebaseUser);

                CheckForDataRemoval(firebaseUser);

                if (firebaseUser.Devices != null && firebaseUser.Devices.Count > 0)
                    alwaysForceInvoke = true;

                user = firebaseUser;
            }

            clearAwaitedGlobalUserTask();

            return user != null;
        }

        private void clearAwaitedGlobalUserTask()
        {
            isGlobalUserExecuting = false;
            if (globalUserStack.Count > 0)
            {
                globalUserStack.Clear();
                SetGlobalUserTask().RunAsync();
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

        /// <summary>
        /// This sets call back to the binder events with an attached interface.<br/>
        /// Must be used after <see cref="FirebaseSingleton.BindUI(IFirebaseBinder)"/>
        /// </summary>
        private async void SetCallback()
        {
            if (isBinded) return;
            try
            {
                await client.OnAsync($"users/{UID}", (o, a, c) =>
                {
                    if (BindDatabase)
                        binder.OnDataAdded(a);
                }, (o, a, c) =>
                {
                    if (BindDatabase)
                        binder.OnDataChanged(a);
                }, (o, a, c) =>
                {
                    if (BindDatabase)
                        binder.OnDataRemoved(a);
                }).ConfigureAwait(false);

                isBinded = true;
            }
            catch(Exception ex)
            {
                if (ex.Message.Contains("401 (Unauthorized)"))
                {
                    if (await RefreshAccessToken(FirebaseCurrent).ConfigureAwait(false))
                    {
                        await CreateNewClient().ConfigureAwait(false);
                    }
                    else MsgBoxHelper.ShowError(ex.Message);
                }
                LogHelper.Log(this, ex.StackTrace);
            }
        }

        #endregion

        #region User Related Method

        /// <summary>
        /// Checks if the user exist in the nodes or not.
        /// </summary>
        /// <returns></returns>
        private async Task<bool> IsUserExist()
        {
            var response = await client.SafeGetAsync($"users/{UID}").ConfigureAwait(false);
            return response != null && response.Body != "null";
        }

        /// <summary>
        /// Add an empty user to the node.
        /// </summary>
        /// <returns></returns>
        public async Task<User> RegisterUser()
        {
            if (!BindDatabase) return new User();
            var exist = await IsUserExist().ConfigureAwait(false);
            if (!exist)
            {
                var user = new User();
                SetCommonUserInfo(user);
                this.user = user;
                await client.SafeSetAsync($"users/{UID}", user).ConfigureAwait(false);
            }
            return user;
        }

        /// <summary>
        /// Removes all data associated with the UID.
        /// </summary>
        /// <returns></returns>
        public async Task RemoveUser()
        {
            await client.SafeDeleteAsync($"users/{UID}").ConfigureAwait(false);
            await RegisterUser().ConfigureAwait(false);
        }

        /// <summary>
        /// Returns the user details.
        /// </summary>
        /// <returns></returns>
        public User GetUser()
        {
            return user;
        }

        public async Task<List<Device>?> GetDeviceListAsync()
        {
            if (!BindDatabase) return new List<Device>();

            if (await SetGlobalUserTask(true).ConfigureAwait(false))
                return user.Devices;

            return new List<Device>();
        }

        public async Task<List<Device>> RemoveDevice(string DeviceId)
        {
            if (!BindDatabase) return new List<Device>();

            if (await SetGlobalUserTask(true).ConfigureAwait(false))
            {
                user.Devices = user.Devices.Where(d => d.id != DeviceId).ToList();
                await client.SafeUpdateAsync($"users/{UID}", user).ConfigureAwait(false);
                return user.Devices;
            }

            return new List<Device>();
        }

        /// <summary>
        /// Add a clip data to the server instance. Also support multiple calls which
        /// is maintained through stack.
        /// </summary>
        /// <param name="Text"></param>
        /// <returns></returns>
        public async Task AddClip(string? Text)
        {
            // If some add operation is going, we will add it to stack.
            if (isPreviousAddRemaining && Text != null)
            {
                addStack.Add(Text);
                return;
            }
            isPreviousAddRemaining = true;
            if (await SetGlobalUserTask().ConfigureAwait(false))
            {
                if (Text == null) return;
                if (Text.Length > DatabaseMaxItemLength) return;
                if (user.Clips == null)
                    user.Clips = new List<Clip>();
                // Remove clip if greater than item
                if (user.Clips.Count > DatabaseMaxItem)
                    user.Clips.RemoveAt(0);
                
                // Add data from current [Text]
                user.Clips.Add(new Clip { data = Text.EncryptBase64(DatabaseEncryptPassword), time = DateTime.Now.ToFormattedDateTime(false) });

                // Also add data from stack
                foreach (var stackText in addStack)
                    user.Clips.Add(new Clip { data = stackText.EncryptBase64(DatabaseEncryptPassword), time = DateTime.Now.ToFormattedDateTime(false) });

                // Clear the stack after adding them all.
                addStack.Clear(); 

                await client.SafeUpdateAsync($"users/{UID}", user).ConfigureAwait(false);
            }
            isPreviousAddRemaining = false;
        }

        public async Task RemoveClip(int position)
        {
            if (await SetGlobalUserTask().ConfigureAwait(false))
            {
                if (user.Clips == null)
                    return;
                user.Clips.RemoveAt(position);
                await client.SafeUpdateAsync($"users/{UID}", user).ConfigureAwait(false);
            }
        }

        /// <summary>
        /// Removes the clip data of user.
        /// </summary>
        /// <param name="Text"></param>
        /// <returns></returns>
        public async Task RemoveClip(string? Text)
        {
            // If some remove operation is going, we will add it to stack.
            if (isPreviousRemoveRemaining && Text != null)
            {
                removeStack.Add(Text);
                return;
            }

            isPreviousRemoveRemaining = true;

            if (await SetGlobalUserTask().ConfigureAwait(false))
            {
                if (Text == null) return;
                if (user.Clips == null)
                    return;

                var originalListCount = user.Clips.Count;
                // Add current one to stack as well to perform LINQ 
                removeStack.Add(Text);

                user.Clips.RemoveAll(c => removeStack.Exists(d => d == c.data.DecryptBase64(DatabaseEncryptPassword)));

                if (originalListCount != user.Clips.Count)
                    await client.SafeUpdateAsync($"users/{UID}", user).ConfigureAwait(false);
                
                removeStack.Clear();
            }
            isPreviousRemoveRemaining = false;
        }


        /// <summary>
        /// Remove all clip data of user.
        /// </summary>
        /// <returns></returns>
        public async Task RemoveAllClip()
        {
            if (await SetGlobalUserTask().ConfigureAwait(false))
            {
                if (user.Clips == null)
                    return;
                user.Clips.Clear();
                await client.SafeUpdateAsync($"users/{UID}", user).ConfigureAwait(false);
            }
        }

        #endregion

    }

    #region Entities

    public class User
    {
        /// <summary>
        /// Property tells what type of license user owns.
        /// </summary>
        public LicenseType LicenseStrategy { get; set; }

        /// <summary>
        /// Property tells whether the user has purchased license for this software or not.
        /// </summary>
        public bool IsLicensed { get; set; }

        /// <summary>
        /// Property tells the maximum number of device to be connected.
        /// </summary>
        public int TotalConnection { get; set; } = DatabaseMaxConnection;

        /// <summary>
        /// Property denotes the maximum this database can hold.
        /// </summary>
        public int MaxItemStorage { get; set; } = DatabaseMaxItem;

        /// <summary>
        /// Property tells the last connected Android device given its ID. Null means no one is connected.
        /// </summary>
        public List<Device>? Devices { get; set; }

        /// <summary>
        /// Property stores all the clip data.
        /// </summary>
        public List<Clip>? Clips { get; set; }
    }

    public class Device
    {
        public string id { get; set; }
        public int sdk { get; set; }
        public string model { get; set; }
    }

    public class Clip
    {
        public string data { get; set; }
        public string time { get; set; }
    }

    public class FirebaseData
    {
        public OAuth DesktopAuth { get; set; }
        public OAuth MobileAuth { get; set; }
        public string Endpoint { get; set; }
        public string AppId { get; set; }
        public string ApiKey { get; set; }
        public bool isAuthNeeded { get; set; }
    }
    public class OAuth
    {
        public string ClientId { get; set; }
        public string? ClientSecret { get; set; }
    }

    #endregion
}
