package com.example.camerademo.bean;

import com.example.camerademo.LatLngM;

import java.util.List;

public class postionListBean extends baseBean {

    private List<LatLngM> data;

    public List<LatLngM> getData() {
        return data;
    }

    public void setData(List<LatLngM> data) {
        this.data = data;
    }
}
