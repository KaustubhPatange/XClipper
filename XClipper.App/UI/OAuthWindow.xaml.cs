using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using static Components.TranslationHelper;
using static Components.FirebaseHelper;

namespace Components.UI
{
    public partial class OAuthWindow : Window
    {
        private AuthManager authManager = new AuthManager();
        public OAuthWindow(string ClientId, string ClientSecret)
        {
            InitializeComponent();
            authManager.SetFields(ClientId, ClientSecret);
            authManager.SuccessEvent += AuthManager_SuccessEvent;
            authManager.FailureEvent += AuthManager_FailureEvent;
        }

        private void AuthManager_SuccessEvent(AuthEventArgs args)
        {
            string access_token = args.JsonPairs["access_token"];
            string refresh_token = args.JsonPairs["refresh_token"];
            HandleTokenDetails(access_token, refresh_token);
            DialogResult = true;
            Close();
        }

        private void AuthManager_FailureEvent(ErrorEventArgs args)
        {
            MessageBox.Show(args.GetException().Message, Translation.MSG_ERR, MessageBoxButton.OK, MessageBoxImage.Error);
            DialogResult = false;
            Close();
        }

        private async void GoogleSignIn_Clicked(object sender, RoutedEventArgs e)
        {
            _progressBar.Visible();
            await authManager.SignInWithGoogle().ConfigureAwait(false);
        }
    }  
}
