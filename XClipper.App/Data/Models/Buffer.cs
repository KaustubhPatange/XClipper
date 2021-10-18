using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Windows.Forms;
using System.Xml.Linq;

namespace Components
{
    public class Buffer : INotifyPropertyChanged, IEquatable<Buffer>
    {
        public Buffer()
        {
            Copy.PropertyChanged += (o, e) => PropertyChanged?.Invoke(o, e);
            Paste.PropertyChanged += (o, e) => PropertyChanged?.Invoke(o, e);
            Cut.PropertyChanged += (o, e) => PropertyChanged?.Invoke(o, e);
        }
        public event PropertyChangedEventHandler PropertyChanged;
        public Keymap Copy { get; set; } = new();
        public Keymap Paste { get; set; } = new();
        public Keymap Cut { get; set; } = new();
        public bool PlaySound { get; set; }
        public string Data { get; set; }

        public static XElement ToNode(Buffer t)
        {
            var node = new XElement(nameof(Buffer));
            node.Add(new XElement(nameof(PlaySound), t.PlaySound));
            node.Add(new XElement(nameof(Data), t.Data));
            node.Add(Keymap.ToNode(t.Copy, nameof(Copy)));
            node.Add(Keymap.ToNode(t.Paste, nameof(Paste)));
            node.Add(Keymap.ToNode(t.Cut, nameof(Cut)));
            return node;
        }

        public static Buffer FromNode(XElement t)
        {
            var buff = new Buffer();
            buff.Data = t.Element(nameof(Data)).Value;
            buff.PlaySound = t.Element(nameof(PlaySound)).Value.ToBool();

            // We will mutate the existing values to make sure INotifyPropertyChanged is invoked perfectly.
            var co = Keymap.FromNode(t.Element(nameof(Copy)));
            var pa = Keymap.FromNode(t.Element(nameof(Paste)));
            var cu = Keymap.FromNode(t.Element(nameof(Cut)));

            buff.Copy.HotKey = co.HotKey;
            buff.Copy.IsAlt = co.IsAlt;
            buff.Copy.IsCtrl = co.IsCtrl;
            buff.Copy.IsShift = co.IsShift;
            
            buff.Paste.HotKey = pa.HotKey;
            buff.Paste.IsAlt = pa.IsAlt;
            buff.Paste.IsCtrl = pa.IsCtrl;
            buff.Paste.IsShift = pa.IsShift;
            
            buff.Cut.HotKey = cu.HotKey;
            buff.Cut.IsAlt = cu.IsAlt;
            buff.Cut.IsCtrl = cu.IsCtrl;
            buff.Cut.IsShift = cu.IsShift;
            
            return buff;
        }

        public override bool Equals(object obj) => obj is Buffer buffer && Copy.Equals(buffer.Copy) && Paste.Equals(buffer.Paste) && Cut.Equals(buffer.Cut) && PlaySound == buffer.PlaySound && Data == buffer.Data;

        public override int GetHashCode()
        {
            int hashCode = 222168374;
            hashCode = hashCode * -1521134295 + EqualityComparer<Keymap>.Default.GetHashCode(Copy);
            hashCode = hashCode * -1521134295 + EqualityComparer<Keymap>.Default.GetHashCode(Paste);
            hashCode = hashCode * -1521134295 + EqualityComparer<Keymap>.Default.GetHashCode(Cut);
            hashCode = hashCode * -1521134295 + PlaySound.GetHashCode();
            hashCode = hashCode * -1521134295 + EqualityComparer<string>.Default.GetHashCode(Data);
            return hashCode;
        }

        public bool Equals(Buffer other) => Equals(other as object);
    }

    public class Keymap : INotifyPropertyChanged, IEquatable<Keymap>
    {
        public event PropertyChangedEventHandler PropertyChanged;
        public bool IsAlt { get; set; }
        public bool IsCtrl { get; set; }
        public bool IsShift { get; set; }
        public string HotKey { get; set; }

        public static XElement ToNode(Keymap t, string title)
        {
            var node = new XElement(title);
            node.Add(new XElement(nameof(IsAlt), t.IsAlt));
            node.Add(new XElement(nameof(IsCtrl), t.IsCtrl));
            node.Add(new XElement(nameof(IsShift), t.IsShift));
            node.Add(new XElement(nameof(HotKey), t.HotKey));
            return node;
        }

        public static Keymap FromNode(XElement t)
        {
            var keymap = new Keymap();
            keymap.HotKey = t.Element(nameof(HotKey)).Value;
            keymap.IsAlt = t.Element(nameof(IsAlt)).Value.ToBool();
            keymap.IsCtrl = t.Element(nameof(IsCtrl)).Value.ToBool();
            keymap.IsShift = t.Element(nameof(IsShift)).Value.ToBool();
            return keymap;
        }

        public override bool Equals(object obj) => obj is Keymap keymap && IsAlt == keymap.IsAlt && IsCtrl == keymap.IsCtrl && IsShift == keymap.IsShift && HotKey == keymap.HotKey;

        public override int GetHashCode()
        {
            int hashCode = 1609328514;
            hashCode = hashCode * -1521134295 + IsAlt.GetHashCode();
            hashCode = hashCode * -1521134295 + IsCtrl.GetHashCode();
            hashCode = hashCode * -1521134295 + IsShift.GetHashCode();
            hashCode = hashCode * -1521134295 + EqualityComparer<string>.Default.GetHashCode(HotKey);
            return hashCode;
        }

        public bool Equals(Keymap other) => Equals(other as object);
    }
}
