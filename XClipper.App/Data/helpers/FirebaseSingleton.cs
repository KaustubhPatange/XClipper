using FireSharp;
using FireSharp.Config;
using FireSharp.Interfaces;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;
using static Components.Core;
using static Components.DefaultSettings;

#nullable enable

namespace Components
{
    public sealed class FirebaseSingleton
    {
        #region Variable Declaration

        private static FirebaseSingleton Instance;
        private IFirebaseClient client;
        private IFirebaseBinder binder;
        private string UID;
        private bool alwaysForceInvoke = false;
        private User user;

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
        {
            // Automatically Instantiate Firebase client
            InitConfig();
        }

        #endregion

        #region Private Methods

        public void InitConfig()
        {
            UID = UniqueID;
            IFirebaseConfig config = new FirebaseConfig
            {
                AuthSecret = FirebaseSecret,
                BasePath = FirebaseEndpoint
            };
            client = new FirebaseClient(config);

            Task.Run(async () => await SetGlobalUser(true));
        }

        /// <summary>
        /// This will submit configuration change to database.
        /// </summary>
        /// <returns></returns>
        public async Task SubmitConfigurations()
        {
            if (!BindDatabase) return;
            await SetGlobalUser();

            user.MaxItemStorage = DatabaseMaxItem;
            user.TotalConnection = DatabaseMaxConnection;
            user.IsLicensed = IsPurchaseDone;

            await client.SetAsync($"users/{UID}", user);
        }

        /// <summary>
        /// This will load the user from the firebase database.
        /// </summary>
        /// <param name="forceInvoke">Forcefully load the data even if user is not null.</param>
        /// <returns></returns>
        public async Task SetGlobalUser(bool forceInvoke = false)
       {
            if (!BindDatabase) return;

            if (alwaysForceInvoke || user == null || forceInvoke)
            {
                user = await _GetUser();

                // todo: Set some other details for user...
                user.IsLicensed = IsPurchaseDone;
                user.TotalConnection = DatabaseMaxConnection;
                user.MaxItemStorage = DatabaseMaxItem;

                if (user.Devices != null && user.Devices.Count > 0)
                    alwaysForceInvoke = true;
            }
        }
        private async Task<User> _GetUser()
        {
            var data = await client.GetAsync($"users/{UID}");
            if (data.Body != "null")
            {
                return data.ResultAs<User>().Also((user) => { this.user = user; });
            }
            else return await RegisterUser();
        }

        #endregion

        #region Methods

        /// <summary>
        /// Initialize the Instance with the UID supplied with it.
        /// </summary>
        /// <param name="UID"></param>
        public void Init(string UID) => this.UID = UID;

        /// <summary>
        /// This sets call back to the binder events with an attached interface.
        /// </summary>
        /// <param name="binder"></param>
        public async void SetCallback(IFirebaseBinder binder)
        {
            this.binder = binder;
            await client.OnAsync($"users/{UID}", (o, a, c) => 
            {
                if (BindDatabase)
                    binder.OnDataAdded(a); 
            }, 
            (o,a,c)=> 
            {
                if (BindDatabase)
                    binder.OnDataChanged(a); 
            }, 
            (o,a,c)=> 
            {
                if (BindDatabase)
                    binder.OnDataRemoved(a); 
            });
        }

        /// <summary>
        /// Checks if the user exist in the nodes or not.
        /// </summary>
        /// <returns></returns>
        private async Task<bool> IsUserExist()
        {
            var response = await client.GetAsync($"users/{UID}");
            return response.Body != "null";
        }

        /// <summary>
        /// Add an empty user to the node.
        /// </summary>
        /// <returns></returns>
        public async Task<User> RegisterUser()
        {
            if (!BindDatabase) return new User();
            var exist = await IsUserExist();
            if (!exist)
            {
                var user = new User();
                user.IsLicensed = IsPurchaseDone;
                this.user = user;
                await client.SetAsync($"users/{UID}", user);
            }
            return user;
        }

        /// <summary>
        /// Removes all data associated with the UID.
        /// </summary>
        /// <returns></returns>
        public async Task RemoveUser()
        {
            await client.DeleteAsync($"users/{UID}");
            await RegisterUser();
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

            await SetGlobalUser(true);
            return user.Devices;
        }

        public async Task<List<Device>> RemoveDevice(string DeviceId)
        {
            if (!BindDatabase) return new List<Device>();

            await SetGlobalUser(true);

            user.Devices = user.Devices.Where(d => d.id != DeviceId).ToList();
            await client.UpdateAsync($"users/{UID}", user);
            return user.Devices;
        }

        /// <summary>
        /// Add a clip data to the server instance.
        /// </summary>
        /// <param name="Text"></param>
        /// <returns></returns>
        public async Task AddClip(string Text)
        {
            await SetGlobalUser();

            if (!BindDatabase) return;

            if (Text == null) return;
            if (Text.Length > DatabaseMaxItemLength) return;
            if (user.Clips == null)
                user.Clips = new List<Clip>();
            // Remove clip if greater than item
            if (user.Clips.Count > DatabaseMaxItem)
                user.Clips.RemoveAt(0);
            user.Clips.Add(new Clip { data = Text.EncryptBase64(DatabaseEncryptPassword), time = DateTime.Now.ToFormattedDateTime(false) });
            await client.UpdateAsync($"users/{UID}", user);
        }

        public async Task RemoveClip(int position)
        {
            if (!BindDatabase) return;

            await SetGlobalUser();
            if (user.Clips == null)
                return;
            user.Clips.RemoveAt(position);
            await client.UpdateAsync($"users/{UID}", user);
        }

        /// <summary>
        /// Removes the clip data of user.
        /// </summary>
        /// <param name="Text"></param>
        /// <returns></returns>
        public async Task RemoveClip(string Text)
        {
            await SetGlobalUser();

            if (!BindDatabase) return;

            if (Text == null) return;
            if (user.Clips == null)
                return;
            foreach (var item in user.Clips)
            {
                if (item.data.DecryptBase64(DatabaseEncryptPassword) == Text)
                {
                    user.Clips.Remove(item);
                    await client.UpdateAsync($"users/{UID}", user);
                }
            }
        }

        /// <summary>
        /// Remove all clip data of user.
        /// </summary>
        /// <returns></returns>
        public async Task RemoveAllClip()
        {
            if (!BindDatabase) return;

            await SetGlobalUser();
            if (user.Clips == null)
                return;
            user.Clips.Clear();
            await client.UpdateAsync($"users/{UID}", user);
        }

        #endregion

    }

    #region Entities

    public class User
    {
        
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

    #endregion
}
