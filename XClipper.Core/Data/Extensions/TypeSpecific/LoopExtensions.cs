using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Components
{
    public static class LoopExtensions
    {
        public static void ForEach(this string[] vals, Action<string> action)
        {
            foreach(var val in vals)
                action(val);
        }

        public static List<T> Futher<T> (this List<T> t, Action<T> block)
        {
            foreach (T s in t) block.Invoke(s);
            return t;
        }
    }
}
