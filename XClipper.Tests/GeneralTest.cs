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

        /*Changed:wjrzmr+0JCb0yyvv3QnvHME1aNgJRNBE2BIddC7OQQA=
Changed:qElnMBX0LM6estYMtX+lLu/L2PyLXM6KUQ+wyH89GqjH5djWXo6uaycmNxIz4tvccyjBuQnYgTjMBbAmIsJ9APQf9SEqiDD4padIGTNeAnDMn+g+YOSItiZ+7g37W5yzKb1CBhEHFVSw99wf2bBtLr7pS1rwWZLToCftwoU5VjVCmR/uFYx4NmCBI6J8H08XL8DJ9Erpe+ue0UM35brvLdpFSQWZRwC6RsKOuYNZ+pkWHtCrLTV3k4eIh56dfvqZZX5UTBcMqF1IdiecQSchZVLz92MVUOb+95z4jJrAyOnHKhXiiirh/euzDcUg2YD2qKaZxOXRNDzBxPsuAQ+XWw==
Changed:LXdvykCZoNHiP8+KLaWI4ScpFr0gK9i738UTtoNVDYzyRfHftegxa2GO4Sf06bkTt1u0zIii/N/06GR+u2p+mbqBBkRpm61tL2CALZ1e3ZdI6atoRgYR2SYo8EqDJdbhYIsCNW9UD47OTpfbbiH7ayc0Y3FV8KDCkblGNjkazqhnmGIUGn5LlrVC3ucwjeWj+4x8O+l/FFJbb09q0E97rEXUpIsAteKu4sNEyKG+fXqNbTSS60wpM5k73u/wau6JIsZlGInaSLnI5o6Iq1/xOlxnsuEQc/CI9eP1eYr3gi0ojyyz7HjiY1h5qYwnqiUN
Changed:rF8bddSXR6eX5Nuzte8YAICByhI35y6ulkSJvdAqyXzVSUILz21UmnMJ+d1D0rHg
Changed:LBuw8Q2NgDhhbVOl2fUzV/27YRkB7RT4ax1OtkvdtHLJBKSkZeIm/j1cC1xiRPeF*/

        [TestMethod]
        public void AESEncryptTest()
        {
            Debug.WriteLine(Base64Cipher.Encrypt("A basic test", key));
        }

        [TestMethod]
        public void AESDecrypt()
        {
            string encryptedText = "";
            Debug.WriteLine("wjrzmr+0JCb0yyvv3QnvHME1aNgJRNBE2BIddC7OQQA=".DecryptBase64());
            Debug.WriteLine("LBuw8Q2NgDhhbVOl2fUzV/27YRkB7RT4ax1OtkvdtHLJBKSkZeIm/j1cC1xiRPeF".DecryptBase64());
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


        public class Person
        {
            public string Name, Age, Position, Status;
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

        public class PersonJobBuilder: PersonBuilder
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
