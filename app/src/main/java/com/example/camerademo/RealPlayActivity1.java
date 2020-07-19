package com.example.camerademo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.company.PlaySDK.IPlaySDK;
import com.dh.DpsdkCore.Enc_Channel_Info_Ex_t;
import com.dh.DpsdkCore.Get_RealStream_Info_t;
import com.dh.DpsdkCore.IDpsdkCore;
import com.dh.DpsdkCore.Return_Value_Info_t;
import com.dh.DpsdkCore.fMediaDataCallback;
import com.example.camerademo.application.AppApplication;

public class RealPlayActivity1 extends Activity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        m_pDLLHandle = AppApplication.get().getDpsdkHandle();

        // 查找控件
        findViews();
        // 设置监听器
        setListener();

    }

    private void findViews() {
        tv_camera_title = (TextView) findViewById(R.id.tv_camera_title);
        tv_camera_longitude = (TextView) findViewById(R.id.tv_camera_longitude);
        tv_camera_latitude = (TextView) findViewById(R.id.tv_camera_latitude);

        etCam = (TextView) findViewById(R.id.et_cam_id);
        btOpenVideo = (Button) findViewById(R.id.bt_open_video);
        btCloseVideo = (Button) findViewById(R.id.bt_close_video);
        m_svPlayer = (SurfaceView) findViewById(R.id.sv_player);

        getIntentInfo();

        //int nRet;
        m_nPort = IPlaySDK.PLAYGetFreePort();
        SurfaceHolder holder = m_svPlayer.getHolder();
        holder.addCallback(new Callback() {
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

    private void getIntentInfo() {
        Intent intent = getIntent();
        tv_camera_title.setText(intent.getStringExtra("title"));
        tv_camera_longitude.setText("longitude:" + intent.getStringExtra("longitude"));
        tv_camera_latitude.setText("latitude:" + intent.getStringExtra("latitude"));
		if (getIntent().getStringExtra("channelId") != null)
			m_szCameraId = getIntent().getStringExtra("channelId").getBytes();
        etCam.setText(getIntent().getStringExtra("channelName"));
    }

    private void setListener() {
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
}
