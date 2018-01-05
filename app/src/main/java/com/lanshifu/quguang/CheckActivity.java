package com.lanshifu.quguang;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lanshifu.quguang.bean.User2;
import com.lanshifu.quguang.utils.BmobUtils;
import com.lanshifu.quguang.utils.PrefUtils;
import com.lanshifu.quguang.utils.ToastUtils;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by 蓝师傅 on 2016/6/11.
 */
public class CheckActivity extends Activity {
    MaterialDialog mMaterialDialog;
    LinearLayout ll_1;
    TextView tv_check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Bmob SDK
        Bmob.initialize(this, "21c4f08065bc84698c71cb048cf5a024");
        setContentView(R.layout.activity_check);
        ll_1 = (LinearLayout) findViewById(R.id.ll_1);
        tv_check = (TextView) findViewById(R.id.tv_check);
        boolean isFirestInsert = PrefUtils.getPrefBool(getApplicationContext(),"isFirestInset",true);
        if(!isFirestInsert){
//            ll_1.setVisibility(View.INVISIBLE);


            //增加缓存，
            boolean hadregisted = PrefUtils.getPrefBool(this, "hadregisted", false);
            if(hadregisted){
                enterHome("版本1.7，2017.5.28");
                ToastUtils.showShortToast("软件已注册，可以放心使用");
            }else {
                check_toServer();
                showDialog(null,"正在验证登录...请稍后",null,true);
            }
        }else{
            ll_1.setVisibility(View.VISIBLE);
        }
    }



    /**
     * 点击按钮
     * @param v
     */
    public void pass(View v){
//        ll_1.setVisibility(View.INVISIBLE);
        PrefUtils.setPrefBool(getApplicationContext(),"isFirestInset",false);
        showDialog(null,"正在验证登录...请回稍后","",false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                check_toServer();
            }
        },300);


    }

    /**
     * 连接服务器
     */
    public void check_toServer(){

        String phoneNumber = BmobUtils.getPhoneNumber(getApplicationContext());
        if(phoneNumber==null||phoneNumber==""||phoneNumber.equals("000000000000000")){
            phoneNumber=PrefUtils.getPrefString(getApplicationContext(),"machineNumber",null);
        }
        queryUser(phoneNumber);

    }


    /**
     *
     * @param title
     * @param message
     * @param havePositiveButton
     */
    public void showDialog(String title, final String message, final String positionButtonName, boolean havePositiveButton){

        mMaterialDialog = new MaterialDialog(this);
        if(havePositiveButton){
            mMaterialDialog.setPositiveButton(positionButtonName, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(positionButtonName!=null&&positionButtonName!=""&&positionButtonName.equals("复制")){
                        mMaterialDialog.setCanceledOnTouchOutside(true);
                        copyNumber(message);
                    }else{
                        enterHome("软件未注册");
                    }


                }
            });
        }else {
            mMaterialDialog.setCanceledOnTouchOutside(false);
        }
        // You can change the message anytime. before show
        if(title!=null){
            mMaterialDialog.setTitle(title);
        }

        // You can change the message anytime. after show
        mMaterialDialog.setMessage(message);
        mMaterialDialog.show();
    }

    /**
     * 复制机器码
     */
    public void copyNumber(String number){
		ClipboardManager cmb = (ClipboardManager) getApplicationContext()
				.getSystemService(Context.CLIPBOARD_SERVICE);
				cmb.setText(number);

				Toast.makeText(getApplicationContext(), "已复制", Toast.LENGTH_SHORT).show();
	}



    private void enterHome(String users_have_to_know) {
        Intent intent = new Intent(CheckActivity.this,MainActivity.class);
        intent.putExtra("users_have_to_know", users_have_to_know);
        intent.putExtra("for",tv_check.getText().toString());
        startActivity(intent);
        finish();

    }



    /**
     *
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
                int b=-1;
                // TODO Auto-generated method stub
                for (User2 user : object) {
                    if (user.getMachineNumber().equals(number)) {
                            b=0;
                            break;
                    }
                }

                if(mMaterialDialog!=null){
                    mMaterialDialog.dismiss();
                }
                if(b==0){
                    enterHome("123");
                    PrefUtils.setPrefBool(CheckActivity.this,"hadregisted",true);
                }else if(b==-1){
                    showDialog("提示信息","软件未注册，进入主页面后点击右上方获取机器码发给卖家进行注册","确定",true);

                }else if(b==1){
                    showDialog("警告","你已经被列入黑名单，不能正常使用软件","确定",true);
                }

                //注意：这里的Person对象中只有指定列的数据。
            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
//                ToastUtils.showToast(getApplication(), "查询用户信息失败：" + msg);
                showDialog(null,"连接服务器失败："+msg,"",false);
            }
        });

    }


}
