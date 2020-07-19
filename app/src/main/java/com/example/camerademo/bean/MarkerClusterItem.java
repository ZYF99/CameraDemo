package com.example.camerademo.bean;

import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterItem;

public class MarkerClusterItem implements ClusterItem {
    private final LatLng mLatLng;

    // 自定义实例化方法
    public MarkerClusterItem(double latitude, double longitude) {
        // TODO Auto-generated constructor stub
        mLatLng = new LatLng(latitude, longitude);
    }

    @Override
    public LatLng getPosition() {
        // TODO Auto-generated method stub
        return mLatLng;
    }
}