android-theme
============

[![Build Status](https://img.shields.io/github/actions/workflow/status/ThePalaceProject/android-theme/.github%2Fworkflows%2Fandroid-main.yml?style=flat-square)](https://github.com/ThePalaceProject/android-theme/actions?query=workflow%3A%22Android+CI+%28Authenticated%29%22)
[![Maven Central](https://img.shields.io/maven-central/v/org.thepalaceproject.theme/org.thepalaceproject.theme.core?style=flat-square)](https://repo1.maven.org/maven2/org/thepalaceproject/theme)
[![Maven Central (Snapshots)](https://img.shields.io/nexus/s/org.thepalaceproject.theme/org.thepalaceproject.theme.core?server=https%3A%2F%2Fs01.oss.sonatype.org%2F)](https://oss.sonatype.org/content/repositories/snapshots/org/thepalaceproject/theme/)

The Palace Project's Android theme.

![theme](./src/site/resources/sandbox.png?raw=true)

### What Is This?

The contents of this repository define the neutral [Material 3](https://m3.material.io/) theme
used for the Palace Android components.

### Building

#### Build!

Make sure you clone this repository with `git clone --recursive`.
If you forgot to use `--recursive`, then execute:

```
$ git submodule init
$ git submodule update --remote --recursive
```

The short version: Install an [Android SDK](#android-sdk) and run:

~~~
$ ./gradlew clean assembleDebug test
~~~

Please read the list of instructions below for specific details on configurations.

#### Android SDK

Install the [Android SDK and Android Studio](https://developer.android.com/studio/). We don't
support the use of any other IDE at the moment.

#### JDK

Install a reasonably modern JDK: Java 17 is the current recommendation for Android Studio.

The `JAVA_HOME` environment variable must be set correctly. You can check what it is set to in
most shells with `echo $JAVA_HOME`. If that command does not show anything, adding the following
line to `$HOME/.profile` and then executing `source $HOME/.profile` or opening a new shell
should suffice:

~~~w
# Replace NNN with your particular version of 17.
export JAVA_HOME=/path/to/jdk17_NNN
~~~

You can verify that everything is set up correctly by inspecting the results of both
`java -version` and `javac -version`:

~~~
$ java -version
openjdk version "17.0.8" 2023-07-18
OpenJDK Runtime Environment (build 17.0.8+7)
OpenJDK 64-Bit Server VM (build 17.0.8+7, mixed mode)
~~~

### Versioning

The API complies with [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html), and this
is enforced using [japicmp](https://github.com/siom79/japicmp). The current version of the
code is analyzed with respect to the previous version, and incompatible changes will
require a major version increment. Please see the [VERSIONING.txt](VERSIONING.txt)
file for the list of packages that are _exempt_ from versioning rules
due to being private implementation packages.
