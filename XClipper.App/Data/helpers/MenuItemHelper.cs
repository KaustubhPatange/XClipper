using System;
using System.Windows.Forms;

namespace Components
{
    public static class MenuItemHelper
    {
        public static MenuItem CreateNewItem(string Text, EventHandler handler)
        {
            var item = new MenuItem(Text);
            if (handler != null)
            {
                item.Click += handler;
            }
            return item;
        }

        public static MenuItem CreateSeparator()
        {
            return new MenuItem("-");
        }
    }
}
