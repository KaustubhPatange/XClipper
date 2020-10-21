#nullable enable

namespace Components
{
    public class OAuth
    {
        public string ClientId { get; set; }
        public string? ClientSecret { get; set; }

        public override bool Equals(object obj)
        {
            if (obj != null)
                if (obj is OAuth)
                {
                    var other = (OAuth)obj;
                    if (this == other) return true;
                    return (ClientId == other.ClientId && ClientSecret == other.ClientSecret);
                }
            return false;
        }
    }
}
