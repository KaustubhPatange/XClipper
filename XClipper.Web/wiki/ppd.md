## Steps

- [Why to use secure database?](#why-to-use-secure-database)
- [Migrating database to secure database](#migrating-database-to-secure-database)

### Why to use secure database?

XClipper saves all the clipboard data locally in `%appdata%/XClipper/data.db` which is not at all not encrypted. Even though the online database can be further protected it is also necessary to protect the local database.

**Why?** Well, if you see anyone with this database can read all your saved clipboard data.

<>

| Before (no secure database)                                                    | After (secure database)                                                       |
| ------------------------------------------------------------------------------ | ----------------------------------------------------------------------------- |
| <img src="https://androdevkit.files.wordpress.com/2020/09/protect-before.png"> | <img src="https://androdevkit.files.wordpress.com/2020/09/protect-after.png"> |

### Migrating database to secure database

> Secure database makes sure that no one else other than you with the correct password can perform R/W operation on it.

- Open the "**settings**" of the XClipper application from the system tray.
- Enable the "**Protect database with encryption**" option (as shown below).

<>

<img src="https://androdevkit.files.wordpress.com/2020/09/2020-09-26-11-18-26.png" height="350px"/>

<>

- You can also set a "**Custom password**" as shown above if you want. Make sure your password has no _spaces_ between them.
- Once done, proceed to click the "**Apply**" button. XClipper will then migrate your current database with a secure one.
