namespace Components
{
    public class Update
    {
        public Windows Desktop { get; set; }
        public Android Mobile { get; set; }
        public class Android
        {
            public string Version { get; set; }
        }
        public class Windows
        {
            /// <summary>
            /// In the format of 1.0.0.0
            /// </summary>
            public string Version { get; set; }
            public string Changelog { get; set; }
            public string PostDate { get; set; }
            public string DownloadUri { get; set; }
            public long FileSize { get; set; }
        }
    }
}
