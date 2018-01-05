package com.lanshifu.quguang.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by 蓝师傅 on 2016/7/1.
 */
public class BmobUtils {


    /***
     * 获取机器码
     * @return
     */
    public static String getPhoneNumber(Context context){
        //获取设备码

        final TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String number = tm.getDeviceId();
        if ("000000000000000".equals(number)){
            return "";
        }
        return number;

    }

}
