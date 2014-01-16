package com.xtremelabs.devicewall.utils;

public class IdCreator {

	private long mCurrentId = 10 ;
	
	public synchronized Long getNewId() {
		return mCurrentId++;
	}

	public synchronized Long getCurrentId() {
		return mCurrentId;
	}

}
