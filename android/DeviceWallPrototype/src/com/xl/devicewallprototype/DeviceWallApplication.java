package com.xl.devicewallprototype;

import android.app.Application;

import com.xtreme.utils.Logger;

public class DeviceWallApplication extends Application {
	@Override
	public void onCreate() {
		Logger.setup(true, "DeviceWall");
		super.onCreate();
	}
}
