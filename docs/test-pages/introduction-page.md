# What is XClipper?

Well **XClipper** is an application which helps you to manage clipboard activities easily.

?> Whenever you copy something to your clipboard (via <kbd>Ctrl + C</kbd> or standard _Right Click > Copy_) XClipper will save it to the database. So that you can retrieve and use it later.

<iframe class="embed-responsive-item"  src="https://www.youtube.com/embed/gC0Ps3A9zfA?autoplay=0" ></iframe>

1. Run XClipper.
2. Copy something to clipboard by using <kbd>Ctrl + C</kbd> or _Right Click > Copy_ (It can be **text** or **files** or **image stream** by using <kbd>Print-scr</kbd>).
3. Go back into your editor for eg: notepad window.
4. Now press <kbd>Ctrl + `</kbd> i.e hold Control and tilde (~) key to display a short window (bottom-right) containing your stored clips. You can also double click the icon in system tray to open XClipper.
5. Double click or press Enter on the clip to paste it into your editor.

# Key bindings

| Key                    | Function                                                 |
| ---------------------- | -------------------------------------------------------- |
| Ctrl + `               | Activate/Deactive XClipper window from anywhere.         |
| ↑ ↓                    | Navigate through the items.                              |
| Ctrl + Q               | Focus on the search box.                                 |
| Esc                    | Deactive XClipper window.                                |
| Ctrl + C               | Make selected item as current clipboard content.         |
| Ctrl + Tab             | Open up the popup window which displays content preview. |
| Delete                 | Remove the selected items from clip database.            |
| Ctrl + T               | Pin/Unpin selected item to top.                          |
| Ctrl + F               | Open up the filter window.                               |
| Ctrl + R               | Display QR Code (it does not support unicode chars).     |
| Ctrl + \|, numeric key | Paste data from an index assigned by the app.            |

### Popup window binds

| Key      | Function                                                                             |
| -------- | ------------------------------------------------------------------------------------ |
| Space    | Activate the popup window to accept the following key binds.                         |
| Ctrl + E | Toggle Edit mode, this will make the content editable.                               |
| Enter    | For editable content it saves, for image content it opens the image in image viewer. |
| Esc      | Close the popup window.                                                              |

### Filter window binds

| Key   | Function                                           |
| ----- | -------------------------------------------------- |
| ↑ ↓   | Navigate through the items.                        |
| Enter | Filter the main window with the property selected. |
