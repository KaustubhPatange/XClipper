using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Security.Cryptography;
using System.Text.RegularExpressions;
using static Components.Constants;

#nullable enable

namespace Components
{
    public class FirebaseParser
    {
        private User? user;

        public void SetUser(User user)
        {
            this.user = user.DeepCopy();
        }

        public User Parse(string eventName, string path, string json)
        {
            // Put event gives whole data...
            if (eventName == "put" && !string.IsNullOrWhiteSpace(json))
            {
                if (path == "/")
                    user = JsonConvert.DeserializeObject<User>(json);
                else ManagePaths(path, json);
            }
            // Will be used to remove last data...
            if (eventName == "put" && string.IsNullOrWhiteSpace(json))
            {
                ManageEmptyStructure(path);
            }

            // Make sure user is not null at this stage.
            if (user == null) throw new AccessViolationException("User must not be null");

            // Patch event overwrites existing data... 
            if (eventName == "patch")
            {
                if (path == "/")
                {
                    bool toSetIsLicense = false;
                    bool toSetLicenseStrategy = false;
                    bool toSetMaxItemStorage = false;
                    bool toSetTotalConnections = false;
                    bool toSetClips = false;
                    bool toSetDevices = false;

                    if (json.Contains(Quotes(nameof(User.IsLicensed)))) toSetIsLicense = true;
                    if (json.Contains(Quotes(nameof(User.LicenseStrategy)))) toSetLicenseStrategy = true;
                    if (json.Contains(Quotes(nameof(User.MaxItemStorage)))) toSetMaxItemStorage = true;
                    if (json.Contains(Quotes(nameof(User.TotalConnection)))) toSetTotalConnections = true;
                    if (json.Contains(Quotes(nameof(User.Clips)))) toSetClips = true;
                    if (json.Contains(Quotes(nameof(User.Devices)))) toSetDevices = true;

                    var copyUser = JsonConvert.DeserializeObject<User>(json);
                    if (toSetIsLicense) user.IsLicensed = copyUser.IsLicensed;
                    if (toSetLicenseStrategy) user.LicenseStrategy = copyUser.LicenseStrategy;
                    if (toSetMaxItemStorage) user.MaxItemStorage = copyUser.MaxItemStorage;
                    if (toSetTotalConnections) user.TotalConnection = copyUser.TotalConnection;
                    if (toSetClips) user.Clips = copyUser.Clips;
                    if (toSetDevices) user.Devices = copyUser.Devices;
                }
                else ManagePaths(path, json);
            }
            return user.DeepCopy();
        }

        private void ManagePaths(string path, string json)
        {
            try
            {
                if (json == null)
                    return;
                else if (path == PATH_DEVICES)
                {
                    var devices = JsonConvert.DeserializeObject<List<Device>>(json);
                    user.Devices = devices;
                }
                else if (path == PATH_CLIPS)
                {
                    var clips = JsonConvert.DeserializeObject<List<Clip>>(json);
                    user.Clips = clips;
                }
                else if (Regex.IsMatch(path, PATH_CLIP_REGEX_PATTERN))
                {
                    int index = Regex.Match(path, PATH_CLIP_REGEX_PATTERN).Groups[1].Value.ToInt();
                    var subPath = Regex.Replace(path, PATH_CLIP_REGEX_PATTERN, "");
                    switch (subPath)
                    {
                        case nameof(Clip.data):
                            user.Clips[index].data = json;
                            break;
                        case nameof(Clip.time):
                            user.Clips[index].time = json;
                            break;
                        default:
                            var clip = JsonConvert.DeserializeObject<Clip>(json);
                            user.Clips[index] = clip;
                            break;
                    }
                }
                else if (Regex.IsMatch(path, PATH_DEVICE_REGEX_PATTERN))
                {
                    int index = Regex.Match(path, PATH_DEVICE_REGEX_PATTERN).Groups[1].Value.ToInt();
                    var subPath = Regex.Replace(path, PATH_DEVICE_REGEX_PATTERN, "");
                    switch (subPath)
                    {
                        case nameof(Device.id):
                            user.Devices[index].id = json;
                            break;
                        case nameof(Device.model):
                            user.Devices[index].model = json;
                            break;
                        case nameof(Device.sdk):
                            user.Devices[index].sdk = json.Trim().ToInt();
                            break;
                        default:
                            var device = JsonConvert.DeserializeObject<Device>(json);
                            user.Devices[index] = device;
                            break;
                    }
                }
                else if (path == $"/{nameof(User.IsLicensed)}")
                {
                    user.IsLicensed = json.ToBool();
                }
                else if (path == $"/{nameof(User.LicenseStrategy)}")
                {
                    user.LicenseStrategy = json.ToEnum<LicenseType>();
                }
                else if (path == $"/{nameof(User.MaxItemStorage)}")
                {
                    user.MaxItemStorage = json.ToInt();
                }
                else if (path == $"/{nameof(User.TotalConnection)}")
                {
                    user.TotalConnection = json.ToInt();
                }
            }
            catch (FormatException e) 
            {
                LogHelper.Log("FirebaseParser", e.Message + e.StackTrace); 
            }  
        }

        private void ManageEmptyStructure(string path)
        {
            if (path == PATH_DEVICES)
            {
                user.Devices = null;
            }
            else if (path == PATH_CLIPS)
            {
                user.Clips = null;
            }
        }

        private string Quotes(string t) => $@"""{t}"":";
    }
}
