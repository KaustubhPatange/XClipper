using FireSharp;
using FireSharp.Config;
using FireSharp.Interfaces;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Windows.Forms;
using static Components.Core;
using static Components.DefaultSettings;

namespace Components
{
    public sealed class FirebaseSingleton
    {
        #region Variable Declaration

        private static FirebaseSingleton Instance;
        public IFirebaseClient client;
        private IFirebaseBinder binder;
        private string UID;
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

            IFirebaseConfig config = new FirebaseConfig
            {
                AuthSecret = FIREBASE_SECRET,
                BasePath = FIREBASE_PATH
            };
            client = new FirebaseClient(config);
        }

        #endregion

        #region Private Methods

        private async Task SetGlobalUser()
        {
            if (user == null)
            {
                user = await _GetUser();

                // todo: Set some other details for user...
                user.IsLicensed = IsPurchaseDone;
                user.TotalConnection = DatabaseMaxConnection;
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
            await client.OnAsync("", (o, a, c) => { binder.OnDataAdded(a); }, (o,a,c)=> { binder.OnDataChanged(a); }, (o,a,c)=> { binder.OnDataRemoved(a); });
        }

        /// <summary>
        /// Checks if the user exist in the nodes or not.
        /// </summary>
        /// <returns></returns>
        public async Task<bool> IsUserExist()
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
            var exist = await IsUserExist();
            if (!exist)
            {
                var user = new User();
                this.user = user;
                await client.SetAsync($"users/{UID}", user);
            }
            return user;
        }

        /// <summary>
        /// Returns the user details.
        /// </summary>
        /// <returns></returns>
        public User GetUser()
        {
            return user;
        }

        /// <summary>
        /// Add a clip data to the server instance.
        /// </summary>
        /// <param name="Text"></param>
        /// <returns></returns>
        public async Task AddClip(string Text)
        {
            await SetGlobalUser();
            if (Text == null) return;
            if (Text.Length > DatabaseMaxItemLength) return;
            if (user.Clips == null)
                user.Clips = new List<Clip>();
            // Remove clip if greater than item
            if (user.Clips.Count > DatabaseMaxItem)
                user.Clips.RemoveAt(0);
            user.Clips.Add(new Clip { data = Text.EncryptBase64(), time = DateTime.Now.ToFormattedDateTime(false) });
            await client.UpdateAsync($"users/{UID}", user);
        }

        ///// <summary>
        ///// Add a list of clip data to the server instance.
        ///// </summary>
        ///// <param name="clips"></param>
        ///// <returns></returns>
        //public async Task AddAllClip(List<Clip> clips)
        //{
        //    await SetGlobalUser();
        //    if (user.Clips == null)
        //        user.Clips = new List<Clip>();
        //    clips.ForEach((l) => { l.data.EncryptBase64(); });
        //    user.Clips.AddRange(clips);
        //    user.Clips.RemoveRange(0, user.Clips.Count > DatabaseMaxItem ? user.Clips.Count - DatabaseMaxItem : 0);
        //    await client.UpdateAsync($"users/{UID}", user);
        //}

        /// <summary>
        /// Removes the clip data of user.
        /// </summary>
        /// <param name="position"></param>
        /// <returns></returns>
        public async Task RemoveClip(int position)
        {
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
            if (user.Clips == null)
                return;
            foreach (var item in user.Clips)
            {
                if (item.data == Text)
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
        /// Property tells the last connected Android device given its ID. Null means no one is connected.
        /// </summary>
        public List<Device> Devices { get; set; }

        /// <summary>
        /// Property stores all the clip data.
        /// </summary>
        public List<Clip> Clips { get; set; }
    }

    public class Device
    {
        public string ID { get; set; }
        public int SDK { get; set; }
        public string Model { get; set; }
    }

    public class Clip
    {
        public string data { get; set; }
        public string time { get; set; }
    }

    #endregion
}
