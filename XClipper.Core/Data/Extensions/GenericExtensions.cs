using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Components
{
    /** More of a kotlin guy so I need this... */
    public static class GenericExtensions
    {
        public static T Also<T>(this T t, Action<T> block)
        {
            block.Invoke(t);
            return t;
        }
    }
}
