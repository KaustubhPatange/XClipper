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
using static Components.MainHelper;
using static Components.Constants;
using Firebase.Storage;
using System.IO;
using RestSharp;
using Newtonsoft.Json.Linq;
using System.Text.RegularExpressions;
using System.Security.RightsManagement;
using System.Xml.Linq;
using System.Data.SqlTypes;
using System.Diagnostics;
using System.Drawing.Text;

#nullable enable

// todo: Write what this class does.

namespace Components
{
    public sealed class FirebaseSingleton : IDisposable
    {
        #region Variable Declaration

        /// <summary>
        /// We will set a boolean which will let me know if there is on going operation is going.
        /// </summary>
        private bool isPreviousAddRemaining, isPreviousRemoveRemaining, isPreviousUpdateRemaining, isGlobalUserExecuting = false;

        private bool isClientInitialized = false;
        private readonly List<string> addStack = new List<string>();
        private readonly List<string> removeStack = new List<string>();
        private readonly Dictionary<string, string> updateStack = new Dictionary<string, string>();
        private readonly List<object> globalUserStack = new List<object>();

        private TimeSpan TIMEOUT_SPAN = TimeSpan.FromSeconds(15);

        private static FirebaseSingleton Instance;
        private IFirebaseClient client;
        private IFirebaseBinder binder;
        private string UID;

        /// <summary>
        /// This will make sure that if there are any devices connected it will forcefully execute <br/>
        /// the if branch in <see cref="SetGlobalUserTask(bool)"/> which will then check for any <br/>
        /// data removal objects.
        /// </summary>
        private bool alwaysForceInvoke = false;
        private User user;
        private readonly string tag = nameof(FirebaseSingleton);

        /// <summary>
        /// Since <see cref="InitConfig(FirebaseData?)"/> is used in many cases it is not safe to call <br/>
        /// <see cref="SetUserCallback"/> more than once. This boolean will make sure to call it once.
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

        private void Log(string? message = null)
        {
            LogHelper.Log(tag, message);
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
                if (await RefreshAccessToken(FirebaseCurrent).ConfigureAwait(false))
                {
                    await CreateNewClient().ConfigureAwait(false);
                    return true;
                }
            }
            else return true;

            return false;
        }

        private async Task<User?> FetchUser()
        {
            Log();
            var data = await client.SafeGetAsync($"users/{UID}").ConfigureAwait(false);
            if (data != null && data.StatusCode == System.Net.HttpStatusCode.Unauthorized)
                return null;
            if (data == null || data.Body == "null") // Sometimes it catch to this exception which is due to unknown error.
            {
                Log("Data body is null");
                binder.SendNotification(Translation.SYNC_ERROR_TITLE, Translation.SYNC_UNKNOWN_ERROR, LogHelper.OpenLogFile);
                return await RegisterUser().ConfigureAwait(false);
            }
            else return data.ResultAs<User>();//.Also((user) => { this.user = user; });
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
                await client.SafeUpdateAsync($"users/{UID}", user).ConfigureAwait(false);
        }

        private bool CheckForDataRemoval(User? firebaseUser)
        {
            bool userInvoked = false;
            bool firebaseInvoked = false;
            try
            {
                if (firebaseUser != null && user != null && BindDelete)
                {
                    Log();
                    if (user.Clips == null || firebaseUser.Clips == null) return false;

                    userInvoked = true;
                    var userClips = user.Clips?.ConvertAll(c => c.data).ToList();

                    firebaseInvoked = true;
                    var firebaseClips = firebaseUser.Clips?.ConvertAll(c => c.data).ToList();

                    var items = userClips.Except(firebaseClips);
                    //   var items = user.Clips?.ConvertAll(c => c.data).Except(firebaseUser.Clips?.ConvertAll(c => c.data));
                    foreach (var data in items ?? new List<string>())
                        binder.OnClipItemRemoved(new RemovedEventArgs(data.DecryptBase64(DatabaseEncryptPassword)));
                }
                return true;
            }
            catch
            {
                /**
                 * User must not try to remove data manually from database, this may cause inconsistent of data which
                 * will result failure in delivering proper results.
                 * 
                 * The best solution in such case would be to upload the current state of user.
                 */
                if (firebaseInvoked)
                {
                    PushUser();
                }else
                {
                    ResetUser();
                }
                return false;
            }
        }

        /// <summary>
        /// This must be called whenever client is changed.
        /// </summary>
        private async Task CreateNewClient()
        {
            Log();
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
            if (client != null) client.Dispose();
            client = new FirebaseClient(config);

            await SetGlobalUserTask(true).ConfigureAwait(false);

            // BindUI is already set, make sure to set callback to it.
            SetUserCallback();

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
            Log();
            isClientInitialized = false;

            UID = UniqueID;
            if (data == null)
            {
                Log("Configuration is null");
                return;
            }
            else FirebaseCurrent = data;

            /* Load the previous state of user or if user is not null it means some configuration
            *  has changed and we should delete the previous state file to avoid any further errors.
            */
            if (user == null)
                LoadUserState();
            else
                File.Delete(UserStateFile);

            // Clearing stacks...
            ClearAllStack();

            if (FirebaseCurrent != null)
            {
                CreateCurrentQRData(); // Create QR data for settings window.
                if (FirebaseCurrent.isAuthNeeded)
                {
                    if (!IsValidCredential())
                    {
                        Log("Token not valid");
                        binder.OnNeedToGenerateToken(FirebaseCurrent.DesktopAuth.ClientId, FirebaseCurrent.DesktopAuth.ClientSecret);
                        return;
                    }
                    else if (NeedToRefreshToken())
                    {
                        Log("We need to refresh token");
                        CheckForAccessTokenValidity(); // PS: I don't care.
                        return;
                    }
                }
                CreateNewClient();
            }
            else
                MsgBoxHelper.ShowError(Translation.MSG_FIREBASE_UNKNOWN_ERR);
        }

        public void UpdateConfigurations()
        {
            Log();
            if (user != null)
                SetCommonUserInfo(user);
            else Log("Oops, user is still null");
        }

        // TODO: Code is of no use
        /// <summary>
        /// This will submit configuration change to database.
        /// </summary>
        /// <returns></returns>
        public async Task SubmitConfigurationsTask()
        {
            Log();
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
            Log();
            if (isGlobalUserExecuting)
            {
                globalUserStack.Add(new object());
                Log($"Added to global stack: {globalUserStack.Count}");
                return false;
            }
            isGlobalUserExecuting = true;

            // Return if database observing is disabled.
            if (!BindDatabase)
            {
                //clearAwaitedGlobalUserTask();
                return false;
            }
            if (FirebaseCurrent == null) return false;
            if (client == null)
            {
                MsgBoxHelper.ShowError(Translation.MSG_FIREBASE_CLIENT_ERR);
                //clearAwaitedGlobalUserTask();

                // todo: Do something when client isn't initialized (I guess this check would be of use).
                /* 
                 * We can implement a call stack to this, all you need to do is to make
                 * a stack that accepts data & operation name. Once this client is
                 * initialized we can do our job.
                 */
                return false;
            }

            if (await CheckForAccessTokenValidity().ConfigureAwait(false) && (alwaysForceInvoke || user == null || forceInvoke))
            {
                Log("Check complete");
                var firebaseUser = await FetchUser().ConfigureAwait(false);

                if (firebaseUser != null)
                {
                    await SetCommonUserInfo(firebaseUser).ConfigureAwait(false);

                    bool allChecksPassed = CheckForDataRemoval(firebaseUser);

                    Log("Check Passed: " + allChecksPassed);

                    if (allChecksPassed)
                    {
                        if (firebaseUser.Devices != null && firebaseUser.Devices.Count > 0)
                            alwaysForceInvoke = true;
                        user = firebaseUser;
                    }
                }
                else
                {
                    globalUserStack.Clear();
                    return false;
                }
            }

            await clearAwaitedGlobalUserTask().ConfigureAwait(false);

            Log("Execution complete");

            return user != null;
        }

        private async Task clearAwaitedGlobalUserTask()
        {
            Log();
            isGlobalUserExecuting = false;
            if (globalUserStack.Count > 0)
            {
                Log("You need to clear the task");
                globalUserStack.Clear();
                await SetGlobalUserTask().ConfigureAwait(false);
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
        private async void SetUserCallback()
        {
            Log();
            if (isBinded) return;
            try
            {
                await client.OnAsync($"users/{UID}", added: (o, a, c) =>
                {
                    if (BindDatabase)
                        binder.OnDataAdded(a);
                }, changed: (o, a, c) =>
                {
                    if (BindDatabase)
                    {
                        //changedClipList.Add(a.)
                        binder.OnDataChanged(a);
                    }
                }, removed: (o, a, c) =>
                {
                    if (BindDatabase)
                        binder.OnDataRemoved(a);
                            
                }).ConfigureAwait(false);

                isBinded = true;
            }
            catch (Exception ex)
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
        private List<string> changedClipList = new List<string>();

        #endregion

        #region User Handling Method

        /// <summary>
        /// Checks if the user exist in the nodes or not.
        /// </summary>
        /// <returns></returns>
        private async Task<bool> IsUserExist()
        {
            Log();
            var response = await client.SafeGetAsync($"users/{UID}").ConfigureAwait(false);
            return response != null && response.Body != "null";
        }

        /// <summary>
        /// Add an empty user to the node.
        /// </summary>
        /// <returns></returns>
        public async Task<User> RegisterUser()
        {
            Log();
            if (!BindDatabase) return new User();
            var exist = await IsUserExist().TimeoutAfter(TimeSpan.FromSeconds(30)).ConfigureAwait(false);
            if (!exist)
            {
                Log("Registering data for first time");
                var user = new User();
                SetCommonUserInfo(user);
                this.user = user;
                await PushUser().ConfigureAwait(false);
            }
            return user;
        }

        /// <summary>
        /// Removes all data associated with the UID.
        /// </summary>
        /// <returns></returns>
        public async Task ResetUser()
        {
            Log();
            await client.SafeDeleteAsync($"users/{UID}").ConfigureAwait(false);
            await RegisterUser().ConfigureAwait(false);
        }

        /// <summary>
        /// This will push the current state of the user to the database
        /// </summary>
        /// <returns></returns>
        public async Task PushUser()
        {
            Log("Is User null: " + (user != null));
            if (user != null)
                await client.SafeSetAsync($"users/{UID}", user).ConfigureAwait(false);
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
            Log();
            if (!BindDatabase) return new List<Device>();

            if (await SetGlobalUserTask(true).ConfigureAwait(false))
                return user.Devices;

            return new List<Device>();
        }

        public async Task<List<Device>> RemoveDevice(string DeviceId)
        {
            Log($"Device Id: {DeviceId}");
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
            if (Text == null) return;
            Log();
            // If some add operation is going, we will add it to stack.
            if (isPreviousAddRemaining)
            {
                addStack.Add(Text);
                Log($"Adding to addStack: {addStack.Count}");
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

                Log("Completed");
            }
            isPreviousAddRemaining = false;
        }

        [Obsolete("Currently of no use")] // todo: Remove if not needed.
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
        /// Removes the clip data of user. Synchronization is possible.
        /// </summary>
        /// <param name="Text"></param>
        /// <returns></returns>
        public async Task RemoveClip(string? Text)
        {
            if (Text == null) return;
            Log();
            // If some remove operation is going, we will add it to stack.
            if (isPreviousRemoveRemaining)
            {
                removeStack.Add(Text);
                Log($"Adding to removeStack: {removeStack.Count}");
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

                Log("Completed");
            }
            isPreviousRemoveRemaining = false;
        }

        /// <summary>
        /// Removes list of Clip item that matches input list of string items.
        /// </summary>
        /// <param name="items"></param>
        /// <returns></returns>
        public async Task RemoveClip(List<string> items)
        {
            Log();

            if (await SetGlobalUserTask().ConfigureAwait(false))
            {
                if (items.IsEmpty()) return;
                if (user.Clips == null) return;

                var originalCount = items.Count;

                foreach (var item in items)
                    user.Clips.RemoveAll(c => c.data.DecryptBase64(DatabaseEncryptPassword) == item);

                if (originalCount != user.Clips.Count)
                    await client.SafeUpdateAsync($"users/{UID}", user).ConfigureAwait(false);

                Log("Completed");
            }
        }


        /// <summary>
        /// Remove all clip data of user.
        /// </summary>
        /// <returns></returns>
        public async Task RemoveAllClip()
        {
            Log();
            if (await SetGlobalUserTask().ConfigureAwait(false))
            {
                if (user.Clips == null)
                    return;
                user.Clips.Clear();
                await client.SafeUpdateAsync($"users/{UID}", user).ConfigureAwait(false);
            }
        }

        /// <summary>
        /// Updates an existing data with the new data. Both this data should not be in
        /// any encrypted format.
        /// </summary>
        /// <param name="oldUnencryptedData"></param>
        /// <param name="newUnencryptedData"></param>
        /// <returns></returns>
        public async Task UpdateData(string oldUnencryptedData, string newUnencryptedData)
        {
            Log();
            // Adding new data to stack to save network calls.
            if (isPreviousUpdateRemaining)
            {
                updateStack.Add(oldUnencryptedData, newUnencryptedData);
                Log($"Adding to updateStack: {updateStack.Count}");
                return;
            }
            isPreviousUpdateRemaining = true;

            if (await SetGlobalUserTask().ConfigureAwait(false))
            {
                if (user.Clips == null)
                    return;

                // Add current item to existing stack.
                updateStack.Add(oldUnencryptedData, newUnencryptedData);
                foreach (var clip in user.Clips)
                {
                    var decryptedData = clip.data.DecryptBase64(DatabaseEncryptPassword);
                    var item = updateStack.FirstOrDefault(c => c.Key == decryptedData);
                    if (item.Key != null && item.Value != null)
                    {
                        clip.data = item.Value.EncryptBase64(DatabaseEncryptPassword);
                    }
                }

                updateStack.Clear();

                await client.SafeUpdateAsync($"users/{UID}", user).ConfigureAwait(false);

                Log("Completed");
            }

            isPreviousUpdateRemaining = false;
        }

        /// <summary>
        /// Add image related data to firebase, well not whole image but it's uploaded on
        /// Firebase Storage & then the url is shared in the database.
        /// </summary>
        /// <param name="imagePath"></param>
        /// <returns></returns>
        public async Task AddImage(string? imagePath)
        {
            if (imagePath == null) return;
            Log();
            if (FirebaseCurrent?.Storage == null) return;

            var stream = File.Open(imagePath, FileMode.Open);
            var fileName = Path.GetFileName(imagePath);

            var pathRef = new FirebaseStorage(FirebaseCurrent.Storage)
               .Child("XClipper")
               .Child("images")
               .Child(fileName);

            await pathRef.PutAsync(stream); // Push to storage

            binder.SendNotification(Translation.MSG_IMAGE_UPLOAD_TITLE, Translation.MSG_IMAGE_UPLOAD_TEXT);

            stream.Close();
            var downloadUrl = await pathRef.GetDownloadUrlAsync().ConfigureAwait(false); // Retrieve download url

            AddClip($"![{fileName}]({downloadUrl})");
        }

        /// <summary>
        /// Removes an image from Firebase Storage as well as routes to call remove clip method.
        /// </summary>
        /// <param name="fileName"></param>
        /// <returns></returns>
        public async Task RemoveImage(string fileName, bool onlyFromStorage = false)
        {
            Log();
            if (FirebaseCurrent?.Storage == null) return;

            var pathRef = new FirebaseStorage(FirebaseCurrent.Storage)
                .Child("XClipper")
                .Child("images")
                .Child(fileName);

            try
            {
                var downloadUrl = await pathRef.GetDownloadUrlAsync().ConfigureAwait(false);
                await new FirebaseStorage(FirebaseCurrent.Storage)
                .Child("XClipper")
                .Child("images")
                .Child(fileName)
                .DeleteAsync().ConfigureAwait(false);

                if (!onlyFromStorage)
                    RemoveClip($"![{fileName}]({downloadUrl})"); // PS I don't care what happens next!
            }
            finally
            { }
        }

        /// <summary>
        /// This will remove list of images from storage & route to remove it from firebase database.
        /// </summary>
        /// <param name="fileNames"></param>
        /// <returns></returns>
        public async Task RemoveImageList(List<string> fileNames)
        {
            Log();
            if (FirebaseCurrent?.Storage == null) return;

            foreach (var fileName in fileNames)
            {
                await RemoveImage(fileName).ConfigureAwait(false);
            }
        }

        #endregion

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

        public void Dispose()
        {
            client.Dispose();
        }
    }
}
