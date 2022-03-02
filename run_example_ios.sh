#!/bin/sh

# Updates, builds, and runs the example app in an iOS simulator.
# All commands have been copied from the project's CircleCI workflow.

# Update and build the root-level components..
npm install
npm build
npm install -g @ionic/cli

pushd example

# Update and build the example app for iOS.
npm install
ionic build

# Run the example app in an iPhone 12 simulator.
npx cap sync
ionic capacitor run ios --target $(ionic capacitor run ios --list | grep -o -m 1 '[A-F0-9]\{8\}-[A-F0-9]\{4\}-[A-F0-9]\{4\}-[A-F0-9]\{4\}-[A-F0-9]\{12\}')

popd

