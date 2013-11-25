Photon [![Build Status](https://travis-ci.org/Emudofus/Photon.png?branch=master)](https://travis-ci.org/Emudofus/Photon)
======

A Scala Dofus 1.29 emulator

## How to use Photon ?

#### For users

1. Clone this repository with

  ```sh
  git clone https://github.com/Emudofus/Photon.git
  ```

  You may install Git before

2. Then, go to the Photon folder with `cd Photon/` and run Gradle

  ```sh
  gradlew installApp
  ```

3. After that, get `login` folder from `login/build/install/` and `realm` folder from `realm/build/install/`
   and copy them to a `prod/` folder

  ```sh
  cd Photon/
  mkdir prod/
  cp -rf login/build/install/login/**/* prod/
  cp -rf realm/build/install/realm/ prod/
  ```
  
4. Finally, go to `prod/` and run

  ```sh
  bin/login
  ```
  
  or
  
  ```sh
  bin/realm
  ```

5. *todo* instructions with configuration files

#### For contributors

First copy `application.conf.dist` and `logback.groovy.dist` to `application.conf` and `logback.conf`
from both login and realm login projects, edit them as you have to and then run `gradle login:run` or
`gradle realm:run`.
