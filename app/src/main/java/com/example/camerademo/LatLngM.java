package com.example.camerademo;

import java.io.Serializable;

import static com.example.camerademo.MainActivity.convertToDouble;

public class LatLngM  implements Serializable
{


    private int td_id;
    private String devid_DH;
    private String td_MC; //名称
    private String dzjd;  //经度
    private String dzwd;  //纬度
    private String qx;    //0 （实时） 1（可控制）
    private String syfl;  //使用分类
    private String jsrq;  //建设日期
    private String td_WHDW;  //维护单位
    private String devtype; //类型

    public LatLngM(int td_id, String devid_DH, String td_MC, String dzjd, String dzwd, String qx) {
        this.td_id = td_id;
        this.devid_DH = devid_DH;
        this.td_MC = td_MC;
        this.dzjd = dzjd;
        this.dzwd = dzwd;
        this.qx = qx;
    }

    public LatLngM(int td_id, String devid_DH, String td_MC, String dzjd, String dzwd, String qx, String syfl, String jsrq, String td_WHDW, String devtype) {
        this.td_id = td_id;
        this.devid_DH = devid_DH;
        this.td_MC = td_MC;
        this.dzjd = dzjd;
        this.dzwd = dzwd;
        this.qx = qx;
        this.syfl = syfl;
        this.jsrq = jsrq;
        this.td_WHDW = td_WHDW;
        this.devtype = devtype;
    }

    public int getTd_id() {
        return td_id;
    }

    public void setTd_id(int td_id) {
        this.td_id = td_id;
    }

    public String getDevid_DH() {
        return devid_DH;
    }

    public void setDevid_DH(String devid_DH) {
        this.devid_DH = devid_DH;
    }

    public String getTd_MC() {
        return td_MC;
    }

    public void setTd_MC(String td_MC) {
        this.td_MC = td_MC;
    }

    public String getDzjd() {
        return dzjd;
    }

    public void setDzjd(String dzjd) {
        this.dzjd = dzjd;
    }

    public String getDzwd() {
        return dzwd;
    }

    public void setDzwd(String dzwd) {
        this.dzwd = dzwd;
    }

    public String getQx() {
        return qx;
    }

    public void setQx(String qx) {
        this.qx = qx;
    }

    public String getSyfl() {
        return syfl;
    }

    public void setSyfl(String syfl) {
        this.syfl = syfl;
    }

    public String getJsrq() {
        return jsrq;
    }

    public void setJsrq(String jsrq) {
        this.jsrq = jsrq;
    }

    public String getTd_WHDW() {
        return td_WHDW;
    }

    public void setTd_WHDW(String td_WHDW) {
        this.td_WHDW = td_WHDW;
    }

    public String getDevtype() {
        return devtype;
    }

    public void setDevtype(String devtype) {
        this.devtype = devtype;
    }

    public double getDzjdValue() {
        return convertToDouble(dzjd, 0);
    }

    public double getDzwdValue() {
        return convertToDouble(dzwd, 0);
    }

    @Override
    public String toString() {
        return "LatLngM{" +
                "td_id=" + td_id +
                ", devid_DH='" + devid_DH + '\'' +
                ", td_MC='" + td_MC + '\'' +
                ", dzjd='" + dzjd + '\'' +
                ", dzwd='" + dzwd + '\'' +
                ", qx='" + qx + '\'' +
                ", syfl='" + syfl + '\'' +
                ", jsrq='" + jsrq + '\'' +
                ", td_WHDW='" + td_WHDW + '\'' +
                ", devtype='" + devtype + '\'' +
                '}';
    }
}
