package com.lanshifu.quguang.utils;

/**
 * Created by lWX385269 on 2016/11/2.
 */
public class ThreadUtil {
    public static void sleep(long l){
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
