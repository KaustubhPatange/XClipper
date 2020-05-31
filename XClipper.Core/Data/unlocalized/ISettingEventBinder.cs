using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Components
{
    public interface ISettingEventBinder
    {
        void OnBuyButtonClicked();
        void OnConnectedDeviceClicked();
    }
}
