package com.xtreme.wall.service.utils;

import android.content.Context;
import android.content.res.Configuration;

public class ScreenUtils {

	public static boolean isTablet(final Context context) {
		if (context == null)
			return false;
		return isLargeScreen(context);
	}

	private static boolean isLargeScreen(final Context context) {
		if (context == null)
			return false;
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE || isExtraLargeScreen(context);

	}

	private static boolean isExtraLargeScreen(final Context context) {
		if (context == null)
			return false;
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE;

	}
}
