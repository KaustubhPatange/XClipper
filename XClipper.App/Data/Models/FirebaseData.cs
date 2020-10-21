namespace Components
{
    public class FirebaseData
    {
        private string _endpoint;
        public OAuth DesktopAuth { get; set; }
        public OAuth MobileAuth { get; set; }
        public string Endpoint
        {
            get { return _endpoint; }
            set
            {
                _endpoint = value;
                Storage = _endpoint.Replace("firebaseio.com/", "appspot.com").Replace("https://", "");
            }
        }
        public string AppId { get; set; }
        public string ApiKey { get; set; }
        public string Storage { get; set; }
        public bool IsAuthNeeded { get; set; } = false;
        public bool IsEncrypted { get; set; } = false;

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
