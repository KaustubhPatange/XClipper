# Data synchronization

- You must first install [XClipper for Windows](https://kaustubhpatange.github.io/XClipper) in order to continue!
- In this guide we will learn how to synchronize desktop's clipboard data across mobile in XClipper.

?> Synchronization is purely based on the Firebase's Real-time database storage. Read why XClipper is not using Firestore instead, in this [FAQ](https://github.com/KaustubhPatange/XClipper/wiki/FAQs#q-why-not-using-firestore-instead-of-real-time-database).

?> XClipper currently supports text-related data to be synchronized. To enable image synchronization, read this [guide](https://github.com/KaustubhPatange/XClipper/wiki/Enable-Image-synchronization) after completing the below steps.

!> There are some limitation to this feature in free version, [read more](#limitations).

## Steps <!-- {docsify-ignore} -->

These are the steps we are going to follow, if you have any questions check the [FAQs](https://github.com/KaustubhPatange/XClipper/wiki/FAQs).

- [Creating your own data storage on Firebase](#creating-your-own-data-storage-on-firebase).
- [Get all the required credential for setup](#get-all-the-required-credential-for-setup).
- [Setting up the credential in the desktop app](#setting-up-the-in-the-desktop-app).
- [Enabling required settings for sync](#enabling-required-settings-for-sync).
- [Connecting mobile device to the storage](#connecting-mobile-device-to-the-storage).
- [Sharing data with another PC](#sharing-data-with-another-PC).
- [Testing if clipboard data is syncing](#testing-if-clipboard-data-is-syncing).
- [Synchronization between multiple Android devices.](https://github.com/KaustubhPatange/XClipper/wiki/FAQs#q-can-we-sync-data-between-multiple-android-devices)
- [Limitations (for free users)](#limitations)

## Creating your own data storage on Firebase

?> Firebase is a suite of tools provided by Google which makes a lot of things smoother, one of them is Real-time database. This is where all your clipboard data will be stored by the XClipper.

If you want to know why I don't host my own storage for this service, read this [FAQ](https://github.com/KaustubhPatange/XClipper/wiki/FAQs#q-why-do-i-need-to-create-my-firebase-database) answer.

- Go to firebase.google.com & click on "**Go to console**" button from the top right of the page _(You might need to sign-in with your Google account)_.

- Click on the "**Add**" project card, give your project a name & click _continue_.

- There, **disable** "Enable Google Analytics" option & proceed to "**Create project**".

- Once done you will see something like a dashboard screen.

<img src="https://androdevkit.files.wordpress.com/2020/09/xclipper-firebase-2.png">

- Here click on "**Realtime Database**" > "**Create database**" > "**Enable**".

<img src="https://androdevkit.files.wordpress.com/2020/09/xclipper-firebase-3.png">

- Now go to "**Rules**", update the text as shown above & then click on the "**Publish**" button.

## Get all the required credential for setup

?> In these steps we will note down some of the credentials that you will need to connect your XClipper desktop application to the database.

I would recommend writing these information in a notepad file.

- Go to "**Realtime database**" > "**Data**" & note the database location url as shown below.

<img src="https://androdevkit.files.wordpress.com/2020/09/xclipper-firebase-4.png">

- This one is pretty complex to explain with single image so you need to watch the video given below. You will get "**App Id**" after following it.

**Client ID:**

```
323700069140-7hj54ukss3072gbr7in9627ti35o3mhs.apps.googleusercontent.com
```

<iframe class="embed-responsive-item"
                                src="https://www.youtube.com/embed/Mz0k6C2BIfI?autoplay=0" allowfullscreen></iframe>

- Click on "**Gear icon**" > "**Project Settings**" & note down the _Web API Key_ as shown below.

<img src="https://androdevkit.files.wordpress.com/2020/09/xclipper-firebase-5.png">

## Setting up the credential in the desktop app

?> In this step we will add the credentials in the XClipper application. I assume you were taking notes of them.

- At the end, your notepad file should look like this (this may vary from yours).

```
Firebase Endpoint: https://test-project-9e017.firebaseio.com/

Firebase App Id: 1:1057025207873:android:c7004aea2af70512b220c0

Firebase API Key: AIzaSyC05g-9MkJbFhnxE2137MDeTT07wNHncfs
```

- Now in the XClipper application from system tray, right-click > "**Firebase configuration**" & add those value. Click "**Save**" when done.

<img src="https://androdevkit.files.wordpress.com/2020/09/xclipper-firebase-6-1.png" height="400px">

?> Note: If you need authentication & want to secure your database even more, check this [guide](https://github.com/KaustubhPatange/XClipper/wiki/Using-secure-database).

## Enabling required settings for sync

?> Here we will turn on the setting that will enable the syncing of the clipboard to the database.

- In XClipper application from system tray, right-click > "**Settings**" & enable **Database binding** & **Apply**. If QR code is still not visible then Restart the application.

<img src="https://androdevkit.files.wordpress.com/2020/11/sync-10.png" height="400px">

- That's it now your clipboard item should be added to the database.

?> You can also enable **Bind Deletion** option which will delete items from your local storage if deleted from an online database.

?> Tip: Hover on each item to see what they do.

## Connecting mobile device to the storage

You first need to download the XClipper application on a mobile device.

[<img src="https://camo.githubusercontent.com/f9dc78b44989eb93046dee0cc745b113ae8f9c2c/68747470733a2f2f7777772e62696e672e636f6d2f74683f69643d4f49502e614b56796e464857494546775079454c6b416473775148614353267069643d4170692672733d31" width="170px">
](https://play.google.com/store/apps/details?id=com.kpstv.xclipper)

- In the app go to "**Settings**" > "**Account**" > "**Connect to database**" & scan the QR code shown in the _Settings_ window of XClipper desktop application.

?> Using this above approach you can connect as many device you want.

## Sharing data with another PC

!> This can have some side effects, if you find them kindly report to me.

With this approach you can share the same database between multiple XClipper desktop application. This is useful if you want to share clipboard with your personal PC & work PC.

?> You need to purchase license for both system inorder for this to work.

- In **Right click** > **Firebase Configurations**, there are two buttons at the bottom which can export & import the current Firebase configuration.

<img src="https://androdevkit.files.wordpress.com/2020/12/sync-multiple.png" height="400px">

- From one PC you've to export the configuration (which will save it in a file) & from other PC you just 've to import it.
- Make sure you enter the database password which it will ask while importing. Your current password can be found in **Settings**.

## Testing if clipboard data is syncing?

In most cases restarting the application solves the problem. But there might be a case if this does not work & you want to know if your clipboard data is properly syncing.

This can be used to determine the source of the problem. Is the desktop application not syncing properly or the mobile app not receiving it?

After all, it leads to this [FAQs](https://github.com/KaustubhPatange/XClipper/wiki/FAQs) section.

- You can check if the online database receives updates or not by going to firebase.google.com selecting your project which we created & going to the real-time database. It should update automatically when changes are made by **XClipper**. If this is not the case create a GitHub issue.
- If your application is crashed or reported any issues. There is a log file that **XClipper** creates on every new instance located in `%appdata%\XClipper\logs`. You must upload the proper log file if you are reporting an issue.

## Limitations

?> There are certain limitations imposed for free users. Unlocking premium will remove those as well as will encourage me to work on this project even more.

| Feature                                    | Free  | Premium |
| ------------------------------------------ | ----- | ------- |
| Maximum number of clipboard data           | 10    | 120     |
| Maximum length of text (characters)        | 1,000 | 10,000  |
| Maximum number of connected Android device | 1     | 10+     |
| Securing data using encryption             | ❌    | ✔️      |
| Adding authentication (Google sign-in)     | ❌    | ✔️      |

If you want to know why these features are not unlimited for premium users, [read here](https://github.com/KaustubhPatange/XClipper/wiki/FAQs#q-why-some-features-are-not-unlimited-for-premium-users).
