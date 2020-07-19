package com.example.camerademo.Api;


import com.example.camerademo.bean.postionListBean;
import com.example.camerademo.bean.postionListDraw;
import com.example.camerademo.bean.postionListLight;
import com.example.camerademo.bean.userBean;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GetRequestInterface {
//    /**
//     * method：网络请求的方法（区分大小写）
//     * path：网络请求地址路径
//     * hasBody：是否有请求体
//     */
//    @HTTP(method = "POST", path = "blog/{id}", hasBody = false)
//    Call<ResponseBody> getCall(@Path("id") int id);
//    // {id} 表示是一个变量
//    // method 的值 retrofit 不会做处理，所以要自行保证准确

//    Retrofit retrofit = new Retrofit.Builder()
//            .baseUrl(APP_HOST) //设置网络请求的Url地址
//            .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
//            .build();

    //登录
    @FormUrlEncoded
    @POST("/login")
    Call<userBean> login(
            @Field("username") String username, @Field("password") String password
    );

    @FormUrlEncoded
    @POST("/add")
    Call<userBean> register(
            @Field("username") String username, @Field("password") String password
    );

    @POST("/list")
    Call<postionListBean> getPositionList();

    @POST("/light")
    Call<postionListLight> getPositionLight();

    @POST("/draw")
    Call<postionListDraw> getPositionDraw();

    @POST("/draw")
    Call<postionListDraw> getPositionDrawids(@Query("ids") String ids);
}
