package com.example.camerademo.groupTree;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.company.PlaySDK.IPlaySDK;
import com.dh.DpsdkCore.Enc_Channel_Info_Ex_t;
import com.dh.DpsdkCore.Get_RealStream_Info_t;
import com.dh.DpsdkCore.IDpsdkCore;
import com.dh.DpsdkCore.Login_Info_t;
import com.dh.DpsdkCore.Return_Value_Info_t;
import com.dh.DpsdkCore.dpsdk_retval_e;
import com.dh.DpsdkCore.fMediaDataCallback;
import com.example.camerademo.MainActivity;
import com.example.camerademo.R;
import com.example.camerademo.application.AppApplication;
import com.example.camerademo.baseclass.BaseActivity;
import com.example.camerademo.view.PullDownListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LoginRealPlayActivity extends BaseActivity implements
        PullDownListView.OnRefreshListioner {

    // 打印标签
    private static final String TAG = "LoginRealPlayActivity";

    // 获取实例
    private GroupListManager mGroupListManager = null;

    // 等待对话框
    private ProgressBar mWattingPb = null;


    private String deviceId;
    private String deviceName;
    private String[] dialogList;
    private LinearLayout layLogout;
    private String mDeviceId;

    //    //标记是否第一次登入
//    private String isfirstLogin;
    String tmp_strPort;
    byte[] tmp_szIp, tmp_szUsername, tmp_szPassword;
    protected ProgressDialog mProgressDialog;
    private AppApplication mAPP = AppApplication.get();
    boolean isLogin = false;//记录是否登录成功


    /**
     * 视频播放
     */
    private TextView tv_camera_title;
    private TextView tv_camera_longitude;
    private TextView tv_camera_latitude;

    //打开视频按钮
    private Button btOpenVideo;
    //关闭视频按钮
    private Button btCloseVideo;
    private TextView etCam;
    private byte[] m_szCameraId = null;
    private int m_pDLLHandle = 0;
    SurfaceView m_svPlayer = null;
    private int m_nPort = 0;
    private int m_nSeq = 0;
    private int mTimeOut = 30 * 1000;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        login();//先登录，若登录成功则显示视频
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume--");
    }

    /**
     * <p>
     * 获取布局控件
     * </p>
     *
     * @author fangzhihua 2014-5-6 下午2:27:25
     */
    private void findViews() {

        layLogout = (LinearLayout) findViewById(R.id.title_lay);

        // 等待对话框布局
        mWattingPb = (ProgressBar) findViewById(R.id.grouplist_waitting_pb);


        /**
         * 视频播放
         */
        tv_camera_title = (TextView) findViewById(R.id.tv_camera_title);
        tv_camera_longitude = (TextView) findViewById(R.id.tv_camera_longitude);
        tv_camera_latitude = (TextView) findViewById(R.id.tv_camera_latitude);

        etCam = (TextView) findViewById(R.id.et_cam_id);
        btOpenVideo = (Button) findViewById(R.id.bt_open_video);
        btCloseVideo = (Button) findViewById(R.id.bt_close_video);
        m_svPlayer = (SurfaceView) findViewById(R.id.sv_player);
    }

    /**
     * <p>
     * 设置控件的监听事件
     * </p>
     *
     * @author fangzhihua 2014-5-6 下午2:34:04
     */
    private void setListener() {
        layLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                logOut();
            }

        });


        /**
         * 视频播放
         */
        btCloseVideo.setEnabled(false);
        btCloseVideo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //int ret = IDpsdkCore.DPSDK_CloseRealStreamByCameraId(m_pDLLHandle, m_szCameraId, mTimeOut);
                int ret = IDpsdkCore.DPSDK_CloseRealStreamBySeq(m_pDLLHandle, m_nSeq, mTimeOut);
                if (ret == 0) {
                    btOpenVideo.setEnabled(true);
                    btCloseVideo.setEnabled(false);
                    Log.e("xss", "DPSDK_CloseRealStreamByCameraId success!");
                    Toast.makeText(getApplicationContext(), "Close video success!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("xss", "DPSDK_CloseRealStreamByCameraId failed! ret = " + ret);
                    Toast.makeText(getApplicationContext(), "Close video failed!", Toast.LENGTH_SHORT).show();
                }
                StopRealPlay();
            }
        });

    }

    ///////////////////////////////////////////////
    private void login() {
        showLoadingProgress(R.string.login);

        tmp_szIp = "127.0.0.1".getBytes();
        tmp_strPort = "9000";
        tmp_szUsername = "admin".getBytes();
        tmp_szPassword = "123456".getBytes();

        new LoginTask().execute(); //请求登录线程过程
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

            // 登录成功后初始化数据
            initDate();//显示实时监控 测试用，可删

            if (result == 0) {

                // 登录成功后初始化数据
                initDate();
                isLogin = true;


                //登录成功，开启GetGPSXMLTask线程
                new GetGPSXMLTask().execute();

                Log.d("DpsdkLogin success:", result + "");
                IDpsdkCore.DPSDK_SetCompressType(mAPP.getDpsdkCreatHandle(), 0);
                mAPP.setLoginHandler(1);
                //	m_loginHandle = 1;

                initDate();//显示实时监控

            } else {
                if (mWattingPb != null) {
                    mWattingPb.setVisibility(View.GONE);
                }
                isLogin = false;

                Log.d("DpsdkLogin failed:", result + "");
                Toast.makeText(getApplicationContext(), "login failed" + result, Toast.LENGTH_SHORT).show();
                mAPP.setLoginHandler(0);
                //m_loginHandle = 0;
                //jumpToContentListActivity();
            }
        }

    }

    private void saveLoginInfo() {   //保存信息
        SharedPreferences sp = getSharedPreferences("LOGININFO", 0);
        SharedPreferences.Editor ed = sp.edit();
        StringBuilder sb = new StringBuilder();
        sb.append(tmp_szIp).append(",").append(tmp_strPort).append(",")
                .append(tmp_szPassword).append(",").append(tmp_szUsername.toString());
        ed.putString("INFO", sb.toString());
        ed.putString("ISFIRSTLOGIN", "false");
        ed.commit();
        Log.i("TestDpsdkCoreActivity", "saveLoginInfo" + sb.toString());
    }


    protected void showLoadingProgress(int resId) {   //showLoadingProgress(R.string.login); 正在登录~
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
        } else {
            mProgressDialog = ProgressDialog.show(this, null, getString(resId));
            mProgressDialog.setCancelable(false);
        }
    }


    //读取GPSXMl 模块
    class GetGPSXMLTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            int nRet = GetGPSXML();
            return nRet;
        }


        @Override
        protected void onPostExecute(Integer result) {
            Toast.makeText(LoginRealPlayActivity.this, "GetGPSXML nRet" + result, Toast.LENGTH_SHORT).show();
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

    ///////////////////////////////////////
    private void logOut() {

        if (isLogin) {
            int nPDLLHandle = AppApplication.get().getDpsdkHandle();
            int nRet = IDpsdkCore.DPSDK_Logout(nPDLLHandle, 30000);
            if (nRet == 0) {
                showToast(getResources().getString(R.string.logout));
            } else {
                showToast(getResources().getString(R.string.logout_fail));
            }
        }

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        LoginRealPlayActivity.this.finish();
    }


    /**
     * <p>
     * 初始化数据
     * </p>
     *
     * @author fangzhihua 2014-5-7 上午10:05:54
     */
    private void initDate() {
        setContentView(R.layout.group_list_fragment);  //显示视频界面
        m_pDLLHandle = AppApplication.get().getDpsdkHandle();

        //查找控件
        findViews();

        // 设置监听器
        setListener();

        Display display = this.getWindowManager().getDefaultDisplay();

        mGroupListManager = GroupListManager.getInstance();

        initPlay();//初始化
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // mGroupListHelper.closeSetTimePopupWindow();
        if (mGroupListManager != null) {
            if (mGroupListManager.getRootNode() != null) {
                mGroupListManager.setRootNode(null);
            }
        }
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRefresh() {
        Log.i("LoginRealPlayActivity", "onRefresh..");
    }

    @Override
    public void onLoadMore() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mGroupListManager.getRootNode() != null) {
                mGroupListManager.setRootNode(null);
                Log.i("TAG", "onKeyDown");
            }
            logOut();
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 视频播放
     */
    private void initPlay() {
        getIntentInfo();

        //int nRet;
        m_nPort = IPlaySDK.PLAYGetFreePort();
        SurfaceHolder holder = m_svPlayer.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("xss", "surfaceCreated");
                IPlaySDK.InitSurface(m_nPort, m_svPlayer);
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                Log.d("xss", "surfaceChanged");
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("xss", "surfaceDestroyed");
            }
        });

        final fMediaDataCallback fm = new fMediaDataCallback() {

            @Override
            public void invoke(int nPDLLHandle, int nSeq, int nMediaType,
                               byte[] szNodeId, int nParamVal, byte[] szData, int nDataLen) {

                int ret = IPlaySDK.PLAYInputData(m_nPort, szData, nDataLen);
                if (ret == 1) {
                    Log.e("xss", "playing success=" + nSeq + " package size=" + nDataLen);
                } else {
                    Log.e("xss", "playing failed=" + nSeq + " package size=" + nDataLen);
                }
            }
        };




        btOpenVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!StartRealPlay()) {
                    Log.e("xss", "StartRealPlay failed!");
                    Toast.makeText(getApplicationContext(), "Open video failed!", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    Return_Value_Info_t retVal = new Return_Value_Info_t();

                    Get_RealStream_Info_t getRealStreamInfo = new Get_RealStream_Info_t();
                    //m_szCameraId = etCam.getText().toString().getBytes();

                    System.arraycopy(m_szCameraId, 0, getRealStreamInfo.szCameraId, 0, m_szCameraId.length);
                    //getRealStreamInfo.szCameraId = "1000096$1$0$0".getBytes();
                    getRealStreamInfo.nMediaType = 1;
                    getRealStreamInfo.nRight = 0;
                    getRealStreamInfo.nStreamType = 1;
                    getRealStreamInfo.nTransType = 1;
                    Enc_Channel_Info_Ex_t ChannelInfo = new Enc_Channel_Info_Ex_t();
                    IDpsdkCore.DPSDK_GetChannelInfoById(m_pDLLHandle, m_szCameraId, ChannelInfo);
                    int ret = IDpsdkCore.DPSDK_GetRealStream(m_pDLLHandle, retVal, getRealStreamInfo, fm, mTimeOut);
                    if (ret == 0) {
                        btOpenVideo.setEnabled(false);
                        btCloseVideo.setEnabled(true);
                        m_nSeq = retVal.nReturnValue;
                        Log.e("DPSDK_GetStream ok", ret + "");
                        Toast.makeText(getApplicationContext(), "Open video success!", Toast.LENGTH_SHORT).show();
                    } else {
                        StopRealPlay();
                        Log.e("DPSDK_GetStream failed", ret + "");
                        Toast.makeText(getApplicationContext(), "Open video failed!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("xss", e.toString());
                }
            }
        });
    }

    public void StopRealPlay() {
        try {
            IPlaySDK.PLAYStopSoundShare(m_nPort);
            IPlaySDK.PLAYStop(m_nPort);
            IPlaySDK.PLAYCloseStream(m_nPort);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean StartRealPlay() {
        if (m_svPlayer == null)
            return false;

        boolean bOpenRet = IPlaySDK.PLAYOpenStream(m_nPort, null, 0, 1500 * 1024) == 0 ? false : true;
        IPlaySDK.PLAYSetDecodeThreadNum(m_nPort, 4);
        if (bOpenRet) {
            boolean bPlayRet = IPlaySDK.PLAYPlay(m_nPort, m_svPlayer) == 0 ? false : true;
            Log.i("StartRealPlay", "StartRealPlay1");
            if (bPlayRet) {
                boolean bSuccess = IPlaySDK.PLAYPlaySoundShare(m_nPort) == 0 ? false : true;

                Log.i("StartRealPlay", "StartRealPlay2");
                if (!bSuccess) {
                    IPlaySDK.PLAYStop(m_nPort);
                    IPlaySDK.PLAYCloseStream(m_nPort);
                    Log.i("StartRealPlay", "StartRealPlay3");
                    return false;
                }
            } else {
                IPlaySDK.PLAYCloseStream(m_nPort);
                Log.i("StartRealPlay", "StartRealPlay4");
                return false;
            }
        } else {
            Log.i("StartRealPlay", "StartRealPlay5");
            return false;
        }

        return true;
    }

    private void getIntentInfo() {
        Intent intent = getIntent();
        String strDeviceId = intent.getStringExtra("channelId");
//        byte[] bt = (strDeviceId.trim() + "$1$0$0").getBytes();
//        byte[] szId = new byte[dpsdk_constant_value.DPSDK_CORE_DEV_ID_LEN];
//        System.arraycopy(bt, 0, szId, 0, bt.length);
//
//        String channelname = new String(szId).trim();

        tv_camera_title.setText(intent.getStringExtra("title"));
        tv_camera_longitude.setText("longitude:" + intent.getStringExtra("longitude"));
        tv_camera_latitude.setText("latitude:" + intent.getStringExtra("latitude"));
        if (intent.getStringExtra("channelId") != null)
            m_szCameraId = strDeviceId.getBytes();
        etCam.setText(intent.getStringExtra("channelName"));
    }

}
