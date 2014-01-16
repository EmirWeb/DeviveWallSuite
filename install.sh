#!/bin/bash

for DEVICE in $(adb devices | grep -v '^List' | awk '{print $1}'); do
	(
		echo $DEVICE
		adb -s $DEVICE install /Users/devfloater56/Development/build/DeviceWallImageApp.apk 
		adb -s $DEVICE install /Users/devfloater56/Development/build/DeviceWallMemoryGame.apk 
		adb -s $DEVICE install /Users/devfloater56/Development/build/DeviceWallPrototype.apk 
		adb -s $DEVICE install /Users/devfloater56/Development/build/DeviceWallService.apk 
		adb -s $DEVICE install /Users/devfloater56/Development/build/DeviceWallIdentifierApp.apk 
	) &
done