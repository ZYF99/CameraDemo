package com.example.camerademo.Demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.company.PlaySDK.IPlaySDK;
import com.dh.DpsdkCore.Enc_Channel_Info_Ex_t;
import com.dh.DpsdkCore.Get_RealStream_Info_t;
import com.dh.DpsdkCore.IDpsdkCore;
import com.dh.DpsdkCore.Login_Info_t;
import com.dh.DpsdkCore.Ptz_Direct_Info_t;
import com.dh.DpsdkCore.Ptz_Operation_Info_t;
import com.dh.DpsdkCore.Return_Value_Info_t;
import com.dh.DpsdkCore.fMediaDataCallback;
import com.example.camerademo.LatLngM;
import com.example.camerademo.R;
import com.example.camerademo.application.AppApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RealPlayActivity extends Activity {
	
	//打开视频按钮
	private Button btOpenVideo;
	//关闭视频按钮
	private Button btCloseVideo;
	private Button btLeft;
	private Button btRight;
	private Button btTop;
	private Button btBottom;
	/*private Button btAddZoom;
	private Button btReduceZoom;*/
	private Button btAddFocus;
	private Button btReduceFocus;
	private Button btAddAperture;
	private Button btReduceAperture;

    // 超时时间
    private static final int DPSDK_CORE_DEFAULT_TIMEOUT = 600000;
	private Button btnCaptureImg;
	public final static String IMAGE_PATH = Environment.getExternalStorageDirectory().getPath() + "/snapshot/";
	public final static String IMGSTR = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";
	private static final int PicFormat_JPEG = 1;
	
	//private TextView etCam;
	private TextView etLon;
	private TextView etLat;
	private TextView etName;
	private TextView sxtName;
	private TextView etdirbction;
	private TextView etCam;
	private TextView cam_SYFL;
	private TextView cam_jsrq;
	private TextView cam_TDWHDW;
	private TextView cam_LDLX;
	private TextView cam_oabmmc;
	private TextView tvRet;
	
	private byte[] m_szCameraId = null;
	private int m_pDLLHandle = 0;
	SurfaceView m_svPlayer = null;
	private int m_nPort = 0;
	private int m_nSeq = 0;
	private int m_control = 0;
	private int mTimeOut = 30*1000;

    private AppApplication mAPP = AppApplication.get();
	// 设备树是否获取过
    private static boolean mIsDeviceInfo = false;

    private LatLngM lngM;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_real_play);
		
		m_pDLLHandle = AppApplication.get().getDpsdkHandle();
		new LoginTask().execute();
		lngM = (LatLngM) getIntent().getSerializableExtra("Tag");


		findViews();
		setListener();
//		etLon.setText(getIntent().getStringExtra("lon"));
//		etLat.setText(getIntent().getStringExtra("lat"));
//		etName.setText(getIntent().getStringExtra("name"));
//		m_szCameraId = getIntent().getStringExtra("channelId").getBytes();

		String dzjd = lngM.getDzjd();
		String dzwd = lngM.getDzwd();
		etLon.setText(dzjd);
		etLat.setText(dzwd);
		etName.setText(lngM.getTd_MC());
		String devied_DH = lngM.getDevid_DH();
		if (devied_DH!=null){
			m_szCameraId = devied_DH.getBytes();
		}

//		cam_TDWHDW.setText(getIntent().getStringExtra("tdwhdw"));
//	    cam_SYFL.setText(getIntent().getStringExtra("syfl"));
//	    cam_LDLX.setText(getIntent().getStringExtra("ldlx"));
//	    cam_jsrq.setText(getIntent().getStringExtra("jsrq"));
//		cam_oabmmc.setText(getIntent().getStringExtra("oabmmc"));
//		sxtName.setText(getIntent().getStringExtra("nzdd"));
//		etdirbction.setText(getIntent().getStringExtra("dirbction"));
//        //m_subtype = getIntent().getStringExtra("channelId").getBytes();
//       //tCam.setText(getIntent().getStringExtra("channelName"));
//
//    	m_control = Integer.parseInt(getIntent().getStringExtra("control"));
		m_control = 0;
		m_nPort = IPlaySDK.PLAYGetFreePort();
		
		if(m_control == 0) {
			btLeft.setVisibility(View.GONE);
			btRight.setVisibility(View.GONE);
			btTop.setVisibility(View.GONE);
			btBottom.setVisibility(View.GONE);
			btAddFocus.setVisibility(View.GONE);
			btReduceFocus.setVisibility(View.GONE);
		} else {
			btLeft.setVisibility(View.VISIBLE);
			btRight.setVisibility(View.VISIBLE);
			btTop.setVisibility(View.VISIBLE);
			btBottom.setVisibility(View.VISIBLE);
			btAddFocus.setVisibility(View.VISIBLE);
			btReduceFocus.setVisibility(View.VISIBLE);
		}
	}
//	tmp_szIp = "127.0.0.1".getBytes();
//	tmp_strPort = "9000";
//	tmp_szUsername = "admin".getBytes();
//	tmp_szPassword = "123456".getBytes();

    class LoginTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected Integer doInBackground(Void... arg0) {               //在此处处理UI会导致异常
	    	Login_Info_t loginInfo = new Login_Info_t();
	    	Integer error = Integer.valueOf(0);
	    	loginInfo.szIp 	= "127.0.0.1".getBytes();;
	    	String strPort 	= "9000";
	    	loginInfo.nPort = Integer.parseInt(strPort);
	    	loginInfo.szUsername = "admin".getBytes();
	    	loginInfo.szPassword = "123456".getBytes();
	    	loginInfo.nProtocol = 2;
	    	saveLoginInfo(getIntent().getStringExtra("szIp"), getIntent().getStringExtra("strPort"), getIntent().getStringExtra("szUsername"), getIntent().getStringExtra("szPassword"));
	    	int nRet = IDpsdkCore.DPSDK_Login(mAPP.getDpsdkCreatHandle(), loginInfo, 30000);
    		return nRet;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			super.onPostExecute(result);
    		if (result == 0) {
    			
    			//登录成功，开启GetGPSXMLTask线程
    			new GetGPSXMLTask().execute();
    			
		    	Log.d("DpsdkLogin success:",result+"");
		    	IDpsdkCore.DPSDK_SetCompressType(mAPP.getDpsdkCreatHandle(), 0);
		    	mAPP.setLoginHandler(1);
		    	m_pDLLHandle = AppApplication.get().getDpsdkHandle();
	    	} else {
		    	Log.d("DpsdkLogin failed:",result+"");
		    	Toast.makeText(getApplicationContext(), "login failed" + result, Toast.LENGTH_SHORT).show();
		    	mAPP.setLoginHandler(0);
	    	}
		}

    }

    private void saveLoginInfo(String ip, String port, String username, String password){
    	SharedPreferences sp = getSharedPreferences("LOGININFO", 0);
		Editor ed = sp.edit();
		StringBuilder sb = new StringBuilder();
		sb.append(ip).append(",").append(port).append(",").append(password).append(",").append(username);
		ed.putString("INFO", sb.toString());
		ed.putString("ISFIRSTLOGIN", "false");
		ed.commit(); 	
		Log.i("TestDpsdkCoreActivity", "saveLoginInfo" + sb.toString());
    }

	//读取GPSXMl 模块
	class GetGPSXMLTask extends AsyncTask<Void, Integer, Integer> {
		
		@Override
		protected Integer doInBackground(Void... params) {
			Return_Value_Info_t rvInfo2 = new Return_Value_Info_t();
			int nRet = 0;
			//if(!mIsDeviceInfo) {
				nRet = IDpsdkCore.DPSDK_LoadDGroupInfo(AppApplication.get().getDpsdkHandle(), rvInfo2, DPSDK_CORE_DEFAULT_TIMEOUT);
				//mIsDeviceInfo = true;
			//}
			return nRet;
		}

		@Override
		protected void onPostExecute(Integer result) {
			Toast.makeText(RealPlayActivity.this, "LoadDGroupInfo nRet"+result, Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
			new RealPlayLTask().execute();
		}
				
	}

	//读取GPSXMl 模块
	class RealPlayLTask extends AsyncTask<Void, Integer, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			
			SurfaceHolder holder = m_svPlayer.getHolder();
	        holder.addCallback(new Callback() {
	     	   	public void surfaceCreated(SurfaceHolder holder)
	     		{



	     	   		Log.d("xss", "surfaceCreated");
	     	   	    IPlaySDK.InitSurface(m_nPort, m_svPlayer);
	     		}
	     		
	     		public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                           int height)
	     		{
	     			Log.d("xss", "surfaceChanged");
	     		}

	     		public void surfaceDestroyed(SurfaceHolder holder)
	     		{
	     			Log.d("xss", "surfaceDestroyed");
	     		}
	        });
		
			final fMediaDataCallback fm = new fMediaDataCallback() {
				
				@Override
				public void invoke(int nPDLLHandle, int nSeq, int nMediaType,
						byte[] szNodeId, int nParamVal, byte[] szData, int nDataLen) {
					int time =1;
					int ret = IPlaySDK.PLAYInputData(m_nPort, szData, nDataLen);
					if(ret == 1){
						Log.e("xss","playing success=" + nSeq + " package size=" + nDataLen);
					}else{
						Log.e("xss","playing failed=" + nSeq + " package size=" + nDataLen);
					}
				}
			};
			
			btOpenVideo.setOnClickListener(new View.OnClickListener() {
				@SuppressLint("LongLogTag")
				@Override
				public void onClick(View arg0) {
					if(!StartRealPlay()){
						Log.e("xss", "StartRealPlay failed!");
						Toast.makeText(getApplicationContext(), "Open video failed!", Toast.LENGTH_SHORT).show();
						return;
					}

					try{
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
						if(ret == 0){
							btOpenVideo.setEnabled(false);
							btCloseVideo.setEnabled(true);
							m_nSeq = retVal.nReturnValue;
							Log.e("xss DPSDK_GetRealStream success!",ret+"");
							Toast.makeText(getApplicationContext(), "Open video success!", Toast.LENGTH_SHORT).show();
						}else{
							StopRealPlay();
							Log.e("xss DPSDK_GetRealStream failed!",ret+"");
							Toast.makeText(getApplicationContext(), "Open video failed!code:"+ret, Toast.LENGTH_SHORT).show();
						}
					}catch(Exception e){
						Log.e("xss", e.toString());
					}
				}
			});
			return 0;
		}
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			btOpenVideo.performClick();
		}
	}
	private void findViews(){
		btOpenVideo = (Button)findViewById(R.id.bt_open_video);
		btCloseVideo = (Button)findViewById(R.id.bt_close_video);
		btLeft = (Button)findViewById(R.id.button_ptz_left);
		btRight = (Button)findViewById(R.id.button_right);
		btTop = (Button)findViewById(R.id.button_top);
		btBottom = (Button)findViewById(R.id.button_bottom);
	  /*  btAddZoom = (Button)findViewById(R.id.button_add_zoom);
		btReduceZoom = (Button)findViewById(R.id.button_reduce_zoom);*/
		btAddFocus = (Button)findViewById(R.id.button_add_focus);
		btReduceFocus = (Button)findViewById(R.id.button_reduce_focus);
		btAddAperture = (Button)findViewById(R.id.button_add_aperture);
		btReduceAperture = (Button)findViewById(R.id.button_reduce_aperture);
		//etCam = (TextView)findViewById(R.id.et_cam_id);
		sxtName= (TextView)findViewById(R.id.nzdd_cam_id);
		cam_LDLX= (TextView)findViewById(R.id.sxttype_cam_id);
		cam_TDWHDW= (TextView)findViewById(R.id.weihu_cam_id);
		cam_SYFL= (TextView)findViewById(R.id.use_cam_id);
		cam_jsrq= (TextView)findViewById(R.id.jsdate_cam_id);
		cam_oabmmc= (TextView)findViewById(R.id.sxxq_cam_id);
		etLon= (TextView)findViewById(R.id.lon_cam_id);
		etLat = (TextView)findViewById(R.id.lat_cam_id);
		etName = (TextView)findViewById(R.id.sxtname_cam_id);
		etdirbction = (TextView)findViewById(R.id.na_cam_id);
		tvRet = (TextView)findViewById(R.id.tv_excute_result);
		m_svPlayer = (SurfaceView)findViewById(R.id.sv_player);
		btnCaptureImg = (Button)findViewById(R.id.capture_img);
	}

	private void setListener(){

		btnCaptureImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				captureBitmap();
				//saveIntoMediaCore();
			}
		});

		btCloseVideo.setEnabled(false);
		btCloseVideo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//int ret = IDpsdkCore.DPSDK_CloseRealStreamByCameraId(m_pDLLHandle, m_szCameraId, mTimeOut);
				int ret = IDpsdkCore.DPSDK_CloseRealStreamBySeq(m_pDLLHandle, m_nSeq, mTimeOut);
				if(ret == 0){
					btOpenVideo.setEnabled(true);
					btCloseVideo.setEnabled(false);
					Log.e("xss","DPSDK_CloseRealStreamByCameraId success!");
					Toast.makeText(getApplicationContext(), "Close video success!", Toast.LENGTH_SHORT).show();
				}else{
					Log.e("xss","DPSDK_CloseRealStreamByCameraId failed! ret = " + ret);
					Toast.makeText(getApplicationContext(), "Close video failed!", Toast.LENGTH_SHORT).show();
				}
				StopRealPlay();
			}
		});
				
		btLeft.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(m_control == 0) {
					btLeft.setVisibility(View.GONE);
					Toast.makeText(getApplicationContext(), "未有该摄像头云台控制权限", Toast.LENGTH_SHORT).show();
					return false;
				}
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					btLeft.setVisibility(View.VISIBLE);
					Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
					ptzDirectInfo.bStop = false;
					ptzDirectInfo.nDirect = 3;	
					ptzDirectInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzDirection success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzDirection failed!");
					}
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
					ptzDirectInfo.bStop = true;
					ptzDirectInfo.nDirect = 3;	
					ptzDirectInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzDirection success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzDirection failed!");
					}	
				}
				
				return false;
			}
		});
		
		btRight.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(m_control == 0) {
					btRight.setVisibility(View.GONE);
					Toast.makeText(getApplicationContext(), "未有该摄像头云台控制权限", Toast.LENGTH_SHORT).show();
					return false;
				}
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					btRight.setVisibility(View.VISIBLE);
					Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
					ptzDirectInfo.bStop = false;
					ptzDirectInfo.nDirect = 4;	
					ptzDirectInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzDirection success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzDirection failed!");
					}
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
					ptzDirectInfo.bStop = true;
					ptzDirectInfo.nDirect = 4;	
					ptzDirectInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzDirection success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzDirection failed!");
					}	
				}
				
				return false;
			}
		});
				
		btTop.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(m_control == 0) {
					btTop.setVisibility(View.GONE);
					Toast.makeText(getApplicationContext(), "未有该摄像头云台控制权限", Toast.LENGTH_SHORT).show();
					return false;
				}
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					btTop.setVisibility(View.VISIBLE);
					Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
					ptzDirectInfo.bStop = false;
					ptzDirectInfo.nDirect = 1;	
					ptzDirectInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzDirection success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzDirection failed!");
					}
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
					ptzDirectInfo.bStop = true;
					ptzDirectInfo.nDirect = 1;	
					ptzDirectInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzDirection success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzDirection failed!");
					}	
				}
				
				return false;
			}
		});
				
		btBottom.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(m_control == 0) {
					btBottom.setVisibility(View.GONE);
					Toast.makeText(getApplicationContext(), "未有该摄像头云台控制权限", Toast.LENGTH_SHORT).show();
					return false;
				}
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					btBottom.setVisibility(View.VISIBLE);
					Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
					ptzDirectInfo.bStop = false;
					ptzDirectInfo.nDirect = 2;	
					ptzDirectInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzDirection success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzDirection failed!");
					}
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
					ptzDirectInfo.bStop = true;
					ptzDirectInfo.nDirect = 2;	
					ptzDirectInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzDirection success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzDirection failed!");
					}	
				}
				
				return false;
			}
		});
		
		/*btAddZoom.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(m_control == 0) {
					Toast.makeText(getApplicationContext(), "未有该摄像头云台控制权限", Toast.LENGTH_SHORT).show();
					return false;
				}
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = false;
					ptzOperationInfo.nOperation = 0;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = true;
					ptzOperationInfo.nOperation = 0;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				
				return false;
			}
		});
		
		btReduceZoom.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(m_control == 0) {
					Toast.makeText(getApplicationContext(), "未有该摄像头云台控制权限", Toast.LENGTH_SHORT).show();
					return false;
				}
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = false;
					ptzOperationInfo.nOperation = 3;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = true;
					ptzOperationInfo.nOperation = 3;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				
				return false;
			}
		});*/

		btAddFocus.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(m_control == 0) {
					btAddFocus.setVisibility(View.GONE);
					Toast.makeText(getApplicationContext(), "未有该摄像头云台控制权限", Toast.LENGTH_SHORT).show();
					return false;
				}
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					btAddFocus.setVisibility(View.VISIBLE);
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = false;
					ptzOperationInfo.nOperation = 1;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = true;
					ptzOperationInfo.nOperation = 1;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				
				return false;
			}
		});
		
		btReduceFocus.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(m_control == 0) {
					btReduceFocus.setVisibility(View.GONE);
					Toast.makeText(getApplicationContext(), "未有该摄像头云台控制权限", Toast.LENGTH_SHORT).show();
					return false;
				}
				
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					btReduceFocus.setVisibility(View.VISIBLE);
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = false;
					ptzOperationInfo.nOperation = 4;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = true;
					ptzOperationInfo.nOperation = 4;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				
				return false;
			}
		});
		
		btAddAperture.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(m_control == 0) {
					Toast.makeText(getApplicationContext(), "未有该摄像头云台控制权限", Toast.LENGTH_SHORT).show();
					return false;
				}
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = false;
					ptzOperationInfo.nOperation = 2;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = true;
					ptzOperationInfo.nOperation = 2;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				
				return false;
			}
		});
		
		btReduceAperture.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(m_control == 0) {
					Toast.makeText(getApplicationContext(), "未有该摄像头云台控制权限", Toast.LENGTH_SHORT).show();
					return false;
				}
				if(arg1.getAction() == MotionEvent.ACTION_DOWN)
				{
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = false;
					ptzOperationInfo.nOperation = 5;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				else if(arg1.getAction() == MotionEvent.ACTION_UP)
				{
					Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
					System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
					ptzOperationInfo.bStop = true;
					ptzOperationInfo.nOperation = 5;	
					ptzOperationInfo.nStep = 4;
					
					int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
					if(ret == 0)
					{
						Log.e("xss","DPSDK_PtzCameraOperation success!");
					}
					else
					{
						Log.e("xss","DPSDK_PtzCameraOperation failed!");
					}
				}
				
				return false;
			}
		});
	}
	
	/**
     * 创建文件夹  保存截图 图片
     */
    private void captureBitmap() {
    	
        String path = IMAGE_PATH + IMGSTR;
    	//先创建一个文件夹
    	File dir = new File(IMAGE_PATH);
    	File file = new File(IMAGE_PATH, IMGSTR);
        if(!dir.exists()) {
        	dir.mkdir();
        } else {
        	if(file.exists()) {
        		file.delete();
        	}
        }
    	
        int result = IPlaySDK.PLAYCatchPicEx(m_nPort, path, PicFormat_JPEG);
        Log.i("PLAYCatchPicEx", String.valueOf(result));
        if (result > 0) {
        	showToast(R.string.capture_success);
        	saveIntoMediaCore();
        } else {
            showToast(R.string.capture_fail);
        }
    }
    
    private void saveIntoMediaCore(){
    	Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    	//intent.setAction(MEDIA_ROUTER_SERVICE);
    	Uri uri = Uri.parse(IMAGE_PATH + IMGSTR);
    	intent.setData(uri);
    	RealPlayActivity.this.setIntent(intent);
    }

    private void showToast(int str){
		Toast.makeText(getApplicationContext(), getResources().getString(str), Toast.LENGTH_SHORT).show();
	}
	public void StopRealPlay()
    {
    	try {
    		IPlaySDK.PLAYStopSoundShare(m_nPort);
    		IPlaySDK.PLAYStop(m_nPort);
    		IPlaySDK.PLAYCloseStream(m_nPort);
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public boolean StartRealPlay()
    { 
        if(m_svPlayer == null)
        	return false;
        
        boolean bOpenRet = IPlaySDK.PLAYOpenStream(m_nPort,null,0,1500*1024) == 0? false : true;
        IPlaySDK.PLAYSetDecodeThreadNum(m_nPort, 4);
    	if(bOpenRet)
		{
			boolean bPlayRet = IPlaySDK.PLAYPlay(m_nPort, m_svPlayer) == 0 ? false : true;
			Log.i("StartRealPlay", "StartRealPlay1");
			if(bPlayRet)
			{
				boolean bSuccess = IPlaySDK.PLAYPlaySoundShare(m_nPort) == 0 ? false : true;

				Log.i("StartRealPlay", "StartRealPlay2");
				if(!bSuccess)
				{
					IPlaySDK.PLAYStop(m_nPort);
					IPlaySDK.PLAYCloseStream(m_nPort);
					Log.i("StartRealPlay", "StartRealPlay3");
					return false;
				}
			}
			else
			{
				IPlaySDK.PLAYCloseStream(m_nPort);
				Log.i("StartRealPlay", "StartRealPlay4");
				return false;
			}
		}
    	else
    	{
    		Log.i("StartRealPlay", "StartRealPlay5");
    		return false;
    	}
        
        return true;
    }
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		btCloseVideo.setEnabled(true);
		int nRet = IDpsdkCore.DPSDK_Logout(mAPP.getDpsdkCreatHandle(),30000);
		if(nRet==0){
			btCloseVideo.setEnabled(true);
			Toast.makeText(getApplicationContext(), "退出视频播放",
					Toast.LENGTH_SHORT).show();
		}else if(nRet==5){
			Toast.makeText(getApplicationContext(), " 已登出",
					Toast.LENGTH_SHORT).show();
		}
		
		return super.onKeyDown(keyCode, event);
	}
	

}
