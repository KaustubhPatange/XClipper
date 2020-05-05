using Components.viewModels;
using QRCoder;
using System;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Windows;
using System.Windows.Input;
using System.Windows.Interop;
using static Components.MainHelper;

namespace Components
{
    public partial class QRWindow : Window
    {
        #region Variable Declaration

        /// <summary>
        /// This variable determines if focus is give to this window.
        /// </summary>
        private bool isFocus;
        private bool FirstActivate;

        #endregion

        #region Constructor

        public QRWindow()
        {
            InitializeComponent();

            double X = 0, Y = 0;

            CalculateXY(ref X, ref Y, this);

            this.Left = X;
            this.Top = Y;

            Deactivated += FilterWindow_Deactivated;
            Activated += QRWindow_Activated;
        }

        private void QRWindow_Activated(object sender, EventArgs e)
        {
            if (FirstActivate)
                isFocus = true;
            FirstActivate = true;
        }

        #endregion

        #region UI Events

        private void FilterWindow_Deactivated(object sender, EventArgs e)
        {
            if (isFocus)
                AppSingleton.GetInstance.MakeExitRequest();
        }
        private void QRWindow_GotFocus(object sender, RoutedEventArgs e)
        {
            isFocus = true;
        }
        private void CloseButton_Clicked(object sender, RoutedEventArgs e)
        {
            CloseWindow();
        }
        private void Window_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Escape)
                CloseWindow();
        }

        #endregion

        #region Methods

        public void SetUp(string Text)
        {
            var qrGenerator = new QRCodeGenerator();
            var qrCodeData = qrGenerator.CreateQrCode(Text, QRCodeGenerator.ECCLevel.L);
            var qrCode = new QRCode(qrCodeData);
            _imgQR.Source = qrCode.GetGraphic(20).ToImageSource();
            FirstActivate = false;
            
        }

        public void CloseWindow()
        {
            isFocus = false;
            Hide();
        }

        #endregion
    }
}
