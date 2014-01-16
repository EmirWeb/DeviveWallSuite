#!/bin/bash
PACKAGES=( 
com.xtreme.wall.service
com.xl.devicewallprototype
com.xtremelabs.devicewallimageapp
com.xtremelabs.devicewallmemorygame
com.xtremelabs.devicewallidentifierapp
)

DEVICE=$1
# for DEVICE in $(adb devices | grep -v '^List' | awk '{print $1}'); do
	echo $DEVICE
	for i in ${PACKAGES[@]}; do
		(	
			echo $DEVICE
			if [ "$(adb -s $DEVICE shell pm path $i)" ]; then
				echo "Uninstalling $i from $DEVICE"
				adb -s $DEVICE uninstall $i 
			else
				echo "$i not found on $DEVICE"
			fi
		) &
	done

# done