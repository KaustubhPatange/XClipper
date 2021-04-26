# Scripting

?> XClipper allows you to register scripts on Copy & Paste action. During the action it will run those scripts & based on the returned result (`boolean`) will define if the action should abort or not.

## Rules <!-- {docsify-ignore} -->

Scripting in XClipper uses [CSScript](http://www.csscript.net/) engine (a C# scripting technology) & can be called during on copy or paste action.

- Each script must contain a function with a `clip` parameter & must return `true` or `false`. If a function returns `true` then the action will be aborted.
- The function name can be any valid identifier.
- Scripts can be added from `Settings` > `Scripting`.
- Only text & image (location) is supported,
- Make sure you do not spend too much time in script otherwise the action will be delayed which will deteriorate the application performance.

## Supported Methods <!-- {docsify-ignore} -->

- Following are the methods or properties that can be called within the script or by referencing the `clip` parameter.

```cs
(string) clip.RawText { get; set; } // The clipboard text which can be changed.
(string) clip.ImagePath { get; } // Image location generated from "PrtSc" or Snip or Sketch tool.
(enum) clip.ContentType { get; } // Specifies if data is text or image, ContentType.TEXT or ContentType.IMAGE

(bool) clip.ShouldPin { get; set; } // If set to "true" then this clip will be pinned. Available for Copy scripts only.

// Some helper functions avaible from "ClipHelper.cs" class

(string) ClipHelper.GetMD5(string); // Returns MD5 Hash for the string.
(string) ClipHelper.GetActiveApp(); // Returns the active window process name eg: chrome.exe
(string) ClipHelper.GetActiveAppTitle(); // Returns the active window title.
```

## How it works <!-- {docsify-ignore} -->

Whenever a copy (i.e <kbd>Ctrl + C</kbd>) or paste action is detected. The registered scripts will run before saving the data into the database. You can modify the content within the script & must return `false` to proceed the next chain.

If any script during this process returns `true` then the whole operation is aborted & the action is discontinued.

This is useful if you need to modify the incoming data maybe format or replace some characters or to avoid certain text to be ever stored in the database. Well, depending on your use-case you can create the scripts.

## How to create

- Go to `Settings` > `Scripting` tab. From here you can add/edit or delete copy or paste scripts.

<img src="https://androdevkit.files.wordpress.com/2021/04/scripting-1.png" height="300px">

- The one which are strike throughed (eg: ~New Script~) are disabled & will not be run during the action.
- Click on Add (➕) button to add a new script.

<img src="https://androdevkit.files.wordpress.com/2021/04/scripting-2.png" height="400px">

- Give it a name, paste the code in the section. You can use [VSCode](https://code.visualstudio.com/download) or [Notepad++](https://notepad-plus-plus.org/downloads/) to write the script in C# & load it from the file.
- Test the script by clicking the Run (▶️) button to test the script.
- Once done save it by pressing the **Save** button.

## Examples

?> You can add multiple scripts to copy/paste actions.

- **Ignore the applications where you don't want "XClipper" to monitor clipboard copy action.**

```cs
using System;
using XClipper;

public bool IgnoreCopy(Clipper clip) {
    var apps = new string[] { "devenv.exe", "rider64.exe", "code.exe" }; // Add your process names to this array.
    var name = ClipHelper.GetActiveApp();
    if (apps.Contains(name)) return true;
    return false;
}
```

- **Remove Trailing whitespaces & empty lines**

```cs
using System;

public bool TrimText(Clipper clip) {
    if (clip.ContentType == ContentType.TEXT) {
        clip.RawText = clip.RawText.Trim();
    }
    return false;
}
```

- **Do not save screenshots from Google Meet.** I'm a college student so during online meets we have to take a screenshot after every lecture as our attendance. This script will detect them & will not save those.

```cs
using System;
using System.IO;
using XClipper;

public bool RestrictScreenshotsFromMeet(Clipper clip) {
    string browser = ClipHelper.GetActiveApp();
    if (clip.ContentType == ContentType.IMAGE && (browser == "msedge.exe" || browser == "chrome.exe")) {
        string title = ClipHelper.GetActiveAppTitle();
        if (title.StartsWith("Google Meet")) {
            File.Delete(clip.ImagePath); // remove the image;
            return true;
        }
    }
    return false;
}
```

- **Avoid storing emails & passwords**. As a professional lazy person I have used almost same password for all my logins to other websites. Most of the time I save the password to my clipboard & pastes it.

```cs
using System;

public bool FixPrivacyIssues(Clipper clip) {
    if (clip.ContentType == ContentType.TEXT) {
        var emails = new string[] { "your_email.gmail.com", "your_second_email.gmail.com" };
        var passwords = new string[] { "password1", "password2" };
        if (emails.Contains(clip.RawText) || passwords.Contains(clip.RawText))
            return true;
    }
    return false;
}
```
