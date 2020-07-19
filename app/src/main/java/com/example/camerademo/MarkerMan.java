package com.example.camerademo;

import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.vector.utils.MarkerManager;

class MarkerMan extends MarkerManager implements TencentMap.OnCameraChangeListener {

    TencentMap.OnCameraChangeListener onCameraChange;

    public MarkerMan(TencentMap tencentMap,TencentMap.OnCameraChangeListener onCameraChange) {
        super(tencentMap);
        this.onCameraChange = onCameraChange;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        onCameraChange.onCameraChange(cameraPosition);
    }

    @Override
    public void onCameraChangeFinished(CameraPosition cameraPosition) {
        onCameraChange.onCameraChangeFinished(cameraPosition);
    }
}
