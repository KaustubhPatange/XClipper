using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

#nullable enable

namespace Components
{
    public class AuthManager
    {
        const string AuthorizationEndpoint = "https://accounts.google.com/o/oauth2/v2/auth";
        public string? ClientId { get; set; }
        public string? ClientSecret { get; set; }

        public delegate void SuccessEventHandler(AuthEventArgs args);
        public event SuccessEventHandler? SuccessEvent;

        public delegate void FailureEventHandler(ErrorEventArgs args);
        public event FailureEventHandler? FailureEvent;
        protected virtual void OnSuccess(Dictionary<string, string> jsonPairs)
        {
            SuccessEvent?.Invoke(new AuthEventArgs { JsonPairs = jsonPairs });
        }
        public virtual void OnFailure(string message)
        {
            FailureEvent?.Invoke(new ErrorEventArgs(new Exception(message)));
        }
        /// <summary>
        /// This method will set all required parameters need to be their for making OAuth2 call.
        /// </summary>
        /// <param name="ClientId"></param>
        /// <param name="ClientSecret"></param>
        public void SetFields(string ClientId, string ClientSecret)
        {
            this.ClientId = ClientId;
            this.ClientSecret = ClientSecret;
        }
        public void RemoveSubscribers()
        {
            FailureEvent = null;
            SuccessEvent = null;
        }
        public async Task SignInWithGoogle()
        {
            // Generates state and PKCE values.
            string state = randomDataBase64url(32);
            string code_verifier = randomDataBase64url(32);
            string code_challenge = base64urlencodeNoPadding(sha256(code_verifier));
            const string code_challenge_method = "S256";

            // Creates a redirect URI using an available port on the loopback address.
            string redirectURI = $"http://{IPAddress.Loopback}:{GetRandomUnusedPort()}/";

            // Creates an HttpListener to listen for requests on that redirect URI.
            var http = new HttpListener();
            http.Prefixes.Add(redirectURI);

            http.Start();

            // Creates the OAuth 2.0 authorization request.
            string authorizationRequest = $"{AuthorizationEndpoint}?response_type=code&scope=https://www.googleapis.com/auth/firebase.database%20https://www.googleapis.com/auth/userinfo.email&redirect_uri={System.Uri.EscapeDataString(redirectURI)}&client_id={this.ClientId}&state={state}&code_challenge={code_challenge}&code_challenge_method={code_challenge_method}";

            // Opens request in the browser.
            System.Diagnostics.Process.Start(authorizationRequest);

            // Waits for the OAuth authorization response.
            var context = await http.GetContextAsync();

            // Sends an HTTP response to the browser.
            var response = context.Response;
            string responseString = string.Format("<html><head><meta http-equiv='refresh' content='10;url=https://kaustubhpatange.github.io/XClipper'></head><body>Please return to the app.</body></html>");
            var buffer = Encoding.UTF8.GetBytes(responseString);
            response.ContentLength64 = buffer.Length;
            var responseOutput = response.OutputStream;
            Task responseTask = responseOutput.WriteAsync(buffer, 0, buffer.Length).ContinueWith((task) =>
            {
                responseOutput.Close();
                http.Stop();
                Console.WriteLine("HTTP server stopped.");
            }, TaskScheduler.FromCurrentSynchronizationContext());

            // Checks for errors.
            if (context.Request.QueryString.Get("error") != null)
            {
                OnFailure($"OAuth authorization error: {context.Request.QueryString.Get("error")}.");
                return;
            }
            if (context.Request.QueryString.Get("code") == null
                || context.Request.QueryString.Get("state") == null)
            {
                OnFailure("Malformed authorization response. " + context.Request.QueryString);
                return;
            }

            // extracts the code
            var code = context.Request.QueryString.Get("code");
            var incoming_state = context.Request.QueryString.Get("state");

            // Compares the received state to the expected value, to ensure that
            // this app made the request which resulted in authorization.
            if (incoming_state != state)
            {
                OnFailure($"Received request with invalid state ({incoming_state})");
                return;
            }
            // output("Authorization code: " + code);

            // Starts the code exchange at the Token Endpoint.
            performCodeExchange(code, code_verifier, redirectURI);
        }
        
        private async void performCodeExchange(string code, string code_verifier, string redirectURI)
        {
            // builds the  request
            string tokenRequestURI = "https://www.googleapis.com/oauth2/v4/token";
            string tokenRequestBody = $"code={code}&redirect_uri={System.Uri.EscapeDataString(redirectURI)}&client_id={ClientId}&code_verifier={code_verifier}&client_secret={ClientSecret}&scope=&grant_type=authorization_code";

            // sends the request
            HttpWebRequest tokenRequest = (HttpWebRequest)WebRequest.Create(tokenRequestURI);
            tokenRequest.Method = "POST";
            tokenRequest.ContentType = "application/x-www-form-urlencoded";
            tokenRequest.Accept = "Accept=text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
            byte[] _byteVersion = Encoding.ASCII.GetBytes(tokenRequestBody);
            tokenRequest.ContentLength = _byteVersion.Length;
            Stream stream = tokenRequest.GetRequestStream();
            await stream.WriteAsync(_byteVersion, 0, _byteVersion.Length);
            stream.Close();

            try
            {
                // gets the response
                WebResponse tokenResponse = await tokenRequest.GetResponseAsync();
                using (StreamReader reader = new StreamReader(tokenResponse.GetResponseStream()))
                {
                    // reads response body
                    string responseText = await reader.ReadToEndAsync();

                    // converts to dictionary
                    Dictionary<string, string> tokenEndpointDecoded = JsonConvert.DeserializeObject<Dictionary<string, string>>(responseText);

                    // Handle when everything works fine
                    OnSuccess(tokenEndpointDecoded);
                    return;
                }
            }
            catch (WebException ex)
            {
                if (ex.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = ex.Response as HttpWebResponse;
                    if (response != null)
                    {
                        using (StreamReader reader = new StreamReader(response.GetResponseStream()))
                        {
                            // reads response body
                            string responseText = await reader.ReadToEndAsync().ConfigureAwait(true);
                            OnFailure("Error HTTP: " + response.StatusCode);
                            return;
                        }
                    }
                }
            }

            OnFailure("Authentication failed due to unknown error.");
        }

        // ref http://stackoverflow.com/a/3978040
        private static int GetRandomUnusedPort()
        {
            var listener = new TcpListener(IPAddress.Loopback, 0);
            listener.Start();
            var port = ((IPEndPoint)listener.LocalEndpoint).Port;
            listener.Stop();
            return port;
        }

        /// <summary>
        /// Returns URI-safe data with a given input length.
        /// </summary>
        /// <param name="length">Input length (nb. output will be longer)</param>
        /// <returns></returns>
        private static string randomDataBase64url(uint length)
        {
            RNGCryptoServiceProvider rng = new RNGCryptoServiceProvider();
            byte[] bytes = new byte[length];
            rng.GetBytes(bytes);
            return base64urlencodeNoPadding(bytes);
        }

        /// <summary>
        /// Returns the SHA256 hash of the input string.
        /// </summary>
        /// <param name="inputStirng"></param>
        /// <returns></returns>
        private byte[] sha256(string inputStirng)
        {
            byte[] bytes = Encoding.ASCII.GetBytes(inputStirng);
            SHA256Managed sha256 = new SHA256Managed();
            return sha256.ComputeHash(bytes);
        }

        /// <summary>
        /// Base64url no-padding encodes the given input buffer.
        /// </summary>
        /// <param name="buffer"></param>
        /// <returns></returns>
        private static string base64urlencodeNoPadding(byte[] buffer)
        {
            string base64 = Convert.ToBase64String(buffer);

            // Converts base64 to base64url.
            base64 = base64.Replace("+", "-");
            base64 = base64.Replace("/", "_");
            // Strips padding.
            base64 = base64.Replace("=", "");

            return base64;
        }
    }
}
