using System.IO;

namespace Components
{
    public static class PathHelper
    {
        /// <summary>
        /// Thiis will create and return the path of an empty temporary directory.
        /// </summary>
        /// <returns></returns>
        public static string GetTemporaryPath()
        {
            var tmp = Path.GetTempFileName();
            File.Delete(tmp);
            Directory.CreateDirectory(tmp);
            return tmp;
        }
    }
}
