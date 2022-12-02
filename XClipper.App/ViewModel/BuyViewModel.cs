using System.IO;
using static Components.LicenseHandler;
using static Components.Constants;
using System.Windows.Input;
using GalaSoft.MvvmLight.Command;
using static Components.DefaultSettings;
using System.Windows;
using static Components.MainHelper;
using static Components.Core;
using RestSharp;
using Autofac;
using Newtonsoft.Json.Linq;
using System.Windows.Controls;
using System;

namespace Components
{
    public class BuyViewModel : BaseViewModel
    {
        #region Constructor

        private IBuyEventBinder binder;
        public BuyViewModel(IBuyEventBinder binder)
        {
            this.binder = binder;

            VerifyCommand = new RelayCommand(VerificationMethod);
            ActivateCommand = new RelayCommand(ActivationMethod);
            MigrateCommand = new RelayCommand(MigrationMethod);
        }

        #endregion

        #region Actual Bindings

        public LicenseType LT { get; set; } = LicenseType.Invalid;
        public ICommand VerifyCommand { get; set; }
        public ICommand ActivateCommand { get; set; }
        public ICommand MigrateCommand { get; set; }
        public string UID { get; private set; } = UniqueID;
        public string TI { get; set; }
        public string EM { get; set; }
        /// <summary>
        /// This Transaction Id is for migration tab.
        /// </summary>
        public string MTI { get; set; }
        public DateTime DOP { get; set; } = DateTime.Today;
        public int PC { get; set; } = 0;
        public string KEY { get; set; }
        public bool IsProgressiveWork { get; set; } = false;


        #endregion

        #region Method Events

        /// <summary>
        /// Suppose you reinstall windows or somehow your UID changes, XClipper will not be able to activate your license.
        /// In such case you can use this tool migrate existing UID with the current one. 
        /// </summary>
        private async void MigrationMethod()
        {
            if (string.IsNullOrWhiteSpace(MTI) || string.IsNullOrWhiteSpace(UID))
            {
                MsgBoxHelper.ShowWarning(Translation.MSG_FIELD_EMPTY);
                return;
            }
            IsProgressiveWork = true;

            var client = new RestClient(MIGRATION_SERVER(UID, MTI, DOP.ToFormattedDate()));
            var response = await client.ExecuteTaskAsync(new RestRequest(Method.POST).AddHeaders(COMMON_HEADERS())).ConfigureAwait(true);
            if (response.StatusCode != System.Net.HttpStatusCode.NotFound)
            {
                var obj = JObject.Parse(response.Content);
                if (response.StatusCode == System.Net.HttpStatusCode.OK)
                {
                    switch (obj["status"].ToString())
                    {
                        case "success":
                            //MsgBoxHelper.ShowInfo(obj["message"].ToString());
                            VerificationMethod();
                            break;
                    }
                }
                else
                    MsgBoxHelper.ShowError(obj["message"].ToString());
            }
            else
                MsgBoxHelper.ShowError(Translation.MSG_UNKNOWN_ERR);

            IsProgressiveWork = false;
        }

        /// <summary>
        /// Verification method is same as License Checker from <see cref="ILicense.Initiate(System.Action{System.Exception?})"/><br/>
        /// We are creating a loading progress when we are validating.
        /// </summary>
        private void VerificationMethod()
        {
            var recorder = AppModule.Container.Resolve<ILicense>();
            recorder.Initiate((e) =>
            {
                if (e == null)
                    MsgBoxHelper.ShowInfo(Translation.MSG_LICENSE_CHECK);
                else 
                    MsgBoxHelper.ShowError(Translation.MSG_LICENSE_CHECK_ERR);
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
                MsgBoxHelper.ShowWarning(Translation.MSG_FIELD_EMPTY);
                return;
            }
            IsProgressiveWork = true;

            var client = new RestClient(ACTIVATION_SERVER(UID, EM, TI, LT.GetName()));
            var response = await client.ExecuteTaskAsync(new RestRequest(Method.POST).AddHeaders(COMMON_HEADERS())).ConfigureAwait(true);
            try {
                var obj = JObject.Parse(response.Content);
                if (response.StatusCode == System.Net.HttpStatusCode.OK)
                {
                    switch (obj["status"].ToString())
                    {
                        case "success":
                            MsgBoxHelper.ShowInfo(Translation.BUY_LICENSE_SUCCESS);
                            binder.OnLicenseActivationSucceed();
                            break;
                        case "exist":
                            MsgBoxHelper.ShowInfo(Translation.BUY_LICENSE_EXIST);
                            break;
                    }
                }
                else
                    MsgBoxHelper.ShowError(obj["message"].ToString());
            } catch(Exception e)
            {
                MsgBoxHelper.ShowError(Translation.MSG_UNKNOWN_ERR);
            }

            IsProgressiveWork = false;
        }

        #endregion
    }
}
