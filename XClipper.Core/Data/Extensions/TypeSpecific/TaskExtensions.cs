using System.Threading.Tasks;

namespace Components
{
    public static class TaskExtensions
    {
        /// <summary>
        /// Code will be run on different thread just like fire &#38; forget.
        /// <code> 
        /// Task.Run(async () => await t.ConfigureAwait(false));
        /// </code>
        /// </summary>
        /// <param name="t"></param>
        public static void RunAsync(this Task t)
        {
            Task.Run(async () => await t.ConfigureAwait(false));
        }
    }
}
