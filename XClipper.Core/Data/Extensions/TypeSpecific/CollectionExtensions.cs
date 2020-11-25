using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Linq;
using System.Text.RegularExpressions;

#nullable enable

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

        public static List<T> ToNotNullList<T>(this IEnumerable<T>? t)
        {
            return t.ToList() ?? new List<T>();
        }

        public static T Pop<T>(this List<T> t)
        {
            T item = t[t.Count - 1];
            t.RemoveAt(t.Count - 1);
            return item;
        }

        public static KeyValuePair<T, R> Pop<T, R>(this Dictionary<T, R> t)
        {
            var item = t.LastOrDefault();
            t.Remove(item.Key);
            return item;
        }

        public static IEnumerable<TSource> DistinctBy<TSource, TKey>(this IEnumerable<TSource> source, Func<TSource, TKey> keySelector)
        {
            HashSet<TKey> seenKeys = new HashSet<TKey>();
            foreach (TSource element in source)
            {
                if (seenKeys.Add(keySelector(element)))
                {
                    yield return element;
                }
            }
        }

        /// <summary>
        /// This will check if items are equal by using Equals() which can then 
        /// be used for checking content equality
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="first"></param>
        /// <param name="second"></param>
        /// <returns></returns>
        public static IEnumerable<T> ExceptEquals<T>(this IEnumerable<T> first, IEnumerable<T> second)
        {
            foreach (var item in first)
            {
                bool isExist = false;
                foreach (var item2 in second)
                {
                    if (item.Equals(item2))
                        isExist = true;
                }
                if (!isExist)
                    yield return item;
            }
        }

        /// <summary>
        /// Note: For non null types like int, bool IsNull will return false if the value is null.
        /// As the default(T) would make int type to 0 and bool as false.
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="t"></param>
        /// <param name="predicate"></param>
        /// <returns></returns>
        public static Result<T> FirstOrNull<T>(this IEnumerable<T> t, Func<T, bool> predicate)
        {
            foreach(var item in t)
            {
                if (predicate.Invoke(item))
                    return new Result<T> { Value = item};
            }
            return new Result<T>();
        }

        public static bool IsEmpty<T>(this IEnumerable<T> t)
        {
            return !t.Any();
        }

        public static bool IsNotEmpty<T>(this IEnumerable<T> t)
        {
            return !t.IsEmpty();
        }
    }

    public struct Result<T>
    {
        public T Value { get; set; }
        public bool IsNull() => Value == null;
    }
}
