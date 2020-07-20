using System;

#nullable enable

namespace Components
{
    public interface ILicense
    {
        /// <summary>
        /// This will initiate all the steps that involves after a user has
        /// purchased a license.<br/>
        /// It will automatically set <see cref="DefaultSettings.IsPurchaseDone"/> &amp; <see cref="DefaultSettings.LicenseStrategy"/>
        /// </summary>
        /// <param name="block">This block will be run when license is validated</param>
        void Initiate(Action<Exception?> block);
    }
}
