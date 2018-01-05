package com.lanshifu.quguang.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by 蓝师傅 on 2016/6/12.
 */
public class BaseUser extends BmobObject {
    public String getMachineNumber() {
        return MachineNumber;
    }

    public void setMachineNumber(String machineNumber) {
        this.MachineNumber = machineNumber;
    }

    private String MachineNumber;
    private String Message;

    public String getConnected_times() {
        return connected_times;
    }

    public void setConnected_times(String connected_times) {
        this.connected_times = connected_times;
    }

    private String connected_times;

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
