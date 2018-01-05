package com.lanshifu.quguang.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {
	
	//bool
	public static boolean getPrefBool(Context context,String key,boolean defValue)
	{
		SharedPreferences sp = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
		return sp.getBoolean(key, defValue);
	}
	
	public static void setPrefBool(Context context,String key,boolean value)
	{
		SharedPreferences sp = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
		sp.edit().putBoolean(key, value).commit();
	}
	
	//string
	public static String getPrefString(Context context,String key,String defValue)
	{
		SharedPreferences sp = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
		return sp.getString(key, defValue);
	}
	
	public static void setPrefString(Context context,String key,String value)
	{
		SharedPreferences sp = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
		sp.edit().putString(key, value).commit();
	}

}
