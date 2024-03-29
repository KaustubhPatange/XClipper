﻿using GalaSoft.MvvmLight.Command;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Windows.Input;
using System.Windows.Threading;
using static Components.MainHelper;

#nullable enable

namespace Components
{
    public class DeviceViewModel : BaseViewModel
    {
        public DeviceViewModel()
        {
            RemoveCommand = new RelayCommand(DisconnectDevice);

            LoadDeviceData();

            FirebaseSingletonV2.AddDeviceChangeListener(UpdateDeviceList);
        }

        #region Bindings

        public bool ButtonEnabled { get; private set; } = false;
        public bool ShowProgress { get; private set; } = false;
        public ICommand RemoveCommand { get; set; }
        public List<Device> Devices { get; set; }
        public int SelectedIndex { get; set; } = -1;

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
                var devices = await FirebaseSingletonV2.GetInstance.GetDeviceListAsync().ConfigureAwait(false);

                RunOnMainThread(() =>
                {
                    UpdateDeviceList(devices);
                });
            });
        }

        private void UpdateDeviceList(List<Device> devices)
        {
            Devices = devices;
            ShowProgress = false;
            if (devices?.Count > 0)
            {
                SelectedIndex = 0;
                ButtonEnabled = true;
            }
        }

        /// <summary>
        /// This will invoke whenever disconnect button is clicked.
        /// </summary>
        private void DisconnectDevice()
        {
            if (SelectedIndex == -1) return;

            ShowProgress = true;
            ButtonEnabled = false;

            Task.Run(async () => 
            { 
                Devices = await FirebaseSingletonV2.GetInstance.RemoveDevice(Devices[SelectedIndex].id).ConfigureAwait(false);
                ShowProgress = false;
                ButtonEnabled = true;
            });
        } 

        #endregion
    }
}
