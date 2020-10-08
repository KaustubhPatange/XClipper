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
        public bool isAuthNeeded { get; set; }
    }
}
