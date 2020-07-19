package com.example.camerademo.bean;

import com.example.camerademo.UsersInfo;

public class userBean extends baseBean{

    /**
     * code : 200
     * msg : 成功
     * data : {"id":4,"username":"admin","password":"5747973"}
     */


    private UsersInfo data;

    public UsersInfo getData() {
        return data;
    }

    public void setData(UsersInfo data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "userBean{"+
                "data=" + data + '\'' +
                '}';
    }
}
