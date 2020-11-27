using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Xml.Linq;
using static Components.DefaultSettings;

#nullable enable

namespace Components
{
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

        /// <summary>
        /// For safety purpose an endpoint attribute will be stored to prevent
        /// running restoration on any other remote connected database.
        /// </summary>
        /// <param name="t"></param>
        /// <param name="endpoint"></param>
        /// <returns></returns>
        public static XElement ToNode(User t, string endpoint)
        {
            var node = new XElement(nameof(User));
            node.SetAttributeValue(nameof(FirebaseCurrent.Endpoint), endpoint);
            node.Add(new XComment("This data structure will hold the previous state of Firebase User as a cache."));
            node.Add(new XElement(nameof(t.IsLicensed), t.IsLicensed));
            node.Add(new XElement(nameof(t.MaxItemStorage), t.MaxItemStorage));
            node.Add(new XElement(nameof(t.TotalConnection), t.TotalConnection));
            node.Add(new XElement(nameof(t.LicenseStrategy), (int)t.LicenseStrategy));

            var deviceList = new XElement(nameof(t.Devices));
            foreach (var device in t.Devices) deviceList.Add(Device.ToNode(device));

            var clipList = new XElement(nameof(t.Clips));
            foreach (var clip in t.Clips) clipList.Add(Clip.ToNode(clip));

            node.Add(deviceList);
            node.Add(clipList);

            return node;
        }

        public static KeyValuePair<User, string> FromNode(XElement t)
        {
            var model = new User();
            var endPoint = t.Attribute(nameof(FirebaseCurrent.Endpoint)).Value;
            model.IsLicensed = t.Element(nameof(model.IsLicensed)).Value.ToBool();
            model.MaxItemStorage = t.Element(nameof(model.MaxItemStorage)).Value.ToInt();
            model.TotalConnection = t.Element(nameof(model.TotalConnection)).Value.ToInt();
            model.LicenseStrategy = (LicenseType)t.Element(nameof(model.LicenseStrategy)).Value.ToInt();

            var deviceList = new List<Device>();
            var xDeviceList = t.Element(nameof(model.Devices));
            foreach (var xDevice in xDeviceList.Elements()) deviceList.Add(Device.FromNode(xDevice));
            model.Devices = deviceList;

            var clipList = new List<Clip>();
            var xClipList = t.Element(nameof(model.Clips));
            foreach (var xClip in xClipList.Elements()) clipList.Add(Clip.FromNode(xClip));
            model.Clips = clipList;

            return new KeyValuePair<User, string>(model, endPoint);
        }
    }

    #region Extension methods
    public static class UserExtensions
    {
        public static User DeepCopy(this User t)
        {
            var json = JsonConvert.SerializeObject(t);
            return JsonConvert.DeserializeObject<User>(json);
        }

    }

    #endregion
}
