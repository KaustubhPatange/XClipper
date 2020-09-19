using System.Collections.Generic;
using System.Collections.Specialized;
using System.Linq;
using System.Text.RegularExpressions;

namespace Components
{
    public static class CollectionExtensions
    {
        public static string[] ToLines(this string text) => Regex.Split(text, "\r\n|\r|\n");

        public static StringCollection ToCollection(this IEnumerable<string> t)
        {
            var c = new StringCollection();
            c.AddRange(t.ToArray());
            return c;
        }

        public static List<T> Reverse<T>(this List<T> t)
        {
            t.Reverse();
            return t;
        }

        public static bool IsEmpty<T>(this List<T> t)
        {
            return t != null && t.Count == 0;
        }
    }
}
