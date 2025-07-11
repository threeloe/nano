#!/bin/bash

# Check if in the Nano project directory
if [ ! -f "gradlew" ]; then
  echo "Please make sure the script is running in the root directory of the Nano project!"
  exit 1
fi

# Check for build type parameter
if [ -z "$1" ]; then
  echo "Usage: $0 <buildType>"
  echo "buildType: debug or release"
  exit 1
fi

BUILD_TYPE=$1

# Convert BUILD_TYPE to capitalize the first letter
CAPITALIZED_BUILD_TYPE=$(echo "$BUILD_TYPE" | awk '{print toupper(substr($0,1,1)) tolower(substr($0,2))}')

APK_FILE="app/build/outputs/apk/$BUILD_TYPE/app-$BUILD_TYPE.apk"

# Build the APK
echo "Building the $BUILD_TYPE APK..."
./gradlew :app:assemble$CAPITALIZED_BUILD_TYPE
if [ $? -ne 0 ]; then
  echo "Build failed, please check the error logs!"
  exit 1
fi

# Check if the APK file exists
if [ ! -f "$APK_FILE" ]; then
  echo "The APK file was not found: $APK_FILE"
  exit 1
fi
echo "Found the APK file: $APK_FILE"

# Install the APK to the emulator
echo "Installing the APK to the emulator..."
adb install -r "$APK_FILE"
if [ $? -ne 0 ]; then
  echo "Failed to install the APK, please check the emulator status!"
  exit 1
fi

echo "Installation successful!"