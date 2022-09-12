#!/bin/sh

if [ "$#" -ne 1 ]
then
    echo "Usage: $0 [android|ios]"
    exit
fi

# Updates, builds, and loads the example app in an IDE.
# All commands have been copied from the project's CircleCI workflow.

# Update and build the root-level components..
npm install run build
npm install -g @ionic/cli

pushd example

# Update and build the example app.
npm install
ionic build
npx cap sync

if [ "$1" = "android" ]
then
    pushd android
    ./gradlew assemble
    popd
fi

ionic capacitor run ${1}

popd

