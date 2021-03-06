/*
 *  Copyright (C) 2012-2015 Jason Fang ( ifangyucun@gmail.com )
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.hellofyc.base.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings.Secure;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.WindowManager;

/**
 * Device tool
 * Create on 2014年12月6日 下午12:16:36
 *
 * @author Jason Fang
 */
public final class DeviceUtils {
	private static final boolean DEBUG = false;

	public static final String DEFAULT_IMEI = "default_imei";
	public static final String DEFAULT_SERIAL = "default_serial";

	private static String sDeviceUniqueId;

	public static boolean isTablet(Context context) {
        if (context == null) {
            throw new RuntimeException("context cannot be null!");
        }

		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static boolean isEmulator() {
		return (Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"));
	}

	public static boolean isGenymotion() {
		return Build.DEVICE.startsWith("vbox86");
	}

	public static long getAvailableMemorySize(Context context) {
        if (context == null) {
            throw new RuntimeException("context cannot be null!");
        }

		ActivityManager aManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo info = new MemoryInfo();
		aManager.getMemoryInfo(info);
		return info.availMem;
	}

	public static int getHeapSize(Context context) {
        if (context == null) {
            throw new RuntimeException("context cannot be null!");
        }

		ActivityManager aManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		return aManager.getMemoryClass();
	}

	public static String getSerial() {
		if (TextUtils.isEmpty(Build.SERIAL)) {
			return DEFAULT_SERIAL;
		}
    	return Build.SERIAL;
    }

	/**
	 * 获取IMEI
	 */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
	public static String getIMEI(Context context) {
        if (context == null) {
            throw new RuntimeException("context cannot be null!");
        }

		try {
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getDeviceId() == null ? "" : tm.getDeviceId();
		} catch (SecurityException e) {
			FLog.e("Requires android.Manifest.permission#READ_PHONE_STATE");
		}
		return DEFAULT_IMEI;
	}

	public static String getAndroidId(Context context) {
		if (context == null) {
			throw new RuntimeException("context cannot be null!");
		}

		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	/**
	 * 获取设备唯一号
	 */
	@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
	public static String getDeviceUniqueId(Context context) {
		if (!TextUtils.isEmpty(sDeviceUniqueId)) {
			return sDeviceUniqueId;
		}

		String imei = getIMEI(context);
		String androidId = getAndroidId(context);
		String serial = getSerial();

		return MD5Utils.encode(imei + androidId + serial);
	}

	/**
	 * 获取系统app运行内存
	 */
    public static void getSystemMemory(Context context) {
        if (context == null) {
            throw new RuntimeException("context cannot be null!");
        }

    	ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo info = new MemoryInfo();
        activityManager.getMemoryInfo(info);

        if (DEBUG) FLog.i("系统剩余内存:" + Formatter.formatFileSize(context, info.availMem));
        if (DEBUG) FLog.i("系统是否处于低内存运行：" + info.lowMemory);
        if (DEBUG) FLog.i("当系统剩余内存低于" + Formatter.formatFileSize(context, info.threshold) + "时就看成低内存运行");
    }

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public static Point getScreenSize(Context context) {
        if (context == null) {
            throw new RuntimeException("context cannot be null!");
        }

		Point point = new Point();
		WindowManager wM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

		if (Build.VERSION.SDK_INT >= 13) {
			wM.getDefaultDisplay().getSize(point);
		} else {
			point.x = wM.getDefaultDisplay().getWidth();
			point.y = wM.getDefaultDisplay().getHeight();
		}
		return point;
	}

	public static int getScreenDensity(Context context) {
        if (context == null) {
            throw new RuntimeException("context cannot be null!");
        }

		return context.getResources().getDisplayMetrics().densityDpi;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static int getActionBarHeight(Context context) {
        if (context == null) {
            throw new RuntimeException("context cannot be null!");
        }

        TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        return TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
	}

	public static int getAvailableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}

	/**
	 * 设备是否支持该属性
	 *
	 * Use {@link PackageManager#hasSystemFeature(String)}
	 */
	public static boolean isSupportFeature(Context context, String feature) {
		if (context == null) {
			throw new RuntimeException("context cannot be null!");
		}
		if (TextUtils.isEmpty(feature)) return false;

		FeatureInfo[] infos = context.getPackageManager().getSystemAvailableFeatures();
		if (infos == null || infos.length == 0) return false;
		for (FeatureInfo info : infos) {
			if (DEBUG) FLog.i("Feature Name:" + info.name);
			if (feature.equals(info.name)) {
				return true;
			}
		}
		return false;
	}

	private DeviceUtils(){/*Do not new me!*/}
}
