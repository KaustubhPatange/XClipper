using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Components;
using System.Diagnostics;
using System.Threading.Tasks;
using System.Windows;
using System.Threading;

namespace XClipper.Tests
{
    [TestClass]
    public class FirebaseTest
    {
        [TestMethod]
        public void TestMethod1()
        {

            FirebaseSingleton.GetInstance.Init("12345-67890-abcde-fghij");

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

        public void method()
        {
            //var t = Task.Run(async () =>
            //{
            //    await FirebaseHelper.GetInstance.RegisterUser();
            //});

        }
    }
}
