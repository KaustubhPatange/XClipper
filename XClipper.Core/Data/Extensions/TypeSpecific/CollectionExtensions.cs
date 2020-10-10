﻿using System;
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
    }
}
