package com.example.camerademo.application;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.dh.DpsdkCore.IDpsdkCore;
import com.dh.DpsdkCore.Return_Value_Info_t;
import com.dh.DpsdkCore.fDPSDKStatusCallback;


public class AppApplication extends Application {

    private static final String TAG = "AppApplication";
    private static final String LOG_PATH = Environment
            .getExternalStorageDirectory().getPath() + "/DPSDKlog.txt";
    public static final String LAST_GPS_PATH = Environment
            .getExternalStorageDirectory().getPath() + "/LastGPS.xml";

    private static AppApplication _instance;
    private int m_loginHandle = 0; // 标记登录是否成功 1登录成功 0登录失败
    private int m_nLastError = 0;
    private Return_Value_Info_t m_ReValue = new Return_Value_Info_t();

    public static synchronized AppApplication get() {
        return _instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        initApp();
    }

    /**
     * 全局初始化，在SplashActivity中调用
     */
    public void initApp() {

        // Creat DPSDK
        Log.d("initApp:", m_nLastError + "");
        int nType = 1;
        m_nLastError = IDpsdkCore.DPSDK_Create(nType, m_ReValue);//创建SDK句柄
        Log.d("DpsdkCreate:", m_nLastError + "");

        // set logPath
        m_nLastError = IDpsdkCore.DPSDK_SetLog(m_ReValue.nReturnValue,
                LOG_PATH.getBytes());
        Log.d("DPSDK_SetLog:", m_nLastError + "");

        int ret = IDpsdkCore.DPSDK_SetDPSDKStatusCallback(//注册平台状态
                m_ReValue.nReturnValue, new fDPSDKStatusCallback() {

                    @Override
                    public void invoke(int nPDLLHandle, int nStatus) {
                        Log.v("fDPSDKStatusCallback", "nStatus = " + nStatus);
                    }
                });
    }

    @Override
    public void onTerminate() {
        Logout();

        IDpsdkCore.DPSDK_Destroy(getDpsdkCreatHandle());
        super.onTerminate();
    }

    public void Logout() {
        if (getLoginHandler() == 0) {
            return;
        }
        int nRet = IDpsdkCore.DPSDK_Logout(getDpsdkCreatHandle(), 30000);

        if (0 == nRet) {
            // m_loginHandle = 0;
            setLoginHandler(0);
        }
    }

    public int getDpsdkHandle() {
        if (m_loginHandle == 1) // 登录成功，返回PDSDK_Creat时返回的 有效句柄
            return m_ReValue.nReturnValue;
        else
            return 0;
    }

    public int getDpsdkCreatHandle() { // 仅用于获取DPSDK_login的句柄
        return m_ReValue.nReturnValue;
    }

    public void setLoginHandler(int loginhandler) {
        this.m_loginHandle = loginhandler;
    }

    public int getLoginHandler() {
        return this.m_loginHandle;
    }

}
