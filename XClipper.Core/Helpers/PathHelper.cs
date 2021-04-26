using System.IO;
using System.Security.Cryptography;
using System.Text;

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

        /// <summary>
        /// Returns the MD5 hash for the file.
        /// </summary>
        public static string GetMD5(string fileName)
        {
            using (var md5 = MD5.Create())
            {
                using (var stream = File.OpenRead(fileName))
                {
                    StringBuilder sb = new();
                    byte[] hash = md5.ComputeHash(stream);
                    foreach (byte bt in hash) {
                        sb.Append(bt.ToString("x2"));
                    }
                    return sb.ToString();
                }
            }
        }
    }
}
