using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Components
{
    public class FilterModel
    {
        public FilterModel(string Text, string Filter_Text)
        {
            this.Text = Text;
            this.Filter_Text = Filter_Text;
        }
        public string Text { get; set; }
        public string Filter_Text { get; set; }
    }
}
