using System;
using System.Diagnostics;
using Components;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace XClipper.Tests
{
    [TestClass]
    public class ExtensionTests
    {
        [TestMethod]
        public void Truncate_Test_MaxLength()
        {
            string t = "wjdk jw kwf kdkw mdkmwd mwmd,wmdmm d dmw dow odkod okdw"
                .Truncate(10);

            Debug.WriteLine("MaxLength: " + t);

            Assert.IsNotNull(t);
        }
        [TestMethod]
        public void Truncate_Test_MinLength()
        {
            string t = "wjdk jw kwf kdkw mdkmwd mwmd,wmdmm d dmw dow odkod okdw"
                .Truncate(100);

            Debug.WriteLine("MinLength: " + t);

            Assert.IsNotNull(t);
        }
    }
}
