#!/bin/bash

cd /Users/devfloater56/Development/xl-device_wall-suite/android

PACKAGES=( 
DeviceWallService
DeviceWallShared
DeviceWallImageApp
DeviceWallMemoryGame
DeviceWallPrototype
)

DIRECTORIES=`ls`

echo $DIRECTORIES

for i in ${PACKAGES[@]}; do
  cd $i
  
  echo update $i
  android update project --name $i --path .
  cd ..
done

for i in ${PACKAGES[@]}; do
  cd $i
  
  echo build $i
  ant debug
  cd ..

done
