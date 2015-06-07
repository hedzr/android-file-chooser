
## Schemes

### Old1

在library的build.gradle中加入：

    apply plugin: 'maven'
    version = "1.1.3"
    group = "com.obsez.android.lib.filechooser"
    task installArchivesOld(type: Upload) {
        description "Installs the artifacts to the local Maven repository."
        configuration = configurations['archives']
        repositories {
            mavenDeployer {
                repository url: "file://${System.properties['user.home']}/.m2/repository"
            }
        }
    }

### 正确工作的, 但本项目未用

在library的build.gradle中加入：

    apply plugin: 'maven'
    //http://stackoverflow.com/questions/18559932/gradle-maven-plugin-install-task-does-not-work-with-android-library-project
    task installArchives(type: Upload) {
        description "Installs the artifacts to the local Maven repository."
        repositories.mavenInstaller {
            configuration = configurations.default
            pom.groupId = 'com.obsez.android.lib.filechooser'
            pom.artifactId = 'library'
            pom.version = '1.1.3'
        }
    }

然后使用`gradle installArchives`即可安装到本地仓库中。




### Manually Install a JAR to your Local Maven Repo (without source code)

At the moment a lot of Google artifacts (=JARs, AARs) are not available at the Maven Central repo, but you can install them manually to your local Maven repo

    mvn install:install-file -Dfile=android-support-v4.jar -DgroupId=com.google -DartifactId=android-support-v4 -Dversion=0.1 -Dpackaging=jar

一个实例是：

    mvn install:install-file -Dfile=library/build/outputs/library-release.aar -DgroupId=com.obsez.android.lib.filechooser -DartifactId=library -Dversion=1.1.3 -Dpackaging=aar

see also:

    <http://www.alonsoruibal.com/my-gradle-tips-and-tricks/>




### 使用mvn-publish脚本

https://code.google.com/p/maven-android-plugin/wiki/GettingStarted

[Dependency-management-with-Gradle (libgdx)](https://github.com/libgdx/libgdx/wiki/Dependency-management-with-Gradle)

#### 1. AutomatedScreenshots

https://code.google.com/p/maven-android-plugin/wiki/AutomatedScreenshots

介绍了自动屏幕截图。

#### 2. android-maven-plugin

To do android development using maven

https://github.com/simpligility/android-maven-plugin

http://simpligility.github.io/android-maven-plugin/

leading page: https://github.com/dcendents/android-maven-plugin

#### 3. android-maven-gradle-plugin

To let you install your aar libraries into the local maven repository:

https://github.com/dcendents/android-maven-gradle-plugin

#### 4. maven-publish plugin发布到本地仓库

在library的build.gradle中加入：

    apply plugin: 'maven-publish'
    version = "1.1.3"
    group = "com.obsez.android.lib.filechooser"
    android.libraryVariants
    publishing {
        publications {
            maven(MavenPublication) {
                artifact bundleRelease
            }
        }
    }

然后使用`gradle clean build publishToMavenLocal`可以发布到本地仓库。

see also: <http://www.flexlabs.org/2013/06/using-local-aar-android-library-packages-in-gradle-builds>
and: <http://stackoverflow.com/questions/18559932/gradle-maven-plugin-install-task-does-not-work-with-android-library-project>


#### 5. 使用gradle-mvn-push 发布到远程仓库MavenCentral

apply from: 'https://raw.github.com/chrisbanes/gradle-mvn-push/master/gradle-mvn-push.gradle'


<hr>
<p>
<p>





