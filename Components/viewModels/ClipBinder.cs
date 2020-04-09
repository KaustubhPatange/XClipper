using ClipboardManager.models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Components.viewModels
{
    public interface ClipBinder
    {
        void OnUpdate(List<TableCopy> models);
    }
}
