using PropertyChanged;
using System.ComponentModel;
using static Components.DefaultSettings;

namespace Components
{
    [ImplementPropertyChanged]
    public class SettingViewModel : INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged = (sender, events) => { 
        
        };

        #region Checkable settings

        // For Start application on system startup.
        public bool SASS { get; set; } = StartOnSystemStartup;

        public bool PNS { get; set; } = PlayNotifySound;

        public int TCL { get; set; } = TotalClipLength;

        #endregion
    }
}
