namespace Components
{
    public class FirebaseData
    {
        private string _storage = null;
        public OAuth DesktopAuth { get; set; }
        public OAuth MobileAuth { get; set; }
        public string Endpoint { get; set; }
        public string AppId { get; set; }
        public string ApiKey { get; set; }
        public string Storage 
        {
            get 
            {
                // Memoize
                if (_storage == null)
                    _storage = Endpoint.Replace("firebaseio.com/", "appspot.com").Replace("https://", "");
                return _storage;
            }
            set { _storage = value; }
        }
        public bool IsAuthNeeded { get; set; } = false;
        public bool IsEncrypted { get; set; } = true;

        public override bool Equals(object obj)
        {
            if (obj != null)
                if (obj is FirebaseData)
                {
                    var other = (FirebaseData)obj;
                    if (this == other) return true;
                    return (ApiKey == other.ApiKey && AppId == other.AppId && Storage == other.Storage && IsAuthNeeded == other.IsAuthNeeded && Endpoint == other.Endpoint
                            && DesktopAuth.Equals(other.DesktopAuth) && MobileAuth.Equals(other.MobileAuth)
                        );
                }
            return false;
        }
    }
}
