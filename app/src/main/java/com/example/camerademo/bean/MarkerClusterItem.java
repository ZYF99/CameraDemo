package com.example.camerademo.bean;

import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterItem;

public class MarkerClusterItem implements ClusterItem {
    private final LatLng mLatLng;

    // 自定义实例化方法
    public MarkerClusterItem(double latitude, double longitude) {
        mLatLng = new LatLng(latitude, longitude);
    }


    @Override
    public LatLng getPosition() {
        return mLatLng;
    }
}