package com.example.camerademo;

/**
 * @BelongsProject: UsersInfo
 * @BelongsPackage: com.example.administrator.myapplication
 * @Author: Administrator
 * @CreateTime: 2020-01-08 10:58
 */

public class UsersInfo {
    public int id;
    public String username;
    public String password;
    public int age;
    public String info;

    public UsersInfo() {

    }

    public UsersInfo(String name) {
        this.username = name;
    }

    public UsersInfo(String name, String password) {
        this.username = name;
        this.password = password;
    }

    public UsersInfo(String name, int age, String info) {
        this.username = name;
        this.age = age;
        this.info = info;
    }

    public UsersInfo(String name, String password, int age, String info) {
        this.username = name;
        this.password = password;
        this.age = age;
        this.info = info;
    }

    @Override
    public String toString() {
        return "UsersInfo{"+
                "id=" + id +
                ",username='" + username +'\''+
                ",password='" + password +'\''+
                "age=" + age +
                ",info='" + info +'\''+
                '}';
    }
}
