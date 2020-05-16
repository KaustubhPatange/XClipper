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

        #region Constructor

        public QRWindow()
        {
            InitializeComponent();

            double X = 0, Y = 0;

            CalculateXY(ref X, ref Y, this);

            this.Left = X;
            this.Top = Y;

        }

      

        #endregion

        #region UI Events

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
        }

        public void CloseWindow()
        {
            Hide();
        }

        #endregion
    }
}
