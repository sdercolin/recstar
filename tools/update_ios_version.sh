#!/bin/sh

PLIST=../iosApp/iosApp/Info.plist
VERSION_NAME=$1
VERSION_CODE=$2

/usr/libexec/PlistBuddy -c "Set :CFBundleShortVersionString $VERSION_NAME" "$PLIST"
/usr/libexec/PlistBuddy -c "Set :CFBundleVersion $VERSION_CODE" "$PLIST"
