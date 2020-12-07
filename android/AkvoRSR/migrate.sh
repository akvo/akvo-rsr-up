#!/usr/bin/env bash

#Create new application module directory `app` in `AkvoRSR`
mkdir -p app/src/main/java

#Move source codes to app
git mv src/com app/src/main/java
git mv src/org app/src/main/java

#Move res and asserts
mv res app/src/main

#Move AndroidManifest
mv AndroidManifest.xml app/src/main

#Create gradle build  files
touch settings.gradle
touch app/build.gradle

#do it after committing
#rm -rf src