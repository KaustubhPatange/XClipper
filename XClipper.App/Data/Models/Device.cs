using System.Xml.Linq;

namespace Components
{
    public class Device
    {
        public string id { get; set; }
        public int sdk { get; set; } = 0;
        public string model { get; set; }

        public bool IsValid()
        {
            return (id != null && model != null);
        }

        public static XElement ToNode(Device t)
        {
            var node = new XElement(nameof(Device));
            node.Add(
                new XElement(nameof(t.id), t.id),
                new XElement(nameof(t.sdk), t.sdk.ToString()),
                new XElement(nameof(t.model), t.model)
            );
            return node;
        }
        public static Device FromNode(XElement t)
        {
            var model = new Device();
            model.id = t.Element(nameof(id)).Value;
            model.model = t.Element(nameof(model)).Value;
            model.sdk = t.Element(nameof(sdk)).Value.ToInt();
            return model;
        }

        public override bool Equals(object obj)
        {
            if (obj is Device)
            {
                var other = (Device)obj;
                if (this == other) return true;
                return (this.id == other.id && this.model == other.model && this.sdk == other.sdk);
            }
            return false;
        }
    }
}
