﻿using QRCoder;
using System;
using System.Globalization;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Interop;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using static Components.DefaultSettings;

namespace Components
{
    [ValueConversion(typeof(string), typeof(ImageSource))]
    public class ImageQRConverter : IValueConverter
    {
        public static ImageQRConverter Instance = new ImageQRConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (value == null)
            {
                return new BitmapImage(new Uri("pack://application:,,,/Resources/error.png", UriKind.Absolute));
            }
            var qrCodeData = value as QRCodeData;
            var valueToConvert = qrCodeData.UID + ";" + qrCodeData.EncryptedData;
            var qrData = new QRCodeGenerator().CreateQrCode(valueToConvert, QRCodeGenerator.ECCLevel.L);
            var image = new QRCode(qrData).GetGraphic(20, System.Drawing.Color.Black, System.Drawing.Color.White, true);

            return Imaging.CreateBitmapSourceFromHBitmap(image.GetHbitmap(),
                IntPtr.Zero, Int32Rect.Empty, BitmapSizeOptions.FromWidthAndHeight(image.Width, image.Height));
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return Binding.DoNothing;
        }
    }
}
