using System.Runtime.CompilerServices;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Xml.Linq;

namespace Components
{
    public class Clip
    {
        public string data { get; set; }
        public string time { get; set; }

        public bool IsValid()
        {
            return (data != null && time != null);
        }

        public static XElement ToNode(Clip t)
        {
            var node = new XElement(nameof(Clip));
            node.Add(new XElement(nameof(data), t.data));
            node.Add(new XElement(nameof(time), t.time));
            return node;
        }

        public static Clip FromNode(XElement t)
        {
            var model = new Clip();
            model.data = t.Element(nameof(model.data)).Value;
            model.time = t.Element(nameof(model.time)).Value;
            return model;
        }
    }
}
