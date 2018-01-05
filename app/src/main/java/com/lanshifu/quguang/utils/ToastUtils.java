package com.lanshifu.quguang.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by lanxiaobin on 2017/9/7.
 */

public class ToastUtils {

	private static Toast mToast;

	private static Context mContext;

	public static void init(Context context){
		mContext = context;
	}


	public static void showShortToast(int resId) {
		showShortToast(mContext.getResources().getText(resId));
	}

	public static void showShortToast(CharSequence hint) {
		showToast(mContext, hint, Toast.LENGTH_SHORT);

	}

	public static void showLongToast(CharSequence hint) {
		showToast(mContext, hint, Toast.LENGTH_LONG);

	}

	private static void showToast(Context context, CharSequence hint, int duration) {
		if (mToast == null) {
			mToast = Toast.makeText(context, hint, duration);
		} else {
			mToast.setText(hint);
			mToast.setDuration(duration);
		}
		mToast.show();
	}


}
