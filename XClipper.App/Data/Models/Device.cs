using System.Xml.Linq;

namespace Components
{
    public class Device
    {
        public string id { get; set; }
        public int sdk { get; set; }
        public string model { get; set; }

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
    }
}
