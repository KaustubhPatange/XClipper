using System;
using System.Diagnostics;
using System.IO.Compression;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace XClipper.Tests
{
    [TestClass]
    public class GeneralTest
    {
        [TestMethod]
        public void TestMethod1()
        {
            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;

            Debug.WriteLine(milliseconds);

            ZipFile.CreateFromDirectory(@"D:\VisualStudioProjects\XClipper\XClipper.App\bin\Debug\test", @"C:\Users\devel\Desktop\new.zip");

            long milliseconds1 = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;



            Debug.WriteLine(milliseconds1);

            Debug.WriteLine("offset: " + (milliseconds1 - milliseconds).ToString());
        }
    }
}
