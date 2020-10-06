> Android cannot hold image data on it's clipboard that's why I used a different approach to design this. Read about this in [FAQ](https://github.com/KaustubhPatange/XClipper/wiki/FAQs#q-why-does-xclipper-does-not-synchronize-files--images-to-the-device)

> XClipper does not support synchronization of images by default, you need to change a setting option after you [enable synchronization](https://github.com/KaustubhPatange/XClipper/wiki/Data-Synchronization).

## Steps

- [Enable storage setting in firebase](#enable-storage-setting-in-firebase)
- [Securing storage access](#securing-storage-access) (Optional)
- [Turn on image synchronization](#turn-on-image-synchronization)

### Enable storage setting in firebase

> Once you've enable synchronization by following this [guide](https://github.com/KaustubhPatange/XClipper/wiki/Data-Synchronization) there is one last step you need to perform to enable image sync.

- Go to firebase.google.com, and select the project you've created while following above mentioned guide.
- Now go to Storage > Get started > Next > Done to create a default storage in firebase (as shown below).

<>

![](https://androdevkit.files.wordpress.com/2020/10/storage.gif)

<>

### Securing storage access

> Currently our storage is R/W by everyone which means anyone can read / write or even delete data. To prevent this we are securing our storage by applying some restrictions.

- Go to `Rules` tab & change the rules to below.

```less
service firebase.storage {
  match /b/{bucket}/o {
    match /XClipper/images/{imageId} {
    	allow read: if true;
        allow delete: if true;
    	allow write: if request.resource.size < 5 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
  }
}
```

- So above `rules` says that the XClipper has access to files & folders that are created within `/XClipper/images` path which can only contain `images` restricting it's size to be less than `5mb`.

### Turn on image synchronization

> Now we have to enable a setting which will then trigger image uploads.

- In XClipper application, go to "**Settings**" > "**Connect**" tab.
- From there, check "**Bind Image**" option (as shown below).

<>

<img src="https://androdevkit.files.wordpress.com/2020/10/storage-4-1.png" height="400px"/>

<>

- Now, whenever you press `PRTSC` or would use Windows snip (`Shift` + `Win` + `S`) it will be automatically synchronized to the device.
