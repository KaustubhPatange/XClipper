using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Media;
using Components;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace XClipper.Tests
{
    [TestClass]
    public class LicenseTest
    {
        public static string RoamingDirectory = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        public static string LicenseFilePath = Path.Combine(RoamingDirectory, "xclipper.lic");
        [TestMethod]
        public void GenerateUniqueId()
        {
            var id = LicenseHandler.UniqueID;

            Debug.WriteLine(id);

            Assert.IsNotNull(id);
        }

        [TestMethod]
        public void GenerateLicense()
        {
            
            var id = LicenseHandler.UniqueID;

            var key = LicenseHandler.GenerateLicense(id);

            Debug.WriteLine(key);

            Assert.IsNotNull(key);
        }

        [TestMethod]
        public void TestLicense()
        {
            Uri uri = new Uri(LicenseFilePath);

            Debug.WriteLine(uri.LocalPath);

            Assert.IsTrue(LicenseHandler.IsActivated(uri));
        }
    }
}
