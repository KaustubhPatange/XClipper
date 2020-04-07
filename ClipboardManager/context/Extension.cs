using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClipboardManager.context
{
    public static class Extension
    {
        public static string ToFormattedDateTime(this DateTime value)
        {
            //2020-04-01 225308
            return value.ToString("yyyy-MM-dd HH-mm-ss");
        }
    }
}
