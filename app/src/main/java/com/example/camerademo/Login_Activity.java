package com.example.camerademo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.camerademo.Api.GetRequestInterface;
import com.example.camerademo.bean.userBean;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @BelongsProject: Login_Activity
 * @BelongsPackage: com.example.administrator.myapplication
 * @Author: Administrator
 * @CreateTime: 2020-01-08 10:58
 */

public class Login_Activity extends Activity implements View.OnClickListener {

    private EditText edit_account, edit_password;
    private TextView text_msg;
    private Button btn_login, btn_register;
    private ImageButton openpwd;
    private boolean flag = false;
    private String account, password;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    private void init() {
        edit_account = (EditText) findViewById(R.id.edit_account);
        edit_account.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    edit_account.clearFocus();
                }
                return false;
            }
        });
        edit_password = (EditText) findViewById(R.id.edit_password);
        edit_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    edit_password.clearFocus();
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edit_password.getWindowToken(), 0);
                }
                return false;
            }
        });
        text_msg = (TextView) findViewById(R.id.text_msg);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_register = (Button) findViewById(R.id.btn_register);
        openpwd = (ImageButton) findViewById(R.id.btn_openpwd);
        text_msg.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        openpwd.setOnClickListener(this);
        dbHelper = new DBHelper(this, "Data.db", null, 1);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (edit_account.getText().toString().trim().equals("") | edit_password.getText().
                        toString().trim().equals("")) {
                    Toast.makeText(this, "请输入账号或者注册账号！", Toast.LENGTH_SHORT).show();
                } else {
                    readUserInfo();
                }
                break;
            case R.id.btn_register:
                Intent intent = new Intent(Login_Activity.this, Register_Activity.class);
                startActivity(intent);
                break;
            case R.id.btn_openpwd:
                if (flag == true) {//不可见
                    edit_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    flag = false;
                    openpwd.setBackgroundResource(R.drawable.invisible);
                } else {
                    edit_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    flag = true;
                    openpwd.setBackgroundResource(R.drawable.visible);
                }
                break;
            case R.id.text_msg:
                Intent i = new Intent(Login_Activity.this, ForgotInfo_activity.class);
                startActivity(i);
                break;
        }
    }

    /**
     * 读取SharedPreferences存储的键值对
     */
    public void readUsersInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("UsersInfo", MODE_PRIVATE);
        account = sharedPreferences.getString("username", "");
        password = sharedPreferences.getString("password", "");
    }

    /**
     * 读取UserData.db中的用户信息
     */
    protected void readUserInfo() {
        netLogin(edit_account.getText().toString(), edit_password.getText().toString());
//        if (login(edit_account.getText().toString(), edit_password.getText().toString())) {
//            Toast.makeText(this, "登陆成功！", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(Login_Activity.this,MainActivity.class);
//            intent.putExtra("Username",edit_account.getText().toString());
//            startActivity(intent);
//        } else {
//            Toast.makeText(this, "账户或密码错误，请重新输入！！", Toast.LENGTH_SHORT).show();
//        }
    }

    /**
     * 验证登录信息
     */
    public boolean login(String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "Select * from usertable where username=? and password=?";
        Cursor cursor = db.rawQuery(sql, new String[]{username, password});
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        }
        return true;//todu
    }

    private void netLogin(String username, String password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfg.APP_HOST) //设置网络请求的Url地址"http://192.168.0.100:8081/map/"
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .build();

        final GetRequestInterface request = retrofit.create(GetRequestInterface.class);
        Call<userBean> call = request.login(username, password);

        //发送网络请求(异步)
        call.enqueue(new Callback<userBean>() {
            @Override
            public void onResponse(Call<userBean> call, Response<userBean> response) {
                //Log.i(TAG, "loadDetail->onResponse(MainActivity.java): "+response.body());
                Gson gson = new Gson();
                userBean userInfo = response.body();
                Log.i("cd login info", gson.toJson(userInfo));
                if (userInfo == null){
                    Toast.makeText(Login_Activity.this, "服务器错误！！", Toast.LENGTH_SHORT).show();
                }
                if (userInfo.getCode() == 200){
                    Toast.makeText(Login_Activity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login_Activity.this, MainActivity.class);
                    intent.putExtra("Username", edit_account.getText().toString());
                    startActivity(intent);
                }else {
                    Toast.makeText(Login_Activity.this, "账户或密码错误，请重新输入！！", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<userBean> call, Throwable t) {
//                Log.i(TAG, "loadDetail->onFailure(MainActivity.java): " + t.toString());
//                Log.i("uuu", t.toString());
                Toast.makeText(Login_Activity.this, "账户或密码错误，请重新输入！！", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
