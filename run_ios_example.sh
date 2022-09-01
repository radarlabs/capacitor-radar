#!/bin/sh

# Updates, builds, and loads the example app in an IDE.
# All commands have been copied from the project's CircleCI workflow.

# Update and build the root-level components..
npm install
npm run build
npm install -g @ionic/cli

pushd example

# Update and build the example app.
npm install
ionic build

npx cap sync

ionic capacitor run ios

popd

