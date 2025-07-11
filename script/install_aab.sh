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

# Build the bundle package
echo "Building the bundle$BUILD_TYPE package..."
./gradlew :app:bundle$CAPITALIZED_BUILD_TYPE
if [ $? -ne 0 ]; then
  echo "Build failed, please check the error logs!"
  exit 1
fi

# Specify the .aab file path
AAB_FILE="app/build/outputs/bundle/$BUILD_TYPE/app-$BUILD_TYPE.aab"
if [ ! -f "$AAB_FILE" ]; then
  echo "The .aab file was not found: $AAB_FILE"
  exit 1
fi
echo "Found the .aab file: $AAB_FILE"

# Check if bundletool is installed
if ! command -v bundletool &> /dev/null; then
  echo "bundletool is not installed, please install it and try again!"
  exit 1
fi

# Prepare output directory
OUTPUT_DIR="build/output"
if [ -d "$OUTPUT_DIR" ]; then
  echo "Cleaning up the output directory..."
  rm -rf "$OUTPUT_DIR"
fi
mkdir -p "$OUTPUT_DIR"

# Generate Split APKs and install to the emulator
echo "Generating Split APKs..."
SCRIPT_DIR=$(dirname "$0")
DEVICE_SPEC="$SCRIPT_DIR/device-spec.json"
if [ ! -f "$DEVICE_SPEC" ]; then
  echo "The device-spec.json file was not found in the script directory!"
  exit 1
fi

bundletool build-apks --bundle="$AAB_FILE" --output="$OUTPUT_DIR/app-split.apks" --mode=default --device-spec="$DEVICE_SPEC"
if [ $? -ne 0 ]; then
  echo "Failed to generate Split APKs, please check the error logs!"
  exit 1
fi

# Unzip the .apks file and install
echo "Unzipping and installing Split APKs..."
unzip -o "$OUTPUT_DIR/app-split.apks" -d "$OUTPUT_DIR"
adb install-multiple "$OUTPUT_DIR/splits/"*.apk
if [ $? -ne 0 ]; then
  echo "Failed to install Split APKs, please check the emulator status!"
  exit 1
fi

echo "Installation successful!"