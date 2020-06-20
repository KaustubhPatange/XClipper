using System;

#nullable enable
namespace Components
{
    public interface IUpdater
    {
        /// <summary>
        /// This method will check for update and will run the method.
        /// </summary>
        /// <param name="block"></param>
        void Check(Action<bool, Update?>? block);

    }
}
