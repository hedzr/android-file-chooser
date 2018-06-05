# android-file-chooser


## Overview

a lightweight file/folder chooser


[ ![Download](https://api.bintray.com/packages/hedzr/maven/filechooser/images/download.svg) ](https://bintray.com/hedzr/maven/filechooser/_latestVersion)

<img src="captures/choose_file.png" width="360"/>
<img src="captures/choose_folder.png" width="360"/>

### Demo Application

A demo-app can be installed from [Play Store](https://play.google.com/store/apps/details?id=com.obsez.android.lib.filechooser.demo).

<a href='https://play.google.com/store/apps/details?id=com.obsez.android.lib.filechooser.demo&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' width='240' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>


## Changes

Since 1.1.6, AS 3.0+ + Gradle 4.1+ + Android SDK & Building Tools 26.0+ are prerequisites.

Two withXXX calls added for AlertDialog icon and layout resource. See Also: [withIcon()](./library/src/main/java/com/obsez/android/lib/filechooser/ChooserDialog.java#L114), [withLayoutView()](./library/src/main/java/com/obsez/android/lib/filechooser/ChooserDialog.java#L119)


**NOTE**:

> minSDK will be moved up to 14+ at next release, since we like Android Supports Library 26.+.


## Usage

### Configuration

#### build.gradle

android-file-chooser was released at jcenter, declare deps with:

```gradle
implementation 'com.obsez.android.lib.filechooser:filechooser:1.1.9'
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

1.1.9+. `withNavigateUpTo`

```java
                .withNavigateUpTo(new ChooserDialog.CanNavigateUp() {
                    @Override
                    public boolean canUpTo(File dir) {
                        return true;
                    }
                })
```

#### `withNavigateTo(CanNavigateTo)`

1.1.9+. `withNavigateTo`

```java
                .withNavigateTo(new ChooserDialog.CanNavigateTo() {
                    @Override
                    public boolean canNavigate(File dir) {
                        return true;
                    }
                })
```



## Build me

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

## Contrib

Contributions and translations welcome.

## License

Copyright 2015 Hedzr Yeh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

