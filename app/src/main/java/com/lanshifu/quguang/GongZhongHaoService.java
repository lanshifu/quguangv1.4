package com.lanshifu.quguang;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.lanshifu.quguang.bean.User2;
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
    private String STATE_UNKNOW = "未知转态";
    private String currentSatate = STATE_UNKNOW;

    private String[] key_world = {"取消关注", "不再关注", "删除该聊天"};  //监听的关键词
    private boolean firstInsert = true;
    private boolean isLongClick = false;  //
    private boolean isLongClickSuccess = false;   //是否成功调用长按，固定状态为“取消关注”
    private boolean isCancleClickSuccess = false;   //"取消关注是否成功调用"，成功调用的话转态固定为“不再关注”
    private long timeOut;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Bmob.initialize(this, "21c4f08065bc84698c71cb048cf5a024");
        check_toServer();
        String string = PrefUtils.getPrefString(getApplicationContext(),"timeout","0");
        timeOut = Long.parseLong(string);


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
        bmobQuery.findObjects(this, new FindListener<User2>() {
            @Override
            public void onSuccess(List<User2> object) {
                int b = -1;
                // TODO Auto-generated method stub
                for (User2 user : object) {
                    if (user.getMachineNumber().equals(number)) {
//						if(user.getBlackNumber()){
//							b=1;
//							break;
//						}else {
                        b = 0;
                        break;
//						}
                    }
                }

                if (b == 0) {
                    permission = true;
                    PrefUtils.setPrefBool(GongZhongHaoService.this,"hadregisted",true);

                } else if (b == -1) {
                    ToastUtils.showLongToast("软件未注册，没有权限使用");
                } else if (b == 1) {
                    ToastUtils.showLongToast( "黑名单用户，没有权限使用");
                }

                //注意：这里的Person对象中只有指定列的数据。
            }

            @Override
            public void onError(int code, String msg) {
                ToastUtils.showShortToast("连接服务器失败，请检查网络：" + msg);
            }
        });

    }


//	/***
//	 * 获取机器码
//	 * @return
//	 */
//	public String getPhoneNumber(){
//		//获取设备码
//		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//		String id = tm.getDeviceId();
//		return id;
//
//	}

    // 长按事件{
    public void LongClick(AccessibilityNodeInfo info) {
//		Log.e("111"," info.getText"+ info.getText());
//		Log.e("111"," info.getclassname()"+info.getClassName());

        //&&info.getPackageName().toString().contains("com.ten")
        if (!firstInsert) {  //第一次进来不执行长按
            if (info.getText() != null) {
//                if (info.getClassName().equals("android.widget.TextView")) {
                    if (info.getParent() == null) {
                        return;
                    }
                    //空指针
                    ThreadUtil.sleep(timeOut);
                    info.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
                    boolean performAction = info.getParent().performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
                    if (performAction) {
                        isLongClick = true;
                        currentSatate = STATE_CANCAL_NOTICE;
                        //长按调用成功，状态先不改变，点击“取消关注按钮”成功调用，
                        Log.e(TAG,"长按成功");
                    }
                }
//            }
        }
        isLongClick = false;


    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void recycle(AccessibilityNodeInfo info) {

        //更新状态；
        Log.e("111", "recycle()---->currentSatate = " + currentSatate);
        // 没有子控件

        if (info != null && info.getChildCount() == 0) {
//			Log.e("111","child widget----------------------------"+ info.getClassName());
//			Log.e("111", "showDialog:" + info.canOpenPopup());
//			Log.e("111", "Text：" + info.getText());
//			Log.e("111", "windowId:" + info.getWindowId());
//			Log.e("111"," info.getclassname()"+info.getClassName());

            // if(info.getText()==null) return;
            if (currentSatate == STATE_CANCAL_NOTICE) {
                if (info.getText() != null)
                    if (info.getText().toString().trim().equals("取消关注")
//                            && info.getClassName().toString().equals("android.widget.TextView")
                            ) {
                        Log.e(TAG, "监听到关键字“取消关注--->>>>>>>>>>,执行点击按钮操作");
                        if (firstInsert == true) {
                            firstInsert = false;
                        }
                        ThreadUtil.sleep(timeOut);
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        boolean performAction = info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if (performAction == true) {
                            currentSatate = STATE_NOMORE;
                            isLongClickSuccess = false;   //取消关注按钮调用结束，可以更新状态
                            isCancleClickSuccess = true;  //取消关注按钮调用成功，updatastate
                        }
//						Log.i(TAG, "点击取消关注，performAction="+performAction+",currentSatate="+currentSatate);

                    }
            } else if (currentSatate == STATE_NOMORE) {
                if (info.getText() != null) {
                    if (info.getText().toString().equals("不再关注")) {
                        Log.e(TAG, "监听到关键字“不再关注--->>>>>>>>>>");

                        ThreadUtil.sleep(timeOut);
                        boolean performAction = info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//						if(performAction==false){
//							 performAction2 = info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//						}
                        if (performAction) {
                            Log.i(TAG, "监听到关键字“不再关注--->>>>>>>>>>,不再关注按钮执行");
                            currentSatate = STATE_LONG_CLICK;
                            isCancleClickSuccess = false;
                        } else {
                            Log.i(TAG, "监听到关键字“不再关注--->>>>>>>>>>,不再关注按钮没有执行成功");
                        }
                    }

                }
            } else if (currentSatate == STATE_DELETE) {
                if (info.getText() != null && info.getText().toString().equals("删除该聊天")) {
                    ThreadUtil.sleep(timeOut);
                    info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    currentSatate = STATE_LONG_CLICK;
                }
            } else if (currentSatate == STATE_LONG_CLICK && isLongClick == false) {  //保证每次只调一次长按

                if (info.getClassName().equals("android.widget.LinearLayout") || info.getClassName().equals("android.widget.TextView")) {
                    Log.e("111", "info.getClassName()=" + info.getClassName() + ">>>>>>> 调用LongClick(rootNodeInfo)--->>>>>>>");

                    LongClick(info);// 调用长按

                }

            }

        } else {    //有子控件
//			if(info!=null){
//			Log.i(TAG, "当前窗口有子控件");
            if (info != null) {
                for (int i = 0; i < info.getChildCount(); i++) {
                    recycle(info.getChild(i));
                }

            }


        }
    }

    /**
     * 更新状态
     *
     * @param text
     */
    private String updateState(String text) {

        if (text.contains("不再收到其下发的消息")) {
            currentSatate = STATE_NOMORE;
        } else if (text.contains("删除该聊天")) {
            currentSatate = STATE_DELETE;
        } else if (text.equals(STATE_CANCAL_NOTICE)) {
            currentSatate = STATE_CANCAL_NOTICE;
        }

        if (isLongClickSuccess == true) {   //调用了长按未调用“取消关注按钮，状态不变”
            currentSatate = STATE_CANCAL_NOTICE;
            return currentSatate;
        }
        if (isCancleClickSuccess == true) {  //说明成功调用了“取消关注按钮，未调用不再关注按钮，状态固定为不再关注”
            currentSatate = STATE_NOMORE;
            return currentSatate;
        }


        return currentSatate;

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (permission == false) {
            return;
        }
        // 接收事件,如触发了通知栏变化、界面变化等
        this.rootNodeInfo = event.getSource();
//		Log.e(TAG, "onAccessibilityEvent");


        if (rootNodeInfo == null) {
//			Log.e(TAG, "rootNodeInfo == null-->return");
            return;
        }


        checkNodeInfo();

    }

    /**
     * 检查节点信息
     */
    private void checkNodeInfo() {

        if (rootNodeInfo == null) {
            return;
        }

		/* 聊天会话窗口，遍历节点匹配 */
        List<AccessibilityNodeInfo> nodes1 = this
                .findAccessibilityNodeInfosByTexts(this.rootNodeInfo, key_world);

        if (!nodes1.isEmpty()) {
            String text = nodes1.get(0).getText().toString();
            currentSatate = updateState(text);

            recycle(rootNodeInfo);

            return;
        } else {

            currentSatate = updateState("没有关键词，长按状态");     //调用
//			Log.i(TAG, "currentSatate==updateState(没有关键词，长按状态)");
            if (currentSatate != STATE_UNKNOW) {
                recycle(rootNodeInfo);
            } else {
//				Log.i("111", "内容不包含关键词，调用recycle---");
                recycle(rootNodeInfo);
            }

        }

    }


    /**
     * 批量化执行AccessibilityNodeInfo.findAccessibilityNodeInfosByText(text).
     * 由于这个操作影响性能,将所有需要匹配的文字一起处理,尽早返回
     *
     * @param nodeInfo 窗口根节点
     * @param texts    需要匹配的字符串们
     * @return 匹配到的节点数组
     */
    private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByTexts(
            AccessibilityNodeInfo nodeInfo, String[] texts) {

        for (String text : texts) {
            if (text == null)
                continue;

            List<AccessibilityNodeInfo> nodes = nodeInfo
                    .findAccessibilityNodeInfosByText(text);

            if (!nodes.isEmpty()) {
                return nodes;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void onInterrupt() {
        Log.d("111", "qianghongbao service interrupt");
        Toast.makeText(this, "中断脚本服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "服务已关闭，蓝师傅已停止服务", Toast.LENGTH_LONG).show();
        return super.onUnbind(intent);
    }

    @Override
    protected void onServiceConnected() {
        // TODO Auto-generated method stub
        String timeout = PrefUtils.getPrefString(getApplicationContext(), "fast", 50 + "");
        setServiceInfo(Integer.parseInt(timeout));
        Toast.makeText(this, "已连接脚本服务", Toast.LENGTH_SHORT).show();
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


}