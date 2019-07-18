package com.core.sfzface;

/**
 * 作者：李阳
 * 时间：2019/4/18
 * 描述：
 */
public class People {

     /**
     * 姓名
     */
    private String peopleName;

    /**
     * 性别
     */
    private String peopleSex;

    /**
     * 民族
     */
    private String peopleNation;

    /**
     * 出生日期
     */
    private String peopleBirthday;

    /**
     * 住址
     */
    private String peopleAddress;

    /**
     * 身份证号
     */
    private String peopleIDCode;

    /**
     * 签发机关
     */
    private String department;

    /**
     * 有效期限：开始
     */
    private String startDate;

    /**
     * 有效期限：结束
     */
    private String endDate;

    /**
     * 身份证头像
     */
    private byte[] photo;


    /**
     * 三代证指纹模板数据，正常位1024，如果为null，说明为二代证，没有指纹模板数据
     */
    private byte[] model;

}
