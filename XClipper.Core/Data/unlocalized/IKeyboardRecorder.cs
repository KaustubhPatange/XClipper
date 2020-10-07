using System;

namespace Components
{
    public interface IKeyboardRecorder
    {
        /// <summary>
        /// Start monitoring clipboard activities.
        /// </summary>
        /// 
        void StartRecording();

        /// <summary>
        /// Stop monitoring clipboard activities.
        /// </summary>
        void StopRecording();

        /// <summary>
        /// Method when keyboard keys are pressed and detected.
        /// </summary>
        void OnChanged();

        /// <summary>
        /// This provides a block to perform actions with clipboard manager
        /// without monitoring it.
        /// </summary>
        /// <param name="block"></param>
        void Ignore(Action block);

        /// <summary>
        /// An injected interface that provides a smooth communication with the App class.
        /// </summary>
        /// <param name="binder"></param>
        void SetAppBinder(IClipServiceBinder binder);
    }
}
