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
using System.Windows.Navigation;
using System.Data;
using System.Windows.Threading;
using System.Management;
using System.Dynamic;
using System.Xml.Linq;
using System.Text.RegularExpressions;

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
        public void LinqTest()
        {            
            var e = new XElement("Value");
            var f = e.Element("D").Value;
            var s = DateTime.Parse("2020-11-08T05:32:08Z");
            Debug.WriteLine(s);
            var list = new List<int> { 1, 2, 3, 4, 5, 6, 7 };
            var list1 = new List<string> { "1","2","3","4" };

            var item = list.FirstOrNull(c => c == 24);
            var item1 = list1.FirstOrNull(c => c == "5");
            Debug.WriteLine(item1.Value);
            Debug.WriteLine(item1.IsNull() ? "Item is null" : "We found him");

            //var m = list.Select(c => c > 8).ToList();
            //Debug.WriteLine(m.Count);

            //var dictionary = new Dictionary<string, string>();
            //dictionary.Add("1", "one");
            //dictionary.Add("2", "two");
            //dictionary.Add("3", "three");
            //dictionary.Add("4", "four");
            //var data = dictionary.FirstOrDefault(c => c.Value == "four");
            //if (data.Key == null && data.Value == null)
            //    Debug.WriteLine("not exist");
            //else Debug.WriteLine("exist");
            //Debug.WriteLine(data);
            //if (data.is)
            //    Debug.WriteLine("not exist");
            //else Debug.WriteLine("exist");
        }

        [TestMethod]
        public void RandomTest()
        {
            //UWPToast.Builder()
            //Debug.WriteLine(AppDomain.CurrentDomain.BaseDirectory);
            //    try
            //    {
            //    Debug.WriteLine($"OS: {Environment.OSVersion} {(Environment.Is64BitOperatingSystem ? "x64" : "x86")} {Environment.UserName}\n");
            //        foreach (dynamic obj in GetAllObjects("Win32_VideoController")) 
            //        {
            //            Debug.WriteLine($"VideoController: {obj.Name}, {((long)obj.AdapterRAM).ToFileSizeApi()}, {obj.VideoProcessor}\n");
            //        }
            //    }
            //    catch { }
            //string test = "{\r\n  \"Clips\": [\r\n    {\r\n      \"data\": \"AccessViolationException\",\r\n      \"time\": \"20201103173108\"\r\n    }\r\n  ]\r\n}";
            //Debug.WriteLine(test.Contains(Quotes(nameof(User.Clips))));
            //var key = "d3s2mt7gAPNyqYDU0L1ySt6WSmN7ElkUbHWz+kp7YWfzDOQ3xsHRj9ldRyxE48iyyzjNvHBCHsn8TkWS1H0NHcmv+JKhD0yDc+S2KhTWF7vU5SCmn3huoEH458clRUUA".Decrypt();
            //var value = "7u0wHXG8dCfNuJwM0nEuCwIMHFJgU0jxz4ikNZcODmUJKb8vDdoRVDC/LWdm6+y5BaTNTSc1des0bt5cD85iS+GN1/bcHc+0298HoeTIMgI=".DecryptBase64(key);
            //Debug.WriteLine(value);
        }

        public static List<dynamic> GetAllObjects(string className)
        {
            ManagementObjectCollection col = new ManagementObjectSearcher("SELECT * FROM " + className).Get();
            List<dynamic> objects = new List<dynamic>();

            foreach (ManagementObject obj in col)
            {
                var currentObject = new ExpandoObject() as IDictionary<string, Object>;

                foreach (PropertyData prop in obj.Properties)
                {
                    currentObject.Add(prop.Name, prop.Value);
                }

                objects.Add(currentObject);
            }

            return objects;
        }

        private string Quotes(string t) => $@"""{t}"":";

        [TestMethod]
        public void FindTest()
        {
            var str = "RegExr was created by gskinner.com, and is proudly hosted by Media Temple.";

            Debug.WriteLine(Regex.IsMatch(str, @"[\s\S]{60}", RegexOptions.Multiline));

          //  var nice = "5".ToBool();
            var node = new XElement("User");
            node.SetAttributeValue("Endpoint", "Test");
            Debug.WriteLine("Xml:" + node.ToString());
            //string regex = "[^\\s";
            //string data = "device";
            //Assert.IsTrue(new Regex(regex, RegexOptions.None).IsMatch(data));
            //if (!IgnoreHelper.IsMatch(data))
            //    Assert.IsTrue(true);
            //else 
            //    Assert.Fail();
            //var w = "Hello";
            //Debug.WriteLine(w.Substring(1));
            //var item = new ReleaseItem { body = "### Added\r\n\r\n- This is a test added.\r\n\r\n### Update\r\n\r\n- A test update.\r\n\r\n### Bug (Fixed)\r\n\r\n- Some issue has fixed\r\n- Wow, let's see this `code` comment".Replace(Environment.NewLine, string.Empty) };
            //var b = item.GetFormattedBody();
            //Debug.WriteLine(b);
        }

        private List<Clip> userClips()
        {
            return new List<Clip>
{
    new Clip
    {
        data = "SSjU+RxELxLlwWtipeUxrQF/YrQDDay+NFDcwW6TBdXk3hBtCuvUwHWhEWJNK1SUCooo7tvly0aWjdyHU6vb0OV2NY/XP5Fkd0leLM2z2Drqz+iZETsRCBOD7WMEnx85",
        time = "20200712121739"
    },
    new Clip
    {
        data = "stscRIpo6kDs8+ocKaolg5Vl2OiOPYYNfWmEyCU/d4yzCDdpzuJhuX9T7b+va1k/+/zRsX3oEuDamPB6xK6JUhbZlyKnUDtA42MaaCUonFM=",
        time = "20200712121745"
    },
    new Clip
    {
        data = "A/9LuKrLy+f7wgBR1a5/40S7NdHhs6UdMfxaKEY+2dBKsUI9lpfETBe0HjCSC8Y/IlWJX1V8Dsf7iIptGA+22pQBMdkrKDEZUu3VJ35wHIL3B11bsjUo+BicDa6kI8VKjzuUe7SnOuD9Z08dW11/MXW0j6j4Vr8M7jDbJqV7qdHqyNXmwu4HXYH+gGYkETvKKGeqEVFMd48YyzZTma8w27Br5lW8xKXFKAzamAuwY24JdQIr0qoaEnyHaHI8J52FXDWMPw26uScBQ/nSexxvCvxcowkfsa68IWnViYD1ZnKAB65vmC4LAQIftJfnaLiMZT6abJp+20pI647LWEvSqsxJTjIT/HNjHvpOAuOf3yY=",
        time = "20200712122113"
    },
    new Clip
    {
        data = "uNkGO5q9O3iePSZ56zY1pw==",
        time = "20200712122228"
    },
    new Clip
    {
        data = "wTKTthUqHNiJwGT87PFD0w==",
        time = "20200712122255"
    },
    new Clip
    {
        data = "LyEeWWprd0EG2kDUeLWKQfK5LxGjTBMa00WI/IKkHssjWF5m+z9be5ydPQQd4ryn4N5jGmaIh8jARBl4jxAY6wvkbahDkh3Hz3AZiy4hKFKkjgf9PCmW4ZR8E9ILsVy2kgJOc7924HT1RujOoZsGkvNRe8clsMbDahaMybrsN64/SyBUjxsUUjTrxUqOmQCc8cuh1yffNJR0HSwEtiut7c1nk3hUnVNkW47WugawS2e5QRUAJFOXDDOVcLiT8B1Omajen8yt3U0mDtjCFlxJCNAfhoGkHpNlKu6e5VWwOqz6shsNBo3AfZMvqTZE75dGu8U46rCS0mA/BxupfJ6l0Oo+qnldKPPyBFlYJTjkCLKSOXtaqdmexszsqfxe0N1O60+fHD7hU433SIcFzuZQwdj7vRLGwp+7yrW1yAWh/YAJhZUrNgK73Sz3XgYPP042b6jHD2/c9Vn5PzoSZdmKD9zTKKnJ4owskA5APb4bjIoKV9iTrFLYS5GHiQJGk3zzbqsbb8UYfhCo35tT/4PRoA==",
        time = "20200714215730"
    },
    new Clip
    {
        data = "Q+02+HIFXj1PbvMqOEym7f9/KEiWw5MBPnN3E4CLsexMxANVBg0bAE8d0/OSzOiTOSOxsqxMdpARj9BrkjdHDQ==",
        time = "20200714223156"
    }
};
        }

        private List<Clip> firebaseClips()
        {
            return new List<Clip>
{
    new Clip
    {
        data = "SSjU+RxELxLlwWtipeUxrQF/YrQDDay+NFDcwW6TBdXk3hBtCuvUwHWhEWJNK1SUCooo7tvly0aWjdyHU6vb0OV2NY/XP5Fkd0leLM2z2Drqz+iZETsRCBOD7WMEnx85",
        time = "20200712121739"
    },
    new Clip
    {
        data = "stscRIpo6kDs8+ocKaolg5Vl2OiOPYYNfWmEyCU/d4yzCDdpzuJhuX9T7b+va1k/+/zRsX3oEuDamPB6xK6JUhbZlyKnUDtA42MaaCUonFM=",
        time = "20200712121745"
    },
    new Clip
    {
        data = "A/9LuKrLy+f7wgBR1a5/40S7NdHhs6UdMfxaKEY+2dBKsUI9lpfETBe0HjCSC8Y/IlWJX1V8Dsf7iIptGA+22pQBMdkrKDEZUu3VJ35wHIL3B11bsjUo+BicDa6kI8VKjzuUe7SnOuD9Z08dW11/MXW0j6j4Vr8M7jDbJqV7qdHqyNXmwu4HXYH+gGYkETvKKGeqEVFMd48YyzZTma8w27Br5lW8xKXFKAzamAuwY24JdQIr0qoaEnyHaHI8J52FXDWMPw26uScBQ/nSexxvCvxcowkfsa68IWnViYD1ZnKAB65vmC4LAQIftJfnaLiMZT6abJp+20pI647LWEvSqsxJTjIT/HNjHvpOAuOf3yY=",
        time = "20200712122113"
    },
    new Clip
    {
        data = "uNkGO5q9O3iePSZ56zY1pw==",
        time = "20200712122228"
    },
    new Clip
    {
        data = "wTKTthUqHNiJwGT87PFD0w==",
        time = "20200712122255"
    },
    new Clip
    {
        data = "LyEeWWprd0EG2kDUeLWKQfK5LxGjTBMa00WI/IKkHssjWF5m+z9be5ydPQQd4ryn4N5jGmaIh8jARBl4jxAY6wvkbahDkh3Hz3AZiy4hKFKkjgf9PCmW4ZR8E9ILsVy2kgJOc7924HT1RujOoZsGkvNRe8clsMbDahaMybrsN64/SyBUjxsUUjTrxUqOmQCc8cuh1yffNJR0HSwEtiut7c1nk3hUnVNkW47WugawS2e5QRUAJFOXDDOVcLiT8B1Omajen8yt3U0mDtjCFlxJCNAfhoGkHpNlKu6e5VWwOqz6shsNBo3AfZMvqTZE75dGu8U46rCS0mA/BxupfJ6l0Oo+qnldKPPyBFlYJTjkCLKSOXtaqdmexszsqfxe0N1O60+fHD7hU433SIcFzuZQwdj7vRLGwp+7yrW1yAWh/YAJhZUrNgK73Sz3XgYPP042b6jHD2/c9Vn5PzoSZdmKD9zTKKnJ4owskA5APb4bjIoKV9iTrFLYS5GHiQJGk3zzbqsbb8UYfhCo35tT/4PRoA==",
        time = "20200714215730"
    }
};
        }

        [TestMethod]
        public void CheckInternet()
        {
            Debug.WriteLine(string.Format("{0} World", "Hello"));
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
            DispatcherTimer timer = new DispatcherTimer
            {
                Interval = TimeSpan.FromSeconds(5),
                IsEnabled = true,
            };
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
            // FileVersionInfo fileVersionInfo = FileVersionInfo.GetVersionInfo(@"C:\Users\devel\Desktop\setup.exe");

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
