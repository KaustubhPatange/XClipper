using System;
using System.Xml.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace XClipper.Tests
{
    [TestClass]
    public class PathTest
    {
        [TestMethod]
        public void TestMethod1()
        {
            Run();
        }

        string CustomFirebasePath = @"C:\Users\devel\AppData\Roaming\XClipper\new.xml";
        string SETTINGS = "Settings";
        string FIREBASE = "Firebase";
        string DESKTOP_AUTH = "DesktopAuth";
        string MOBILE_AUTH = "MobileAuth";
        FirebaseData FirebaseCurrent = new FirebaseData
        {
            Endpoint = "jwdj",
            ApiKey = "29813923",
            AppId = "dwjidiwd",
            isAuthNeeded = true,
            DesktopAuth = new OAuth
            {
                ClientId = "diwodidw",
                ClientSecret = "73983213"
            },
            MobileAuth = new OAuth
            {
                ClientId = "wodiwoid"
            }
        }; 

        string UniqueID = "71293-23172-37219-28180";
        string DatabaseEncryptPassword = "testKey";
        int DatabaseMaxItem = 2;
        int DatabaseMaxConnection = 2;
        int DatabaseMaxItemLength = 2;
        private void Run()
        {
            var firebaseDoc = new XDocument();
            var config = new XElement(SETTINGS);
            config
                 .Add(
                     new XElement(nameof(UniqueID), UniqueID),
                     new XElement(nameof(DatabaseEncryptPassword), DatabaseEncryptPassword),
                     new XElement(nameof(DatabaseMaxItem), DatabaseMaxItem.ToString()),
                     new XElement(nameof(DatabaseMaxItemLength), DatabaseMaxItemLength.ToString()),
                     new XElement(nameof(DatabaseMaxConnection), DatabaseMaxConnection.ToString())
                 );
            var firebaseConfig = new XElement(FIREBASE);
            firebaseConfig
                .Add(
                    new XElement(nameof(FirebaseCurrent.Endpoint), FirebaseCurrent.Endpoint.ToString()),
                    new XElement(nameof(FirebaseCurrent.ApiKey), FirebaseCurrent.ApiKey.ToString()),
                    new XElement(nameof(FirebaseCurrent.AppId), FirebaseCurrent.AppId.ToString()),
                    new XElement(nameof(FirebaseCurrent.isAuthNeeded), FirebaseCurrent.isAuthNeeded.ToString())
                );

            var desktopOAuth = new XElement(DESKTOP_AUTH);
            desktopOAuth
                .Add(
                    new XElement(nameof(FirebaseCurrent.DesktopAuth.ClientId), FirebaseCurrent.DesktopAuth.ClientId.ToString()),
                    new XElement(nameof(FirebaseCurrent.DesktopAuth.ClientSecret), FirebaseCurrent.DesktopAuth.ClientSecret?.ToString())
                );
            var mobileOAuth = new XElement(MOBILE_AUTH); // Mobile auth doesn't need ClientSecret
            mobileOAuth
                .Add(
                    new XElement(nameof(FirebaseCurrent.MobileAuth.ClientId), FirebaseCurrent.MobileAuth.ClientId.ToString())
                );

            firebaseConfig.Add(mobileOAuth);
            firebaseConfig.Add(desktopOAuth);

            config.Add(firebaseConfig);

            firebaseDoc.Add(config);
            firebaseDoc.Save(CustomFirebasePath);
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
            public string ClientSecret { get; set; }
        }
    }
}
