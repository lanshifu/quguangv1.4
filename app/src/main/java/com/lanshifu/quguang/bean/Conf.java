package com.lanshifu.quguang.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by 蓝师傅 on 16-8-7.
 */
public class Conf extends BmobObject {
    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDatabaseCount() {
        return databaseCount;
    }

    public void setDatabaseCount(String databaseCount) {
        this.databaseCount = databaseCount;
    }

    private String price;
    private String message;
    private String databaseCount;
}
