# android-file-chooser


## Overview

![banner](captures/banner.svg)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-android--file--chooser-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/6982)
[![Download](https://api.bintray.com/packages/hedzr/maven/filechooser/images/download.svg)](https://bintray.com/hedzr/maven/filechooser/_latestVersion)
[![Release](https://jitpack.io/v/hedzr/android-file-chooser.svg)](https://jitpack.io/#hedzr/android-file-chooser)

`android-file-library` is a lightweight file/folder chooser.


### Snapshots

<table><tr><td>
<img src="captures/choose_file.png" width="360"/>
</td><td>
<img src="captures/choose_folder.png" width="360"/>
</td></tr></table>


### Demo Application

A demo-app can be installed from [Play Store](https://play.google.com/store/apps/details?id=com.obsez.android.lib.filechooser.demo).

<a href='https://play.google.com/store/apps/details?id=com.obsez.android.lib.filechooser.demo&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' width='240' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>


## Changes

- create new folder on the fly, and the optional multiple select mode for developer, thx [Guiorgy](https://github.com/Guiorgy) and his [android-smbfile-chooser](https://github.com/Guiorgy/android-smbfile-chooser)



## Usage

### Configuration

#### build.gradle

android-file-chooser was released at jcenter, declare deps with:

```gradle
implementation 'com.obsez.android.lib.filechooser:filechooser:1.1.10'
```

#### taste the fresh

there is a way to taste the `master` branch with [jitpack.io](https://jitpack.io):

1. add the jitpack repository url to your root build.gradle:

```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

2. import `android-file-chooser`

```gradle
implementation 'com.github.hedzr:android-file-chooser:master-SNAPSHOT'
// implementation 'com.github.hedzr:android-file-chooser:v1.1.10'
```



### Codes

FileChooser android library give a simple file/folder chooser in single call:

#### Choose a Folder

```java
    new ChooserDialog().with(this)
            .withFilter(true, false)
            .withStartFile(startingDir)
            .withChosenListener(new ChooserDialog.Result() {
                @Override
                public void onChoosePath(String path, File pathFile) {
                    Toast.makeText(MainActivity.this, "FOLDER: " + path, Toast.LENGTH_SHORT).show();
                }
            })
            .build()
            .show();
```

#### Choose a File

```java
    new ChooserDialog().with(this)
            .withStartFile(path)
            .withChosenListener(new ChooserDialog.Result() {
                @Override
                public void onChoosePath(String path, File pathFile) {
                    Toast.makeText(MainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                }
            })
            .build()
            .show();

```

#### Wild-match

```java
    new ChooserDialog().with(this)
            .withFilter(false, false, "jpg", "jpeg", "png")
            .withStartFile(path)
            .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
            .withChosenListener(new ChooserDialog.Result() {
                @Override
                public void onChoosePath(String path, File pathFile) {
                    Toast.makeText(MainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                }
            })
            .build()
            .show();

```

#### Regex filter

```java
    new ChooserDialog().with(this)
            .withFilterRegex(false, false, ".*\\.(jpe?g|png)")
            .withStartFile(path)
            .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
            .withChosenListener(new ChooserDialog.Result() {
                @Override
                public void onChoosePath(String path, File pathFile) {
                    Toast.makeText(NewMainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                }
            })
            .build()
            .show();

```

#### Date Format String

Since 1.1.3, new builder options `withDateFormat(String)` added.

```java
    new ChooserDialog().with(this)
            .withFilter(true, false)
            .withStartFile(startingDir)
            .withDateFormat("HH:mm")    // see also SimpleDateFormat format specifiers
            .withChosenListener(new ChooserDialog.Result() {
                @Override
                public void onChoosePath(String path, File pathFile) {
                    Toast.makeText(MainActivity.this, "FOLDER: " + path, Toast.LENGTH_SHORT).show();
                }
            })
            .build()
            .show();
```

#### Modify Icon or View Layout of `AlertDialog`:

Since 1.1.6, 2 new options are available:

```java
    new ChooserDialog().with(this)
            .withFilter(true, false)
            .withStartFile(startingDir)
            .withIcon(R.drawable.ic_file_chooser)
            .withLayoutView(R.layout.alert_file_chooser)
            .withChosenListener(new ChooserDialog.Result() {
                @Override
                public void onChoosePath(String path, File pathFile) {
                    Toast.makeText(MainActivity.this, "FOLDER: " + path, Toast.LENGTH_SHORT).show();
                }
            })
            .build()
            .show();
```

#### Customizable NegativeButton

1.1.7 or Higher, try `withNegativeButton()` and `withNegativeButtonListener()`.

#### New calling chain

1.1.7+, new constructor `ChooserDialog(context)` can simplify the chain invoking, such as:

```java
    new ChooserDialog(this)
            .withFilter(true, false)
            .withStartFile(startingDir)
            ...
```

And, old style is still available. No need to modify your existing codes.

#### `withRowLayoutView(resId)`

1.1.8+. Now you can customize each row.

#### `withFileIcons`

1.1.9+. `withFileIcons(resolveMime, fileIcon, folderIcon)` and
`withFileIconsRes(resolveMime, fileIconResId, folderIconResId)` allow
user-defined file/folder icon.

`resolveMime`: true means that `DirAdapter` will try get icon from the associated app with the file's mime type.

```java
    new ChooserDialog(ctx)
            .withStartFile(_path)
            .withResources(R.string.title_choose_any_file, R.string.title_choose, R.string.dialog_cancel)
            .withFileIconsRes(false, R.mipmap.ic_my_file, R.mipmap.ic_my_folder)
            .withChosenListener(new ChooserDialog.Result() {
                @Override
                public void onChoosePath(String path, File pathFile) {
                    Toast.makeText(ctx, "FILE: " + path, Toast.LENGTH_SHORT).show();
                }
            })
            .build()
            .show();
```

#### `withAdapterSetter(setter)`

1.1.9+. a `AdapterSetter` can be use to customize the `DirAdapter`.

```java
            .withAdapterSetter(new ChooserDialog.AdapterSetter() {
                @Override
                public void apply(DirAdapter adapter) {
                    adapter.setDefaultFileIcon(fileIcon);
                    adapter.setDefaultFolderIcon(folderIcon);
                    adapter.setResolveFileType(tryResolveFileTypeAndIcon);
                }
            })
```

#### `withNavigateUpTo(CanNavigateUp)`

1.1.10+. `withNavigateUpTo`

```java
                .withNavigateUpTo(new ChooserDialog.CanNavigateUp() {
                    @Override
                    public boolean canUpTo(File dir) {
                        return true;
                    }
                })
```

#### `withNavigateTo(CanNavigateTo)`

1.1.10+. `withNavigateTo`

```java
                .withNavigateTo(new ChooserDialog.CanNavigateTo() {
                    @Override
                    public boolean canNavigate(File dir) {
                        return true;
                    }
                })
```



## Under Kotlin

```kotlin
class MyFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_book, container, false)
        root.upload_button.setOnClickListener { _: View ->
            ChooserDialog().with(activity)
                    .withStartFile(Environment.getExternalStorageDirectory().absolutePath)
                    // .withStartFile(Environment.getExternalStorageState()+"/")
                    .withFilterRegex(false, false, ".*\\.(jpe?g|png)")
                    .withChosenListener { path, pathFile -> activity!!.toast("FILE: $path / $pathFile") }
                    .build()
                    .show()
        }

        return root
    }
}
```

And:

```kotlin
        ChooserDialog(context)
                .withFilterRegex(false, true, ".*\\.(jpe?g|png)")
                .withStartFile(startPath)
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener { path, pathFile ->
                    Toast.makeText(context, "FILE: $path; PATHFILE: $pathFile", Toast.LENGTH_SHORT).show()

                    //_path = path
                    //_tv.setText(_path)
                    ////_iv.setImageURI(Uri.fromFile(pathFile));
                    //_iv.setImageBitmap(ImageUtil.decodeFile(pathFile))
                }
                .withNavigateUpTo { true }
                .withNavigateTo { true }
                .build()
                .show()
```


## Build me

### 1. legacy

```bash
cat >keystore.properties<<EOF
keyAlias=youKeyAlias
keyPassword=password
storeFile=/Users/me/android-file-chooser.keystore
storePassword=password
EOF
git clone git@github.com:hedzr/android-file-chooser.git somewhere
cd somewhere
./gradlew assembleDebug
```

you'd better generate a new file `android-file-chooser.keystore` at homedir or else. such as: `keytool -genkey -alias android.keystore -keyalg RSA -validity 20000 -keystore android.keystore`, see also [Sign an app](https://developer.android.com/studio/publish/app-signing).
Or, erase the `KS_PATH` lines and signature section in app/build.gradle.

### 2. current

just fork me and build me currently.

## Contrib

Contributions and translations are welcome.

## Feedback

feel free to make an new issue.

## Acknowledges

many peoples report or contribute to improve me, but only a few of them be put here — it's hard to list all.

- logo and banner by: [**iqbalhood**](https://github.com/iqbalhood)
- codes and reports: [**bostrot**](https://github.com/bostrot), [**SeppPenner**](https://github.com/SeppPenner), [**lucian-cm**](https://github.com/lucian-cm), [**ghost**](https://github.com/ghost), [**UmeshBaldaniya46**](https://github.com/UmeshBaldaniya46), [Guiorgy](https://github.com/Guiorgy) ...



## License

Copyright 2015-2018 Hedzr Yeh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

