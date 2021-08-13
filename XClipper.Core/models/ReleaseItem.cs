using System;
using System.Collections.Generic;
using System.Text;
using System.Linq;
using System.Text.RegularExpressions;

namespace Components
{
    public class ReleaseItem
    {
        private string ReleaseBodyPattern = @"###\s?Added\s?(.*?)###\s?Update\s?(.*?)###\s?Bug\s?(.*?)\s(.*)";
        private string TicketPattern = @"\s?#(\d)+|<!--(.*?)-->";

        public string url { get; set; }
        public string tag_name { get; set; }
        public string name { get; set; }
        public string target_commitish { get; set; }
        public string created_at { get; set; }
        public string published_at { get; set; }
        public string body { get; set; }
        public bool draft { get; set; }
        public bool prerelease { get; set; }
        public List<ReleaseAsset> assets { get; set; }

        public int GetVersion()
        {
            return tag_name.Substring(1).Replace(".", "").ToInt();
        }

        public string GetDatePretty()
        {
            return DateTime.Parse(published_at).ToFormattedDate();
        }

        public string GetFormattedBody()
        {
            var ir = Regex.Replace(body, TicketPattern, "")
                .Replace("##", "");
            return Regex.Replace(ir, @"^(?:[\t ]*(?:\r?\n|\r))+", "", RegexOptions.Multiline);
        }
    }

    public class ReleaseAsset
    {
        public string url { get; set; }
        public string name { get; set; }
        public string content_type { get; set; }
        public long size { get; set; }
        public string download_count { get; set; }
        public string browser_download_url { get; set; }
    }
}
