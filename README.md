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
  
4. You may also want to create schemas and tables on your SQL server. You can find all SQL scripts inside `login/main/res/db/migration` for login tables and inside `realm/main/res/db/migration` for realm tables.
  
5. Finally, go to `prod/` and run

  ```sh
  bin/login
  ```
  
  or
  
  ```sh
  bin/realm
  ```

6. *todo* instructions with configuration files

#### For contributors

1. copy `application.conf.dist` and `logback.groovy.dist` to `application.conf` and `logback.conf`
from both login and realm login projects

2. edit them as you have to

3. apply database migrations with

  for login server
  `gradle login:flywayMigrate -Dflyway.url='insert_database_url_here'`

  for realm server
  `gradle realm:flywayMigrate -Dflyway.url='insert_database_url_here'`

4. run `gradle login:run` or `gradle realm:run`.
