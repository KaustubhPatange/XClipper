using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Components;
using System.Diagnostics;
using System.Threading.Tasks;
using System.Windows;
using System.Threading;
using FireSharp.Core;
using FireSharp.Core.Config;
using Firebase.Storage;
using System.IO;
using System.Windows.Controls;

namespace XClipper.Tests
{
    [TestClass]
    public class FirebaseTest
    {
        [TestMethod]
        public void TestMethod1()
        {

          //  FirebaseSingleton.GetInstance.Init("12345-67890-abcde-fghij");

            Task.Run(async () =>
            {
                await FirebaseSingleton.GetInstance.AddClip("this is a small clip which I am goind to add...");
                Debug.WriteLine("Make pause here");
            });
           
            
           // method();
            //t.Wait();
            //t1.Wait();
            Thread.Sleep(10000);

            Debug.WriteLine("Done");
        }

        [TestMethod]
        public async Task UserFetchCheck()
        {
            var client = new FirebaseClient(new FirebaseConfig
            {
                BasePath = "https://quickstart-1550501068702.firebaseio.com/"
            });
            var user = await client.SafeGetAsync($"users/i2e289ejdidjiwieuiejkdj").ConfigureAwait(false);

            Assert.IsTrue(user.Body == "null");
        }

        [TestMethod]
        public async Task StorageTest()
        {
            var imagePath = @"C:\Users\devel\AppData\Roaming\XClipper\Image\2020-11-23 12-42-17.png";
            var pathRef = new FirebaseStorage("kps-tv.appspot.com")
                .Child("XClipper")
                .Child("images")<
                .Child("small-image-test.png");

            var tmp = Path.GetTempFileName();
            File.Copy(imagePath, tmp, true);
            using (var stream = new FileStream(tmp, FileMode.Open))
            {
                await pathRef.PutAsync(stream); // Push to storage
                File.Delete(tmp);
            }
        }
    }
}
