package com.example.camerademo;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.example.camerademo.Api.GetRequestInterface;
import com.example.camerademo.Demo.RealPlayActivity;
import com.example.camerademo.bean.Draw;
import com.example.camerademo.bean.Light;
import com.example.camerademo.bean.MarkerClusterItem;
import com.example.camerademo.bean.TuCengBean;
import com.example.camerademo.bean.postionListBean;
import com.example.camerademo.bean.postionListDraw;
import com.example.camerademo.bean.postionListLight;
import com.google.gson.Gson;
import com.tencent.map.sdk.utilities.heatmap.Gradient;
import com.tencent.map.sdk.utilities.heatmap.HeatMapTileProvider;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.Projection;
import com.tencent.tencentmap.mapsdk.maps.SupportMapFragment;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.UiSettings;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.PolygonOptions;
import com.tencent.tencentmap.mapsdk.maps.model.TileOverlay;
import com.tencent.tencentmap.mapsdk.maps.model.TileOverlayOptions;
import com.tencent.tencentmap.mapsdk.maps.model.VisibleRegion;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.Cluster;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterItem;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterManager;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";
    private LatLngM lngM = null;
    private Light light = null;
    private Draw draw = null;

    private String[] province = new String[]{"显示标点的标题", "显示标点的图标"};

    ///初始化其他图层数据 这里设置名字 flid和对应的图标
    private TuCengBean[] province1 = new TuCengBean[]
            {new TuCengBean("前校门口区域图标", false, "10", R.drawable.m1),
                    new TuCengBean("后校门口区域图标", false, "21", R.drawable.m2),
                    new TuCengBean("测试3", false, "3", R.drawable.m3),
                    new TuCengBean("测试4", false, "11", R.drawable.m4),
                    new TuCengBean("测试5", false, "63", R.drawable.m5),
                    new TuCengBean("测试6", false, "27", R.drawable.m1),
                    new TuCengBean("测试7", false, "13", R.drawable.m1),
                    new TuCengBean("测试8", false, "99", R.drawable.m1),
                    new TuCengBean("测试9", false, "111", R.drawable.m1)};
    private ListView lv;

    private boolean mIsClean = true;
    TencentMap tencentMap = null;

    List<LatLngM> latLngs = new ArrayList<>();
    List<Draw> latLngs2 = new ArrayList<>();
    List<Light> lights = new ArrayList<>();
    List<Draw> draws = new ArrayList<>();

    private boolean showbdTcbt = false;
    private boolean showbdTctb = false;

    private boolean showqttc = false;


    private List<LatLng> nodes = new ArrayList<>();//热力图数据
    private TileOverlay mOverlay;//热力图

    private Marker marker;

    /**
     * 当前是否正在刷新
     */
    boolean isRefreshing = false;
    private boolean isShowLight = false;
    private boolean isShow = false;
    private boolean isShowdraw = false;

    ClusterManager mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment =
                (SupportMapFragment) fm.findFragmentById(R.id.frag_map);
        tencentMap = mapFragment.getMap();
        initMap();
        tencentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.5178400000, 113.0404500000), 17));
        initCluster();
    }

    final List<Marker> markerItems = new ArrayList<>();
    final List<Marker> lightItems = new ArrayList<>();
    final List<MarkerClusterItem> markerClusterItems = new ArrayList<>();

    private void initCluster() {
        // 实例化点聚合管理者
        mClusterManager = new ClusterManager<MarkerClusterItem>(this, tencentMap);

        // 默认聚合策略，调用时不必添加，如果需要其他聚合策略可以按以下代码修改
        NonHierarchicalDistanceBasedAlgorithm<MarkerClusterItem> ndba = new NonHierarchicalDistanceBasedAlgorithm<>(this);
        // 设置点聚合生效距离，以dp为单位
        ndba.setMaxDistanceAtZoom(200);
        // 设置策略
        mClusterManager.setAlgorithm(ndba);

        // 设置聚合渲染器，默认使用的是DefaultClusterRenderer，可以不调用下列代码
        DefaultClusterRenderer<MarkerClusterItem> renderer = new DefaultClusterRenderer<>(this, tencentMap, mClusterManager);
        // 设置最小聚合数量，默认为4，这里设置为2，即有2个以上不包括2个marker才会聚合
        renderer.setMinClusterSize(2);
        // 定义聚合的分段，当超过5个不足10个的时候，显示5+，其他分段同理
        renderer.setBuckets(new int[]{5, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160});
        mClusterManager.setRenderer(renderer);
        tencentMap.enableMultipleInfowindow(true);
        tencentMap.setOnCameraChangeListener(new TencentMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onCameraChangeFinished(CameraPosition cameraPosition) {
                mClusterManager.onCameraChangeFinished(cameraPosition);
                if (cameraPosition.zoom >= 18.0) {
                    markerItems.forEach(new Consumer<Marker>() {
                        @Override
                        public void accept(Marker marker) {
                            marker.getTitle();

                            marker.setVisible(true);
                            if(showbdTcbt){//显示标题
                                Log.d("!!!!!!!",""+marker.isInfoWindowShown()+"!!!!!!"+marker.isInfoWindowEnable());
                                marker.showInfoWindow();// 设置默认显示一个infoWindow
                            }
                            tencentMap.addMarker(marker.getOptions());
                        }
                    });
                    if(showbdTctb){//显示灯
                        lightItems.forEach(new Consumer<Marker>() {
                            @Override
                            public void accept(Marker marker) {
                                tencentMap.addMarker(marker.getOptions());
                            }
                        });
                    }

                } else {
                    tencentMap.clearAllOverlays();
                    if(isShow){
                        markerClusterItems.forEach(new Consumer<MarkerClusterItem>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void accept(final MarkerClusterItem markerClusterItem) {
                                mClusterManager.addItem(markerClusterItem);
                            }
                        });
                    }
                }
                if(isShow){
                    mClusterManager.cluster();
                }
            }
        });

    }

    /**
     * 标点的请求
     */
    private void netGetPostionList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfg.APP_HOST) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .build();

        final GetRequestInterface request = retrofit.create(GetRequestInterface.class);
        Call<postionListBean> call = request.getPositionList();

        //发送网络请求(异步)
        call.enqueue(new Callback<postionListBean>() {
            @Override
            public void onResponse(Call<postionListBean> call, Response<postionListBean> response) {
                postionListBean postionList = response.body();
                Gson gson = new Gson();
                Log.i("cd getPostionList", gson.toJson(postionList));
                if (postionList == null) {
                    Toast.makeText(MainActivity.this, "服务器错误！！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (postionList.getCode() == 200) {
                    Toast.makeText(MainActivity.this, "获取数据成功！", Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < postionList.getData().size(); i++) {
                        Log.i(TAG, "服务器数据: " + postionList.getData().get(i).toString());
                        LatLngM latLngM = postionList.getData().get(i);
                        lngM = new LatLngM(latLngM.getTd_id(), latLngM.getDevid_DH(), latLngM.getTd_MC(),
                                latLngM.getDzjd(), latLngM.getDzwd(), latLngM.getQx());
                        latLngs.add(lngM);

                        //热力图数据
                        if (!TextUtils.isEmpty(latLngM.getDzwd()) && !TextUtils.isEmpty(latLngM.getDzjd())) {
                            double dzwd = Double.parseDouble(latLngM.getDzwd());  //纬度
                            double dzjd = Double.parseDouble(latLngM.getDzjd());  //经度
                            nodes.add(new LatLng(dzwd, dzjd));
                        } else {
                            Log.i(TAG, latLngM.getDzwd() + "异常: " + latLngM.getDzjd());
                        }


                    }
                    init();
                }
            }

            @Override
            public void onFailure(Call<postionListBean> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //查询（其他图层）前后门的图标
    private void qhrequest() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfg.APP_HOST) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .build();

        final GetRequestInterface request = retrofit.create(GetRequestInterface.class);
        Call<postionListDraw> call = request.getPositionDraw();

        //发送网络请求(异步)
        call.enqueue(new Callback<postionListDraw>() {
            @Override
            public void onResponse(Call<postionListDraw> call, Response<postionListDraw> response) {
                postionListDraw postionList = response.body();
                Gson gson = new Gson();
                Log.i("cd getPostionList", gson.toJson(postionList));
                if (postionList == null) {
                    Toast.makeText(MainActivity.this, "服务器错误！！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (postionList.getCode() == 200) {
                    latLngs2.clear();
                    Toast.makeText(MainActivity.this, "获取数据成功！", Toast.LENGTH_SHORT).show();
                    latLngs2.addAll(postionList.getData());

                    init();
                }
            }

            @Override
            public void onFailure(Call<postionListDraw> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 红绿灯的请求
     */
    private void netGetPostionLight() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfg.APP_HOST) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .build();

        final GetRequestInterface request = retrofit.create(GetRequestInterface.class);
        Call<postionListLight> call = request.getPositionLight();

        //发送网络请求(异步)
        call.enqueue(new Callback<postionListLight>() {
            @Override
            public void onResponse(Call<postionListLight> call, Response<postionListLight> response) {
                postionListLight postionList = response.body();
                Gson gson = new Gson();
                Log.i("cd getPositionLight", gson.toJson(postionList));
                if (postionList == null) {
                    Toast.makeText(MainActivity.this, "服务器错误！！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (postionList.getCode() == 200) {
                    for (int i = 0; i < postionList.getData().size(); i++) {
                        Log.i(TAG, "红绿灯数据: " + postionList.getData().get(i).toString());
                        Light lightlist = postionList.getData().get(i);
                        light = new Light(lightlist.getId(), lightlist.getJd(), lightlist.getWd(), lightlist.getMc());
                        lights.add(light);

                    }

                    init();
                }
            }

            @Override
            public void onFailure(Call<postionListLight> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void findCurrentViewPoint2() {
        for (int i = 0; i < lights.size(); i++) {
            Marker marker = tencentMap.addMarker(new MarkerOptions()
                    .tag(lights.get(i))
                    .position(new LatLng(lights.get(i).getDzwdValue(), lights.get(i).getDzjdValue()))
                    .title(lights.get(i).getMc())
                    .anchor(1, 1)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.gdsxt_32)));
            marker.showInfoWindow();// 设置默认显示一个infoWindow
        }
    }


    private void findCurrentViewPoint3() {
        if (mIsClean && !showbdTctb && !showqttc) {
            tencentMap.clearAllOverlays();
        }
        if ((markerItems.isEmpty() && markerClusterItems.isEmpty()) || mIsClean) {
            for (int i = 0; i < latLngs.size(); i++) {
/*                if (!polygonCon(new LatLng(latLngs.get(i).getDzwdValue(), latLngs.get(i).getDzjdValue()))) {
                    continue;
                }*/

                final Marker marker = tencentMap.addMarker(new MarkerOptions()
                        .tag(latLngs.get(i))
                        .position(new LatLng(latLngs.get(i).getDzwdValue(), latLngs.get(i).getDzjdValue()))
                        .title(latLngs.get(i).getTd_MC())
                        .anchor(1, 1)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.camera)));
                marker.showInfoWindow();// 设置默认显示一个infoWindow
                markerItems.add(marker);
                markerClusterItems.add(new MarkerClusterItem(latLngs.get(i).getDzwdValue(), latLngs.get(i).getDzjdValue()));
            }
            mClusterManager.addItems(markerClusterItems);
        }
    }


    private void findCurrentViewPoint31() {
        if (mIsClean && !showbdTcbt && !showqttc) {
            tencentMap.clearAllOverlays();
        }
        for (int i = 0; i < latLngs.size(); i++) {
            LatLng latLng;
            try {
                latLng = new LatLng(latLngs.get(i).getDzwdValue(), latLngs.get(i).getDzjdValue());
            } catch (Exception e) {
                continue;
            }
            if (!polygonCon(latLng)) {
                continue;
            }

            Marker marker = tencentMap.addMarker(new MarkerOptions()
                    .tag(latLngs.get(i))
                    .position(new LatLng(latLngs.get(i).getDzwdValue(), latLngs.get(i).getDzjdValue()))
                    .anchor(1, 1)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.gdsxt_32)));
            lightItems.add(marker);
            marker.showInfoWindow();// 设置默认显示一个infoWindow
        }

    }

    private void findCurrentViewPoint51() {
        if (mIsClean && !showbdTcbt && !showbdTctb) {
            tencentMap.clearAllOverlays();
        }
        for (int i = 0; i < latLngs2.size(); i++) {
/*            if (!polygonCon(latLng)) {
                continue;
            }*/
            if (showqttc && judgeTcContain(latLngs2.get(i).getFlid())) {
                Log.e("sadasd", i + "");
                Marker marker = tencentMap.addMarker(new MarkerOptions()
                        .tag(latLngs2.get(i))
                        .position(new LatLng(latLngs2.get(i).getDzwdValue(), latLngs.get(i).getDzjdValue()))
                        .anchor(1, 1)
                        .icon(BitmapDescriptorFactory.fromResource(getImage(latLngs2.get(i).getFlid()))));
                // marker.showInfoWindow();// 设置默认显示一个infoWindow
            }
        }
    }

    //判断一个点是否在所选的其他图层里面
    private boolean judgeTcContain(String flid) {
        for (int i = 0; i < province1.length; i++) {
            if (province1[i].flg) {
                if (TextUtils.equals(province1[i].id, flid)) {
                    return true;
                }
            }
        }
        return false;
    }

    //通过fid查询marker图标
    private int getImage(String flid) {
        for (int i = 0; i < province1.length; i++) {
            if (province1[i].flg) {
                if (TextUtils.equals(province1[i].id, flid)) {
                    return province1[i].image;
                }
            }
        }
        return R.drawable.m1;
    }


    /**
     * 其他图层
     */
    private void findCurrentViewPoint4() {
        for (int i = 0; i < draws.size(); i++) {
            Marker marker = tencentMap.addMarker(new MarkerOptions()
                    .tag(draws.get(i))
                    .position(new LatLng(draws.get(i).getDzwdValue(), draws.get(i).getDzjdValue()))
                    .title(draws.get(i).getMc())
                    .anchor(1, 1)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.gdsxt_32)));
            marker.showInfoWindow();// 设置默认显示一个infoWindow
        }
    }

    //添加热力图
    private TileOverlay addHeatMap(List<LatLng> nodes) {
        int[] colors = {
//                Color.rgb(102, 225, 0), // 绿色
                Color.rgb(0, 0, 255),   // 红色
                Color.rgb(255, 0, 0)    // 红色
        };
        float[] startPoints = {
                0.01f, 1f
        };
        Gradient gradient = new Gradient(colors, startPoints);
        HeatMapTileProvider provider = new HeatMapTileProvider.Builder().data(nodes).gradient(gradient).build(tencentMap);
//        provider.setOpacity(0.7);
        mOverlay = tencentMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
//        mOverlay.clearTileCache();
//        mOverlay.reload();
        return mOverlay;
    }


    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_marker://显示/隐藏标点
//                showbdTcbt = false;
//                showbdTctb = false;
                if (isShow) {
                    isShow = false;
                    tencentMap.clearAllOverlays();
                    if (item.getTitle().equals("隐藏标点")) {
                        item.setTitle("显示标点");
                    }
                } else {
                    isShow = true;
                    findCurrentViewPoint3();
                    if (item.getTitle().equals("显示标点")) {
                        item.setTitle("隐藏标点");
                    }
                    mClusterManager.cluster();
                }
                tencentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tencentMap.getCameraPosition().target, tencentMap.getCameraPosition().zoom));
                break;

            case R.id.action_show_heat://显示或者取消显示热力图
                if (nodes.isEmpty()) {
                    Toast.makeText(this, "没有可用坐标数据", Toast.LENGTH_SHORT).show();
                    return true;
                }
//                showbdTcbt = false;
//                showbdTctb = false;
                if (mOverlay == null) {
                    isShowTitle = false;
                    findCurrentViewPoint(false);
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            mOverlay = addHeatMap(nodes);
                        }
                    }.start();
                    item.setTitle("隐藏热力图");
                } else {
                    mOverlay.remove();
                    mOverlay = null;
                    isShowTitle = true;
                    findCurrentViewPoint(false);
                    item.setTitle("显示热力图");

                }
                break;

            case R.id.action_show_light:
//                showbdTcbt = false;
//                showbdTctb = false;
                if (isShowLight) {
                    isShowLight = false;
                    tencentMap.clearAllOverlays();
                    if (item.getTitle().equals("隐藏灯")) {
                        item.setTitle("显示灯");
                    }
                } else {
                    isShowLight = true;
                    findCurrentViewPoint2();
                    if (item.getTitle().equals("显示灯")) {
                        item.setTitle("隐藏灯");
                    }
                }
                break;
            case R.id.action_show_draw:
//                showHou = false;
//                showQian = false;
                showMultiChoiceItems();
                break;

            case R.id.action_show_draw1:
//                showbdTcbt = false;
//                showbdTctb = false;
                showMultiChoiceItems2();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMultiChoiceItems() {
        AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle("请选择你的图层：")
                .setMultiChoiceItems(province,
                        new boolean[]{showbdTcbt, showbdTctb, false, false, false},
                        new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {
                                // TODO Auto-generated method stub
                            }
                        })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

//                        String s = "您选择了：";
//                        // 扫描所有的列表项，如果当前列表项被选中，将列表项的文本追加到s变量中。
//                        for (int i = 0; i < province.length; i++) {
//                            if (lv.getCheckedItemPositions().get(i)) {
//                                s += i + ":" + lv.getAdapter().getItem(i) + " ";
//                            }
//                        }
                        for (int i = 0; i < province.length; i++) {
                            System.out.println(lv.getAdapter().getItem(i).toString());
                        }

                        // 用户至少选择了一个列表项
                        SparseBooleanArray array = lv.getCheckedItemPositions();
                        if (array.size() > 0) {
                            tencentMap.clearAllOverlays();
                            mIsClean = false;
                            showbdTcbt = array.get(0);
                            if (showbdTcbt) {
                                findCurrentViewPoint3();
                            }
                            showbdTctb = array.get(1);
                            if (showbdTctb) {
                                findCurrentViewPoint31();
                            }
                            mIsClean = true;
                            tencentMap.clearAllOverlays();
                        }
//                        if (lv.getCheckedItemPositions().size() > 0) {
//                            findCurrentViewPoint3();
//                        }

                        // 用户未选择任何列表项
                        else if (lv.getCheckedItemPositions().size() <= 0) {
                            tencentMap.clearAllOverlays();
                        }
                        tencentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tencentMap.getCameraPosition().target, tencentMap.getCameraPosition().zoom + 0.2f));
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showbdTcbt = false;
                        showbdTctb = false;
                        //tencentMap.clearAllOverlays();
                    }
                }).create();
        lv = builder.getListView();
        builder.show();
    }

    private void showMultiChoiceItems2() {
        //查询出列表要显示哪些以及选中状态
        String[] items = new String[province1.length];
        boolean[] selects = new boolean[province1.length];
        for (int i = 0; i < province1.length; i++) {
            items[i] = province1[i].name;
            selects[i] = province1[i].flg;
        }
        AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle("请选择你的图层：")
                .setMultiChoiceItems(items, selects,
                        new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {
                                // TODO Auto-generated method stub
                            }
                        })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        String s = "您选择了：";
//                        // 扫描所有的列表项，如果当前列表项被选中，将列表项的文本追加到s变量中。
//                        for (int i = 0; i < province.length; i++) {
//                            if (lv.getCheckedItemPositions().get(i)) {
//                                s += i + ":" + lv.getAdapter().getItem(i) + " ";
//                            }
//                        }
                        for (int i = 0; i < province.length; i++) {
                            System.out.println(lv.getAdapter().getItem(i).toString());
                        }

                        // 用户至少选择了一个列表项
//                        if (lv.getCheckedItemPositions().size() > 0) {
//                            new AlertDialog.Builder(MainActivity.this)
//                                    .setMessage("你选了至少一个").show();
//                            System.out.println(lv.getCheckedItemPositions()
//                                    .size());
//                        }

                        SparseBooleanArray array = lv.getCheckedItemPositions();
                        for (int i = 0; i < province1.length; i++) {
                            //设置选中状态
                            if (lv.getCheckedItemPositions().get(i)) {
                                showqttc = true;
                                province1[i].flg = lv.getCheckedItemPositions().get(i);
                            }
                        }
                        if (array.size() > 0) {
                            findCurrentViewPoint51();

//                            mIsClean = false;
//                            showQian = array.get(0);
//                            if (array.get(0)){
//                                findCurrentViewPoint51();
//                            }
//                            showHou = array.get(1);
//                            if (array.get(1)){
//                                findCurrentViewPoint51();
//                            }
//                            mIsClean = true;
                        }
//                        if (lv.getCheckedItemPositions().size() > 0) {
//                            findCurrentViewPoint3();
//                        }

                        // 用户未选择任何列表项
                        else if (lv.getCheckedItemPositions().size() <= 0) {
                            for (int i = 0; i < province1.length; i++) {
                                province1[i].flg = false;

                            }
                            showqttc = false;
                            tencentMap.clearAllOverlays();
                        }

                        // 用户未选择任何列表项
                        else if (lv.getCheckedItemPositions().size() <= 0) {
                            for (int i = 0; i < province1.length; i++) {
                                province1[i].flg = false;

                            }
                            showqttc = false;
                            tencentMap.clearAllOverlays();
//                            new AlertDialog.Builder(MainActivity.this)
//                                    .setMessage("您未选择任何省份").show();
                        }
                        tencentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tencentMap.getCameraPosition().target, tencentMap.getCameraPosition().zoom + 0.2f));
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int a) {
                        for (int i = 0; i < province1.length; i++) {
                            province1[i].flg = false;
                        }
                        showqttc = false;
                        //tencentMap.clearAllOverlays();
                    }
                }).create();
        lv = builder.getListView();
        builder.show();
    }

    /**
     * 在服务器返回的结果中查询出当前视图区域的点 显示到地图上
     */
    private void findCurrentViewPoint() {
        findCurrentViewPoint(true);
    }

    boolean isShowTitle = true;

    private void findCurrentViewPoint(boolean isUpdate) {
        if (isRefreshing) {
            Log.e("map", "正在刷新....");
            return;
        }
        isRefreshing = true;
        tencentMap.clearAllOverlays();

        for (int i = 0; i < latLngs.size(); i++) {
            LatLngM latLngM = latLngs.get(i);
            LatLng latLng;
            try {
                latLng = new LatLng(Double.parseDouble(latLngM.getDzwd()), Double.parseDouble(latLngM.getDzjd()));
//                if (isUpdate && !nodes.contains(latLng))
//                    nodes.add(latLng);
            } catch (Exception e) {
                continue;
            }
            // 查询当前视图内的
            if (!polygonCon(latLng)) {
                continue;
            }

            // 添加marker聚合点
            if (isShowTitle) {
                marker = tencentMap.addMarker(new MarkerOptions()
                        .tag(latLngs.get(i))
                        .position(new LatLng(latLngs.get(i).getDzwdValue(), latLngs.get(i).getDzjdValue()))
                        .title(latLngs.get(i).getTd_MC())   ///这里
                        .anchor(1, 1)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.camera)));
            } else {
                marker = tencentMap.addMarker(new MarkerOptions()
                        .tag(latLngs.get(i))
                        .position(new LatLng(latLngs.get(i).getDzwdValue(), latLngs.get(i).getDzjdValue()))
                        .anchor(1, 1)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.camera)));

            }

            marker.showInfoWindow();// 设置默认显示一个infoWindow
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isRefreshing = false;
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();


    }


    /**
     * 某个点是否在区域内
     *
     * @param latLng 需要判断的点
     * @return
     */
    public boolean polygonCon(LatLng latLng) {
        PolygonOptions options = new PolygonOptions();
        // 是否在此区域
        Projection projection = tencentMap.getProjection();
        VisibleRegion region = projection.getVisibleRegion();
        LatLngBounds latLngBounds = region.latLngBounds;
        return latLngBounds.contains(latLng);
    }

    // 初始化经纬度保存
    private void initMap() {
        tencentMap.enableMultipleInfowindow(true);
        tencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL);
        latLngs.clear();
        lights.clear();
        netGetPostionList();
        netGetPostionLight();
        qhrequest();

        tencentMap.setOnMapLoadedCallback(new TencentMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

            }
        });

        // Marker点击事件
        tencentMap.setOnMarkerClickListener(new TencentMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {

                if (marker.isVisible()) {
                    // TODO Auto-generated method stub
                    System.out.println("我在尝试");
                    Intent intent = new Intent(MainActivity.this,
                            RealPlayActivity.class);
                    intent.putExtra("Tag", (LatLngM) marker.getTag());
//                intent.putExtra("lon", marker.getPosition().getLongitude() + "");
                    startActivity(intent);
                }


                return false;
            }
        });
        // infoWindow点击事件
        tencentMap.setOnInfoWindowClickListener(new TencentMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(MainActivity.this,
                        RealPlayActivity.class);
                intent.putExtra("Tag", (LatLngM) marker.getTag());

                // intent.putExtra("lon", marker.getPosition().getLongitude() + "");


                startActivity(intent);
            }

            @Override
            public void onInfoWindowClickLocation(int i, int i1, int i2, int i3) {

            }
        });

        tencentMap.setOnCameraChangeListener(new TencentMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) { //地图拖动完再去请求数据

            }

            @Override
            public void onCameraChangeFinished(CameraPosition cameraPosition) {
/*                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isShow) {
                            findCurrentViewPoint3(false);//延迟加载
                        }
                        if (showbdTcbt) {
                            findCurrentViewPoint3(false);
                        }
                        if (showbdTctb) {
                            findCurrentViewPoint31();
                        }
                        if (showqttc) {
                            findCurrentViewPoint51();
                        }
                        //findCurrentViewPoint(); 初始化会走到这里
                    }
                }, 200);*/
            }
        });
    }

    private void init() {

        tencentMap.setMinZoomLevel(12); // 设置缩放级别    修改经纬度小数点第四位变化肉眼可见（第四位是个分界点）

        // 获取UiSettings实例
        UiSettings uiSettings = tencentMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
//        // 设置logo到屏幕底部中心
//        uiSettings.setLogoPosition(UiSettings.LOGO_POSITION_CENTER_BOTTOM);
//        // 设置比例尺到屏幕右下角
//        uiSettings.setScaleViewPosition(UiSettings);
//        // 启用缩放手势(更多的手势控制请参考开发手册)
//        uiSettings.setZoomGesturesEnabled(true);

        /// 设置地图中心点
        if (latLngs != null && latLngs.size() > 0)
            tencentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.5178400000, 113.0404500000), 15));
        else
            tencentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.512739, 113.06661500), 15));
    }

//    /**
//     * 显示摄像头和多边形
//     */
//    private void initData() {
//        if (latLngs == null)
//            return;
//
//        if (markers.size() > 0) {
//
//            for (Marker marker : markers) {
//                marker.remove();
//            }
//        }
//
//        Marker marker = null;
//        for (int i = 0; i < latLngs.size(); i++) {
//            marker = tencentMap.addMarker(new MarkerOptions()
//                    .tag(latLngs.get(i).getId())
//                    .position(new LatLng(latLngs.get(i).getDzwdValue(), latLngs.get(i).getDzjdValue()))
//                    .title(latLngs.get(i).getDevid_DH())
//                    .anchor(1, 1)
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.camera)));
//            marker.showInfoWindow();// 设置默认显示一个infoWindow
//
//            markers.add(marker);
//        }
//        addPolygon(latLngs);
//        showMarksInfo(latLngs, 3);
//    }

    //把String转化为double
    public static double convertToDouble(String number, double defaultValue) {
        if (TextUtils.isEmpty(number)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(number);
        } catch (Exception e) {
            return defaultValue;
        }

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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
