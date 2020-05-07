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

namespace XClipper.Tests
{
    [TestClass]
    public class GeneralTest
    {
        string key = "licensepassword123";
        [TestMethod]
        public void AESEncryptTest()
        {
            Debug.WriteLine(Base64Cipher.Encrypt("A basic test", key));
        }

        [TestMethod]
        public void AESDecrypt()
        {
            string encryptedText = "";
            Debug.WriteLine(Base64Cipher.Decrypt(encryptedText, key));
        }

        [TestMethod]
        public void EncyptionTest()
        {
            Debug.WriteLine("text to encrypt".Encrypt());
        }

        [TestMethod]
        public void DecryptionTest()
        {
            Debug.WriteLine("Z0J6anNTVv+h46RUNcOUYGz5AAtFz3FLHTtZj/ef6Lnt4tfPQHFMXbjjUu5R/w2Q++0BYLiTmyTPFLkjEkxHcNdKd+gu9TbwmkGo46Vnc13dowThDrbYuKh3lD2CzOOwQmwVPPESe4GLDPLh1WDngUAdb7Fznf02OhxcStrVqoAFzDIIycf/1M12u7sY9Xbus8f2eDUvlOsti4S4T5AhkDVynDD8Uo86yQUhP5Ebmw3/aW1dVvSKq79lGHlpmd3/03zlwC21lfOs5atdYPnwpkub1IZ86JXBAEeVdD/PCa6pVP2GaDfY00M423zebMQR+JE1njgHeu0BiOFaAIj0gcfXSvf/VhCjpWNj7cAWy1DP5JqEP6w5Ik7zb30cNYhWD+vyS2YOr0g/j+VqyDK1pKc2HcAxCC9XaIxRbNmraImkxY3z8dMEixOrGcfwVGI/RlaXKrfw27ve7p06XN+ASKQH6fD4de3F1kClLm0x1WRJXNn2Ko39dYH4f/RZsH44zQmCoHmL/pOBooyB23ahPgYE4STuLIjXnTbzJVJtYQijtwDZ3++xJTtnUsioRildoTNh8EQxJVz4DItCBWdmsFncpJvtuniqXtZglN2J4sNMr9vTbQAJ13rNWc1kU2D4oOO7ePPTxNPSUWvNVsqIKAnoYo3x9797owCxpTNJnRGxTNBFk8Nu7SGTiL6RN8CplRiCsikpT9jJwGWBgCA1dhd9btMMviDLHyMc/fOY97s=".DecryptBase64());
        }
        /** Results
         * --------
         *  1. 50 approx (not encrypted): 98-99 ms
         *  2. 20 approx (encrypted): 1233 ms
         */

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
