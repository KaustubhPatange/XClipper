# Compilation guide

- Clone the repository with [git](https://git-scm.com/downloads) or the new [CLI](https://github.com/cli/cli) tool.

```
gh repo clone KaustubhPatange/XClipper
cd XClipper
```

### Windows

- Install [Visual Studio](https://visualstudio.microsoft.com/vs/).

- Run the following commands.

```
tar -xvzf scripts\csproj.tar.gz
nuget restore XClipper.sln
msbuild XClipper.sln
```

### Android

- Install [Android Studio](https://developer.android.com/studio).

- Run the following commands.

```
tar -xvzf scripts\gradle.tar.gz
./gradlew app:assembleDebug
```
