#!/bin/bash
PACKAGES=( 
com.xtreme.wall.service
com.xl.devicewallprototype
com.xtremelabs.devicewallimageapp
com.xtremelabs.devicewallmemorygame
com.xtremelabs.devicewallidentifierapp
)

for DEVICE in $(adb devices | grep -v '^List' | awk '{print $1}'); do
	echo $DEVICE
	for i in ${PACKAGES[@]}; do
		(	
			echo $DEVICE
			if [ "$(adb -s $DEVICE shell pm path $i)" ]; then
				echo "force stopping $i from $DEVICE"
				adb -s $DEVICE shell am force-stop $i
			else
				echo "$i not found on $DEVICE"
			fi
		) &
	done

done