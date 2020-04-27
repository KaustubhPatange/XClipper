using System.IO;

namespace Components
{
    public static class PathHelper
    {
        public static string GetTemporaryPath()
        {
            var tmp = Path.GetTempFileName();
            File.Delete(tmp);
            Directory.CreateDirectory(tmp);
            return tmp;
        }
    }
}
