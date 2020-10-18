using Components;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Data.SqlTypes;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace XClipper.Tests
{
    [TestClass]
    public class EqualityTest
    {
        [TestMethod]
        public void ReferentialEqualityTest()
        {
            var u1 = new User { Name = "John", Age = 30 };
            var u2 = new User { Name = "John", Age = 30 };

            var l1 = new List<User> { u1 };
            var l2 = new List<User> { u2 };

            Assert.IsTrue(l1.ExceptMy(l2).Count() == 0);
        }

        class User
        {
            public string Name { get; set; }
            public int Age { get; set; }

            public override bool Equals(object obj)
            {
                if (obj is User)
                    if (obj != null)
                    {
                        var other = (User)obj;
                        if (this == other) return true;
                        return (this.Name == other.Name && this.Age == other.Age);
                    }
                return false;
            }
        }
    }

    public static class Extentions
    {
        public static IEnumerable<T> ExceptMy<T>(this IEnumerable<T> first, IEnumerable<T> second)
        {
            foreach(var item in first)
            {
                bool isExist = false;
                foreach(var item2 in second)
                {
                    if (item.Equals(item2))
                        isExist = true;
                }
                if (!isExist)
                    yield return item;
            }
        }
    }
}
