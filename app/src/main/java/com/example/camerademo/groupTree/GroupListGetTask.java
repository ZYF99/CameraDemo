package com.example.camerademo.groupTree;

import com.example.camerademo.groupTree.bean.TreeNode;

import android.os.AsyncTask;
import android.util.Log;


/**
 * <p>
 * 获取组织树任务
 * </p>
 * 
 * @author fangzhihua 2014年6月9日 下午7:13:57
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2014年6月9日
 * @modify by reason:{方法名}:{原因}
 */
public class GroupListGetTask extends AsyncTask<Void,Void, TreeNode> {
    private GroupListManager mGroupListManager = null;

    // 组织树的头信息
    private byte[] szCoding = null;

    private IOnSuccessListener mOnSuccessListener;

    public interface IOnSuccessListener {
        public void onSuccess(boolean success, int errCode);
    }

    public GroupListGetTask() {
        mGroupListManager = GroupListManager.getInstance();
    }

    public void setListener(IOnSuccessListener onSuccessListener) {
        mOnSuccessListener = onSuccessListener;
    }

    @Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
    
	@Override
	protected TreeNode doInBackground(Void... arg0) {	    
		mGroupListManager.setFinish(false);
	    szCoding = mGroupListManager.loadDGroupInfoLayered();
	    mGroupListManager.getGroupList(szCoding, mGroupListManager.getRootNode());
	    mGroupListManager.setFinish(true);
	    if (mOnSuccessListener != null) {
			mOnSuccessListener.onSuccess(true, 0);
		}
	    Log.i("GroupListGetTask", "get data");
	    return mGroupListManager.getRootNode();
	   
	}

	/*@Override
	protected void onPostExecute(TreeNode result) {
		super.onPostExecute(result);
		mGroupListManager.setFinish(false);
	}*/
	
}
