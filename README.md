# android-file-chooser


## Overview

a lightweight file/folder chooser

[ ![Download](https://api.bintray.com/packages/hedzr/maven/filechooser/images/download.svg) ](https://bintray.com/hedzr/maven/filechooser/_latestVersion)

<img src="capture/choose_file.png" width="360"/>
<img src="capture/choose_folder.png" width="360"/>


## Usage

### Configuration

#### build.gradle

android-file-chooser was released at jcenter, declare deps with:

```gradle
compile 'com.obsez.android.lib.filechooser:filechooser:1.1.3'
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

#### Specify Date Format inside chooser

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


## References

* https://github.com/hedzr/android-file-chooser

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

