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
using System.Linq;
using Firebase.Storage;
using System.Drawing;
using System.Windows.Documents;
using System.Data;
using System.Diagnostics;
using System.Text.RegularExpressions;
using RestSharp;

#nullable enable

/**
 * A class which needs to be made for safe handling of Firebase data as
 * original V1 needs a complete refactoring.
 */
namespace Components
{
    public sealed class FirebaseSingletonV2 : IDisposable
    {

        #region Variable declarations

        private bool isPreviousAddRemaining, isPreviousRemoveRemaining, isPreviousUpdateRemaining = false;

        private readonly List<string> addStack = new List<string>();
        private readonly List<string> removeStack = new List<string>();
        private readonly Dictionary<string, string> updateStack = new Dictionary<string, string>();
        private List<FirebaseInvoke> firebaseInvokeStack = new List<FirebaseInvoke>();

        private const string USER_REF = "users";
        private const string CLIP_REF = "Clips";
        private const string DEVICE_REF = "Devices";

        private string UID;

        private User user;

        private IFirebaseClient client;
        private IFirebaseBinderV2? binder;

        #endregion

        #region Observables 

        private bool _isClientInitialized = false;
        private bool isClientInitialized
        {
            get { return _isClientInitialized; }
            set
            {
                _isClientInitialized = value;
                OnClientInitialized();
            }
        }

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

            CreateNewClient();
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

        #region Public methods

        /// <summary>
        /// This is mostly called when license is validated.
        /// </summary>
        public void UpdateConfigurations()
        {
            Log();
            if (user != null)
                SetCommonUserInfo(user);
            else Log("Oops, user is still null");
        }

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
            Debug.WriteLine($"FirebaseSingletonV2: {message}");
            LogHelper.Log(nameof(FirebaseSingletonV2), message);
        }

        /// <summary>
        /// If the code checks are true stop the execution.
        /// </summary>
        private bool AssertUnifiedChecks(FirebaseInvoke invoke, object? data = null)
        {
            if (!isClientInitialized)
            {
                Log($"Asserting: {invoke}");
                // Some invokes are not supported yet.
                switch (invoke)
                {
                    case FirebaseInvoke.ADD_CLIP:
                        addStack.Add((string)data);
                        break;
                    case FirebaseInvoke.REMOVE_CLIP:
                        removeStack.Add((string)data);
                        break;
                    case FirebaseInvoke.UPDATE_CLIP:
                        var pair = (KeyValuePair<string, string>)data;
                        updateStack.Add(pair.Key, pair.Value);
                        break;
                    case FirebaseInvoke.RESET:
                        firebaseInvokeStack.Add(FirebaseInvoke.RESET);
                        firebaseInvokeStack = firebaseInvokeStack.Distinct().ToList();
                        break;
                    case FirebaseInvoke.REMOVE_CLIP_ALL:
                        firebaseInvokeStack.Add(FirebaseInvoke.REMOVE_CLIP_ALL);
                        firebaseInvokeStack = firebaseInvokeStack.Distinct().ToList();
                        break;
                    default:
                        binder?.SendNotification(Translation.SYNC_ERROR_TITLE, Translation.MSG_FIREBASE_CLIENT_ERR);
                        break;
                }
            }
            return !BindDatabase || !isClientInitialized;
        }

        private void ClearAllStack()
        {
            addStack.Clear(); isPreviousAddRemaining = false;
            removeStack.Clear(); isPreviousRemoveRemaining = false;
            updateStack.Clear(); isPreviousUpdateRemaining = false;
            firebaseInvokeStack.Clear();
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
            Log();
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
            isClientInitialized = false;

            // Set user for first time..
            if (user == null) user = await FetchUser().ConfigureAwait(false);
            if (user == null) await RegisterUser().ConfigureAwait(false);

            // Apply an auto-fix if needed
            await FixInconsistentData().ConfigureAwait(false); 

            // todo: Methods to check if data is updated...

            Log();
            try
            {
                await client.OnAsync($"users/{UID}",
                    onDataChange: async (o, a, c) =>
                {
                    Log();
                    if (!BindDatabase) return;

                    User? firebaseUser;

                    try
                    {
                        firebaseUser = JsonConvert.DeserializeObject<User>(a.Data);
                    } catch (Exception e)
                    {
                        firebaseUser = null;
                    }

                    // If first device or clip is added it will come here...
                    if (user != null)
                    {
                        if (a.Path == PATH_DEVICES)
                        {
                            var devices = JsonConvert.DeserializeObject<List<Device>>(a.Data);
                            firebaseUser = user.DeepCopy();
                            firebaseUser.Devices = devices;
                        }
                        else if (a.Path == PATH_CLIPS)
                        {
                            var clips = JsonConvert.DeserializeObject<List<Clip>>(a.Data);
                            firebaseUser = user.DeepCopy();
                            firebaseUser.Clips = clips;
                        }
                        else if (string.IsNullOrWhiteSpace(a.Data) && !string.IsNullOrWhiteSpace(a.Path)) // Detecting Remove changes & apply to shallow variable...
                        {
                            // Deep copying variable to firebase user...
                            firebaseUser = user.DeepCopy();

                            // Remove from /Clips
                            if (Regex.IsMatch(a.Path, PATH_CLIP_REGEX_PATTERN))
                            {
                                var index = Regex.Match(a.Path, PATH_CLIP_REGEX_PATTERN).Groups[1].Value.ToInt();
                                if (index + 1 < firebaseUser.Clips?.Count)
                                {
                                    await FixInconsistentData().ConfigureAwait(false);
                                    return;
                                }
                                else
                                    firebaseUser.Clips?.RemoveAt(index);
                            }

                            // Remove from /Devices
                            if (Regex.IsMatch(a.Path, PATH_DEVICE_REGEX_PATTERN))
                            {
                                var index = Regex.Match(a.Path, PATH_DEVICE_REGEX_PATTERN).Groups[1].Value.ToInt();
                                if (index + 1 < firebaseUser.Clips?.Count)
                                {
                                    await FixInconsistentData().ConfigureAwait(false);
                                    return;
                                }else
                                    firebaseUser.Clips?.RemoveAt(index);
                            }
                        }
                    }

                    // If there is no new data then it's of no use...
                    if (firebaseUser == null) return;

                    // Always update the license details of new firebase user...
                    await SetCommonUserInfo(firebaseUser).ConfigureAwait(false);

                    // Perform data addition & removal operation...
                    if (user != null)
                    {
                        // Check for clip data addition...
                        var newClips = firebaseUser?.Clips?.Select(c => c?.data).ToNotNullList();
                        var oldClips = user?.Clips?.Select(c => c?.data).ToNotNullList();
                        var addedClips = newClips.Except(oldClips).ToList();
                        var removedClips = oldClips.Except(newClips).ToList();

                        // Check if clip is updated using following hack
                        if ((addedClips.Count & removedClips.Count) == 1)
                            binder?.OnClipItemUpdated(
                                previousUnEncryptedData: removedClips.FirstOrDefault().DecryptBase64(DatabaseEncryptPassword),
                                newUnEncryptedData: addedClips.FirstOrDefault().DecryptBase64(DatabaseEncryptPassword)
                            );
                        else if (addedClips.Count > 0) // On clip updated
                            binder?.OnClipItemAdded(addedClips.Select(c => c.DecryptBase64(DatabaseEncryptPassword)).ToList());
                        else if (removedClips.Count > 0) // On clip removed
                            binder?.OnClipItemRemoved(removedClips.Select(c => c.DecryptBase64(DatabaseEncryptPassword)).ToList());

                        // Check for device addition & removal...
                        var newDevices = firebaseUser?.Devices ?? new List<Device>();
                        var oldDevices = user?.Devices ?? new List<Device>();
                        foreach (var device in newDevices.ExceptEquals(oldDevices))
                        {
                            binder?.OnDeviceAdded(device);
                        }
                        foreach (var device in oldDevices.ExceptEquals(newDevices))
                        {
                            binder?.OnDeviceRemoved(device);
                        }

                        user = firebaseUser!;
                    }

                    user = firebaseUser!;

                    if (!await RunCommonTask().ConfigureAwait(false)) return;

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

            isClientInitialized = true;
        }

        /// <summary>
        /// This will fix the inconsistent data by pushing previous data.
        /// </summary>
        /// <returns></returns>
        private async Task FixInconsistentData()
        {
            if (user != null)
            {
                user.Clips = user.Clips?.Where(c => c != null).ToList();
                user.Devices = user.Devices?.Where(c => c != null).ToList();
                await PushUser().ConfigureAwait(false);
            }
            else
                await RegisterUser().ConfigureAwait(false);
        }

        /// <summary>
        /// This will push the current state of the user to the database
        /// </summary>
        /// <returns></returns>
        private async Task PushUser()
        {
            Log("Is User null: " + (user != null));
            if (user != null)
                await client.SafeUpdateAsync($"{USER_REF}/{UID}", user).ConfigureAwait(false);
        }

        /// <summary>
        /// <inheritdoc cref="PushUser()"/>
        /// </summary>
        /// <returns></returns>
        private async Task PushUser(User user)
        {
            Log("Is User null: " + (user != null));
            if (user != null)
                await client.SafeUpdateAsync($"{USER_REF}/{UID}", user).ConfigureAwait(false);
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
            if (data != null && data.Body == "null") return null; // A safety check to make sure user is null.
            return data.ResultAs<User>();
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

        /// <summary>
        /// This will run some common configuration if needed so. If returned "True"
        /// then you should continue next consecutive operation otherwise stop.
        /// </summary>
        /// <returns></returns>
        private async Task<bool> RunCommonTask()
        {
            return await CheckForAccessTokenValidity().ConfigureAwait(false);
        }

        /// <summary>
        /// This will merge user according to the data of user1
        /// </summary>
        /// <param name="user1"></param>
        /// <param name="user2"></param>
        private void MergeUser(User user1, User user2)
        {
            if (user1.Devices == null)
                user1.Devices = user2.Devices;
            else
            {
                user1.Devices.AddRange(user2.Devices ?? new List<Device>());
                user1.Devices = user1.Devices.DistinctBy(c => c.id).ToList();
            }

            if (user1.Clips == null)
                user1.Clips = user2.Clips;
            else
            {
                user1.Clips.AddRange(user2.Clips ?? new List<Clip>());
                user1.Clips = user1.Clips.DistinctBy(c => c.data).ToList();
            }
        }

        /// <summary>
        /// This will trigger when our Firebase client is initialized.
        /// </summary>
        private async void OnClientInitialized()
        {
            Log();
            if (addStack.Count > 0) await AddClip(addStack.Pop()).ConfigureAwait(false);
            if (removeStack.Count > 0) await RemoveClip(removeStack.Pop()).ConfigureAwait(false);
            if (updateStack.Count > 0)
            {
                var pair = updateStack.Pop();
                await UpdateData(pair.Key, pair.Value).ConfigureAwait(false);
            }
            foreach(var item in firebaseInvokeStack)
            {
                switch(item)
                {
                    case FirebaseInvoke.RESET: await ResetUser().ConfigureAwait(false); break;
                    case FirebaseInvoke.REMOVE_CLIP_ALL: await RemoveAllClip().ConfigureAwait(false); break;
                }
            }
        }

        #endregion

        #region User handling methods

        /// <summary>
        /// Removes all data associated with the UID.
        /// </summary>
        /// <returns></returns>
        public async Task ResetUser()
        {
            Log();
            if (AssertUnifiedChecks(FirebaseInvoke.RESET)) return;
            await client.SafeDeleteAsync($"users/{UID}").ConfigureAwait(false);
            await RegisterUser().ConfigureAwait(false);
        }

        /// <summary>
        /// This will provide the list of devices associated with the UID.
        /// </summary>
        /// <returns></returns>
        public async Task<List<Device>?> GetDeviceListAsync()
        {
            Log();
            if (AssertUnifiedChecks(FirebaseInvoke.LIST_DEVICES)) return new List<Device>();

            if (!await RunCommonTask().ConfigureAwait(false)) return new List<Device>();

            if (user != null) return user.Devices; else return new List<Device>();
        }

        /// <summary>
        /// Removes a device from database.
        /// </summary>
        /// <param name="DeviceId"></param>
        /// <returns></returns>
        public async Task<List<Device>> RemoveDevice(string DeviceId)
        {
            Log($"Device Id: {DeviceId}");
            if (AssertUnifiedChecks(FirebaseInvoke.REMOVE_DEVICE)) return new List<Device>();

            if (await RunCommonTask().ConfigureAwait(false))
            {
                user.Devices = user.Devices.Where(d => d.id != DeviceId).ToList();
                await PushUser().ConfigureAwait(false);
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
            if (Text == null || AssertUnifiedChecks(FirebaseInvoke.ADD_CLIP, Text)) return;
            Log();
            // If some add operation is going, we will add it to stack.
            if (isPreviousAddRemaining)
            {
                addStack.Add(Text);
                Log($"Adding to addStack: {addStack.Count}");
                return;
            }
            isPreviousAddRemaining = true;
            if (await RunCommonTask().ConfigureAwait(false))
            {
                if (Text == null) return;
                if (Text.Length > DatabaseMaxItemLength) return;

                List<Clip> clips = user.Clips == null ? new List<Clip>() : new List<Clip>(user.Clips);
                // Remove clip if greater than item
                if (clips.Count > DatabaseMaxItem)
                    clips.RemoveAt(0);

                // Add data from current [Text]
                clips.Add(new Clip { data = Text.EncryptBase64(DatabaseEncryptPassword), time = DateTime.Now.ToFormattedDateTime(false) });

                // Also add data from stack
                foreach (var stackText in addStack)
                    clips.Add(new Clip { data = stackText.EncryptBase64(DatabaseEncryptPassword), time = DateTime.Now.ToFormattedDateTime(false) });

                // Clear the stack after adding them all.
                addStack.Clear();

                await client.SafeSetAsync($"{USER_REF}/{UID}/{CLIP_REF}", clips).ConfigureAwait(false);
                //await PushUser().ConfigureAwait(false);

                Log("Completed");
            }
            isPreviousAddRemaining = false;
        }

        /// <summary>
        /// Removes the clip data of user. Synchronization is possible.
        /// </summary>
        /// <param name="Text"></param>
        /// <returns></returns>
        public async Task RemoveClip(string? Text)
        {
            if (Text == null || AssertUnifiedChecks(FirebaseInvoke.REMOVE_CLIP, Text)) return;
            Log();
            // If some remove operation is going, we will add it to stack.
            if (isPreviousRemoveRemaining)
            {
                removeStack.Add(Text);
                Log($"Adding to removeStack: {removeStack.Count}");
                return;
            }

            isPreviousRemoveRemaining = true;

            if (await RunCommonTask().ConfigureAwait(false))
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
            if (AssertUnifiedChecks(FirebaseInvoke.NONE)) return;
            if (await RunCommonTask().ConfigureAwait(false))
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
            if (AssertUnifiedChecks(FirebaseInvoke.REMOVE_CLIP_ALL))
            if (await RunCommonTask().ConfigureAwait(false))
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
            if (AssertUnifiedChecks(FirebaseInvoke.UPDATE_CLIP, new KeyValuePair<string, string>(oldUnencryptedData, newUnencryptedData))) return;

            // Adding new data to stack to save network calls.
            if (isPreviousUpdateRemaining)
            {
                updateStack.Add(oldUnencryptedData, newUnencryptedData);
                Log($"Adding to updateStack: {updateStack.Count}");
                return;
            }
            isPreviousUpdateRemaining = true;

            if (await RunCommonTask().ConfigureAwait(false))
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
            if (AssertUnifiedChecks(FirebaseInvoke.ADD_IMAGE_CLIP, imagePath)) return;
            Log();
            if (FirebaseCurrent?.Storage == null) return;

            var stream = File.Open(imagePath, FileMode.Open);
            var fileName = Path.GetFileName(imagePath);

            var pathRef = new FirebaseStorage(FirebaseCurrent.Storage)
               .Child("XClipper")
               .Child("images")
               .Child(fileName);

            await pathRef.PutAsync(stream); // Push to storage

            binder?.SendNotification(Translation.MSG_IMAGE_UPLOAD_TITLE, Translation.MSG_IMAGE_UPLOAD_TEXT);

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
            if (AssertUnifiedChecks(FirebaseInvoke.REMOVE_IMAGE_CLIP, fileName)) return;
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
            if (AssertUnifiedChecks(FirebaseInvoke.NONE)) return;
            Log();
            if (FirebaseCurrent?.Storage == null) return;

            foreach (var fileName in fileNames)
            {
                await RemoveImage(fileName).ConfigureAwait(false);
            }
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

        public void Dispose()
        {
            client.Dispose();
        }
    }
}
