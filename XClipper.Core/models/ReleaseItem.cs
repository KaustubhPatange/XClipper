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
            var result = Regex.Match(body.Replace(Environment.NewLine, string.Empty), ReleaseBodyPattern);
            if (result.Groups.Count == 0) return body;

            var added = ParseRegex(result, 1, "Added");
            var updated = ParseRegex(result, 2, "Update");
            var bug = ParseRegex(result, 4, "Fix");

            return $"{added}\n{updated}\n{bug}";
        }

        private string ParseRegex(Match result, int group, string by)
        {
            if (result.Groups.Count <= group) return string.Empty;
            var map = result.Groups[group].Value.Split('-').Where(c => !string.IsNullOrWhiteSpace(c)).Select(c => $"- [{by}] {c}");
            return string.Join("\n", map);
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
