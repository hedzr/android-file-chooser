# RELEASES

## 1.1.15 - 2019-02-25

- #42: withOnBackPressedListener not fired
  clarify `onCancelListener`, obsolete `onBackListener`
- #44: .with(this) in the sample code is obsolete
  doc updated, demo codes updated.
  readme doc reviewed
- #45: Does not display the name of a selected folder
  Add `titleFollowsDir(boolean)`.


## 1.1.14 - 2019-01-18

no more details

## 1.1.12 ~ 1.1.14

the publishing had matters.


## 1.1.11 - 2018-10-08

- Create new folder on the fly, and the optional multiple select mode for developer, thx [Guiorgy](https://github.com/Guiorgy) and his [android-smbfile-chooser](https://github.com/Guiorgy/android-smbfile-chooser)
- Up (`..`) on the primary storage root will be replaced with `.. SDCard`, it allows to jump to external storage such as a SDCard and going back available too.
- DPad supports, arrow keys supports (#30)
- ...bugs fixed


## 1.1.10 - 2018-06-05

bugs fixed


## 1.1.9 - 2018-05-20

\+ #14, `withFileIcons(...)`, `withFileIconsRes(...)`, `withAdapterSetter(...)`


## 1.1.8 - 2018-05-17

\+ #13, `withRowLayoutView(resId)` allow color/font customizing...


## 1.1.7 - 2018-05-15

\* #8, misspell typo fixed, thx @bostrot
\* #9, new style constructor from @SeppPenner
\* README for #3 is more friendlier
\* KS_PATH usage in #2 is more friendlier


## 1.1.6 - 2018-02-11

Spring 2018 Version:

\+ permissions check form @bostrot, thx a lot
\* upgrade to AS 3 & gradle 4.1+
\* about #4, add 2: [withIcon()](./library/src/main/java/com/obsez/android/lib/filechooser/ChooserDialog.java#L114), [withLayoutView()](./library/src/main/java/com/obsez/android/lib/filechooser/ChooserDialog.java#L119)


## 1.1.5 - 2017-06-27

fixed issue #2


## 1.1.4.1 - 2017-06-27

bug fixed for last releases

\* navigate and choose a folder without extension cause crash


## 1.1.4 - 2016-12-31

bug fixed for last release

\* remove app_name;
\* dateformat fixed;


## 1.1.3 - 2016-12-30

\+ withDateFormat()
\* remove wrong timestamp display at '..' (parent) folder
\+ add history/changelog
\* upgrade building environment
\+ traditional chinese language


