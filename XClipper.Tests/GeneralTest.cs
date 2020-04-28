using System;
using System.Diagnostics;
using System.IO.Compression;
using ClipboardManager.models;
using System.Linq;
using Components;
using Components.viewModels;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using static Components.MainHelper;

namespace XClipper.Tests
{
    [TestClass]
    public class GeneralTest
    {

        [TestMethod]
        public void EncyptionTest()
        {
            Debug.WriteLine("RidiculousPassword".Encrypt());
        }

        [TestMethod]
        public void DecryptionTest()
        {
            Debug.WriteLine("nD1PkIKddzEcs8dDxniZpZ0ADzUHEFyb/H2xzPVpTJwZDC9Tut3eOLL5xkMR9bC7m41LyeApjqE/jbSQfJMZgc1YOubKA/Ejo5/4tI1OsXSGNREDY1n9igo74b5fk0wN".Decrypt());
        }
        /** Results
         * --------
         *  1. 50 approx (not encrypted): 98-99 ms
         *  2. 20 approx (encrypted): 1233 ms
         */

        [TestMethod]
        public void DatabaseTest()
        {
            AppSingleton.GetInstance.Init();


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
    }
}
