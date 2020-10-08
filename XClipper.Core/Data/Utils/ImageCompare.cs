using System.Collections.Generic;
using System.Drawing;
using System.Linq;

namespace Components
{
    /**
     * An algorithm to compare two images despite of their rotation or alpha effect
     * 
     * Working
     * -------
     * 1. Image is reduced to a specific size determined by <see cref="scalingOffSet"/>
     * 2. Image colors are reduced to B/W to ensure they have only two values 1/0
     * 3. Compare the color set and determine how many values have matched.
     */
    public class ImageCompare
    {
        private int scalingOffset;
        private int totalElements;

        public ImageCompare(int scalingOffset = 100)
        {
            this.scalingOffset = scalingOffset;
            this.totalElements = scalingOffset * scalingOffset;
        }

        public bool IsMatch(string firstImagePath, string secondImagePath)
        {
            return IsMatch(
                bmp1: new Bitmap(firstImagePath),
                bmp2: new Bitmap(secondImagePath)
            );
        }

        public bool IsMatch(Bitmap bmp1, Bitmap bmp2)
        {
            List<bool> hash1 = GetHash(bmp1);
            List<bool> hash2 = GetHash(bmp2);

            int sameElements = hash1.Zip(hash2, (i, j) => i == j).Count(eq => eq);

            return sameElements > totalElements - 5; // Off by 5 pixels
        }

        private List<bool> GetHash(Bitmap bmpSource)
        {
            List<bool> lResult = new List<bool>();
            Bitmap bmpMin = new Bitmap(bmpSource, new Size(scalingOffset, scalingOffset));
            for (int j = 0; j < bmpMin.Height; j++)
            {
                for (int i = 0; i < bmpMin.Width; i++)
                {              
                    lResult.Add(bmpMin.GetPixel(i, j).GetBrightness() < 0.5f);
                }
            }
            return lResult;
        }
    }
}
