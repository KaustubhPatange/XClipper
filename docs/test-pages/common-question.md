# FAQs <!-- {docsify-ignore-all} -->

?> Here are answers to some common questions you might have. If you don't find what you are looking for ask me through [Contact form](https://kaustubhpatange.github.io/XClipper).

## Table of contents

- **General**
  - [Why PC application is a single source of truth?](#q-why-pc-application-is-a-single-source-of-truth)
  - [Why do I need to create my firebase database?](#q-why-do-i-need-to-create-my-firebase-database)
  - [Why not using Firestore instead of Real-time database?](#q-why-not-using-firestore-instead-of-real-time-database)
  - [Why does XClipper does not synchronize files to the device?](#q-why-does-xclipper-does-not-synchronize-files-to-the-device)
  - [Last x actions didn't complete. What to do?](#q-last-x-actions-didnt-complete-what-to-do)
  - [Can we sync data between multiple Android devices?](#q-can-we-sync-data-between-multiple-android-devices)
  - [Why features are not unlimited for premium users?](#q-why-features-are-not-unlimited-for-premium-users)
- **Payments**
  - [Payment completed but premium does not unlock?](#q-payment-completed-but-premium-does-not-unlock)

---

### Q. Why PC application is a single source of truth?

What does a single source of truth mean? Well, if you say without desktop application there will be no establishment of licensing or connection to the online database i.e synchronization between devices. Why so? It is because of the same fact that there can be multiple Android devices connected to only one PC instance.

What you see is XClipper desktop is responsible for sharing database connectivity details to an Android device (through QR code) & this is possible by initially setting the connection from XClipper desktop, the inverse is not possible. This is the reason why the Android application is not activated but the PC application does! It can be only activated by enabling synchronization to an instance of XClipper desktop which is activated.

Also, if you notice most of the major database related operations like removing multiple devices, setting the required database configuration can only be done from a PC application. Having done this through a mobile app won't make any sense!

You can assume that the XClipper desktop is like a dashboard app.

### Q. Why do I need to create my firebase database?

TL;DR. The answer is very simple, a lot of time we tend to ignore our clipboard data like what we copy or save. Sometimes we save private data (like password, bank details, card numbers, etc.) to the clipboard without knowing & these clipboard manager applications save them. Also, if any type of synchronization is enabled they will upload these data to their servers.

No matter what they say that "they don't sell or spy your data" we can't confirm it. Now imagine you accidentally leak your Gmail account password through clipboard and that triggers such application to save it to their online database, they can then do whatever they want without you being aware of! Obviously, nobody wants any third party company to know their password.

Since firebase (which is a Google product) offers free online storage for everyone, why not use it? You can then control your data and XClipper will provide a bridge between your clipboard data and this database.

### Q. Why not using Firestore instead of Real-time database?

I get these questions a lot but people need to understand that there are differences between these two databases and none of them is an old technology otherwise Google would've just deprecate it (just like they did to Picasso 😢).

Even though Firestore is a great choice to save data into documents it's not fit for saving clipboard data. Why? Well here is the thing Firestore has certain [quota limitations](https://firebase.google.com/docs/firestore/quotas#free-quota) for the free plan. Since they have a limit of read/write it is not fit for clipboard activity.

Imagine how many times you copy something to your clipboard while working on a PC or on a mobile device and if you happen to be a programmer CTRL + C & CTRL + V is your to-go thing. For such a case limitation on read/write is not something anyone wanted.

This is the main reason I moved to a real-time database!

### Q. Why does XClipper does not synchronize files to the device?

The answer is pretty straight forward, you can't store files & images onto the clipboard of an Android device. Just think about it, all Android support is only storing text-related data on the clipboard, unlike a desktop. Even if you select the `Copy Image` button from the Android chrome browser it will set the clipboard as the link of the image, not the actual image bytes.

How can we send and store something which is not defined by Android OS itself? Also, take an example of copying multiple files in a file manager app. It's still irrelevant because the functionality is app-specific. These file managers store the location of these copied files to their buffers & during pasting they just duplicate the files to that location.

What I'm trying to say is all Android clipboard does store text-related data, there can never be a binary data (eg: In Desktop you can store a binary data by pressing `PRTSC` button).

### Q. Why the website isn't hosted on a custom domain?

When we create apps or products we usually don't know if they are going to reach a large amount of audience & if they don't we lose money. This doesn't mean I didn't give my 100% while working on this project (I did), but sometimes things don't work out. Surely we shouldn't get demotivated & continue thinking & building new projects.

That's why when my product will reach to many users I'll buy a separate domain from the money I made.

If this is something keeping you from not buying any paid plan or reflecting a feeling like I'm not enough serious about this (which I'm not), that's just wrong Github sub domains are much secure for carrying out payments. Besides they get don't carried out on Github, PayPal's Checkout API does work like magic.

### Q. Last x actions didn't complete. What to do?

So every event you perform through XClipper is a CRUD for Firebase. If like such there are multiple events chaining one to another, XClipper might forget which events are next (& data associated with). To achieve a solution to this problem there are stacks made for Add, Remove & Update events.

XClipper keeps the record of this stack & their success time (if the event has succeeded). For some cases, the stack goes on increasing without poping previous events, why? Because they are not completed due to various reasons. In such case, you should try the following solutions,

- Check if your internet is active.
- Check if your Firebase database is valid & visible (visit [firebase.google.com](http://firebase.google.com/)).
- Check the firebase details & credentials are valid especially the token expiry date from `%appdata%\XClipper`.
- Always check the latest log XClipper writes to from `%appdata%\XClipper\logs`.
- If any of the above conditions fail to solve your problem. Create an issue & attach your log file.

### Q. Can we sync data between multiple Android devices?

If you have this question perhaps you still have a doubt on how XClipper works! As said desktop XClipper application is a [single source of truth](https://github.com/KaustubhPatange/XClipper/wiki/FAQs#q-why-pc-application-is-a-single-source-of-truth) with which you can add multiple Android devices. This means PC application is a hub for all these devices & internally they act as a chain of information transfer.

In short, you can! All you've to do is follow the how-to synchronization guide from [here](https://github.com/KaustubhPatange/XClipper/wiki/Data-Synchronization) & add another device to the database. You are good to go :)

### Q. Why features are not unlimited for premium users?

This is one of the common question everyone must have (it's not wrong to have). Basically if I'm paying for certain software I want unlimited number of x things. But let me tell you the reason, the thing is you don't actually need it! How? it is because you forgot that clipboard is always a temporary storage of data. When I was testing the application using internal testers it came out that no one were pasting clip from 100th index. At max, they were going over 50 (that too in extreme cases) because a human brain cannot remember all of it! Yes it's true we use Ctrl + C shortcut more than any other shortcut, it's obvious we don't remember all of the copied data.

Also XClipper is smart enough to sort the data according to the most used clip (descending) hence as per my research this restrictions are made. If I get enough request to increase it as per your use-case I'll consider improving it.

But why not keep it unlimited? Like I said we don't need that much! Also, keeping it unlimited will cause unnecessary delays for XClipper to process it which will cause the functionality to run slow.

## Purchase FAQs

### Q. Payment completed but premium does not unlock?

This could happen when your internet connection is slow or there is a problem with my server. Read this [guide](https://github.com/KaustubhPatange/XClipper/wiki/Manual-License-Activation) for solution!
