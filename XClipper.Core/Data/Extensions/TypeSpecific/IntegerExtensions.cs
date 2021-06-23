using System;

namespace Components
{
    public static class IntegerExtensions
    {
        public static int CoerceAtMost(this int value, int max)
        {
            return Math.Min(value, max);
        }
    }
}