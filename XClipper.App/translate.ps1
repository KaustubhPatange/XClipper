﻿# This script generates a Translation class file (%2) from the input (%1)
# ResourceDictionary xaml file.

# This is used by visual studio to update Data/helpers/TranslationHelper.cs with the
# values from Locales/en.xaml

# Make sure this file is in UTF-8-BOM encoding otherwise CI build
# will failed as VS will try to manually change it.

[xml]$xml = Get-Content $args[0]
$sb = [System.Text.StringBuilder]::new()
[void]$sb.AppendLine("using static Components.App;")
[void]$sb.AppendLine("namespace Components")
[void]$sb.AppendLine("{")
[void]$sb.AppendLine("`t// Auto-generated by build event")
[void]$sb.AppendLine("`t// Build upon " + $args[0])
[void]$sb.AppendLine("`tpublic class Translation")
[void]$sb.AppendLine("`t{")
# [void]$sb.AppendLine("`t`tprivate static TranslationHelper Instance;")
# [void]$sb.AppendLine("`t`tpublic static TranslationHelper Translation")
# [void]$sb.AppendLine("`t`t{")
# [void]$sb.AppendLine("`t`t`tget")
# [void]$sb.AppendLine("`t`t`t{")
# [void]$sb.AppendLine("`t`t`t`tif (Instance != null) return Instance;")
# [void]$sb.AppendLine("`t`t`t`tInstance = new TranslationHelper();")
# [void]$sb.AppendLine("`t`t`t`treturn Instance;")
# [void]$sb.AppendLine("`t`t`t}")
# [void]$sb.AppendLine("`t`t}")
foreach($n in $xml.FirstChild.ChildNodes) {
    if ($n.Attributes.Count -gt 0) {
        [void]$sb.AppendLine("`t`tpublic static string " + $n.Attributes[0].Value.ToUpper() + ' => GetString("' + $n.Attributes[0].Value + '");')
    }
}
[void]$sb.AppendLine("`t`tpublic static string GetString(string key)")
[void]$sb.AppendLine("`t`t{")
[void]$sb.AppendLine("`t`t`tvar s = rm.GetString(key);")
[void]$sb.AppendLine("`t`t`tif (!string.IsNullOrEmpty(s)) return s;")
[void]$sb.AppendLine("`t`t`treturn rmf.GetString(key);")
[void]$sb.AppendLine("`t`t}")
[void]$sb.AppendLine("`t}")
[void]$sb.AppendLine("}")

echo $sb.ToString()
Set-Content -Path $args[1] -Value $sb.ToString()