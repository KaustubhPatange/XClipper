/** 
 *  @Created by Kaustubh Patange
 *  @Last-Modified: 10-4-2020
 *  
 *  @Summary: A Message Box window based on WPF Material Design component.
 *  
 **/

using System;
using System.Windows;
using System.Windows.Input;

namespace Components
{
    public partial class MaterialMessage : Window
    {
        #region Variable Definition

        public static bool isClosed = false;

        #endregion


        #region Constructor
        public MaterialMessage()
        {
            InitializeComponent();


            Loaded += (o, e) => { Owner.Opacity = 0.5; };

            Closed += (o, e) => { Owner.Opacity = 1; };

            /** A WPF window cannot work correctly unless the WPF dispatcher
             * loop provides it with notifications. It also transport this
             * main UI KeyDown event here.
             */
            KeyDown += (o, e) => { if (e.Key == Key.Escape) Close(); };

            this._btn_OK_Clicked.Focus();
        }

        #endregion


        #region Builder

        /** Pretty self explainatory function. I am basically more of an Android Developer
         *  so couldn't get the hang of those Builder methods unless implemented here :) */

        public MaterialMessage SetOnOpenListener(Action name)
        {
            Loaded += (o, e) => { name?.Invoke(); };
            return this;
        }

        public MaterialMessage SetOnCloseListener(Action name)
        {
            Closed += (o, e) => { name?.Invoke(); };
            return this;
        }

        public MaterialMessage SetOwner(Window window)
        {
            Owner = window;
            return this;
        }
        public MaterialMessage SetMessage(string message)
        {
            _tbTitle.Text = message;
            return this;
        }

        public MaterialMessage SetType(MessageType type)
        {
            if (type == MessageType.OKOnly)
                _btn_Cancel_Clicked.Hide();
            return this;
        }

        public MaterialMessage SetOnOKClickListener(Action name)
        {
            _btn_OK_Clicked.Click += (o, e) =>
            {
                name?.Invoke();
                Close();
            };
            return this;
        }

        public MaterialMessage SetOnCancelClickListener(Action name)
        {
            _btn_Cancel_Clicked.Click += (o, e) =>
            {
                name?.Invoke();
                Close();
            };
            return this;
        }

        #endregion
    }
}
