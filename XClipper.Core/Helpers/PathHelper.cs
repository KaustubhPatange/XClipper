using System.IO;

namespace Components
{
    public static class PathHelper
    {
        /// <summary>
        /// This will create and return the path of an empty temporary directory.
        /// </summary>
        /// <returns></returns>
        public static string GetTemporaryPath()
        {
            var tmp = Path.GetTempFileName();
            File.Delete(tmp);
            Directory.CreateDirectory(tmp);
            return tmp;
        }

        /// <summary>
        /// Get the name from the path. <br/>
        /// Eg: C:\users\devel\Desktop will return "Desktop"
        /// </summary>
        /// <returns></returns>
        public static string GetName(string path)
        {
            return path.Substring(path.LastIndexOf('\\') + 1);
        }
    }
}
