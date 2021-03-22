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
using System.Linq;
using Firebase.Storage;
using System.Data;
using System.Diagnostics;
using System.Text.RegularExpressions;
using System.Windows.Threading;
using FireSharp.Core.Response;

#nullable enable

/**
 * A class which needs to be made for safe handling of Firebase data as
 * original V1 needs a complete refactoring.
 * 
 * Why this class exist?
 * Currently Google doesn't provide any APIs for .Net to work with firebase,
 * hence there is need to find some third party tools like FireSharp which 
 * works but there are issues with authentication & also it uses old 
 * deprecated database secret key instead of access token.
 * 
 * Hence I need to built a complete solution by refactoring the abandoned FireSharp
 * project to make it work with my database, trust me it was not easy. The current 
 * public APIs provided by the library doesn't fit the case many of them won't invoke
 * change events at appropriate time, so I updated the lib to make it work my case but
 * even then it doesn't handle network change, token saving or slow initialization 
 * (mainly due to refreshing existing token).
 * 
 * That's why instead of modeling the API for my use case I made my first-class solution
 * which will handle all of these, also provides some routes to communicate with my app.
 * 
 * Currently the class does the following thing.
 * 1. Detect appropriate changes & fires onChange event which provides "path" & "data"
 *    which are affected. Using my <see cref="Components.FirebaseParser"/> (which is basically a diff
 *    util) determines the changes associated with the data.
 * 2. Handles saved instance. If application is stopped, a current snapshot of data is saved
 *    locally which is retrieved when the app is started again (for diff).
 * 3. Detects data addition, deletion, updation (using a quirky hack) & dispatches this information
 *    to main thread to perform further actions.
 * 4. Automatically handles OAuth & token refreshing, in case any error it will post the error to
 *    main threaded application class.
 * 5. Supports queuing, if for some reasons client takes some time to initialize & during this 
 *    user forwarded some firebase actions, those will be added to queue & will be executed once
 *    initialized.
 * 6. Handle firebase errors like permission denied for R/W. Also capable of handling other
 *    certain errors. It try to resolve it itself, in case if it fails then it post the error
 *    to main threaded application class for further handling.
 * 7. Provides a helper class <see cref="Components.FirebaseHelper"/> for making safe update, push
 *    calls to firebase. It is mostly a wrapper around this class (it most cases this class is 
 *    responsible for detecting authorization error, network change error).
 *    
 */
namespace Components
{
    public sealed class FirebaseSingletonV2 : IDisposable
    {

        #region Variable declarations

        private bool isPreviousAddRemaining, isPreviousRemoveRemaining, isPreviousUpdateRemaining = false;
        private bool _isDeinitialized = false, invokeStackAfterClientInitialization = false;
        public bool IsDeinitialized { get => _isDeinitialized; }

        private readonly List<string> addStack = new List<string>();
        private readonly List<string> removeStack = new List<string>();
        private readonly Dictionary<string, string> updateStack = new Dictionary<string, string>();
        private List<FirebaseInvoke> firebaseInvokeStack = new List<FirebaseInvoke>();

        private EventStreamResponse? onAsyncStream = null;

        private const string USER_REF = "users";
        private const string CLIP_REF = "Clips";
        private const string DEVICE_REF = "Devices";

        private DispatcherTimer dTimer = new DispatcherTimer
        {
            Interval = TimeSpan.FromSeconds(10)
        };

        private string UID;

        private User user;

        private IFirebaseClient client;
        private IFirebaseBinderV2? binder;

        private FirebaseParser fparser;

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
        {
            dTimer.Tick += SurpassEvent;
        }

        #endregion

        #region Configuration methods

        public void Initialize()
        {
            UID = UniqueID;

            _isDeinitialized = false;
            fparser = new FirebaseParser();

            if (onAsyncStream != null) onAsyncStream.Cancel();

            if (FirebaseCurrent == null) return;

            if (!invokeStackAfterClientInitialization)
                ClearAllStack();

            DefaultSettings.ValidateFirebaseSetting();

            if (FirebaseCurrent.IsAuthNeeded)
            {
                if (!IsValidCredential())
                {
                    Log("Token not valid");
                    binder?.OnNeedToGenerateToken(DesktopAuth.ClientId, DesktopAuth.ClientSecret);
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

        public void Deinitialize()
        {
            _isDeinitialized = true;
            onAsyncStream?.Cancel();
            client = null;
            isClientInitialized = false;
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
        /// This can be used to migrate clip data if <see cref="FirebaseData.IsEncrypted"/> setting has changed.
        /// </summary>
        public async Task MigrateClipData(MigrateAction action, Action? onSuccess = null, Action? onError = null)
        {
            Log();
            if (user == null || user.Clips == null)
            {
                Log("Migration failed: User is null");
                if (onError != null)
                    Dispatcher.CurrentDispatcher.Invoke(onError);
                return;
            }

            var isDataAlreadyEncrypted = FirebaseCurrent.IsEncrypted;
            if (user.Clips.Count > 0)
            {
                isDataAlreadyEncrypted = user.Clips.FirstOrDefault().data.IsBase64Encrypted(DatabaseEncryptPassword);
            }

            var clips = user.Clips.Select(s =>
               new Clip
               {
                   time = s.time,
                   data = action == MigrateAction.Encrypt ?
                            isDataAlreadyEncrypted ? s.data : Core.EncryptBase64(s.data, DatabaseEncryptPassword)
                          : 
                            !isDataAlreadyEncrypted ? s.data : Core.DecryptBase64(s.data, DatabaseEncryptPassword)
               }
          ).ToList();

            user.Clips = clips;
            user.Devices = null;

            await PushUser().ConfigureAwait(false);

            Log("Completed Migration");

            if (onSuccess != null)
                Dispatcher.CurrentDispatcher.Invoke(onSuccess);
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
            if (!isClientInitialized && BindDatabase)
            {
                if (!isClientInitialized) invokeStackAfterClientInitialization = true;

                dTimer.Start();
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
            Log($"Checking for token : {FirebaseCurrent?.IsAuthNeeded}");
            if (FirebaseCurrent?.IsAuthNeeded == false) return true;

            if (!IsValidCredential())
            {
                Log("Credentials are not valid");
                if (FirebaseCurrent != null)
                    Dispatcher.CurrentDispatcher.Invoke(() =>
                        binder?.OnNeedToGenerateToken(DesktopAuth.ClientId, DesktopAuth.ClientSecret)
                    );
                else
                    Dispatcher.CurrentDispatcher.Invoke(() =>
                        MsgBoxHelper.ShowError(Translation.MSG_FIREBASE_USER_ERROR)
                    );
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

        /// <summary>
        /// If state is restored then we should find which clips are added & removed
        /// along with some other tasks to make local database in sync with remote.
        /// </summary>
        /// <returns></returns>
        private async Task StatePersistenceTask()
        {
            if (user == null && LoadUserState())
            {
                var currentUser = await FetchUser().ConfigureAwait(false);
                if (user != null && currentUser != null)
                {
                    DiffUserClips(user, currentUser);
                    await SetCommonUserInfo(user).ConfigureAwait(false);
                }
            }
            else
                File.Delete(UserStateFile);
        }

        private void CreateNewClient()
        {
            Log();
            IFirebaseConfig config;
            if (FirebaseCurrent?.IsAuthNeeded == true)
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

            /* Load the previous state of user or if user is not null it means some configuration
             *  has changed and we should delete the previous state file to avoid any further errors.
             */
            await StatePersistenceTask().ConfigureAwait(false);

            // Set user for first time..
            if (user == null) user = await FetchUser().ConfigureAwait(false);
            if (user == null) await RegisterUser().ConfigureAwait(false); else await SetCommonUserInfo(user).ConfigureAwait(false);
           
            // Apply an auto-fixes if needed
            await FixInconsistentData().ConfigureAwait(false);
            await FixEncryptedDatabase().ConfigureAwait(false);

            if (user != null) fparser.SetUser(user);
            if (user?.Clips != null) binder?.OnClipItemAdded(user.Clips.Select(c => c.data).ToList());
            
            Log();
            try
            {
                onAsyncStream = await client.OnAsync($"users/{UID}",
                    onDataChange: async (o, a, c) =>
                {
                    Log();
                    if (!BindDatabase) return;

                    User? firebaseUser;

                    try
                    {
                        firebaseUser = fparser.Parse(a.Event, a.Path, a.Data);
                        //firebaseUser = JsonConvert.DeserializeObject<User>(a.Data);
                    }
                    catch (Exception)
                    {
                        firebaseUser = null;
                    }

                    // If first device or clip is added it will come here...
                    if (user != null)
                    {
                        // Handles path like /Clips/2 or /Devices/1 only if the data is empty it means they
                        // have been removed.
                        if (string.IsNullOrWhiteSpace(a.Data) && !string.IsNullOrWhiteSpace(a.Path))
                        {
                            // Deep copying variable to firebase user...
                            if (firebaseUser == null)
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

                    // Perform data addition & removal operation...
                    if (user != null)
                    {
                        DiffUserClips(user, firebaseUser);
                    }

                    user = firebaseUser!;

                    // Always update the license details of new user...
                    await SetCommonUserInfo(user).ConfigureAwait(false);

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
        /// A common function to perform diffs on user's clips & invoke necessary calls.
        /// </summary>
        /// <param name="user"></param>
        /// <param name="firebaseUser"></param>
        private void DiffUserClips(User user, User firebaseUser)
        {
            var newClips = firebaseUser?.Clips?.Select(c => c?.data).ToList() ?? new List<string?>();
            var oldClips = user?.Clips?.Select(c => c?.data).ToList() ?? new List<string?>();
            var addedClips = newClips.Except(oldClips).ToList();
            var removedClips = oldClips.Except(newClips).ToList();

            // Check if clip is updated using following hack
            if ((addedClips.Count & removedClips.Count) == 1)
                binder?.OnClipItemUpdated(
                    previousUnEncryptedData: removedClips.FirstOrDefault().DecryptBase64(DatabaseEncryptPassword),
                    newUnEncryptedData: addedClips.FirstOrDefault().DecryptBase64(DatabaseEncryptPassword)
                );
            else if (addedClips.IsNotEmpty()) // On clip updated
                binder?.OnClipItemAdded(addedClips.Select(c => c.DecryptBase64(DatabaseEncryptPassword)).ToList());
            else if (removedClips.IsNotEmpty()) // On clip removed
                binder?.OnClipItemRemoved(removedClips.Select(c => c.DecryptBase64(DatabaseEncryptPassword)).ToList());

            // Check for device addition & removal...
            var newDevices = firebaseUser?.Devices ?? new List<Device>();
            var oldDevices = user?.Devices ?? new List<Device>();
            if (newDevices.Count != oldDevices.Count) deviceChanged?.Invoke(newDevices);

            foreach (var device in newDevices.ExceptEquals(oldDevices))
            {
                binder?.OnDeviceAdded(device);
            }
            foreach (var device in oldDevices.ExceptEquals(newDevices))
            {
                binder?.OnDeviceRemoved(device);
            }

            this.user = firebaseUser!;
        }

        /// <summary>
        /// This will fix the inconsistent data by pushing previous data.
        /// </summary>
        /// <returns></returns>
        private async Task FixInconsistentData()
        {
            if (user != null)
            {
                user.Clips = user.Clips?.Where(c => c != null && c.IsValid()).ToList();
                user.Devices = user.Devices?.Where(c => c != null && c.IsValid()).ToList();
                await PushUser().ConfigureAwait(false);
            }
            else
                await RegisterUser().ConfigureAwait(false);
        }

        private async Task FixEncryptedDatabase()
        {
            if (user != null && user.Clips != null)
            {
                var isAlreadyEncrypted = user.Clips.FirstOrDefault().data.IsBase64Encrypted(DatabaseEncryptPassword);
                if (isAlreadyEncrypted != FirebaseCurrent.IsEncrypted)
                {
                    MsgBoxHelper.ShowError(Translation.SYNC_ENCRYPT_DATABASE_ERROR);
                    if (FirebaseCurrent.IsEncrypted)
                        await MigrateClipData(MigrateAction.Encrypt).ConfigureAwait(false);
                    else
                        await MigrateClipData(MigrateAction.Decrypt).ConfigureAwait(false);
                }
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
            if (data == null || data?.Body == "null") return null; // A safety check to make sure user is null.
            return data?.ResultAs<User>();
        }

        private async Task SetCommonUserInfo(User user)
        {
            Log($"User null? {user == null}");
            var originallyLicensed = user.IsLicensed;
            var originalTotalConnection = user.TotalConnection;
            var originalMaxItemStorage = user.MaxItemStorage;
            var originalLicenseStrategy = user.LicenseStrategy;

            // TODO: Set some other details for user...
            user.MaxItemStorage = DatabaseMaxItem;
            user.TotalConnection = DatabaseMaxConnection;
            user.IsLicensed = IsPurchaseDone;
            user.LicenseStrategy = LicenseStrategy;

            bool shouldPush = false;
            if (originallyLicensed != IsPurchaseDone || originalMaxItemStorage != DatabaseMaxItem || originalTotalConnection != DatabaseMaxConnection || originalLicenseStrategy != user.LicenseStrategy)
                shouldPush |= true;
            if (user.MaxItemStorage > FirebaseMaxItem)
            {
                user.MaxItemStorage = FirebaseMaxItem;
                shouldPush |= true;
            }
            if (user.TotalConnection > FirebaseMaxDevice)
            {
                user.TotalConnection = FirebaseMaxDevice;
                shouldPush |= true;
            }

            if (shouldPush)
            {
                DatabaseMaxItem = user.MaxItemStorage;
                DatabaseMaxConnection = user.TotalConnection;
                WriteFirebaseSetting();
                await PushUser().ConfigureAwait(false);
            }
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
            invokeStackAfterClientInitialization = false;
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

        bool dispatcherEventPass = false;
        private void SurpassEvent(object o, EventArgs e)
        {
            if (addStack.Count > 0 || removeStack.Count > 0 || updateStack.Count > 0 || firebaseInvokeStack.Count > 0)
            {
                if (dispatcherEventPass)
                {
                    dTimer.Stop();
                    dispatcherEventPass = false;
                    var eventsCount = addStack.Count + removeStack.Count + updateStack.Count + firebaseInvokeStack.Count;
                    binder?.SendNotification(string.Format(Translation.SYNC_TIMEOUT_ACTION_TITLE, eventsCount), Translation.SYNC_TIMEOUT_ACTION_TEXT, () => {
                        FirebaseHelper.ShowSurpassMessage();
                    });
                }
                else
                {
                    dispatcherEventPass = true;
                }
            }
            else
            {
                dispatcherEventPass = false;
                dTimer.Stop();
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
                var devices = user.Devices.Where(d => d.id != DeviceId).ToList();
                if (devices.IsEmpty())
                    await client.SafeDeleteAsync($"{USER_REF}/{UID}/{DEVICE_REF}").ConfigureAwait(false);
                else 
                    await client.SafeUpdateAsync($"{USER_REF}/{UID}/{DEVICE_REF}", devices).ConfigureAwait(false);
                return devices;
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

                if (user.Clips == null)
                {
                    // Fake push to the database
                    var userClone = user.DeepCopy();
                    userClone.Clips = clips;
                    await PushUser(userClone).ConfigureAwait(false);
                } else
                    await client.SafeSetAsync($"{USER_REF}/{UID}/{CLIP_REF}", clips).ConfigureAwait(false);

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
            if (AssertUnifiedChecks(FirebaseInvoke.REMOVE_CLIP_ALL)) return;
            if (await RunCommonTask().ConfigureAwait(false))
            {
                if (user.Clips == null)
                    return;
                user.Clips.Clear();
                await client.SafeUpdateAsync($"users/{UID}", user).ConfigureAwait(false);

                if (FirebaseCurrent?.Storage != null)
                {
                    await new FirebaseStorage(FirebaseCurrent.Storage)
                        .Child("XClipper")
                        .Child("images")
                        .DeleteAsync().ConfigureAwait(false);  
                }
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

            var fileName = Path.GetFileName(imagePath);

            var pathRef = new FirebaseStorage(FirebaseCurrent.Storage)
               .Child("XClipper")
               .Child("images")
               .Child(fileName);

            using (var stream = new FileStream(imagePath, FileMode.Open))
            {
                await pathRef.PutAsync(stream); // Push to storage
            }

            binder?.SendNotification(Translation.MSG_IMAGE_UPLOAD_TITLE, Translation.MSG_IMAGE_UPLOAD_TEXT);

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
                File.WriteAllText(UserStateFile, User.ToNode(user, FirebaseCurrent.Endpoint).ToString());
            }
        }

        public bool LoadUserState()
        {
            if (File.Exists(UserStateFile))
            {
                try
                {
                    var xml = File.ReadAllText(UserStateFile);
                    File.Delete(UserStateFile);
                    var pair = User.FromNode(XElement.Parse(xml));
                    if (pair.Value == FirebaseCurrent.Endpoint)
                    {
                        user = pair.Key;
                        Log("Previous user state is restored");
                        return true;
                    }
                }
                catch
                {
                    Log("Invalid previous user state");
                }
            }
            return false;
        }

        #endregion


        #region

        public delegate void OnDeviceListChange(List<Device> devices);
        private static event OnDeviceListChange? deviceChanged;

        public static void AddDeviceChangeListener(OnDeviceListChange listener) => deviceChanged += listener;

        #endregion

        public void Dispose()
        {
            client.Dispose();
        }
    }

    public enum MigrateAction
    {
        Encrypt,
        Decrypt
    }
}
