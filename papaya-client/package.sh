#!/bin/bash

mvn clean install

if [ -d "output" ]; then
  rm -rf output
fi

OS=$(uname)

if [ "$OS" == "Darwin" ]; then
  ICON="icons/icon.icns"
  echo "Detected macOS. Using icon: $ICON"
elif [ "$OS" == "Linux" ]; then
  ICON="icons/icon.png"
  echo "Detected Linux. Using icon: $ICON"
fi

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

jpackage \
  --input target \
  --name "Papaya" \
  --main-jar "papaya-client-${VERSION}.jar" \
  --main-class org.springframework.boot.loader.launch.JarLauncher \
  --type app-image \
  --dest output \
  --icon "$ICON" \
  --verbose
