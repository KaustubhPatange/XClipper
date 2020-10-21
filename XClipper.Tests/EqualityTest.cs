using Components;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Data.SqlTypes;
using System.Diagnostics;
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
        
        [TestMethod]
        public void LevenshteinDistanceTest()
        {
            Console.WriteLine("This is a very big text".isCloselyResemble("This is a full large number"));
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
        public static int LevenshteinDistance(string source, string target)
        {
            // degenerate cases
            if (source == target) return 0;
            if (source.Length == 0) return target.Length;
            if (target.Length == 0) return source.Length;

            // create two work vectors of integer distances
            int[] v0 = new int[target.Length + 1];
            int[] v1 = new int[target.Length + 1];

            // initialize v0 (the previous row of distances)
            // this row is A[0][i]: edit distance for an empty s
            // the distance is just the number of characters to delete from t
            for (int i = 0; i < v0.Length; i++)
                v0[i] = i;

            for (int i = 0; i < source.Length; i++)
            {
                // calculate v1 (current row distances) from the previous row v0

                // first element of v1 is A[i+1][0]
                //   edit distance is delete (i+1) chars from s to match empty t
                v1[0] = i + 1;

                // use formula to fill in the rest of the row
                for (int j = 0; j < target.Length; j++)
                {
                    var cost = (source[i] == target[j]) ? 0 : 1;
                    v1[j + 1] = Math.Min(v1[j] + 1, Math.Min(v0[j + 1] + 1, v0[j] + cost));
                }

                // copy v1 (current row) to v0 (previous row) for next iteration
                for (int j = 0; j < v0.Length; j++)
                    v0[j] = v1[j];
            }

            return v1[target.Length];
        }
        public static double isCloselyResemble(this string source, string target)
        {
            if ((source == null) || (target == null)) return 0.0;
            if ((source.Length == 0) || (target.Length == 0)) return 0.0;
            if (source == target) return 1.0;

            int stepsToSame = LevenshteinDistance(source, target);
            Console.WriteLine(stepsToSame);
            return (1.0 - ((double)stepsToSame / (double)Math.Max(source.Length, target.Length)));
        }
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
