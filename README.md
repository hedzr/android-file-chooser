# android-file-chooser


## Overview

![banner](captures/banner.svg)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-android--file--chooser-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/6982)
[![Download](https://api.bintray.com/packages/hedzr/maven/filechooser/images/download.svg)](https://bintray.com/hedzr/maven/filechooser/_latestVersion)
[![Release](https://jitpack.io/v/hedzr/android-file-chooser.svg)](https://jitpack.io/#hedzr/android-file-chooser)

`android-file-library` is a lightweight file/folder chooser.
`android-samba-file-chooser` is a fork that adds jcifs.smb.SmbFile support.


### Snapshots

<img src="captures/choose_file.png" width="360"/>
<img src="captures/choose_folder.png" width="360"/>
<img src="captures/smb.png" width="360"/>

### Demo Application

A demo-app of the original can be installed from [Play Store](https://play.google.com/store/apps/details?id=com.obsez.android.lib.filechooser.demo).

<a href='https://play.google.com/store/apps/details?id=com.obsez.android.lib.filechooser.demo&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' width='240' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>

**NOTE**:

NtlmPasswordAuthentication not yet supported! (haven't had the need for it)

## Usage

```
try{
    SmbFileChooserDialog dialog = SmbFileChooserDialog.newDialog(context, "**.***.*.**")
        .setResources("Select a directory", "choose", "cancel")
        .setFilter(true, false)
        .setOnChosenListener((path, file) -> {
            try{
                Toast.makeText(context, file.isDirectory() ? "directory" : "file" + " selected: " + path, Toast.LENGTH_SHORT).show();
            } catch(SmbException e){
                e.printStackTrace();
            }
        })
        .build()
        .show();
} catch(MalformedURLException | InterruptedException | ExecutionException e){
    e.printStackTrace();
}
```

for more information please refere to the [original](https://github.com/hedzr/android-file-chooser).

## Acknowledges

many peoples report or contribute to improve me, but only a few of them be put here — it's hard to list all.

- logo and banner by: [**iqbalhood**](https://github.com/iqbalhood)
- codes and reports: [**bostrot**](https://github.com/bostrot), [**SeppPenner**](https://github.com/SeppPenner), [**lucian-cm**](https://github.com/lucian-cm), [**ghost**](https://github.com/ghost), [**UmeshBaldaniya46**](https://github.com/UmeshBaldaniya46) ...



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

