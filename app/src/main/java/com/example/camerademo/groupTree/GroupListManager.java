package com.example.camerademo.groupTree;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

import com.dh.DpsdkCore.Dep_Info_Ex_t;
import com.dh.DpsdkCore.Dep_Info_t;
import com.dh.DpsdkCore.Device_Info_Ex_t;
import com.dh.DpsdkCore.Enc_Channel_Info_Ex_t;
import com.dh.DpsdkCore.Get_Channel_Info_Ex_t;
import com.dh.DpsdkCore.Get_Dep_Count_Info_t;
import com.dh.DpsdkCore.Get_Dep_Info_t;
import com.dh.DpsdkCore.IDpsdkCore;
import com.dh.DpsdkCore.Return_Value_ByteArray_t;
import com.dh.DpsdkCore.Return_Value_Info_t;
import com.dh.DpsdkCore.dpsdk_change_type_e;
import com.dh.DpsdkCore.dpsdk_node_type_e;
import com.dh.DpsdkCore.fDPSDKDevStatusCallback;
import com.dh.DpsdkCore.fDPSDKDeviceChangeCallback;
import com.dh.DpsdkCore.fDPSDKNVRChnlStatusCallback;
import com.dh.DpsdkCore.fDPSDKStatusCallback;
import com.example.camerademo.application.AppApplication;
import com.example.camerademo.exception.IDpsdkCoreException;
import com.example.camerademo.groupTree.GroupListGetTask.IOnSuccessListener;
import com.example.camerademo.groupTree.bean.ChannelInfoExt;
import com.example.camerademo.groupTree.bean.DeviceInfo;
import com.example.camerademo.groupTree.bean.TreeNode;
import com.example.camerademo.util.Utils;


/**
 * <p>
 * </p>
 * 
 * @author fangzhihua 2014-5-7 下午2:16:47
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2014-5-7
 * @modify by reason:{方法名}:{原因}
 */
public class GroupListManager {

    private static final String TAG = "GroupListManager";

    private static GroupListManager groupListManager;

    // 保存设备列表
    private List<TreeNode> devList = null;
    
    // 保存Device_Info_Ex_t  在线设备列表，用于通过设备呼叫号码来查询设备id
    private List<Device_Info_Ex_t> deviceExList = null;

    // 选中通道列表
    private List<ChannelInfoExt> channelList = null;

    // 搜索通道列表
    private List<ChannelInfoExt> searchList = null;

    // 服务模式使用
    private static final int DPSDK_CORE_SDK_SERVER = 1;

    // 超时时间
    private static final int DPSDK_CORE_DEFAULT_TIMEOUT = 600000;

    // 节点code
    private byte[] szCoding = null;

    // 根节点
    private TreeNode rootNode = null;

    // 有改变的节点
    private TreeNode tempNode = null;

    // 返回的结果
    private int ret = -1;

    // 获取组织树任务
    private GroupListGetTask task = null;

    // 获取列表任务是否结束
    private boolean isFinish = false;

    // Get_Dep_Count_Info_t
    private Get_Dep_Count_Info_t gdcInfo = null;

    // Get_Dep_Info_t
    private Get_Dep_Info_t gdInfo = null;

    private IOnSuccessListener onSuccessListener = null;
    
    // 根据id获取节点
    private HashMap<String, TreeNode> channelNodeIndex = null;
    

    /**
     * <p>
     * 单例
     * </p>
     * 
     * @author fangzhihua 2014-5-7 下午2:17:55
     * @return
     */
    public static synchronized GroupListManager getInstance() {
        if (groupListManager == null) {
            groupListManager = new GroupListManager();
        }
        return groupListManager;
    }

    public GroupListManager() {
        channelList = new ArrayList<ChannelInfoExt>();
        setSearchList(new ArrayList<ChannelInfoExt>());
        devList = new ArrayList<TreeNode>();
        deviceExList = new ArrayList<Device_Info_Ex_t>();
        channelNodeIndex = new HashMap<String, TreeNode>();
    }

    public int getnPDLLHandle() {
        return AppApplication.get().getDpsdkHandle();
    }

    public void startGroupListGetTask() {
    	channelNodeIndex.clear();
        // 获取组织树任务
        task = new GroupListGetTask();
        task.execute();
    }

    public void setGroupListGetListener(final IOnSuccessListener onSuccessListener) {
        if (task == null)
            return;
        IOnSuccessListener listener = new IOnSuccessListener() {
            @Override
            public void onSuccess(boolean success, int errCode) {
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(success, errCode);
                }
                task = null;
            }
        };
        task.setListener(listener);
    }

    public GroupListGetTask getTask() {
        return task;
    }

    public void setTask(GroupListGetTask task) {
        this.task = task;
    }

    // 平台服务改变回调
    fDPSDKStatusCallback fCallback = new fDPSDKStatusCallback() {
        @Override
        public void invoke(int nPDLLHandle, int nStatus) {
            //

        }
    };

    fDPSDKDeviceChangeCallback fDevCallback = new fDPSDKDeviceChangeCallback() {
        @Override
        public void invoke(int nPDLLHandle, int nChangeType, byte[] szDeviceId, byte[] szDepCode, byte[] szNewOrgCode) {
            dPSDKDeviceChangeCallback(nPDLLHandle, nChangeType, szDeviceId, szDepCode, szNewOrgCode);
        }
    };

    fDPSDKDevStatusCallback fDevStatusCallback = new fDPSDKDevStatusCallback() {
        @Override
        public void invoke(int nPDLLHandle, byte[] szDeviceId, int nStatus) {
            String deviceId = new String(szDeviceId).trim();
            updateDevStatus(deviceId, nStatus, rootNode);
        }
    };
    fDPSDKNVRChnlStatusCallback fNvrChnlStatusCallback = new fDPSDKNVRChnlStatusCallback() {
		
		@Override
		public void invoke(int nPDLLHandle, byte[] szChnlId, int nStatus) {
		     String chnlId = new String(szChnlId).trim();
	         updateNVRChnlStatus(chnlId, nStatus, rootNode);
		}
	};

    /**
     * <p>
     * 组织树改变回调
     * </p>
     * 
     * @author fangzhihua 2014年7月3日 上午11:22:09
     * @param nPDLLHandle
     * @param nChangeType
     * @param szDeviceId
     * @param szDepCode
     * @param szNewOrgCode
     */
    private void dPSDKDeviceChangeCallback(int nPDLLHandle, int nChangeType, byte[] szDeviceId, byte[] szDepCode,
            byte[] szNewOrgCode) {
        switch (nChangeType) {
            case dpsdk_change_type_e.DPSDK_CORE_CHANGE_ADD_ORG:
            case dpsdk_change_type_e.DPSDK_CORE_CHANGE_DELETE_ORG:
            case dpsdk_change_type_e.DPSDK_CORE_CHANGE_MODIFY_ORG:
                if (task == null) {
                    startGroupListGetTask();
                    task.setListener(onSuccessListener);
                }
                break;
            case dpsdk_change_type_e.DPSDK_CORE_CHANGE_ADD_DEV:
            case dpsdk_change_type_e.DPSDK_CORE_CHANGE_DEL_DEV:
            case dpsdk_change_type_e.DPSDK_CORE_CHANGE_MODIFY_DEV:
                if (task == null) {
                    startGroupListGetTask();
                    task.setListener(onSuccessListener);
                }
                break;
            default:
                break;
        }

    }

    /**
     * <p>
     * 替换该节点的组织树
     * </p>
     * 
     * @author fangzhihua 2014年7月3日 下午4:55:43
     * @param szDepCode
     */
    private void getChangeNode(byte[] szDepCode, TreeNode treeNode) {
        tempNode = null;
        if (treeNode == null)
            return;

        if (Arrays.equals(treeNode.getValue().getBytes(), szDepCode)) {
            tempNode = treeNode;
            return;
        }

        for (int i = 0; i < treeNode.getDepChildren().size(); i++) {
            // 循环获取组和设备信息
            getChangeNode(szDepCode, treeNode.getDepChildren().get(i));
        }

    }

    /**
     * <p>
     * 替换该节点的组织树
     * </p>
     * 
     * @author fangzhihua 2014年7月3日 下午4:55:43
     * @param szDepCode
     */
    private void updateNodes(byte[] szDepCode, TreeNode tempNode, TreeNode rootNode) {

        if (tempNode == null)
            return;

        if (Arrays.equals(rootNode.getValue().getBytes(), szDepCode)) {
            rootNode.remove(tempNode);
            rootNode.add(tempNode);
            return;
        }

        for (int i = 0; i < rootNode.getDepChildren().size(); i++) {
            // 循环获取组和设备信息
            updateNodes(szDepCode, tempNode, rootNode.getDepChildren().get(i));
        }

    }

    /**
     * <p>
     * 循环组织树设置
     * </p>
     * 
     * @author fangzhihua 2014年7月1日 下午8:22:17
     * @param deviceId
     * @param nStatus
     * @param curNode
     */
    public void updateDevStatus(String deviceId, int nStatus, TreeNode curNode) {
        if (curNode == null)
            return;
        if (curNode.getType() == 2 && curNode.getDeviceInfo().getDeviceId().equals(deviceId)) {
            curNode.getDeviceInfo().setStatus(nStatus);
            return;
        } else if (curNode.getType() == 1 || curNode.getType() == 0) {
            for (int i = 0; i < curNode.getChildren().size(); i++) {
                // 循环获取组和设备信息
                updateDevStatus(deviceId, nStatus, curNode.getChildren().get(i));
            }
        }
    }
    
    
    public void updateNVRChnlStatus(String chnlId, int nStatus, TreeNode curNode) {
    	TreeNode chnlNode = null;
        synchronized (channelNodeIndex) {
    	   chnlNode = channelNodeIndex.get(chnlId);
        }
        if (chnlNode != null) {
        	chnlNode.getChannelInfo().setState(nStatus);
        }
    }
    

    /**
     * <p>
     * 创建根节点
     * </p>
     * 
     * @author fangzhihua 2014-5-7 下午4:23:53
     * @return
     * @throws IDpsdkCoreException
     */
    public synchronized byte[] loadDGroupInfoLayered()  {
        // IDpsdkCore.DPSDK_SetDPSDKStatusCallback(getnPDLLHandle(), fCallback);
        IDpsdkCore.DPSDK_SetDPSDKDeviceChangeCallback(getnPDLLHandle(), fDevCallback);
        // 设备状态更新回调
        IDpsdkCore.DPSDK_SetDPSDKDeviceStatusCallback(getnPDLLHandle(), fDevStatusCallback);
        // nvr通道状态变更回调
        IDpsdkCore.DPSDK_SetDPSDKNVRChnlStatusCallback(getnPDLLHandle(), fNvrChnlStatusCallback);
        
        Return_Value_Info_t rvInfo2 = new Return_Value_Info_t();
        ret = IDpsdkCore.DPSDK_LoadDGroupInfo(getnPDLLHandle(), rvInfo2, DPSDK_CORE_DEFAULT_TIMEOUT);
  
    	Boolean hasLogicorg = IDpsdkCore.DPSDK_HasLogicOrg(getnPDLLHandle());
		if (!hasLogicorg) {
			// 获取根节点的信息DPSDK_GetDGroupRootInfo
			Dep_Info_t dInfo = new Dep_Info_t();
			ret = IDpsdkCore.DPSDK_GetDGroupRootInfo(getnPDLLHandle(), dInfo);
			szCoding = dInfo.szCoding;
			// 建立树根
			rootNode = new TreeNode(new String(dInfo.szDepName), new String(dInfo.szCoding));
			Log.i("loadDGroupInfoLayered", new String(dInfo.szDepName)
					+ new String(dInfo.szCoding) + ret);
		} else {
			Dep_Info_Ex_t dInfoExt = new Dep_Info_Ex_t();
			ret = IDpsdkCore.DPSDK_GetLogicRootDepInfo(getnPDLLHandle(),dInfoExt);
			szCoding = dInfoExt.szCoding;
			rootNode = new TreeNode(new String(dInfoExt.szDepName), new String(dInfoExt.szCoding));
			Log.i("loadDGroupInfoLayered", new String(dInfoExt.szDepName)
			+ new String(dInfoExt.szCoding) + ret);
		}
 
//        if (ret != 0) {
//            throw new IDpsdkCoreException("DPSDK_GETDGROUPROOTINFO_EXCEPTION", ret);
//        }

        // 分级加载组织信息DPSDK_LoadDGroupInfoLayered
        // Load_Dep_Info_t ldInfo = new Load_Dep_Info_t();
        // ldInfo.szCoding = dInfo.szCoding;
        // ldInfo.nOperation = -1;
        // ret = IDpsdkCore.DPSDK_LoadDGroupInfo(getnPDLLHandle(), ldInfo, rvInfo2,
        // DPSDK_CORE_DEFAULT_TIMEOUT);
        //long start = System.currentTimeMillis();

        //LogUtil.debugLog(TAG, "DPSDK_LoadDGroupInfoLayered---" + (System.currentTimeMillis() - start));
      /*  if (ret != 0 || rvInfo2.nReturnValue == 0) {
            throw new IDpsdkCoreException("DPSDK_LOADDGROUPINFOLAYERED_EXCEPTION" + "errorCode: " + ret, ret);
        }*/
        
    	return szCoding;
    }

    /**
     * <p>
     * </p>
     * 
     * @author fangzhihua 2014-5-7 下午4:23:53
     * @return
     */
    public synchronized void getGroupList(byte[] coding, TreeNode curNode)  {
        if (curNode == null)
            return;
      	Boolean hasLogicorg = IDpsdkCore.DPSDK_HasLogicOrg(getnPDLLHandle());
      	if (hasLogicorg) {
      	  //业务组
            Return_Value_Info_t depNum = new Return_Value_Info_t();
            ret = IDpsdkCore.DPSDK_GetLogicDepNodeNum(getnPDLLHandle(), coding, dpsdk_node_type_e.DPSDK_CORE_NODE_DEP, depNum);
            throwException("DPSDK_GetLogicDepNodeNum_EXCEPTION");
            Dep_Info_Ex_t dep_Info_Ex_t = new Dep_Info_Ex_t();
            TreeNode logicDepNode = null;
            for (int i = 0; i < depNum.nReturnValue; i++ ) {
                IDpsdkCore.DPSDK_GetLogicSubDepInfoByIndex(getnPDLLHandle(), coding, i, dep_Info_Ex_t);
                logicDepNode = new TreeNode(new String(dep_Info_Ex_t.szDepName).trim(), new String(dep_Info_Ex_t.szCoding).trim());
                // 设置为组
                logicDepNode.setType(1);
                logicDepNode.setParent(curNode);
                curNode.add(logicDepNode);
                Log.i(TAG, "name of logicDepNode" + new String(dep_Info_Ex_t.szDepName) + "number of logicDepNode" + depNum.nReturnValue);
       
            }
            //业务通道
            Return_Value_Info_t channelNum = new Return_Value_Info_t();
            ret = IDpsdkCore.DPSDK_GetLogicDepNodeNum(getnPDLLHandle(), coding, dpsdk_node_type_e.DPSDK_CORE_NODE_CHANNEL, channelNum);
            Return_Value_ByteArray_t return_Value_ByteArray_t = new Return_Value_ByteArray_t();
            
            int cNum = channelNum.nReturnValue;
            Enc_Channel_Info_Ex_t[] ecInfo = new Enc_Channel_Info_Ex_t[cNum];
            for (int j = 0; j < cNum; j++ ) {
            	Enc_Channel_Info_Ex_t  enc_Channel_Info_Ex_t = new Enc_Channel_Info_Ex_t();
                IDpsdkCore.DPSDK_GetLogicID(getnPDLLHandle(), coding, j, true, return_Value_ByteArray_t);
                IDpsdkCore.DPSDK_GetChannelInfoById(getnPDLLHandle(), return_Value_ByteArray_t.szCodeID, enc_Channel_Info_Ex_t);
                ecInfo[j] = enc_Channel_Info_Ex_t;
            }
            Log.i(TAG, "cNum" + cNum);
            addChannelInfoNode(ecInfo, curNode);
            throwException("DPSDK_GetLogicDepNodeNum_EXCEPTION");
          
      	} else {
            // 获取组织设备信息串数量DPSDK_GetDGroupCount
            gdcInfo = new Get_Dep_Count_Info_t();
            gdcInfo.szCoding = coding;   //通过组织节点信息，参看节点下的设备个数
            ret = IDpsdkCore.DPSDK_GetDGroupCount(getnPDLLHandle(), gdcInfo);  //先获取设备个数
            throwException("DPSDK_GETDGROUPCOUNT_EXCEPTION");
      
            // 获取组织设备信息串DPSDK_GetDGroupInfo
            gdInfo = new Get_Dep_Info_t(gdcInfo.nDepCount, gdcInfo.nDeviceCount);
            gdInfo.szCoding = coding;
            ret = IDpsdkCore.DPSDK_GetDGroupInfo(getnPDLLHandle(), gdInfo);
            throwException("DPSDK_GETDGROUPINFO_EXCEPTION");

            // 组织信息
            Dep_Info_t[] dInfo = gdInfo.pDepInfo;
            TreeNode depNode = null;
            for (int i = 0; i < dInfo.length; i++) { 
                depNode = new TreeNode(new String(dInfo[i].szDepName).trim(), new String(dInfo[i].szCoding).trim());
               
                //递归查找组织
                getGroupList(depNode.getValue().getBytes(), depNode);
                if(depNode.getChildren().size() == 0){
                	continue;
                }
                
                // 设置为组
                depNode.setType(1);
                depNode.setParent(curNode);                              
                curNode.add(depNode);
                               
            }

             // 递归调用，重新获取组织设备信息串数量        
            gdcInfo = new Get_Dep_Count_Info_t();
            gdcInfo.szCoding = coding;   //通过组织节点信息，参看节点下的设备个数
            ret = IDpsdkCore.DPSDK_GetDGroupCount(getnPDLLHandle(), gdcInfo);  //先获取设备个数
            throwException("DPSDK_GETDGROUPCOUNT_EXCEPTION");     
            // 获取组织设备信息串DPSDK_GetDGroupInfo
            gdInfo = new Get_Dep_Info_t(gdcInfo.nDepCount, gdcInfo.nDeviceCount);
            gdInfo.szCoding = coding;
            ret = IDpsdkCore.DPSDK_GetDGroupInfo(getnPDLLHandle(), gdInfo);
            throwException("DPSDK_GETDGROUPINFO_EXCEPTION");
            
         // 设备信息 
            Device_Info_Ex_t[] dInfo1 = gdInfo.pDeviceInfo;
            List<Device_Info_Ex_t> dInfo2 = new ArrayList<Device_Info_Ex_t>();
            int port = 0;
            // 设备在线离线排序
            for (int i = 0; i < dInfo1.length; i++) {
                if (dInfo1[i].nStatus == 2) {  //offline
                    dInfo2.add(dInfo1[i]);
                } else {
                    dInfo2.add(port, dInfo1[i]);  //在线设备在前，并且按顺序插入
                    port++;
                }
            }

            TreeNode devNode = null;
            DeviceInfo deviceInfo = null;
            String szid = null;
            String devName = null;
            for (int i = 0; i < dInfo2.size(); i++) {
                szid = new String(dInfo2.get(i).szId).trim();
                // 过滤szid为空的数据
                if (szid.equals("")) {
                    continue;
                }
                

                deviceExList.add(dInfo2.get(i));  //添加在线设备信息
                               
                //将设备信息组装为treenode设备节点
                devName = new String(dInfo2.get(i).szName).trim();
                Log.i("GroupListManager", "devName " + devName);
                devNode = new TreeNode(devName, szid);
                deviceInfo = new DeviceInfo();
                deviceInfo.setDeviceId(szid);
                deviceInfo.setDeviceName(devName);
                deviceInfo.setDeviceIp(new String(dInfo2.get(i).szIP));
                deviceInfo.setDevicePort(dInfo2.get(i).nPort);
                deviceInfo.setUserName(new String(dInfo2.get(i).szUser));
                deviceInfo.setPassWord(new String(dInfo2.get(i).szPassword));
                deviceInfo.setChannelCount(dInfo2.get(i).nEncChannelChildCount);
                deviceInfo.setFactory(dInfo2.get(i).nFactory);
                deviceInfo.setStatus(dInfo2.get(i).nStatus);
                deviceInfo.setdeviceType(dInfo2.get(i).nDevType);
                Log.i(TAG, "deviceType:" + dInfo2.get(i).nDevType);
                devNode.setDeviceInfo(deviceInfo);
                
//                devNode.setType(2);     // 设置为设备类型
//                devNode.setParent(curNode);
//                curNode.add(devNode);
                
                // 获取通道信息     通过设备id-->获取该设备下的通道信息  IDpsdkCore.DPSDK_GetChannelInfoEx
                Get_Channel_Info_Ex_t gcInfo = new Get_Channel_Info_Ex_t(dInfo2.get(i).nEncChannelChildCount); //设备信息中有编码子通道个数信息
                gcInfo.szDeviceId = dInfo2.get(i).szId;
                ret = IDpsdkCore.DPSDK_GetChannelInfoEx(getnPDLLHandle(), gcInfo);
                Log.d(TAG, "DPSDK_GetChannelInfoEx=" + ret + ",gcInfo.szDeviceId=" + szid);
                throwException("DPSDK_GetChannelInfoEx_EXCEPTION");
                
                Enc_Channel_Info_Ex_t[] ecInfo = gcInfo.pEncChannelnfo;
                          
                addChannelInfoNode(ecInfo, devNode);   //通道添加过程中，过滤到无权限通道     
                
                //添加完通道后，判断此设备是否具有有权通道，若没有则不显示，组织树中删除此设备
                if(devNode.getChildren().size() > 0) {  
                    devNode.setType(2);     // 设置为设备类型
                    devNode.setParent(curNode);
                    curNode.add(devNode);
                	//curNode.remove(devNode);
                }
            }
            
      	}
        
//        for (int i = 0; i < curNode.getDepChildren().size(); i++) {
//            // 循环获取组和设备信息
//            getGroupList(curNode.getDepChildren().get(i).getValue().getBytes(), curNode.getDepChildren().get(i));
//        }
    }

    private void addChannelInfoNode(Enc_Channel_Info_Ex_t[] ecInfo, TreeNode devNode) {
    	  
          TreeNode chanNode = null;
          String name = "";
          String szId = "";
          // int position = 0;
          for (int j = 0; j < ecInfo.length; j++) {  //ecInfo 特定设备下的编码通道信息
              // 处理如果通道名称为空，则默认显示：通道1
              name = new String(ecInfo[j].szName).trim();
              szId = new String(ecInfo[j].szId).trim();
              // 过滤szid为空的数据
              if (szId.equals("")) {       
                  continue;
              }
              
//              if("ccc_1".equals(name)) {
//            	  Log.e("mydev", "---------- name"+szId);            	  
//            	  Log.e("mydev", "********** right   "+ecInfo[j].nRight);
//              }
                          
              //过滤无权限通道
              if(!Utils.getChannelRight(ecInfo[j].nRight, Utils.U_RIGHT_MONITOR) && !Utils.getChannelRight(ecInfo[j].nRight, Utils.U_RIGHT_PLAYBACK)){
            	  continue;
              }
              
              // pdsdk带过来的是\u0000.空格是
              // position = new String(ecInfo[j].szId).indexOf('\u0000');
              // if (position > 0) {
              // szId = new String(ecInfo[j].szId).substring(0, position);
              // } else {
              // szId = new String(ecInfo[j].szId);
              // }
              chanNode = new TreeNode(name, szId);
              ChannelInfoExt channelInfo = new ChannelInfoExt();
              byte[] szDevId = new byte[64];
              Return_Value_Info_t return_Value_Info_t = new Return_Value_Info_t();
              int ret = IDpsdkCore.DPSDK_GetDevIdByChnId(getnPDLLHandle(), ecInfo[j].szId, szDevId);
              
      /*        if (ecInfo[j].nChnlType == dpsdk_dev_type_e.DEV_TYPE_NVR) {
            	  IDpsdkCore.DPSDK_QueryNVRChnlStatus(getnPDLLHandle(), ecInfo[j].szId, DPSDK_CORE_DEFAULT_TIMEOUT); 
              } else {*/
            	  IDpsdkCore.DPSDK_GetChannelStatus(getnPDLLHandle(), ecInfo[j].szId, return_Value_Info_t);  //根据CemeraId获取通道状态，不支持NVR通道状态获取 
             // }
              
              channelInfo.setState(return_Value_Info_t.nReturnValue);
              channelInfo.setDeviceId(new String(szDevId));
              channelInfo.setDevType(ecInfo[j].nCameraType);
              channelInfo.setSzId(szId);
              channelInfo.setSzName(name);
              channelInfo.setRight(ecInfo[j].nRight);
              chanNode.setChannelInfo(channelInfo);
              
//              Log.e(TAG, "================channel node =================="+"           "+new String(szDevId));   
              
              chanNode.setType(3);  // 设置为通道
              chanNode.setParent(devNode);
              synchronized (channelNodeIndex) {
            	  channelNodeIndex.put(szId.trim(), chanNode);
              }
              devNode.add(chanNode);
              byte[] szLatitude = ecInfo[j].szLatitude;//纬度
              byte[] szLongitude = ecInfo[j].szLongitude;//经度
              try {
            	 String Latitude =  new String(szLatitude,"UTF-8").trim();
            	 String Longitude = new String(szLongitude,"UTF-8").trim();
            	 if (!Latitude.equals("") && !Longitude.equals("")) {
            		 Log.i(TAG, "chnlname = " + name);
            	 }
				 Log.i(TAG,"Latitude:" + Latitude + "|Longitude:"+ Longitude);
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

              Log.i(TAG, "name of logicChannelNode" + name + "number of logicChannelNode" + ecInfo.length);
          }
	}

	private void throwException(String str) {
        if (ret != 0) {
            try {
				throw new IDpsdkCoreException(str, ret);
			} catch (IDpsdkCoreException e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
			}
        }
	}

	/**
     * <p>
     * 通过设备id获取通道信息
     * </p>
     * 
     * @author fangzhihua 2014年6月9日 下午3:31:54
     * @param deviceId
     * @return
     * @throws IDpsdkCoreException
     */
    public List<ChannelInfoExt> getChannelsByDeviceId(String deviceId) {
        Return_Value_Info_t rvInfo = new Return_Value_Info_t();
        IDpsdkCore.DPSDK_GetEncChannelCount(getnPDLLHandle(), deviceId.getBytes(), rvInfo);
        // 选中通道列表
        List<ChannelInfoExt> channelList = new ArrayList<ChannelInfoExt>();
        // 获取通道信息
        Get_Channel_Info_Ex_t gcInfo = new Get_Channel_Info_Ex_t(rvInfo.nReturnValue);
        gcInfo.szDeviceId = deviceId.getBytes();
        ret = IDpsdkCore.DPSDK_GetChannelInfoEx(getnPDLLHandle(), gcInfo);
        if (ret != 0) {
            try {
				throw new IDpsdkCoreException("DPSDK_GetChannelInfoEx_EXCEPTION: " + ret, ret);
			} catch (IDpsdkCoreException e) {
				e.printStackTrace();
			}
        }
        Enc_Channel_Info_Ex_t[] ecInfo = gcInfo.pEncChannelnfo;
        ChannelInfoExt channelInfo = null;
        String name = "";
        String szId = "";
        for (int j = 0; j < ecInfo.length; j++) {
            if (ecInfo[j] == null) {
                continue;
            }
            channelInfo = new ChannelInfoExt();
            name = new String(ecInfo[j].szName).trim();
            szId = new String(ecInfo[j].szId).trim();
            // 过滤szid为空的数据
            if (szId.equals("")) {
                continue;
            }
            channelInfo.setDevType(ecInfo[j].nCameraType);
            channelInfo.setSzId(szId);
            channelInfo.setSzName(name);
            channelInfo.setRight(ecInfo[j].nRight);
            channelList.add(channelInfo);
        }
        return channelList;
    }
    
	/**
     * <p>
     * 通过设备id获取通道信息
     * </p>
     * 
     * @author fangzhihua 2014年6月9日 下午3:31:54
     * @param deviceId
     * @return
     * @throws IDpsdkCoreException
     */
    public List<Enc_Channel_Info_Ex_t> getChannelsByDeviceId(byte[] deviceId) {
    	    	
        Return_Value_Info_t rvInfo = new Return_Value_Info_t();
        int ret = IDpsdkCore.DPSDK_GetEncChannelCount(getnPDLLHandle(), deviceId, rvInfo);
 //       Log.e(TAG, "================en channellist size=================="+"           "+rvInfo.nReturnValue);
        
        // 获取通道信息
        Get_Channel_Info_Ex_t gcInfo = new Get_Channel_Info_Ex_t(rvInfo.nReturnValue);
        gcInfo.szDeviceId = deviceId;
        ret = IDpsdkCore.DPSDK_GetChannelInfoEx(getnPDLLHandle(), gcInfo);
        if (ret != 0) {
            try {
				throw new IDpsdkCoreException("DPSDK_GetChannelInfoEx_EXCEPTION: " + ret, ret);
			} catch (IDpsdkCoreException e) {
				e.printStackTrace();
			}
        }
        
        Enc_Channel_Info_Ex_t[] ecInfo = gcInfo.pEncChannelnfo;
        String szId = "";
        List<Enc_Channel_Info_Ex_t> channelList = new ArrayList<Enc_Channel_Info_Ex_t>();
//        Log.e(TAG, "================ecInfo.length =================="+"           "+ecInfo.length);
        for (int j = 0; j < ecInfo.length; j++) {
            if (ecInfo[j] == null) {
                continue;
            }

//            name = new String(ecInfo[j].szName).trim();
            szId = new String(ecInfo[j].szId).trim();
            // 过滤szid为空的数据
            if (szId.equals("")) {
                continue;
            }

            channelList.add(ecInfo[j]);
        }
        return channelList;
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(TreeNode rootNode) {
        this.rootNode = rootNode;
    }

    public List<TreeNode> getDevList() {
        return devList;
    }
    
    public void setDevList(List<TreeNode> devList) {
        this.devList = devList;
    }
    
    public List<Device_Info_Ex_t> getDeviceExList(){
    	return deviceExList;
    }
    


    /**
     * 获取channel list
     * <p>
     * </p>
     * 
     * @author fangzhihua 2014年5月28日 上午10:07:56
     * @return
     */
    public List<ChannelInfoExt> getChannelList() {
        return channelList;
    }

    public void addChannelLists(TreeNode channelTree) {
        synchronized (channelList) {
            channelList.add(channelTree.getChannelInfo());
        }
    }

    public void setChannelList(List<TreeNode> channelTree) {
        synchronized (channelList) {
            channelList = new ArrayList<ChannelInfoExt>();
            for (int i = 0; i < channelTree.size(); i++) {
                channelList.add(channelTree.get(i).getChannelInfo());
            }
        }
    }

    public void addChannelList(List<ChannelInfoExt> channelInfoExtList) {
        synchronized (channelList) {
            channelList.clear();
            channelList.addAll(channelInfoExtList);
        }
    }

    /**
     * <p>
     * 删除选中的通道
     * </p>
     * 
     * @author fangzhihua 2014年6月3日 上午10:26:28
     * @param cameraId
     */
    public void removeChannelList(String cameraId) {
        synchronized (channelList) {
            for (int i = 0; i < channelList.size(); i++) {
                if (channelList.get(i).getSzId().equals(cameraId)) {
                    channelList.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * <p>
     * 该通道是否已经选中
     * </p>
     * 
     * @author fangzhihua 2014-5-21 上午9:01:11
     * @param treeNode
     */
    public boolean isSelectedChannel(TreeNode treeNode) {
        boolean ret = false;
        for (int i = 0; i < channelList.size(); i++) {
            if (treeNode.getValue().equals(channelList.get(i).getSzId())) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * <p>
     * 该通道是否已经选中
     * </p>
     * 
     * @author fangzhihua 2014-5-21 上午9:01:11
     * @param channelInfoExt
     */
    public boolean isSelectedChannel(ChannelInfoExt channelInfoExt) {
        boolean ret = false;
        for (int i = 0; i < channelList.size(); i++) {
            if (channelInfoExt.getSzId().equals(channelList.get(i).getSzId())) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * <p>
     * 清空选中通道数据
     * </p>
     * 
     * @author fangzhihua 2014-5-13 下午3:38:24
     */
    public void clearChannelList() {
        if (channelList != null) {
            synchronized (channelList) {
                channelList.clear();
            }
        }
    }

    /**
     * <p>
     * 循环设置TreeNode为未选
     * </p>
     * 
     * @author fangzhihua 2014-5-13 下午3:38:24
     */
    public void setNoCheck(TreeNode rootNode) {
        if (rootNode == null) {
            return;
        }
        if (rootNode.isChecked()) {
            rootNode.setChecked(false);
        }
        if (rootNode.getChildren() == null) {
            return;
        }
        for (int i = 0; i < rootNode.getChildren().size(); i++) {
            setNoCheck(rootNode.getChildren().get(i));
        }
    }

    /**
     * <p>
     * 循环设置TreeNode为选中
     * </p>
     * 
     * @author fangzhihua 2014-5-13 下午3:38:24
     */
    public void setCheck(TreeNode rootNode, List<String> channelInfoExt) {
        if (rootNode == null) {
            return;
        }
        if (rootNode.getType() == 3 && channelInfoExt.contains(rootNode.getChannelInfo().getSzId().trim())) {
            rootNode.setChecked(true);
        }
        // 当设备下面的通道都选中的时候，则设备选中
        if (rootNode.getType() == 3) {
            boolean flag = true;
            List<TreeNode> tempList = rootNode.getParent().getChildren();
            for (int i = 0; i < tempList.size(); i++) {
                if (!tempList.get(i).isChecked()) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                rootNode.getParent().setChecked(true);
            }
        }

        for (int i = 0; i < rootNode.getChildren().size(); i++) {
            setCheck(rootNode.getChildren().get(i), channelInfoExt);
        }

    }

    /**
     * <p>
     * 退出时清空数据
     * </p>
     * 
     * @author fangzhihua 2014-5-13 下午3:38:24
     */
    public void clearAll() {

        if (task != null) {
            task.cancel(false);
        }

        if (channelList != null) {
            synchronized (channelList) {
                channelList.clear();
            }
        }

        if (devList != null) {
            synchronized (devList) {
                devList.clear();
            }
        }

        if (deviceExList != null) {
            synchronized (deviceExList) {
            	deviceExList.clear();
            }
        }
        
        
        if (rootNode != null) {
            synchronized (rootNode) {
                rootNode.clear();
            }
        }

        if (searchList != null) {
            synchronized (searchList) {
                searchList.clear();
            }
        }
    }

    public void setSearchList(List<ChannelInfoExt> searchList) {
        this.searchList = searchList;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean isFinish) {
        this.isFinish = isFinish;
    }

    public IOnSuccessListener getOnSuccessListener() {
        return onSuccessListener;
    }

    public void setOnSuccessListener(IOnSuccessListener onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
    }

}
