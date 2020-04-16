using static Components.DefaultSettings;
using Components;

namespace ClipboardManager
{
    /// <summary>
    /// This class will provide some helper to main Context classes to 
    /// determine what clips to store.
    /// </summary>
    public static class WhatToStoreHelper
    {
        public static bool ToStoreTextClips() => WhatToStore == XClipperStore.All || WhatToStore == XClipperStore.Text;
        public static bool ToStoreImageClips() => WhatToStore == XClipperStore.All || WhatToStore == XClipperStore.Image;
        public static bool ToStoreFilesClips() => WhatToStore == XClipperStore.All || WhatToStore == XClipperStore.Files;
    }
}
