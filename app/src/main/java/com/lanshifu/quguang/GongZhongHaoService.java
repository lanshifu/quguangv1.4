package com.lanshifu.quguang;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.lanshifu.quguang.bean.User2;
import com.lanshifu.quguang.log.LogHelper;
import com.lanshifu.quguang.utils.BmobUtils;
import com.lanshifu.quguang.utils.PrefUtils;
import com.lanshifu.quguang.utils.ThreadUtil;
import com.lanshifu.quguang.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class GongZhongHaoService extends BaseAccessibilityService {


    private final String TAG = "111";
    boolean isQuguan = true;  //是否取关，或者删除联系人记录
    private boolean permission = false;
    private AccessibilityNodeInfo rootNodeInfo;
    private String STATE_NOMORE = "不再关注";
    private String STATE_CANCAL_NOTICE = "取消关注";
    private String STATE_LONG_CLICK = "长按";
    private String STATE_DELETE = "删除该聊天";
    private String currentSatate = STATE_CANCAL_NOTICE;

    private String[] key_world = {"取消关注", "不再关注"};  //监听的关键词
    private boolean firstInsert = true;
    private boolean isLongClick = false;  //
    private long timeOut;
    Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();

    }

    /**
     * 连接服务器
     */
    public void check_toServer() {
        if(PrefUtils.getPrefBool(this,"hadregisted",false)){
            permission = true;
            return;
        }
        String phoneNumber = BmobUtils.getPhoneNumber(getApplicationContext());
        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.equals("000000000000000")) {
            phoneNumber = PrefUtils.getPrefString(getApplicationContext(), "machineNumber", null);
        }
        queryUser(phoneNumber);

    }

    /**
     * @param number
     * @return
     */
    private void queryUser(final String number) {
        //查询一下是否已经记录到表
        //只返回Person表的objectId这列的值
        BmobQuery<User2> bmobQuery = new BmobQuery<User2>();
        bmobQuery.addQueryKeys("MachineNumber");
        bmobQuery.setLimit(1000);
        bmobQuery.addWhereEqualTo("MachineNumber",number);
        bmobQuery.findObjects(this, new FindListener<User2>() {
            @Override
            public void onSuccess(List<User2> object) {
                LogHelper.d("queryUser object.size=" + object.size());
                if (object.size() > 0) {
                    permission = true;
                    PrefUtils.setPrefBool(GongZhongHaoService.this,"hadregisted",true);
                    ToastUtils.showLongToast("软件已注册，请放心使用");
                } else  {
                    if (!permission){
                        ToastUtils.showLongToast("软件未注册，没有权限使用");
                    }
                }
                //注意：这里的Person对象中只有指定列的数据。
            }

            @Override
            public void onError(int code, String msg) {
                LogHelper.d("onError");
                LogHelper.e("连接服务器失败，请检查网络：" + msg);
                ToastUtils.showShortToast("连接服务器失败，请检查网络：" + msg);
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void recycle(AccessibilityNodeInfo info) {
        ThreadUtil.sleep(timeOut);
        //更新状态；
        LogHelper.d("recycle()---->currentSatate = " + currentSatate);
        boolean clickCancleNotice = clickTextViewByText("取消关注");
        boolean neverotice = clickTextViewByText("不再关注");

        if (clickCancleNotice){
            currentSatate = STATE_NOMORE;
            isLongClick = false;
        } else if (neverotice){
            currentSatate = STATE_LONG_CLICK;
            isLongClick = false;
        }else if (!isLongClick) {  //保证每次只调一次长按
            LogHelper.d("长按状态...info.getClassName() = "+info.getClassName());
            LogHelper.d("info.getClassName()=" + info.getClassName() + ">>>>>>> 调用LongClick(rootNodeInfo)--->>>>>>>");
            boolean longClick = performLongClick(info,"android.widget.LinearLayout");
            if (longClick) {
                isLongClick = true;
                currentSatate = STATE_CANCAL_NOTICE;
                //长按调用成功，状态先不改变，点击“取消关注按钮”成功调用，
                LogHelper.d("长按成功");
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!permission) {
            return;
        }
        // 接收事件,如触发了通知栏变化、界面变化等
        this.rootNodeInfo = event.getSource();
        if (rootNodeInfo == null) {
            return;
        }
//        checkNodeInfo();

        recycle(rootNodeInfo);
    }



    @Override
    public boolean onUnbind(Intent intent) {
        LogHelper.d("--------------onUnbind 服务");
        ToastUtils.showLongToast("服务已关闭，蓝师傅已停止服务");
        mHandler.removeCallbacksAndMessages(null);
        return super.onUnbind(intent);
    }

    @Override
    protected void onServiceConnected() {
        String timeout = PrefUtils.getPrefString(getApplicationContext(), "fast", 50 + "");
        setServiceInfo(Integer.parseInt(timeout));
        LogHelper.d("-------------onServiceConnected 已连接脚本服务 -----------------------");
//        Toast.makeText(this, "已连接脚本服务", Toast.LENGTH_SHORT).show();
        String message = "已连接脚本服务";
        if (!PrefUtils.getPrefBool(this,PrefUtils.KEY_TRY_FINISH,false)){
            message = "已连接脚本服务,您有一次试用机会，如已注册请忽略";
            permission = true;
            PrefUtils.setPrefBool(this,PrefUtils.KEY_TRY_FINISH,true);
            tryMode();

        }
        ToastUtils.showLongToast(message);

        Bmob.initialize(this, "21c4f08065bc84698c71cb048cf5a024");
        check_toServer();
        String string = PrefUtils.getPrefString(getApplicationContext(),"timeout","0");
        timeOut = Long.parseLong(string);

    }

    private void setServiceInfo(int timeout) {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // We are interested in all types of accessibility events.
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        // We want to provide specific type of feedback.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        // 设置多久监听一次
        info.notificationTimeout = 100;
        // We want to receive accessibility events only from certain packages.
//		info.packageNames = AccessibilityServiceInfo.;
        setServiceInfo(info);
    }

    /**
     * 试用
     */
    private void tryMode(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!PrefUtils.getPrefBool(GongZhongHaoService.this,"hadregisted",false)){
                    permission = false;
                    ToastUtils.showLongToast("试用结束，如果好用请购买");
                }

            }
        },30000);
    }

}