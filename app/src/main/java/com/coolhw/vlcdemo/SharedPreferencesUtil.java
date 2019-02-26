/**
 * Copyright (C) ELC
 * http://www.elclcd.com
 * Title: SharedPreferencesUtil.java
 * Email: fyhack.cn@gmail.com
 * Date 2013-11-2
 * Version V1.0
 */
package com.coolhw.vlcdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * @Description SharedPreferences存储工具类
 * 
 */
public class SharedPreferencesUtil {
	private SharedPreferences sharedPreferences;
	private Editor editor;

	public SharedPreferencesUtil(Context context) {
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		editor = sharedPreferences.edit();
	}

	/**
	 * 清空数据
	 */
	public void clean() {
		editor.clear();
	}

	/**
	 * 保存数据
	 * 
	 * @param key
	 * @param objValue
	 */
	public void save(String key, Object objValue) {
		if (objValue instanceof String) {
			editor.putString(key, objValue.toString());
		} else if (objValue instanceof Boolean) {
			editor.putBoolean(key, (Boolean) objValue);
		} else if (objValue instanceof Integer) {
			editor.putInt(key, (Integer) objValue);
		} else if (objValue instanceof Float) {
			editor.putFloat(key, (Float) objValue);
		} else if (objValue instanceof Integer) {
			editor.putLong(key, (Long) objValue);
		} else if (objValue == null) {
			editor.putString(key, null);
		} else {
			return;
		}
		editor.commit();
	}

	/**
	 * 移除数据
	 * 
	 * @param key
	 */
	public void remove(String key) {
		editor.remove(key);
		editor.commit();
	}

	/**
	 * 读取数据
	 * 
	 * @param key
	 * @return
	 */
	public int getInt(String key) {
		int result;
		result = sharedPreferences.getInt(key, 0);
		return result;
	}

	public int getInt(String key, int defaultVal) {
		int result;
		result = sharedPreferences.getInt(key, defaultVal);
		return result;
	}

	public String getString(String key, String defaultVal) {
		String result = null;
		result = sharedPreferences.getString(key, defaultVal);
		return result;
	}

	public String getString(String key) {
		String result = null;
		result = sharedPreferences.getString(key, null);
		return result;
	}

	public float getFloat(String key) {
		float result;
		result = sharedPreferences.getFloat(key, 0f);
		return result;
	}

	public long getLong(String key) {
		long result;
		result = sharedPreferences.getLong(key, 0);
		return result;
	}

	public boolean getBoolean(String key) {
		boolean result;
		result = sharedPreferences.getBoolean(key, false);
		return result;
	}

	public boolean getBoolean(String key, boolean defaultVal) {
		boolean result;
		result = sharedPreferences.getBoolean(key, defaultVal);
		return result;
	}
}
