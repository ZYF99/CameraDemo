package com.example.camerademo.Demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dh.DpsdkCore.IDpsdkCore;
import com.dh.DpsdkCore.Login_Info_t;
import com.dh.DpsdkCore.Return_Value_Info_t;
import com.dh.DpsdkCore.dpsdk_retval_e;
import com.example.camerademo.R;
import com.example.camerademo.application.AppApplication;
import com.example.camerademo.groupTree.LoginRealPlayActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class TestDpsdkCoreActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    String tmp_strPort;
    byte[] tmp_szIp, tmp_szUsername, tmp_szPassword;



    Button m_btLogin;
    static IDpsdkCore dpsdkcore = new IDpsdkCore();
    EditText m_serverIp;
    EditText m_serverPort;
    EditText m_serverUserName;
    EditText m_serverPassword;
    Resources res;

    //标记是否第一次登入
    private String isfirstLogin;
    protected ProgressDialog mProgressDialog;
    private int mDPSDKHandler;
    private AppApplication mAPP = AppApplication.get();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dh_login);
//        mAPP.initApp();

        getIntentInfo();

        m_btLogin = (Button) findViewById(R.id.buttonLogin);
        m_serverIp = (EditText) findViewById(R.id.editText_server);
        m_serverPort = (EditText) findViewById(R.id.editText_server_Port);
        m_serverUserName = (EditText) findViewById(R.id.editText_server_user);
        m_serverPassword = (EditText) findViewById(R.id.editText_server_password);
        isfirstLogin = getSharedPreferences("LOGININFO", 0).getString("ISFIRSTLOGIN", "");
        if (isfirstLogin.equals("false")) {
            setEditTextContent();
        }

        m_btLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingProgress(R.string.login);

                tmp_szIp = m_serverIp.getText().toString().getBytes();
                tmp_strPort = m_serverPort.getText().toString().trim();
                tmp_szUsername = m_serverUserName.getText().toString().getBytes();
                tmp_szPassword = m_serverPassword.getText().toString().getBytes();

                new LoginTask().execute();
            }
        });

    }

    class LoginTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... arg0) {               //在此处处理UI会导致异常
//			if (mloginHandle != 0) {
//	    		IDpsdkCore.DPSDK_Logout(m_loginHandle, 30000);
//        		m_loginHandle = 0;
//	    	}
            Login_Info_t loginInfo = new Login_Info_t();
            Integer error = Integer.valueOf(0);
            loginInfo.szIp = tmp_szIp;
            String strPort = tmp_strPort;
            loginInfo.nPort = Integer.parseInt(strPort);
            loginInfo.szUsername = tmp_szUsername;
            loginInfo.szPassword = tmp_szPassword;
            loginInfo.nProtocol = 2;
            saveLoginInfo();
            int nRet = IDpsdkCore.DPSDK_Login(mAPP.getDpsdkCreatHandle(), loginInfo, 30000);
            return nRet;
        }

        @Override
        protected void onPostExecute(Integer result) {

            super.onPostExecute(result);
            mProgressDialog.dismiss();
            if (result == 0) {

                //登录成功，开启GetGPSXMLTask线程
                new GetGPSXMLTask().execute();

                Log.d("DpsdkLogin success:", result + "");
                IDpsdkCore.DPSDK_SetCompressType(mAPP.getDpsdkCreatHandle(), 0);
                mAPP.setLoginHandler(1);
                //	m_loginHandle = 1;
                jumpToItemListActivity();
            } else {
                Log.d("DpsdkLogin failed:", result + "");
                Toast.makeText(getApplicationContext(), "login failed" + result, Toast.LENGTH_SHORT).show();
                mAPP.setLoginHandler(0);
                //m_loginHandle = 0;
                //jumpToContentListActivity();
            }
        }

    }

    /**
     * 取出 sharedpreference的登录信息并显示
     */
    private void setEditTextContent() {
        SharedPreferences sp = getSharedPreferences("LOGININFO", 0);
        String content = sp.getString("INFO", "");
        String[] loginInfo = content.split(",");
        if (loginInfo != null) {
            m_serverIp.setText(loginInfo[0]);
            m_serverPort.setText(loginInfo[1]);
            m_serverPassword.setText(loginInfo[2]);
            m_serverUserName.setText(loginInfo[3]);
        }
        Log.i("TestDpsdkCoreActivity", "setEditTextContent" + content);
    }

    private void saveLoginInfo() {
        SharedPreferences sp = getSharedPreferences("LOGININFO", 0);
        Editor ed = sp.edit();
        StringBuilder sb = new StringBuilder();
        sb.append(m_serverIp.getText().toString()).append(",").append(m_serverPort.getText().toString()).append(",")
                .append(m_serverPassword.getText().toString()).append(",").append(m_serverUserName.getText().toString());
        ed.putString("INFO", sb.toString());
        ed.putString("ISFIRSTLOGIN", "false");
        ed.commit();
        Log.i("TestDpsdkCoreActivity", "saveLoginInfo" + sb.toString());
    }

    protected void showLoadingProgress(int resId) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
        } else {
            mProgressDialog = ProgressDialog.show(this, null, getString(resId));
            mProgressDialog.setCancelable(false);
        }
    }

    private String strCameraTitle,strCameraLongitude,strCameraLatitude;
    private void getIntentInfo() {
        Intent intent = getIntent();
        strCameraTitle = intent.getStringExtra("title");
        strCameraLongitude = intent.getStringExtra("longitude");
        strCameraLatitude = intent.getStringExtra("latitude");
    }

    public void jumpToItemListActivity() {
        Intent intent = new Intent();
        intent.setClass(this, LoginRealPlayActivity.class);
        //intent.setClass(this, ItemListActivity.class);

        intent.putExtra("title", strCameraTitle);

        intent.putExtra("longitude", strCameraLongitude);
        intent.putExtra("latitude", strCameraLatitude);


        startActivity(intent);
        finish();
    }

    //逻辑有问题，放在Application类中执行
//    public void Logout()
//    {
//    	if (mAPP.getLoginHandler() == 0)
//    	{
//    		return;
//    	}
//    	int nRet = IDpsdkCore.DPSDK_Logout(mAPP.getDpsdkCreatHandle(), 30000);
//    	
//    	if ( 0 == nRet )
//    	{
//    		//m_loginHandle = 0;
//    		mAPP.setLoginHandler(0);
//    	}
//    }
//    
//	@Override
//	protected void onDestroy() 
//	{   
//		Logout();
//		
//		IDpsdkCore.DPSDK_Destroy(mAPP.getDpsdkCreatHandle());
//		
//		super.onDestroy();		
//	}

    //读取GPSXMl 模块
    class GetGPSXMLTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            int nRet = GetGPSXML();
            return nRet;
        }


        @Override
        protected void onPostExecute(Integer result) {
            Toast.makeText(TestDpsdkCoreActivity.this, "GetGPSXML nRet" + result, Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
        }

    }

    public int GetGPSXML() {
        int res = -1;
        Return_Value_Info_t nGpsXMLLen = new Return_Value_Info_t();
        int nRet = IDpsdkCore.DPSDK_AskForLastGpsStatusXMLStrCount(mAPP.getDpsdkCreatHandle(), nGpsXMLLen, 10 * 1000);
        if (nRet == dpsdk_retval_e.DPSDK_RET_SUCCESS && nGpsXMLLen.nReturnValue > 1) {
            byte[] LastGpsIStatus = new byte[nGpsXMLLen.nReturnValue - 1];
            nRet = IDpsdkCore.DPSDK_AskForLastGpsStatusXMLStr(mAPP.getDpsdkCreatHandle(), LastGpsIStatus, nGpsXMLLen.nReturnValue);

            if (nRet == dpsdk_retval_e.DPSDK_RET_SUCCESS) {

                //System.out.printf("获取GPS XML成功，nRet = %d， LastGpsIStatus = [%s]", nRet, new String(LastGpsIStatus));
                Log.d("GetGPSXML", String.format("获取GPS XML成功，nRet = %d， LastGpsIStatus = [%s]", nRet, new String(LastGpsIStatus)));
                try {
                    File file = new File(AppApplication.LAST_GPS_PATH); // 路径  sdcard/LastGPS.xml
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(LastGpsIStatus);
                    out.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else {
                //System.out.printf("获取GPS XML失败，nRet = %d", nRet);
                Log.d("GetGPSXML", String.format("获取GPS XML失败，nRet = %d", nRet));
            }
        } else if (nRet == dpsdk_retval_e.DPSDK_RET_SUCCESS && nGpsXMLLen.nReturnValue == 0) {
            //System.out.printf("获取GPS XML  XMLlength = 0");
            Log.d("GetGPSXML", "获取GPS XML  XMLlength = 0");
        } else {
            //System.out.printf("获取GPS XML失败，nRet = %d", nRet);
            Log.d("GetGPSXML", String.format("获取GPS XML失败，nRet = %d", nRet));
        }
        //System.out.println();
        res = nRet;
        return res;
    }

}