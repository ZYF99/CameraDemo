package com.example.camerademo.bean;

/**
 *
 */
public class TuCengBean {

    public String name;
    public boolean flg;//是否选中
    public String id;
    public int image;

    public TuCengBean(String name, boolean flg,String id,int image) {
        this.name = name;
        this.flg = flg;
        this.id = id;
        this.image = image;
    }
}
