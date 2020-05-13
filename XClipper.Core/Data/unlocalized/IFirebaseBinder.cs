using FireSharp.EventStreaming;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Components
{
    public interface IFirebaseBinder
    {
        void OnDataAdded(ValueAddedEventArgs e);
        void OnDataChanged(ValueChangedEventArgs e);
        void OnDataRemoved(ValueRemovedEventArgs e);
    }
}
