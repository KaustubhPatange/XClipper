using System.Collections.Generic;
using System.IO;
using System.Windows.Documents;
using System.Windows.Forms;
using static Components.Constants;
using System.Linq;
using System.Text.RegularExpressions;
using System.Runtime.CompilerServices;
using System;

namespace Components
{
    public static class IgnoreHelper
    {
        private const string TAG = "IgnoreHelper";
        private static Dictionary<string, RegexOptions> lazyPairs;

        /// <summary>
        /// If the returned value is true proceed for addition or update.
        /// </summary>
        /// <param name="rawText"></param>
        /// <returns></returns>
        public static bool ToExecute(string rawText)
        {
            try
            {
                return !IsMatch(rawText);
            }catch (ArgumentException e)
            {
                LogHelper.Log(TAG, "Couldn't parse expression: " + e.StackTrace);
                MessageBox.Show("Couldn't parse expression: " + e.StackTrace, TAG);
            }catch (Exception e)
            {
                LogHelper.Log(TAG, "Unknown error: " + e.StackTrace);
            }
            return true;
        }
        public static bool IsMatch(string rawText)
        {
            if (lazyPairs == null)
                lazyPairs = IgnoreParser.Parse().ToDictionary(k => k.Key, v => GetOptions(v.Value));
            foreach(var pair in lazyPairs)
            {
                var pattern = new Regex(pair.Key, pair.Value);
                if (pattern.IsMatch(rawText)) return true;
            }
            return false;
        }

        private static RegexOptions GetOptions(IgnoreFlags flag)
        {
            RegexOptions options = RegexOptions.None;
            if (flag.IsCaseInsensitive == true) options |= RegexOptions.IgnoreCase;
            if (flag.IsMultiline == true) options |= RegexOptions.Multiline;
            if (flag.IsSingleLine == true) options |= RegexOptions.Singleline;
            return options;
        }
    }

    public class IgnoreParser
    {
        public static Dictionary<string, IgnoreFlags> Parse()
        {
            var dictionary = new Dictionary<string, IgnoreFlags>();
            if (File.Exists(IgnoreFilePath))
            {
                var lines = File.ReadAllLines(IgnoreFilePath).Where(l => !l.StartsWith(Grammar.Comment)).Select(l => l.Trim());
                if (lines.IsNotEmpty())
                {
                    foreach (var line in lines)
                    {
                        if (line.StartsWith(Grammar.StartExpression))
                        {
                            var expression = line.Substring(1, line.LastIndexOf('/') - 1);
                            var flags = line.Substring(line.LastIndexOf('/') + 1);

                            var ignoreFlag = new IgnoreFlags();
                            foreach(var c in flags.ToCharArray())
                            {
                                if (c == Grammar.Global) ignoreFlag.IsGlobal = true;
                                if (c == Grammar.SingleLine) ignoreFlag.IsCaseInsensitive = true;
                                if (c == Grammar.Multiline) ignoreFlag.IsMultiline = true;
                                if (c == Grammar.CaseInsensitive) ignoreFlag.IsCaseInsensitive = true;
                            }

                            dictionary.Add(expression, ignoreFlag);
                        }
                    }
                }
            }
            return dictionary;
        }

        public static class Grammar
        {
            public const string Comment = "#";
            public const char Global = 'g';
            public const char Multiline = 'm';
            public const char CaseInsensitive = 'i';
            public const char SingleLine = 's';
            public const string StartExpression = "/";
            public const string EndExpression = "/";
        }
    }

    public class IgnoreFlags
    {
        public bool IsGlobal { get; set; }
        public bool IsMultiline { get; set; }
        public bool IsCaseInsensitive { get; set; }
        public bool IsSingleLine { get; set; }
    }

    public static class IgnoreConstants {
        public const string IntialData = @"
# This is an .ignore file for XClipper application.

# Note: Anything which begins which # is a comment.

# It helps to ignore certain text pattern that should be uploaded to database.

# - All the pattern must be written one after another
# - All patterns are treated as regular expressions. (follow javascript like approach)

# For optimization purpose keep total expressions less than 50;

# Examples (Uncomment the expression to see in actions)

# Eg: Single word/letters are not allowed
# /^[\w]+/g

# Eg: Paths are not allowed
# /(C|D|F|E|J):\\[\w\d_-]+\\?/g

# Eg: Some code related text to ignore
# /(if|public|private|{|}|==|!=|>=|<=|bool|void)/gm";
    }
}
