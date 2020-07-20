namespace Components
{
    public static class NetworkUtility
    {
        [System.Runtime.InteropServices.DllImport("wininet.dll")]
        private extern static bool InternetGetConnectedState(out int Description, int ReservedValue);
        /// <summary>
        /// Method uses wininet.dll assembly to determine active Internet connection.
        /// </summary>
        /// <returns>True if connection is active</returns>
        public static bool CheckForActiveConnection()
        {
            int desc;
            return InternetGetConnectedState(out desc, 0);
        }
    }
}
