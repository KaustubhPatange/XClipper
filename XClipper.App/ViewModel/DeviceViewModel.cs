using GalaSoft.MvvmLight.Command;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Windows.Input;
using System.Windows.Threading;
using static Components.MainHelper;

namespace Components
{
    public class DeviceViewModel : BaseViewModel
    {
        public DeviceViewModel()
        {
            RemoveCommand = new RelayCommand(DisconnectDevice);

            LoadDeviceData();
        }

        public DeviceViewModel SetDeviceList(List<Device> devices)
        {
            Devices = devices;
            return this;
        }

        #region Bindings

        public bool ShowProgress { get; private set; } = false;
        public ICommand RemoveCommand { get; set; }
        public List<Device> Devices { get; set; }

        #endregion
        
        
        #region Methods

        /// <summary>
        /// This method will request connected device list and bind it.
        /// </summary>
        private void LoadDeviceData()
        {
            ShowProgress = true;

            Task.Run(async () =>
            {
                var devices = await FirebaseSingleton.GetInstance.GetDeviceListAsync();

                RunOnMainThread(() =>
                {
                    Devices = devices;
                    ShowProgress = false;
                });
            });
        }

        /// <summary>
        /// This will invoke whenever disconnect button is clicked.
        /// </summary>
        private void DisconnectDevice()
        {
           
        } 

        #endregion
    }
}
