using System.IO;
using static Components.LicenseHandler;
using static Components.Constants;
using System.Windows.Input;
using GalaSoft.MvvmLight.Command;
using static Components.DefaultSettings;
using System.Windows;
using static Components.TranslationHelper;
using static Components.MainHelper;
using static Components.Core;
using RestSharp;
using Autofac;
using Newtonsoft.Json.Linq;

namespace Components
{
    public class BuyViewModel : BaseViewModel
    {
        #region Constructor

        public BuyViewModel()
        {
            VerifyCommand = new RelayCommand(VerificationMethod);
            ActivateCommand = new RelayCommand(ActivationMethod);
        }

        #endregion

        #region Actual Bindings

        public LicenseType LT { get; set; } = LicenseType.Standard;
        public ICommand VerifyCommand { get; set; }
        public ICommand ActivateCommand { get; set; }
        public string UID { get; private set; } = UniqueID;
        public string TI { get; set; }
        public int PC { get; set; } = 0;
        public string KEY { get; set; }
        public bool IsProgressiveWork { get; set; } = false;

        #endregion

        #region Method Events

        /// <summary>
        /// Verification method is same as License Checker from <see cref="ILicense.Initiate(System.Action{System.Exception?})"/><br/>
        /// We are creating a loading progress when we are validating.
        /// </summary>
        private void VerificationMethod()
        {
            var recorder = AppModule.Container.Resolve<ILicense>();
            recorder.Initiate((e) =>
            {
                MessageBox.Show(Translation.MSG_LICENSE_CHECK, Translation.MSG_INFO, MessageBoxButton.OK, MessageBoxImage.Information);
            });
        }

        /// <summary>
        /// If for some reason website licensing fails, user can use this tool to activate
        /// their license.
        /// </summary>
        private async void ActivationMethod()
        {
            if (string.IsNullOrWhiteSpace(TI) || string.IsNullOrWhiteSpace(UID) || LT == LicenseType.Invalid)
            {
                MessageBox.Show(Translation.MSG_FIELD_EMPTY, Translation.MSG_WARNING, MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }
            IsProgressiveWork = true;

            var client = new RestClient(ACTIVATION_SERVER(UID, TI, LT.GetName()));
            var response = await client.ExecuteTaskAsync(new RestRequest(Method.POST)).ConfigureAwait(true);
            if (response.StatusCode != System.Net.HttpStatusCode.NotFound)
            {
                var obj = JObject.Parse(response.Content);
                if (response.StatusCode == System.Net.HttpStatusCode.OK)
                {
                    switch (obj["status"].ToString())
                    {
                        case "success":
                            MessageBox.Show(Translation.BUY_LICENSE_SUCCESS, Translation.MSG_INFO, MessageBoxButton.OK, MessageBoxImage.Information);
                            VerificationMethod();
                            break;
                        case "exist":
                            MessageBox.Show(Translation.BUY_LICENSE_EXIST, Translation.MSG_INFO, MessageBoxButton.OK, MessageBoxImage.Information);
                            break;
                    }
                }
                else
                    MessageBox.Show(obj["message"].ToString(), Translation.MSG_ERR, MessageBoxButton.OK, MessageBoxImage.Error);
            }
            else
                MessageBox.Show(Translation.MSG_UNKNOWN_ERR, Translation.MSG_INFO, MessageBoxButton.OK, MessageBoxImage.Error);

            IsProgressiveWork = false;
        }

        #endregion
    }
}
