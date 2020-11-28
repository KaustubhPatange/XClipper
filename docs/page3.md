# Securing database

?>This will allow you to encrypt your clipboard data locally as well as on remote database.

## Table of contents <!-- {docsify-ignore} -->

- [Local database](#local-database)
- [Remote database](#remote-database)

## Local database

?> Secure database makes sure that no one else other than you with the correct password can perform R/W operation on it.

### Steps <!-- {docsify-ignore} -->

- [Why to do so?](#why-to-do-so)
- [Encrypting database](#encrypting-database)

### Why to do so?

XClipper saves all the clipboard data locally in `%appdata%/XClipper/data.db` which is not at all not encrypted. Even though the online database can be further protected it is also necessary to protect the local database.

**Why?** Well, if you see anyone with this database can read all your saved clipboard data.

| Before (no secure database)                                                    | After (secure database)                                                       |
| ------------------------------------------------------------------------------ | ----------------------------------------------------------------------------- |
| <img src="https://androdevkit.files.wordpress.com/2020/09/protect-before.png"> | <img src="https://androdevkit.files.wordpress.com/2020/09/protect-after.png"> |

### Encrypting database

- Open the "**settings**" of the XClipper application from the system tray.
- Enable the "**Protect database with encryption**" option (as shown below).

<img  src="https://androdevkit.files.wordpress.com/2020/09/2020-09-26-11-18-26.png" height="350px"/>

- You can also set a "**Custom password**" as shown above if you want. Make sure your password has no _spaces_ between them.
- Once done, proceed to click the "**Apply**" button. XClipper will then migrate your current database with a secure one.

## Remote database

!> This is a paid feature read more about it [here]().

?> By default the database we created to enable sync is not secure enough. Anyone with the url can easily read or having all credentials can also perform write operations.

?> Since security is the first concern you should secure it to your account only!

_Note: There is an option to encrypt your database called "Encrypt clips in database" under **Firebase Configuration**, if enabled then XClipper will encrypt all the clips which make them unreadable if not viewed from application itself_.

### Steps <!-- {docsify-ignore} -->

So we are actually adding an authentication that would require a Google sign-in to perform operations on the database.

_Note: Before doing this, make sure you close the XClipper desktop instance & remove any devices that are connected to it, otherwise they will not function properly._

- [Adding security rules to the database](#adding-security-rules-to-the-database)
- [Enable authorization in XClipper](#enable-authorization-in-xclipper)
- [Rules playground](#rules-playground)

### Adding security rules to the database

- Close any running instance of the XClipper desktop. Also, make sure that you've removed all the devices which are connected to the database.
- Go to firebase.google.com & select the project which you've created.
- From there, go to "**Realtime database**" > "**Rules**" & edit the rules as per your requirement from this [section](#rules-playground).

### Enable authorization in XClipper

_Note: If you have an existing database setup, you might get some error but just ignore them. They are because we've changed the database rules when a connection was already present._

- Now launch the XClipper and from system tray "**Right-click**" > "**Firebase Configuration**" > "**Connect**" tab.

- From there, add the required credentials (given below) in their respective field as shown.

<img src="https://androdevkit.files.wordpress.com/2020/10/secure-1-1.png" height="400px"/>

```
- Desktop
  - Client ID: 307507243265-4bart8vep2vtljab28t6g3i5vsr9nv05.apps.googleusercontent.com
  - Client Secret: G8KN0CSDMOxbX8HdWCWXrj4j
- Android
  - Client ID: 323700069140-7hj54ukss3072gbr7in9627ti35o3mhs.apps.googleusercontent.com
```

- Don't forget to "**Save**" the settings.

### Rules playground

?> Add any one of the following rules as per your requirement.

- **Anyone with read, write permission (not secure)**.

```
{
  "rules": {
    ".read":"true",
    ".write": "true",
  }
}
```

- **Only authenticated users will have read, write access (secure)**.

```
{
  "rules": {
    ".read":"auth!=null"
    ,".write": "auth != null",
  }
}
```

- **Restricting read, write access to only one email (highly secure)**.

```
{
  "rules": {
    ".read": "auth != null && auth.token.email == 'youremail@gmail.com'",
    ".write": "auth != null && auth.token.email == 'youremail@gmail.com'",
  }
}
```

- **Retricting read, write access to multiple emails (highly secure)**.

```
{
  "rules": {
    ".read": "auth != null && (auth.token.email == 'firstemail@gmail.com' || auth.token.email == 'secondemail@gmail.com')",
    ".write": "auth != null && (auth.token.email == 'firstemail@gmail.com' || auth.token.email == 'secondemail@gmail.com')",
  }
}
```

_Chain multiple such emails with `||` as shown above._
