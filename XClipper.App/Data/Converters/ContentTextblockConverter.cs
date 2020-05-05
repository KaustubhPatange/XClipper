﻿using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Data;
using static Components.App;
using static Components.TranslationHelper;

namespace Components
{
    [ValueConversion(typeof(bool), typeof(string))]
    public class ContentTextblockConverter : IValueConverter
    {
        public static ContentTextblockConverter Instance = new ContentTextblockConverter();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            bool activated = (bool)value;
            if (activated)
                return Translation.BUY_IS_ACTIVATE;
            else
                return Translation.BUY_NOT_ACTIVATE;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}