using ClipboardManager.models;

namespace XClipper
{
    public class Clipper
    {
        public Clipper(string rawText, string imagePath, ContentType type)
        {
            RawText = rawText;
            ImagePath = imagePath;
            Type = type;
        }
        
        public string RawText { get; set; }
        public string ImagePath { get; private set; }
        public bool ShouldPin { get; set; } = false; // Used only on Copy scripts.
        public ContentType Type { get; private set; }

        public static Clipper CreateSandbox() => new("This is a sample data", null, ContentType.Text);
        public static Clipper ForTextType(string text) => new(text, null, ContentType.Image);
    }
}