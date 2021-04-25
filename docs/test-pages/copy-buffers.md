# Copy Buffers

?> XClipper has a feature of storing clipboard content on a separate buffer other than the `Windows` clipboard one. Just like Ctrl + C and Ctrl + V, there are different shortcuts to use them.

## Summary <!-- {docsify-ignore} -->

- The goal of this feature is to have separate independent buffer that doesn't hook to the `Windows` clipboard one & can be used as secondary clipboard.
- User can specify upto 2 extra buffer & can customize the hotkeys to make them work.
- Only text type of data is supported for now.
- Go to `Settings` > `Copy Buffers` tab to modify the hotkeys for cut/copy/paste operation.

## Behind the scenes <!-- {docsify-ignore} -->

There is nothing like extra buffers in windows which we can hook to. The way this feature works is by first saving the original clipboard content, then sends copy/paste action & finally restore the clipboard with the saved content.
