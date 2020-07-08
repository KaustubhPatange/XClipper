using System;
using System.Diagnostics;
using System.IO.Compression;
using ClipboardManager.models;
using System.Linq;
using Components;
using Components.viewModels;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using static Components.MainHelper;
using System.Windows.Documents;
using System.Collections.Generic;
using RestSharp;
using System.Net.NetworkInformation;
using Components.UI;

namespace XClipper.Tests
{
    [TestClass]
    public class GeneralTest
    {
        [System.Runtime.InteropServices.DllImport("wininet.dll")]
        private extern static bool InternetGetConnectedState(out int Description, int ReservedValue);
        public static bool CheckNet()
        {
            int desc;
            return InternetGetConnectedState(out desc, 0);
        }

        [TestMethod]
        public void RandomTest()
        {
            Debug.WriteLine(DateTime.Now.AddMinutes(50).ToFormattedDateTime());
        }

        [TestMethod]
        public void CheckInternet()
        {
            
            //Stopwatch s = new Stopwatch();
            //s.Start();
            ////    if (new Ping().Send("www.google.com.mx").Status == IPStatus.Success)
            //if (CheckNet())
            //{
            //   Debug.WriteLine("Connection Exist");
            //}
            //Debug.WriteLine("Elapsed time: " + s.ElapsedMilliseconds);
            //s.Stop();
        }

        [TestMethod]
        public void CountMethod()
        {
            int DatabaseMaxItem = 3;
            var user = new List<int>
            {
                1,2,3,4,5,6,7,8
            };
            user.RemoveRange(0, user.Count > DatabaseMaxItem ? user.Count - DatabaseMaxItem : 0);
            Debug.WriteLine(user.Count);
            Assert.AreEqual(user.Count, 3);
        }

        [TestMethod]
        public void ConnectionTest()
        {
            FileVersionInfo fileVersionInfo = FileVersionInfo.GetVersionInfo(@"C:\Users\devel\Desktop\setup.exe");

        }

        public class Upd
        {
            public string obsolute { get; set; }
        }

        [TestMethod]
        public void DatabaseTest()
        {
            // AppSingleton.GetInstance.Init();


            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;

            //var pinnedItems = AppSingleton.GetInstance.dataDB.Query<TableCopy>("select * from TableCopy where IsPinned = 1");
            //pinnedItems.Reverse();
            //if (pinnedItems.Count > 0)
            //    pinnedItems = pinnedItems.Futher((s) => s.Decrypt());
            ////var pinnedItems = dataDB.Query<TableCopy>("select * from TableCopy where IsPinned = 1").Futher((s) => s.Decrypt());
            ////pinnedItems.Reverse();

            //var normalItems = AppSingleton.GetInstance.dataDB.Query<TableCopy>("select * from TableCopy where IsPinned = 0")
            //    .OrderByDescending(x => ParseDateTimeText(x.LastUsedDateTime)).ToList();
            //if (normalItems.Count > 0)
            //    normalItems = normalItems.Futher((s) => s.Decrypt());
            ////s.Stop();
            ////Console.WriteLine(s.ElapsedMilliseconds);
            //var d = pinnedItems.Concat(normalItems).ToList();


            long milliseconds1 = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;


            Debug.WriteLine("offset: " + (milliseconds1 - milliseconds).ToString());
        }


        public class Person
        {
            public string Name, Age, Position, Status;
            public virtual void Go()
            { }
        }

        public class Boy : Person
        {
            public override void Go()
            {
                base.Go();
            }
        }

        public class PersonBuilder
        {
            protected Person person = new Person();
            public PersonBuilder SetName(string Name)
            {
                person.Name = Name;
                return this;
            }
            public PersonBuilder SetAge(string Age)
            {
                person.Age = Age;
                return this;
            }
        }

        public class PersonJobBuilder : PersonBuilder
        {
            public PersonJobBuilder SetPosition(string pos)
            {
                person.Position = pos;
                return this;
            }

            public PersonJobBuilder SetStatus(string stat)
            {
                person.Status = stat;
                return this;
            }
        }
    }
}
