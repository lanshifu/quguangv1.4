package com.lanshifu.quguang;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.lanshifu.quguang.bean.Update;
import com.lanshifu.quguang.bean.User2;
import com.lanshifu.quguang.utils.BmobUtils;
import com.lanshifu.quguang.utils.PrefUtils;
import com.lanshifu.quguang.utils.ToastUtils;

import java.util.List;
import java.util.Random;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import cn.bmob.v3.update.UpdateStatus;
import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final Intent mAccessibleIntent = new Intent(
            Settings.ACTION_ACCESSIBILITY_SETTINGS);

    private Button switchPlugin;
    private ImageView iv_help;
    private String forUser;
    private MaterialDialog mMaterialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "作者：蓝师傅 wx404985095", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        forUser = getIntent().getStringExtra("for");
        switchPlugin = (Button) findViewById(R.id.button_accessible);
        iv_help = (ImageView) findViewById(R.id.iv_help);
        updateServiceStatus();
    }


    public void onButtonClicked(View view) {
        ToastUtils.showLongToast("找到‘"+getString(R.string.app_name) +"',打开开关");
        startActivity(mAccessibleIntent); // 启动系统菜单
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }

    private void updateServiceStatus() {
        boolean serviceEnabled = false; // 默认服务不可用
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices = accessibilityManager
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            // 遍历判断服务是否打开
            if (info.getId().equals(getPackageName() + "/.GongZhongHaoService")) {
                serviceEnabled = true;
                break;
            }
        }

        if (serviceEnabled) {
            switchPlugin.setText("关闭脚本插件");
            iv_help.setVisibility(View.GONE);
            // Prevent screen from dimming
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            switchPlugin.setText("开启脚本插件");
            iv_help.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            setting();
            return true;
        } else if (id == R.id.action_for) {
            showInfoDialog(forUser);
            return true;
        } else if (id == R.id.action_mechineNumber) {
            //获取机器码
            String number = BmobUtils.getPhoneNumber(getApplicationContext());
            showDialog_Number(number);

        } else if (id == R.id.action_check) {
            //检查注册情况
            String phoneNumber = BmobUtils.getPhoneNumber(getApplicationContext());
            if (phoneNumber == null || phoneNumber == "" || phoneNumber.equals("000000000000000")) {
                phoneNumber = PrefUtils.getPrefString(getApplicationContext(), "machineNumber", null);
            }
            queryUser(phoneNumber);
        } else if (id == R.id.update) {
            //检查更新
//            checkUpdate();
            checkUpdataAndDownload();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUpdate() {

        BmobQuery<Update> bmobQuery = new BmobQuery<Update>();
        bmobQuery.getObject(MainActivity.this, "R1MvAAAO", new GetListener<Update>() {
            @Override
            public void onSuccess(Update updateBean) {
                if (updateBean.getNewVersion()) {
                    showUpdateDialog(updateBean.getUrl());

                } else {
                    showInfoDialog("已是最新版本！");
                }
            }

            @Override
            public void onFailure(int i, String s) {
                showInfoDialog("网络连接失败：" + s);
            }
        });

    }

    /**
     * 跳转到网页更新
     *
     * @param url
     */
    private void showUpdateDialog(String url) {
//        final Uri uri = Uri.parse("http://www.baidu.com");
        final Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


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
                if (mMaterialDialog != null) {
                    mMaterialDialog.dismiss();
                }
                if (object.size() >0) {
                    showInfoDialog("软件已注册");
                } else {
                    showInfoDialog("软件未注册");
                }

                //注意：这里的Person对象中只有指定列的数据。
            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                showInfoDialog("连接服务器出错：" + msg);
            }
        });

    }


    public void showDialog_Number(final String number) {
        mMaterialDialog = new MaterialDialog(this);
        if (TextUtils.isEmpty(number)) {
            String random = PrefUtils.getPrefString(getApplicationContext(), "machineNumber", null);
            if (random == null) {
                int i = new Random().nextInt(10) + 8612345;
                random = i + "";
                mMaterialDialog.setMessage("获取不到本机机器码,生成随机机器码:" + random);
                PrefUtils.setPrefString(getApplicationContext(), "machineNumber", random);
            } else {
                mMaterialDialog.setMessage(random);
            }

        } else {
            mMaterialDialog.setMessage(number);
        }
        mMaterialDialog.setCanceledOnTouchOutside(true);
        mMaterialDialog.setPositiveButton("复制机器码", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String random = PrefUtils.getPrefString(getApplicationContext(), "machineNumber", null);
                if (number == null || number.isEmpty()) {
                    copyNumber(random);
                } else {
                    copyNumber(number);
                }

                mMaterialDialog.dismiss();
            }
        });
        mMaterialDialog.show();

    }

    /**
     * 复制机器码
     */
    public void copyNumber(String number) {
        ClipboardManager cmb = (ClipboardManager) getApplicationContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(number + "");
        Toast.makeText(getApplicationContext(), "已复制", Toast.LENGTH_SHORT).show();
    }

    public void showInfoDialog(String info) {
        if (mMaterialDialog != null) {
            mMaterialDialog.dismiss();
        }
        mMaterialDialog = new MaterialDialog(this);
        if (info != null) mMaterialDialog.setMessage(info);
        mMaterialDialog.setCanceledOnTouchOutside(true);
        mMaterialDialog.show();

    }

    /**
     * 、设置灵敏度
     */
    private void setting() {
        // 设置多久监听一次
        //info.notificationTimeout = EVENT_NOTIFICATION_TIMEOUT_MILLIS;
        mMaterialDialog = new MaterialDialog(this);
        mMaterialDialog.setCanceledOnTouchOutside(true);
        mMaterialDialog.setTitle("公众号取关完又出来？来这里设置取关速度吧，越右边速度越慢（建议数字在1000以内）");
        View settingView = View.inflate(getApplicationContext(), R.layout.setting, null);
        mMaterialDialog.setContentView(settingView);
        final SeekBar seekBar = (SeekBar) settingView.findViewById(R.id.seekBar);
        seekBar.setProgress(Integer.parseInt(PrefUtils.getPrefString(getApplicationContext(), "fast", 100 + "")));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Snackbar.make(seekBar, ""+progress, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mMaterialDialog.setPositiveButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = seekBar.getProgress();
                PrefUtils.setPrefString(getApplicationContext(), "fast", progress + "");
                mMaterialDialog.dismiss();
                ToastUtils.showShortToast("设置成功，请重新启动插件服务");
            }
        });
        mMaterialDialog.show();
    }


    private void checkUpdataAndDownload(){
        ToastUtils.showShortToast("正在检查更新");
//        BmobUpdateAgent.initAppVersion(this);
        BmobUpdateAgent.setUpdateOnlyWifi(false);
//        BmobUpdateAgent.update(this);
        BmobUpdateAgent.forceUpdate(this);
        BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {

            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                // TODO Auto-generated method stub
                Log.e(TAG, "updateStatus: "+updateStatus);
                if (updateStatus == UpdateStatus.Yes) {//版本有更新

                }else if(updateStatus == UpdateStatus.No){
                   ToastUtils.showShortToast("版本无更新");
                }
            }
        });
    }

}
