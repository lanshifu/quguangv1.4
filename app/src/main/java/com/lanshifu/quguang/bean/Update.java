package com.lanshifu.quguang.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by 蓝师傅 on 2016/10/19.
 */

public class Update extends BmobObject {

    private String url;
    private Boolean isNewVersion;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getNewVersion() {
        return isNewVersion;
    }

    public void setNewVersion(Boolean newVersion) {
        isNewVersion = newVersion;
    }
}
