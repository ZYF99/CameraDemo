package com.example.camerademo.bean;

import static com.example.camerademo.MainActivity.convertToDouble;

public class Light {
    private String id;
    private String jd;
    private String wd;
    private String mc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJd() {
        return jd;
    }

    public void setJd(String jd) {
        this.jd = jd;
    }

    public String getWd() {
        return wd;
    }

    public void setWd(String wd) {
        this.wd = wd;
    }

    public String getMc() {
        return mc;
    }

    public void setMc(String mc) {
        this.mc = mc;
    }
    public double getDzjdValue() {
        return convertToDouble(jd, 0);
    }

    public double getDzwdValue() {
        return convertToDouble(wd, 0);
    }

    public Light(String id, String jd, String wd, String mc) {
        this.id = id;
        this.jd = jd;
        this.wd = wd;
        this.mc = mc;
    }


    @Override
    public String toString() {
        return "Light{" +
                "id='" + id + '\'' +
                ", jd='" + jd + '\'' +
                ", wd='" + wd + '\'' +
                ", mc='" + mc + '\'' +
                '}';
    }
}
