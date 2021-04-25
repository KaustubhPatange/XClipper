# Scripting

?> XClipper allows you to register scripts on Copy & Paste action. During the action it will run those scripts & based on the returned result (`boolean`) will define if the action should abort or not.

## Rules <!-- {docsify-ignore} -->

Scripting in XClipper uses [CSScript](http://www.csscript.net/) engine (a C# scripting technology) & can be called during on copy or paste action.

- Each script must contain a function with a `clip` parameter & must return `true` or `false`. If a function returns `true` then the action will be aborted.
- Scripts can be added from `Settings` > `Scripting`.
- Only text & image (location) is supported,
- Make sure you do not spend too much time in script otherwise the action will be delayed which will deteriorate the application performance.

```
All the functions that are supported.
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

<!-- TODO: -->
